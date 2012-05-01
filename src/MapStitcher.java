import java.util.List;

import javax.swing.SwingWorker;

public class MapStitcher extends SwingWorker<String, String> {

	private LogManager logManager;
	private FileManager fileManager;

	public MapStitcher(FileManager fileManager, LogManager logManager) {
		this.logManager = logManager;
		this.fileManager = fileManager;
	}

	protected String doInBackground() throws Exception {
		int i = 0;
		while (i < 10) {
			Thread.sleep(1000);
			i++;
			publish("Upto " + i);
		}

		return "Done." + i;
	}
	
	protected void process(List<String> logsSoFar){
		for(String line : logsSoFar){
			logManager.append(line);
		}
	}

	protected void done() {
		logManager.append("Finished stitching...");
		logManager.append("Saving file to " + fileManager.getMapDirectory().getAbsolutePath() + ".");
	}
}
