package space4cloudbench.main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space4cloud_bench.S4cJob;
import space4cloudbench.gui.MainWindow;

public class Space4CloudBench extends SwingWorker<Void , Void>{


	private MainWindow window;
	private static final Logger logger = LoggerFactory.getLogger(Space4CloudBench.class); 
	protected BlockingQueue<S4cJob> queue = new ArrayBlockingQueue<S4cJob>(100);
	
	public static final String FILE_NAME = "batch.prop";
	


	private void runNextJob() {		
		if(queue.size() == 0){
			logger.info("Benchmark finished");
			return;
		}		
		S4cJob job = queue.poll();
		logger.info("Running Job, remainign jobs: "+queue.size());
		job.execute();
		while(!job.isDone()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("Error while waiting.", e);
			}
		}		
		window.updateCompletion(job.getName());
		logger.info("Job compleated");
		runNextJob();
		
	}
	

	private void refreshProject(String projectName) {
		try {
			ResourcesPlugin
			.getWorkspace()
			.getRoot()
			.getProject(projectName)
			.refreshLocal(IResource.DEPTH_INFINITE,
					new NullProgressMonitor());
		} catch (CoreException e) {
			logger.error("Could not refresh the project",e);
		}

	}


	private void buildJobs(){
		List<String> projects = window.getSelectedProjects();
		for(String projectName:projects){
			refreshProject(projectName);
//			IFile configurationFile = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFile(FILE_NAME);
			
			IProject projectFolder = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			List<IFile> configurationFiles = new ArrayList<IFile>();
			try {
				IResource files[] = projectFolder.members(IFolder.FILE);
				for (IResource f : files) {
					if (f instanceof IFile && f.getName().startsWith(FILE_NAME))
						configurationFiles.add((IFile)f);
				}
			} catch (CoreException e) {
				logger.error("Error while getting the list of files in the folder.", e);
			}
			
			if(configurationFiles.size() == 0) { //configurationFile == null || !configurationFile.exists()){
				logger.error("No batch configuration file " + FILE_NAME + " found for project "+projectName+" skipping it");
				continue;
			}

			for (IFile configurationFile : configurationFiles) {
				int repetitions= window.getRepetitions();
				for(int attempt = 0; attempt<repetitions;attempt++){
					S4cJob optimJob = new S4cJob(configurationFile.getLocation().toPortableString(),projectName,attempt);		
	//				optimJob.addPropertyChangeLisener(this);
					optimJob.setSeed(attempt+1);
					queue.add(optimJob);
				}
			}
		}
	}


	@Override
	protected Void doInBackground() throws Exception {
		logger.info("Building main window");
		window = new MainWindow();
		for(IProject project:ResourcesPlugin.getWorkspace().getRoot().getProjects()){
			window.addProject(project.getName());
		}
		logger.info("Showing main window");
		window.show();			
		while(!window.isDisposed() && !window.isStarted()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("Error while waiting.", e);
			}				
		}

		if(!window.isStarted()){
			logger.info("Cancelled");
			window = null;
			return null;
		}
		logger.info("Building jobs");
		buildJobs();		
		runNextJob();
		return null;
	}
	
	@Override
	protected void done() {
		try {
			get();
		} catch (InterruptedException e) {
			logger.error("Space4Cloud Bench interrupted",e);
		} catch (ExecutionException e) {
			logger.error("Execution exception in Space4Cloud Bench",e);
		}
			
		
	}



	

}
