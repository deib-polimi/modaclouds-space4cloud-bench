package space4cloudbench.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

public class MainWindow extends WindowAdapter implements ActionListener {

	
	private JFrame frame;
	private JPanel contentPane;
	private JPanel topPanel;
	private JTextField repetitionField;
	private JButton startButton;
	private Map<String, ProjectPanel> projectPanels = new HashMap<String, ProjectPanel>(); 
	private List<String> selectedProjects;
	private boolean started = false;
	private boolean disposed = false;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.show();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		frame = new JFrame();
		frame.setBounds(100, 100, 513, 459);		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		frame.setMinimumSize(new Dimension(400, 50));
		frame.setContentPane(contentPane);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(this);

		contentPane.setLayout(new MigLayout());
		
		topPanel = new JPanel();
		contentPane.add(topPanel, "cell 0 0,grow");
		topPanel.setLayout(new MigLayout());
		
		JPanel lowPanel = new JPanel();
		contentPane.add(lowPanel, "cell 0 1");
		lowPanel.setLayout(new MigLayout());
		
		JLabel repetitionsLabel = new JLabel("Repetitions");
		lowPanel.add(repetitionsLabel);
		
		repetitionField = new JTextField("1");
		repetitionField.setMinimumSize(new Dimension(40,20));
		lowPanel.add(repetitionField, "");
		
		startButton = new JButton("Start");
		lowPanel.add(startButton);
		startButton.addActionListener(this);
			
	}
	
	public void addProject(String name){
		ProjectPanel panel = new ProjectPanel(name);
		projectPanels.put(name, panel);
		topPanel.add(panel,"wrap");
		Dimension minSize = frame.getMinimumSize();
		frame.setMinimumSize(new Dimension(minSize.width, minSize.height+33));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(startButton)){
			selectedProjects = new ArrayList<>();
			for(ProjectPanel p:projectPanels.values()){
				if(p.isSelected())
					selectedProjects.add(p.getProjectName());
			}
			markStarted();
		}
	}

	

	private synchronized void markStarted() {
		started = true;		
	}

	public List<String> getSelectedProjects() {
		return selectedProjects;
	}
	
	public int getRepetitions(){
		return Integer.parseInt(repetitionField.getText());
	}
	
	public void updateCompletion(String projectName){		
		projectPanels.get(projectName).updateCompletion();
	}
	
	public void show(){
		frame.setVisible(true);
	}
	
	public synchronized boolean isStarted(){
		return started;
	}
	
	@Override
	public void windowClosing(WindowEvent e) {		
		super.windowClosing(e);
		frame.dispose();
		disposed = true;
	}
	
	
	public boolean isDisposed(){
		return disposed;
	}


}
