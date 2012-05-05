import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

public class MapStitcher extends SwingWorker<String, String> {

	private LogManager logManager;
	private FileManager fileManager;
	private StitcherUI ui;

	public MapStitcher(FileManager fileManager, LogManager logManager, StitcherUI ui) {
		this.fileManager = fileManager;
		this.logManager = logManager;
		this.ui = ui;
	}

	protected String doInBackground() throws Exception {
		publish("Starting to stitch...");
		File[] sessionDirectories = fileManager.getSessionDirectories();
		List<SessionMap> sessionMaps = new ArrayList<SessionMap>();

		for (File sessionFolder : sessionDirectories) {
			SessionMap sessionMap = new SessionMap(this);

			if (sessionMap.tryLoadTilesFrom(sessionFolder)) {
				sessionMaps.add(sessionMap);
			}
		}

		List<BufferedImage> stitchedMaps = mergeSessionMaps(sessionMaps);

		publish("Writing images...");
		for (int i = 0; i < stitchedMaps.size(); i++) {
			String imageName = "StitchedMap" + i;
			if(fileManager.tryWriteImage(stitchedMaps.get(i), imageName)){
				publish("Successfully written " + imageName + " to file.");
			} else {
				publish("Failed to write " + imageName + " to file.");
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
		ui.setButtonStatus(true);
	}
}
