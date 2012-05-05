import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapStitcher{

	private Logger logger;

	public MapStitcher(Logger logger) {
		this.logger = logger;
	}

	public void stitchMaps(File mapDirectory){
		File[] sessionDirectories = FileManager.getSessionDirectories(mapDirectory);
		List<SessionMap> sessionMaps = constructSessionMaps(sessionDirectories);

		logger.log("Stitching sessions...");
		List<BufferedImage> stitchedMaps = stitchSessionMaps(sessionMaps);
		logger.log("Stitched sessions.");
		writeStitchedMaps(stitchedMaps, mapDirectory);
	}
	
	private List<SessionMap> constructSessionMaps(File[] sessionDirectories){
		List<SessionMap> sessionMaps = new ArrayList<SessionMap>();

		for (File sessionFolder : sessionDirectories) {
			SessionMap sessionMap = new SessionMap();

			if (sessionMap.tryLoadTilesFrom(sessionFolder)) {
				sessionMaps.add(sessionMap);
			}
		}
		
		logger.log("Loaded session tiles from " + sessionMaps.size() + " sessions!");
		
		return sessionMaps;
	}

	private List<BufferedImage> stitchSessionMaps(List<SessionMap> maps) {
		List<BufferedImage> fullyStitchedMaps = new ArrayList<BufferedImage>();

		SessionMap sessionMap = maps.remove(0);
		boolean stitchedAtLeastOne;

		do {
			stitchedAtLeastOne = false;
			for (int i = 0; i < maps.size(); i++) {
				if (sessionMap.tryStitchWith(maps.get(i))) {
					logger.log("Stitched two sessions," + maps.size() +" to go!");
					stitchedAtLeastOne = true;
					maps.remove(i);
					i--;
				}
			}
		} while (stitchedAtLeastOne && maps.size() > 1);
		

		fullyStitchedMaps.add(sessionMap.generateStitchedMap());
		
		if (!maps.isEmpty()) {
			fullyStitchedMaps.addAll(stitchSessionMaps(maps));
		}

		return fullyStitchedMaps;
	}
	
	private void writeStitchedMaps(List<BufferedImage> stitchedMaps, File mapDirectory){
		for (int i = 0; i < stitchedMaps.size(); i++) {
			String imageName = "StitchedMap" + i;
			if(FileManager.tryWriteImage(mapDirectory, stitchedMaps.get(i), imageName)){
				logger.log("Successfully wrote " + imageName + " to file." );
			} else {
				logger.log("Failed to write " + imageName + " to file." );
			}
			
		}
	}
}
