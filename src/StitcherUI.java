import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class StitcherUI extends JPanel implements ActionListener {
	
	private JTextArea log;
	private JTextArea directory;

	public StitcherUI() {
		super(new BorderLayout());
		setOpaque(true);
		
		createLog();
		createStitchButton();
		createDirectoryPanel();
	}

	private void createLog() {
		log = new JTextArea(5,20);
		log.setMargin(new Insets(5,5,5,5));
		log.setEditable(false);
		
		add(new JScrollPane(log), BorderLayout.SOUTH);
	}
	
	private void createStitchButton(){
		JButton stitchButton = new JButton("Stitch map");
		add(stitchButton, BorderLayout.CENTER);
	}
	
	private void createDirectoryPanel() {
		JButton	directorySelectButton = new JButton("Select map directory");
		
		directory = new JTextArea(1,20);
		directory.setMargin(new Insets(5,5,5,5));
		directory.setEditable(true);
		
		JPanel directoryPanel = new JPanel();
		directoryPanel.add(directorySelectButton, BorderLayout.WEST);
		directoryPanel.add(directory, BorderLayout.EAST);
		
		add(directoryPanel, BorderLayout.NORTH);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

	}
	
	public static void createAndShowGUI() {
		JFrame frame = new JFrame("Salem Map Stitcher");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.add(new StitcherUI());
		
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

}
