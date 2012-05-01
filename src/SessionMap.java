import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class SessionMap {

	private MapStitcher stitcher;
	private List<SubMap> subMaps;

	private int width, height, type;
	private int maxX = 0, minX = 0, maxY = 0, minY = 0;
	private static final int MAP_SIZE = 100;

	public SessionMap(MapStitcher stitcher) {
		this.stitcher = stitcher;
		this.subMaps = new ArrayList<SubMap>();
	}

	public boolean loadImagesFromDirectory(File directory) {
		if (directory.listFiles().length == 0) {
			// Sometimes sessions have no maps, we just ignore them.
			return false;
		}

		// We assume that the only files in the session folders are the map
		// images.
		for (File mapImage : directory.listFiles()) {
			try {
				Point mapPoint = findPointFromImageName(mapImage.getName());
				addSubMap(new SubMap(mapPoint, ImageIO.read(mapImage)));
			} catch (IOException e) {
				stitcher.publishToLog("Failed to load " + mapImage.getAbsolutePath() + ".");
				return false;
			} catch (Exception e) {
				stitcher.publishToLog("Encountered an invalid image name: " + mapImage.getName() + ".");
				return false;
			}
		}

		SubMap firstSubMap = subMaps.get(0);

		type = firstSubMap.image.getType();

		return true;
	}

	public BufferedImage generateStitchedMap() {
		BufferedImage stitchedSessionMap = new BufferedImage(width, height, type);

		for (SubMap subMap : subMaps) {
			Point subMapPoint = subMap.mapPoint;
			stitchedSessionMap.createGraphics().drawImage(subMap.image, null, (subMapPoint.x - minX) * MAP_SIZE, (subMapPoint.y - minY) * MAP_SIZE);
		}

		return stitchedSessionMap;
	}

	private static Point findPointFromImageName(String name) throws NumberFormatException {
		int x = 0, y = 0;

		Pattern numberRegex = Pattern.compile("-?\\d+");
		Matcher matcher = numberRegex.matcher(name);

		matcher.find();
		x = Integer.parseInt(matcher.group());
		matcher.find();
		y = Integer.parseInt(matcher.group());

		return new Point(x, y);
	}

	public int size() {
		return subMaps.size();
	}

	public boolean tryMergeWith(SessionMap sessionMap) {
		for (SubMap map1 : subMaps) {
			for (SubMap map2 : sessionMap.subMaps) {
				if (map1.equals(map2)) {
					mergeWith(sessionMap, map1, map2);
					return true;
				}
			}
		}
		return false;
	}

	private void mergeWith(SessionMap sessionMap, SubMap map1, SubMap map2) {
		int dX = map1.mapPoint.x - map2.mapPoint.x;
		int dY = map1.mapPoint.y - map2.mapPoint.y;

		for (int i = 0; i < sessionMap.subMaps.size(); i++) {
			SubMap subMap = sessionMap.subMaps.get(i);
			
			Point transformedPoint = new Point(subMap.mapPoint.x + dX, subMap.mapPoint.y + dY);
			SubMap transformedSubMap = new SubMap(transformedPoint, subMap.image);
			
			if (!containsSubMapAt(transformedPoint)) {
				addSubMap(transformedSubMap);
			}
		}

	}

	private void addSubMap(SubMap newSubMap) {
		Point newPoint = newSubMap.mapPoint;

		if (newPoint.x > maxX)
			maxX = newPoint.x;
		if (newPoint.y > maxY)
			maxY = newPoint.y;
		if (newPoint.x < minX)
			minX = newPoint.x;
		if (newPoint.y < minY)
			minY = newPoint.y;
		
		width = (maxX - minX + 1) * MAP_SIZE;
		height = (maxY - minY + 1) * MAP_SIZE;
		
		subMaps.add(newSubMap);
	}

	private boolean containsSubMapAt(Point transformedPoint) {
		for (int i = 0; i < subMaps.size(); i++) {
			if (subMaps.get(i).mapPoint.equals(transformedPoint)) {
				return true;
			}
		}
		return false;
	}

	class SubMap {
		BufferedImage image;
		Point mapPoint;
		HashSet<Point> blackPoints;

		public SubMap(Point mapPoint, BufferedImage image) {
			this.image = image;
			this.mapPoint = mapPoint;
			blackPoints = new HashSet<Point>();
			setupBlackPoints();
		}

		private void setupBlackPoints() {
			for (int x = 0; x < MAP_SIZE; x++) {
				for (int y = 0; y < MAP_SIZE; y++) {
					int rgb = image.getRGB(x, y);
					
					
					//stitcher.publishToLog("("+x+","+y+") = " + rgb);
					//stitcher.publishToLog("Type = " + image.getType());
					
					
					if((rgb & 0x00FFFFFF) == 0){
						blackPoints.add(new Point(x,y));
					}
				}
			}
		}

		public boolean equals(SubMap subMap) {
			//stitcher.publishToLog("Comparing: " + blackPoints.size() + " == " + subMap.blackPoints.size());
			if(blackPoints.isEmpty() || subMap.blackPoints.isEmpty())
				return false;
			
			if(blackPoints.size() == subMap.blackPoints.size()){
				return subMap.blackPoints.containsAll(blackPoints);
			} else {
				return false;
			}
				
		}
	}

}
