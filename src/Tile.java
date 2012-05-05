import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;

import javax.imageio.ImageIO;

class Tile {
	private File imageFile;
	public Point point;
	private int numBlackPoints;

	private static final int SIZE = 100;

	public Tile(Point mapPoint, File imageFile, Date sessionDate) {
		this.imageFile = imageFile;
		this.point = mapPoint;
	}

	public void changePoint(Point transformedPoint) {
		point = transformedPoint;
	}

	public void drawTo(BufferedImage stitchedSessionMap, int minX, int minY) {
		try {
			BufferedImage image = ImageIO.read(imageFile);
			stitchedSessionMap.createGraphics().drawImage(image, null, (point.x - minX) * SIZE, (point.y - minY) * SIZE);
		} catch (IOException e) {

		}
	}

	private HashSet<Point> findAllBlackPoints() {
		HashSet<Point> blackPoints = new HashSet<Point>();
		try {
			BufferedImage image = ImageIO.read(imageFile);
			for (int x = 0; x < SIZE; x++) {
				for (int y = 0; y < SIZE; y++) {
					int rgb = image.getRGB(x, y);

					if ((rgb & 0x00FFFFFF) == 0) {
						blackPoints.add(new Point(x, y));
					}
				}
			}
		} catch (IOException e) {
			return new HashSet<Point>();
		}
		return blackPoints;
	}

	public boolean equals(Tile tile) {
		if (numBlackPoints == tile.numBlackPoints) {
			return tile.findAllBlackPoints().containsAll(findAllBlackPoints());
		} else {
			return false;
		}
	}
}
