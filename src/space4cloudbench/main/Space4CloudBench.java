package space4cloudbench.main;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.SwingWorker;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import space4cloud_bench.S4cJob;
import space4cloudbench.gui.MainWindow;

public class Space4CloudBench extends SwingWorker<Void , Void>{


	MainWindow window;
	protected BlockingQueue<S4cJob> queue = new ArrayBlockingQueue<S4cJob>(100);
	
	
	
	


	private void runNextJob() {
		if(queue.size() == 0){
			System.out.println("Benchmark finished");
			return;
		}
		
		S4cJob job = queue.poll();
		job.execute();
		while(!job.isDone()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		window.updateCompletion(job.getName());
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
		window = new MainWindow();
		for(IProject project:ResourcesPlugin.getWorkspace().getRoot().getProjects()){
			window.addProject(project.getName());
		}
		window.show();			
		while(!window.isDisposed() && !window.isStarted()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}				
		}

		if(!window.isStarted()){
			System.out.println("Cancelled");
			window = null;
			return null;
		}
		
		buildJobs();		
		runNextJob();
		return null;
	}



	

}
