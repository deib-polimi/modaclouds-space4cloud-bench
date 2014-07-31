package space4cloudbench.main;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space4cloud_bench.S4cJob;
import space4cloudbench.gui.MainWindow;

public class Space4CloudBench extends SwingWorker<Void , Void>{


	private MainWindow window;
	private final Logger logger = LoggerFactory.getLogger(Space4CloudBench.class); 
	protected BlockingQueue<S4cJob> queue = new ArrayBlockingQueue<S4cJob>(100);
	


	private void runNextJob() {		
		if(queue.size() == 0){
			System.out.println("Benchmark finished");
			return;
		}		
		S4cJob job = queue.poll();
		logger.info("Running Job, remainign jobs: "+queue.size());
		job.execute();
		while(!job.isDone()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
		window.updateCompletion(job.getName());
		logger.info("Job compleated");
		runNextJob();
		
	}
	

	


	private void buildJobs(){
		List<String> projects = window.getSelectedProjects();
		for(String projectName:projects){
			IFile configurationFile = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFile("batch.prop");
			if(configurationFile == null || !configurationFile.exists()){
				System.out.println("No batch configuration file batch.prop found for ptoject "+projectName+" skipping it");
				continue;
			}

			int repetitions= window.getRepetitions();
			for(int attempt = 0; attempt<repetitions;attempt++){
				S4cJob optimJob = new S4cJob(configurationFile.getLocation().toPortableString(),projectName,attempt);		
//				optimJob.addPropertyChangeLisener(this);
				optimJob.setSeed(attempt+1);
				queue.add(optimJob);
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
				e.printStackTrace();
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
