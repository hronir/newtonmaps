package newtonpath.builder;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageFusion {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		BufferedImage destination;
		Color bkg = null;
		List<BufferedImage> source = new ArrayList<BufferedImage>();
		File outputfile = null;

		List<String> argList = new ArrayList<String>(Arrays.asList(args));
		while (argList.size() > 0) {
			String arg = argList.remove(0);
			if (arg.equals("-o")) {
				outputfile = new File(argList.remove(0));
			} else if (arg.equals("-c")) {
				bkg = new Color(Integer.decode(argList.remove(0)), false);
			} else {
				source.add(ImageIO.read(new File(arg)));
			}
		}

		BufferedImage im = source.get(0);
		destination = new BufferedImage(im.getWidth(), im.getHeight(),
				im.getType());

		Graphics g = destination.getGraphics();
		if (bkg != null) {
			g.setColor(bkg);
			g.fillRect(0, 0, destination.getWidth(), destination.getHeight());
		}
		for (BufferedImage i : source) {
			g.drawImage(i, 0, 0, null);
		}

		ImageIO.write(destination, "png", outputfile);
	}
}
