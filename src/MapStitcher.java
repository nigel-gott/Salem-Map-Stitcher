import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapStitcher{

	private Logger logger;
	private FileManager fileManager;

	public MapStitcher(FileManager fileManager, Logger logger) {
		this.fileManager = fileManager;
		this.logger = logger;
	}

	public void stitchMaps(){
		File[] sessionDirectories = fileManager.getSessionDirectories();
		List<SessionMap> sessionMaps = new ArrayList<SessionMap>();

		for (File sessionFolder : sessionDirectories) {
			SessionMap sessionMap = new SessionMap();

			if (sessionMap.tryLoadTilesFrom(sessionFolder)) {
				logger.log("Loaded session tiles from " + sessionFolder.getName());
				sessionMaps.add(sessionMap);
			}
		}

		logger.log("Stitching sessions...");
		List<BufferedImage> stitchedMaps = mergeSessionMaps(sessionMaps);
		logger.log("Stitched sessions.");

		for (int i = 0; i < stitchedMaps.size(); i++) {
			String imageName = "StitchedMap" + i;
			if(fileManager.tryWriteImage(stitchedMaps.get(i), imageName)){
				logger.log("Successfully wrote " + imageName + " to file." );
			} else {
				logger.log("Failed to write " + imageName + " to file." );
			}
			
		}
	}

	private List<BufferedImage> mergeSessionMaps(List<SessionMap> maps) {
		List<BufferedImage> fullyStitchedMaps = new ArrayList<BufferedImage>();

		SessionMap sessionMap = maps.remove(0);
		boolean stitchedAtLeastOne;

		do {
			stitchedAtLeastOne = false;
			for (int i = 0; i < maps.size(); i++) {
				logger.log("Attempting to stitch two sessions...");
				if (sessionMap.tryMergeWith(maps.get(i))) {
					logger.log("Succeeded!");
					stitchedAtLeastOne = true;
					maps.remove(i--);
				}
			}
		} while (stitchedAtLeastOne && maps.size() > 1);
		

		fullyStitchedMaps.add(sessionMap.generateStitchedMap());
		
		if (!maps.isEmpty()) {
			fullyStitchedMaps.addAll(mergeSessionMaps(maps));
		}

		return fullyStitchedMaps;
	}
}
