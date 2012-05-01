import java.nio.file.Path;
import java.nio.file.Paths;


public class FileManager {
	
	private static final String HOME_DIRECTORY = System.getProperty("user.home");
	private static final String DEFAULT_MAPS_DIRECTORY = HOME_DIRECTORY + "\\Salem\\map";
	
	private Path mapDirectoryPath;
	private boolean foundMapDirectory;
	
	private LogManager logManager;
	
	public FileManager(LogManager logManager){
		this.logManager = logManager;
		foundMapDirectory = false;
	}
	
	public String tryFindMapsDirectory(){
		try{
			mapDirectoryPath = Paths.get(DEFAULT_MAPS_DIRECTORY);
			mapDirectoryPath.toRealPath();
			logManager.log("Successfully found Salems map directory.");
			foundMapDirectory = true;
			return mapDirectoryPath.toString();
		} catch (Exception e){
			logManager.log("Failed to find Salems map directory.");
			foundMapDirectory = false;
			return "Select your Salem maps directory.";
		}
	}

}
