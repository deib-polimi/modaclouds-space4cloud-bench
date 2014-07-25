package space4cloud_bench;

import it.polimi.modaclouds.space4cloud.mainProgram.Space4Cloud;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;

public class S4cJob extends SwingWorker<Void, Void> implements PropertyChangeListener {
	
	
	private String configurationFile;
	private boolean configurationChange = false;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private int seed;
	private Space4Cloud s4c;
	private String name;
	private int replica;


	public S4cJob(String configurationFile, String projectName) {
		this(configurationFile, projectName,1);
	}
	
	public S4cJob(String configurationFile, String projectName, int replica) {
		this.configurationFile= configurationFile;		
		name=projectName;
		this.replica = replica;
	}
	
	private void updateConfiguration() {
		Properties projectProperties = new Properties();
		try {
			FileInputStream in = new FileInputStream(configurationFile);
			projectProperties.load(in);
			in.close();
			projectProperties.put("RANDOM_SEED", Integer.toString(seed));
			FileOutputStream out = new FileOutputStream(configurationFile);
			projectProperties.store(out,"Random Seed Batch Generation");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getSource().equals(s4c) && evt.getPropertyName().equals("optimizationEnded")){
			System.out.println("Optimization finished");
			pcs.firePropertyChange("optimizationFinished",false,true);
		}
	}

	public void addPropertyChangeLisener(PropertyChangeListener listener){
		pcs.addPropertyChangeListener(listener);
	}
	
	public void setSeed(int seed) {
		this.seed = seed;
		configurationChange = true;
	}

	@Override
	protected Void doInBackground() throws Exception {
		if(configurationChange)
			updateConfiguration();
		
		s4c = new Space4Cloud(configurationFile);
		s4c.addPropertyChangeListener(this);
		s4c.run();
		s4c.join();
		copyResults();
		return null;
	}
	

	@Override
	protected void done() {
		try {
			get();
		} catch (InterruptedException e) {
			e.printStackTrace();			
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (CancellationException e){
			e.printStackTrace();
		}
	}
	
	private void copyResults() {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(configurationFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String baseDir = prop.getProperty("PROJECT_BASE_FOLDER");
		Path basePath = Paths.get(baseDir,"space4cloud");
		
		//clean performance results folder
		try {
			Space4Cloud.cleanFolders(Paths.get(baseDir,"space4cloud","performance_results"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Path resultPath = Paths.get(baseDir,Integer.toString(replica));
		try {
			FileUtils.copyDirectory(basePath.toFile(), resultPath.toFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Files copied");
	}

	public String getName() {
		return name;
	}
}