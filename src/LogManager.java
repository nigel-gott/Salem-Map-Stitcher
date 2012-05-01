import javax.swing.JTextArea;

public class LogManager {

	private JTextArea logArea;
	private static final String NEWLINE = "\n";

	public LogManager(JTextArea logArea) {
		this.logArea = logArea;

	}

	public void append(String newLogLine) {
		logArea.append(newLogLine + NEWLINE);
	}

}
