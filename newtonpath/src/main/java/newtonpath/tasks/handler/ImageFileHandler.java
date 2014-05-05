package newtonpath.tasks.handler;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import newtonpath.logging.KLogger;
import newtonpath.statemanager.OperationResult;
import newtonpath.tasks.NameMapper;
import newtonpath.ui.AproxObs;
import newtonpath.ui.JKSnapShot;
import newtonpath.ui.NewtonSim;
import newtonpath.ui.XMLMapper;

import org.w3c.dom.Element;

public class ImageFileHandler implements ResultHandler {
	private final static KLogger LOGGER = KLogger
			.getLogger(ImageFileHandler.class);
	private final String inputFile;
	private final NameMapper nameMapper;
	private final JKSnapShot snapShot;
	private final List<NewtonSim> orbits = new ArrayList<NewtonSim>();

	public ImageFileHandler(String inputFile, NameMapper nameMapper, int width,
			int height) {
		this.inputFile = inputFile;
		this.snapShot = new JKSnapShot(width, height);
		this.nameMapper = nameMapper;
	}

	public void saveResult(OperationResult x) {
		AproxObs aprox = (AproxObs) x.getResultObject();
		NewtonSim xx = NewtonSim.getCopy(aprox);
		this.orbits.add(xx);

		synchronized (this) {
			BufferedImage z = this.snapShot
					.getImageCopy(new ArrayList<NewtonSim>(this.orbits));
			File outputFile = new File(this.nameMapper.getOutputName(
					this.inputFile, x) + ".png");
			LOGGER.info("Paint orbit to " + outputFile.getAbsolutePath());
			LOGGER.info(Arrays.toString(((AproxObs) x.getResultObject()).section.poincareEndVector));
			try {
				ImageIO.write(z, "png", outputFile);
			} catch (IOException e) {
				LOGGER.error("Error writing to file: " + outputFile);
				LOGGER.error(e.toString());
			}
		}
	}

	public static ImageFileHandler loadImageFileHandler(String baseFilename,
			NameMapper nameMapper, Element el) {
		Element elSize = (Element) el.getElementsByTagName("size").item(0);

		ImageFileHandler rh = new ImageFileHandler(baseFilename, nameMapper,
				Integer.parseInt(elSize.getAttribute("x")),
				Integer.parseInt(elSize.getAttribute("y")));

		rh.snapShot.setColors(XMLMapper.getInstance().loadColorSchema(
				(Element) el.getElementsByTagName("schema").item(0)));

		rh.snapShot.getRef().loadXml(
				(Element) el.getElementsByTagName("reference").item(0));
		return rh;
	}
}