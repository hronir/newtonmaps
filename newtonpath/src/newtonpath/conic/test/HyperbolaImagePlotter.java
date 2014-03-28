/**
 * 
 */
package newtonpath.conic.test;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import newtonpath.conic.drawing.HyperbolaPlotter;


class HyperbolaImagePlotter extends HyperbolaPlotter {
	final private Rectangle rect;
	final private BufferedImage image;
	final private int color;

	public HyperbolaImagePlotter(BufferedImage _img, Rectangle _r, int _color) {
		this.rect = _r;
		this.image = _img;
		this.color = _color;
	}

	@Override
	protected void plot(int x, int y) {
		if (this.rect.contains(x, y)) {
			this.image.setRGB(x - this.rect.x, y - this.rect.y, this.color);
		}
	}

	public void saveImage(String id) {
		// Write generated image to a file
		try {
			// Save as PNG
			File file = new File(id + ".png");
			ImageIO.write(this.image, "png", file);
		} catch (IOException e) {
		}
	}
}