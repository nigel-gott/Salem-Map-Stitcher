import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.SwingWorker;

public class MapStitcher extends SwingWorker<String, String> {

	private LogManager logManager;
	private File mapDirectory;
	private JButton stitchButton;

	public MapStitcher(FileManager fileManager, LogManager logManager, JButton stitchButton) {
		this.logManager = logManager;
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
			} catch (Exception e){
				publish("Failed somehow..");
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
			publish("Stitching two sessions... i: " + i + ", size: " + maps.size());
				if (sessionMap.tryMergeWith(maps.get(i))) {
					publish("Successfully merged two maps.");
					stitchedAtLeastOne = true;
					maps.remove(i--);
				}
			}
		} while (stitchedAtLeastOne && maps.size() > 1);
		
		publish("Generating map.");

		try{
		fullyStitchedMaps.add(sessionMap.generateStitchedMap());
		} catch (Exception e){
			e.printStackTrace();
			System.exit(1);
		}
		publish("Generating map2.");

		if (!maps.isEmpty()) {
			publish("Failed to stitch all sessions into one, creating new map and attempting to stitch remaining " + maps.size() + " sessions.");
			fullyStitchedMaps.addAll(mergeSessionMaps(maps));
		}

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
		stitchButton.setEnabled(true);
	}
}
