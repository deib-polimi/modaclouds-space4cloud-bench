package space4cloudbench.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ProjectPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5817690329760758731L;
	private String name;
	private JCheckBox checkBox;
	private JLabel nameLabel;
	private JLabel progressLabel;
	private int compleated=0;
	
	public ProjectPanel(String projectName){
		this();
		name = projectName;
		nameLabel.setText(name);
	}
	
	/**
	 * Create the panel.
	 */
	private ProjectPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{21, 300, 46, 0};
		gridBagLayout.rowHeights = new int[]{21, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		checkBox = new JCheckBox("");
		GridBagConstraints gbc_checkBox = new GridBagConstraints();
		gbc_checkBox.insets = new Insets(0, 0, 0, 5);
		gbc_checkBox.gridx = 0;
		gbc_checkBox.gridy = 0;
		add(checkBox, gbc_checkBox);
		
		nameLabel = new JLabel("New label");
		GridBagConstraints gbc_nameLabel = new GridBagConstraints();
		gbc_nameLabel.insets = new Insets(0, 0, 0, 5);
		gbc_nameLabel.gridx = 1;
		gbc_nameLabel.gridy = 0;
		add(nameLabel, gbc_nameLabel);
		
		progressLabel = new JLabel("0");
		GridBagConstraints gbc_progressLabel = new GridBagConstraints();
		gbc_progressLabel.gridx = 2;
		gbc_progressLabel.gridy = 0;
		add(progressLabel, gbc_progressLabel);

	}

	public String getProjectName() {
		return name;
	}
	
	public boolean isSelected(){
		return checkBox.isSelected();
	}

	public void updateCompletion() {
		compleated++;
		progressLabel.setText(Integer.toString(compleated));
	}
	
}
