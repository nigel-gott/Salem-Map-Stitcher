import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionMap {

	private Map<Point, Tile> tiles;
	private Map<Point, Tile> borderTiles;

	private int width, height;
	private int maxX = 0, minX = 0, maxY = 0, minY = 0;

	private Date sessionDate;

	public SessionMap() {
		this.tiles = new Hashtable<Point, Tile>();
		this.borderTiles = new Hashtable<Point, Tile>();
	}

	public boolean tryLoadTilesFrom(File directory) {
		if (directory.listFiles().length == 0) {
			// Sometimes sessions have no maps, we just ignore them.
			return false;
		}

		try {
			sessionDate = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss", Locale.ENGLISH).parse(directory.getName());
		} catch (ParseException e) {
			sessionDate = new Date(0);
		}

		// We assume that the only files in the session folders are the map
		// images.
		for (File mapImage : directory.listFiles()) {
			try {
				Point mapPoint = findPointFromImageName(mapImage.getName());
				Tile tile = new Tile(mapPoint, mapImage, sessionDate);
				addTile(tile);
			} catch (Exception e) {
				return false;
			}
		}

		findBorderTiles();

		return !tiles.isEmpty();
	}

	private void findBorderTiles() {
		// TODO Auto-generated method stub

	}

	public BufferedImage generateStitchedMap() {
		BufferedImage stitchedSessionMap = new BufferedImage(width, height, 6);

		for (Map.Entry<Point, Tile> entry : tiles.entrySet()) {
			Tile tile = entry.getValue();
			tile.drawTo(stitchedSessionMap, minX, minY);
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
		return tiles.size();
	}

	public boolean tryStitchWith(SessionMap sessionMap) {
		for (Map.Entry<Point, Tile> entry1 : tiles.entrySet()) {
			for (Map.Entry<Point, Tile> entry2 : sessionMap.tiles.entrySet()) {
				Tile tile1 = entry1.getValue();
				Tile tile2 = entry2.getValue();

				if (tile1.equals(tile2)) {
					stitchWith(sessionMap, tile1, tile2);
					return true;
				}
			}
		}
		return false;
	}

	private void stitchWith(SessionMap sessionMap, Tile map1, Tile map2) {
		int dX = map1.point.x - map2.point.x;
		int dY = map1.point.y - map2.point.y;

		for (Map.Entry<Point, Tile> entry : sessionMap.tiles.entrySet()) {
			Tile tile = entry.getValue();

			Point transformedPoint = new Point(tile.point.x + dX, tile.point.y + dY);
			tile.changePoint(transformedPoint);

			if (!tiles.containsKey(transformedPoint)) {
				addTile(tile);
			} else if (sessionMap.sessionDate.after(sessionDate)) {
				tiles.remove(transformedPoint);
				addTile(tile);
			}
		}

	}

	private void addTile(Tile tile) {
		Point point = tile.point;

		if (point.x > maxX)
			maxX = point.x;
		if (point.y > maxY)
			maxY = point.y;
		if (point.x < minX)
			minX = point.x;
		if (point.y < minY)
			minY = point.y;

		width = (maxX - minX + 1) * 100;
		height = (maxY - minY + 1) * 100;

		tiles.put(point, tile);
	}

	private boolean containsSubMapAt(Point transformedPoint) {
		return tiles.containsKey(transformedPoint);
	}

}
