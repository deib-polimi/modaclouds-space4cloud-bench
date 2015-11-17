package space4cloud_bench;

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
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.modaclouds.space4cloud.mainProgram.Space4Cloud;

public class S4cJob extends SwingWorker<Void, Void>implements PropertyChangeListener {

	private String configurationFile;
	private boolean configurationChange = false;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private int seed;
	private Space4Cloud s4c;
	private String name;
	private int replica;
	private final Logger logger = LoggerFactory.getLogger(S4cJob.class);
	private StopWatch timer = new StopWatch();

	private long initializationTimestamp = -1;
	private long transformationTimestamp = -1;
	private long solutionInitializationTimestamp = -1;
	private long relaxedSolutionGenerationTimestamp = -1;
	private long optimizationTime = -1;

	public S4cJob(String configurationFile, String projectName) {
		this(configurationFile, projectName, 1);
	}

	public S4cJob(String configurationFile, String projectName, int replica) {
		this.configurationFile = configurationFile;
		name = projectName;
		this.replica = replica;
		logger.info("Building job, name: " + projectName + " conf: " + configurationFile + " replica: " + replica);
	}

	private void updateConfiguration() {
		Properties projectProperties = new Properties();
		logger.info("Updating configuration with new seed");
		try {
			FileInputStream in = new FileInputStream(configurationFile);
			projectProperties.load(in);
			in.close();
			projectProperties.put("RANDOM_SEED", Integer.toString(seed));
			FileOutputStream out = new FileOutputStream(configurationFile);
			projectProperties.store(out, "Random Seed Batch Generation");
			out.close();
		} catch (IOException e) {
			logger.error("Error while updating the configuration file.", e);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource().equals(s4c))
			if (evt.getPropertyName().equals(Space4Cloud.INITIALIZATION_COMPLEATED)) {
				logger.info("Initialization done");
				timer.split();
				initializationTimestamp = timer.getSplitTime();
			} else if (evt.getPropertyName().equals(Space4Cloud.RELAXED_SOLUTION_GENERATED)) {
				logger.info("Relaxed Solution generated");
				timer.split();
				relaxedSolutionGenerationTimestamp = timer.getSplitTime();
			} else if (evt.getPropertyName().equals(Space4Cloud.TRANSFORMATION_COMPLEATED)) {
				logger.info("Pcm2LQN Transformation compleated");
				timer.split();
				transformationTimestamp = timer.getSplitTime();
			} else if (evt.getPropertyName().equals(Space4Cloud.SOLUTION_INITIALIZED)) {
				logger.info("Initial solution loaded");
				timer.split();
				solutionInitializationTimestamp = timer.getSplitTime();
			} else if (evt.getPropertyName().equals(Space4Cloud.OPTIMIZATION_ENDED)) {
				logger.info("Optimization finished");
				timer.split();
				optimizationTime = timer.getSplitTime();
				pcs.firePropertyChange("optimizationFinished", false, true);
			}

	}

	public void addPropertyChangeLisener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void setSeed(int seed) {
		this.seed = seed;
		configurationChange = true;
	}

	public int getSeed() {
		return seed;
	}

	@Override
	protected Void doInBackground() throws Exception {
		if (configurationChange)
			updateConfiguration();

		s4c = new Space4Cloud(configurationFile);
		s4c.addPropertyChangeListener(this);
		logger.info("Running Job");
		timer.start();
		s4c.run();
		s4c.join();
		timer.stop();
		logger.info("Job finished, saving results");
		copyResults();
		return null;
	}

	@Override
	protected void done() {
		try {
			get();
		} catch (InterruptedException e) {
			logger.error("interruption error", e);
		} catch (ExecutionException e) {
			logger.error("execution error", e);
		} catch (CancellationException e) {
			logger.error("cancellation error", e);
		}
	}

	private void copyResults() {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(configurationFile));
		} catch (IOException e) {
			logger.error("error loading property ", e);
		}

		String baseDir = prop.getProperty("PROJECT_BASE_FOLDER");
		Path basePath = Paths.get(baseDir, "space4cloud");

		// clean performance results folder

		FileUtils.deleteQuietly(Paths.get(baseDir, "space4cloud", "performance_results").toFile());

		Path resultPath = Paths.get(baseDir, Integer.toString(replica));
		try {
			FileUtils.copyDirectory(basePath.toFile(), resultPath.toFile());
		} catch (IOException e) {
			logger.error("error copying directory", e);
		}
		logger.info("Files copied");
	}

	public String getName() {
		return name;
	}

	public long getInitializationTime() {
		return initializationTimestamp;
	}

	public long getTransformationTime() {
		return transformationTimestamp - initializationTimestamp;
	}

	public long getSolutionInitializationTime() {
		if (relaxedSolutionGenerationTimestamp > 0)
			return solutionInitializationTimestamp - relaxedSolutionGenerationTimestamp;
		return solutionInitializationTimestamp - transformationTimestamp;
	}

	public long getRelaxedSolutionGenerationTime() {
		return relaxedSolutionGenerationTimestamp - transformationTimestamp;
	}

	public long getOptimizationTime() {
		return optimizationTime - solutionInitializationTimestamp;
	}

}
