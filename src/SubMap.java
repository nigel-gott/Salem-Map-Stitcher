import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import javax.imageio.ImageIO;

class SubMap {
	private File imageFile;
	public Point mapPoint;
	private int numBlackPoints;

	private static final int SIZE = 100;

	public SubMap(Point mapPoint, File imageFile) {
		this.imageFile = imageFile;
		this.mapPoint = mapPoint;
	}

	public void changePoint(Point transformedPoint) {
		// TODO Auto-generated method stub

	}

	public void drawTo(BufferedImage stitchedSessionMap, int minX, int minY) {
		try {
			BufferedImage image = ImageIO.read(imageFile);
			stitchedSessionMap.createGraphics().drawImage(image, null, (mapPoint.x - minX) * SIZE, (mapPoint.y - minY) * SIZE);
		} catch (IOException e) {

		}
	}

	public boolean setupSubMap() {
		HashSet<Point> blackPoints = findAllBlackPoints();
		numBlackPoints = blackPoints.size();
		return numBlackPoints != 0;
	}

	private HashSet<Point> findAllBlackPoints() {
		HashSet<Point> blackPoints = new HashSet<Point>();
		try {
			BufferedImage image = ImageIO.read(imageFile);
			for (int x = 1; x < SIZE - 1; x++) {
				for (int y = 1; y < SIZE - 1; y++) {
					int rgb = image.getRGB(x, y);

					// stitcher.publishToLog("("+x+","+y+") = " + rgb);
					// stitcher.publishToLog("Type = " + image.getType());

					if ((rgb & 0x00FFFFFF) == 0) {
						blackPoints.add(new Point(x, y));
					}
				}
			}
		} catch (IOException e) {
		}
		return blackPoints;
	}

	public boolean equals(SubMap subMap) {
		// stitcher.publishToLog("Comparing: " + blackPoints.size() + " == "
		// + subMap.blackPoints.size());

		if (numBlackPoints == subMap.numBlackPoints) {
			return subMap.findAllBlackPoints().containsAll(findAllBlackPoints());
		} else {
			return false;
		}

	}
}
