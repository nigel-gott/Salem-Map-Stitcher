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
		processImages();
		return null;
	}

	private void processImages() {

		File[] mapFolders = mapDirectory.listFiles(new MapFolderFilter());

		for (File mapFolder : mapFolders) {
			// Assume that the only files in the session folders are the images.
			List<BufferedImage> sessionImages = new ArrayList<BufferedImage>();
			for (File mapImage : mapFolder.listFiles()) {
				try {
					publish("Loading: " + mapImage.getAbsolutePath());
					sessionImages.add(ImageIO.read(mapImage));
				} catch (IOException e) {
					publish("Failed to load " + mapImage.getAbsolutePath() + ", exiting.");
					return;
				}
			}
			
		}
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
