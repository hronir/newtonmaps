package newtonpath.images;

import javax.swing.ImageIcon;

public class Images {
	public static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = Images.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		}
		{
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	final public static ImageIcon iconPlay = createImageIcon("media-playback-start.png");
	final public static ImageIcon iconPlayBackwards = createImageIcon("media-playback-startback.png");
	final public static ImageIcon iconStop = createImageIcon("media-playback-pause.png");
	final public static ImageIcon iconBack = createImageIcon("media-seek-backward.png");
	final public static ImageIcon iconRestart = createImageIcon("media-playback-stop.png");

	final public static ImageIcon iconMER = createImageIcon("mer.png");
	final public static ImageIcon iconVEN = createImageIcon("ven.png");
	final public static ImageIcon iconEAR = createImageIcon("ear.png");
	final public static ImageIcon iconMAR = createImageIcon("mar.png");
	final public static ImageIcon iconJUP = createImageIcon("jup.png");
	final public static ImageIcon iconSAT = createImageIcon("sat.png");
	final public static ImageIcon iconURA = createImageIcon("ura.png");
	final public static ImageIcon iconNEP = createImageIcon("nep.png");
	final public static ImageIcon iconPLU = createImageIcon("plu.png");

	final public static ImageIcon iconEAST = createImageIcon("east.png");
	final public static ImageIcon iconWEST = createImageIcon("west.png");
	final public static ImageIcon iconNORTH = createImageIcon("north.png");
	final public static ImageIcon iconSOUTH = createImageIcon("south.png");

	final public static ImageIcon iconPLUS = createImageIcon("out.png");
	final public static ImageIcon iconMINUS = createImageIcon("in.png");

}
