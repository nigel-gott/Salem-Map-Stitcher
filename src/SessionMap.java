import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionMap {

	private List<SubMap> subMaps;

	private int width, height;
	private int maxX = 0, minX = 0, maxY = 0, minY = 0;

	public SessionMap() {
		this.subMaps = new ArrayList<SubMap>();
	}

	public boolean tryLoadTilesFrom(File directory) {
		if (directory.listFiles().length == 0) {
			// Sometimes sessions have no maps, we just ignore them.
			return false;
		}

		// We assume that the only files in the session folders are the map
		// images.
		for (File mapImage : directory.listFiles()) {
			try {
				Point mapPoint = findPointFromImageName(mapImage.getName());
				
				SubMap subMap = new SubMap(mapPoint, mapImage);
				if (subMap.setupSubMap()) {
					addSubMap(subMap);
				} else {
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		}
		return !subMaps.isEmpty();
	}

	public BufferedImage generateStitchedMap() {
		BufferedImage stitchedSessionMap = new BufferedImage(width, height, 6);

		for (SubMap subMap : subMaps) {
			subMap.drawTo(stitchedSessionMap, minX, minY);
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
			subMap.changePoint(transformedPoint);

			if (!containsSubMapAt(transformedPoint)) {
				addSubMap(subMap);
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

		width = (maxX - minX + 1) * 100;
		height = (maxY - minY + 1) * 100;

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

}
