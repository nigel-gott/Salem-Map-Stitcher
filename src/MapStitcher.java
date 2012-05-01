import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.SwingWorker;

public class MapStitcher extends SwingWorker<String, String> {

	private LogManager logManager;
	private FileManager fileManager;
	private File mapDirectory;
	private JButton stitchButton;

	public MapStitcher(FileManager fileManager, LogManager logManager, JButton stitchButton) {
		this.logManager = logManager;
		this.fileManager = fileManager;
		this.mapDirectory = fileManager.getMapDirectory();
		this.stitchButton = stitchButton;
	}

	protected String doInBackground() throws Exception {
		File[] mapFolders = mapDirectory.listFiles(new MapFolderFilter());

		List<SessionMap> sessionMaps = new ArrayList<SessionMap>();

		for (File sessionFolder : mapFolders) {
			SessionMap sessionMap = new SessionMap(this);

			if (sessionMap.loadImagesFromDirectory(sessionFolder)) {
				sessionMaps.add(sessionMap);
			}
		}

		List<BufferedImage> stitchedMaps = mergeSessionMaps(sessionMaps);

		for (int i = 0; i < stitchedMaps.size(); i++) {
			publish("Writing images...");
			String stitchedMapName = mapDirectory.getAbsolutePath() + "\\StitchedMap" + i + ".png";

			try {
				ImageIO.write(stitchedMaps.get(i), "png", new File(stitchedMapName));
			} catch (IOException e) {
				publish("Failed to write stitched image " + i + " to" + stitchedMapName + ".");
			}
		}

		return null;
	}

	private List<BufferedImage> mergeSessionMaps(List<SessionMap> maps) {
		List<BufferedImage> fullyStitchedMaps = new ArrayList<BufferedImage>();

		SessionMap sessionMap = maps.remove(0);
		boolean stitchedAtLeastOne;

		do {
			stitchedAtLeastOne = false;
			for (int i = 0; i < maps.size(); i++) {
				publish("Attempting to stitch two sessions. Size = " + maps.size() + " i = " + i);
				if (sessionMap.tryMergeWith(maps.get(i))) {
					stitchedAtLeastOne = true;
					maps.remove(i--);
				}
			}
		} while (stitchedAtLeastOne && maps.size() > 1);

		fullyStitchedMaps.add(sessionMap.generateStitchedMap());

		if (!maps.isEmpty()) {
			publish("Failed to stitch all sessions into one, creating new map and attempting to stitch remaining " + maps.size() + " sessions.");
			fullyStitchedMaps.addAll(mergeSessionMaps(maps));
		}

		publish("Finished stitching...");
		return fullyStitchedMaps;
	}

	public void publishToLog(String logLine) {
		publish(logLine);
	}

	protected void process(List<String> logsSoFar) {
		for (String line : logsSoFar) {
			logManager.append(line);
		}
	}

	protected void done() {
		logManager.append("Finished stitching...");
		logManager.append("Saving file to " + fileManager.getMapDirectory().getAbsolutePath() + ".");
		stitchButton.setEnabled(true);
	}
}
