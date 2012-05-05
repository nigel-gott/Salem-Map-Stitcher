import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class FileManager {

	private static final String HOME_DIRECTORY = System.getProperty("user.home");
	private static final String DEFAULT_MAPS_DIRECTORY = HOME_DIRECTORY + "\\Salem\\map";

	private LogManager logManager;
	private File mapDirectory;
	private boolean mapDirectoryFound;

	public FileManager(LogManager logManager) {
		this.logManager = logManager;
		mapDirectoryFound = false;
	}

	public String tryFindMapsDirectory() {
		try {
			Path mapDirectoryPath = Paths.get(DEFAULT_MAPS_DIRECTORY);
			mapDirectoryPath.toRealPath();

			setMapDirectory(mapDirectoryPath.toFile());

			logManager.append("Automatically found Salems map directory.");
			return mapDirectoryPath.toString();
		} catch (Exception e) {
			logManager.append("Failed to automatically find Salems map directory.");
			return "Select your Salem map directory.";
		}
	}

	public void setMapDirectory(File selectedFile) {
		mapDirectoryFound = true;
		mapDirectory = selectedFile;
	}

	public boolean hasFoundMapDirectory() {
		return mapDirectoryFound ? mapDirectory.exists() : false;
	}

	public File[] getSessionDirectories() {
		return mapDirectory.listFiles(new MapFolderFilter());
	}

	public boolean tryWriteImage(BufferedImage image, String name) {
		String imageName = mapDirectory.getAbsolutePath() + "\\" + name + ".png";

		try {
			ImageIO.write(image, "png", new File(imageName));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
