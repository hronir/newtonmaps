package newtonpath.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import newtonpath.kepler.OsculatoryElements;
import newtonpath.kepler.events.Event;
import newtonpath.ui.widget.AbstractColorSchema;

public class JKSnapShot {

	public static class ColorSchema extends AbstractColorSchema {
		public Color sky = new Color(0xD9E0E4);
		public Color orbits = new Color(0x888DBF);
		public Color path = new Color(0xD5698B);
	}

	private final ProjectionReference ref;
	private final OrbitsPlotter orbitsPlotter;
	protected PathPlotter linePlotter;
	protected BufferedImage image;
	private final Graphics2D graph;
	private boolean paintSpacecraftPath = true;
	private boolean paintPlanetOsculatoryOrbit = true;
	private boolean paintSpacecraftOsculatoryOrbit = false;
	private boolean paintBackground = true;

	private volatile Dimension siz;
	private final Rectangle bounds;
	public static final ColorSchema colors = new ColorSchema();
	private final double[] centerPoint = new double[3];
	public ColorSchema instanceColors;

	public ProjectionReference getRef() {
		return this.ref;
	}

	public JKSnapShot(int width, int height) {
		this(new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR));
	}

	public JKSnapShot(BufferedImage image) {
		this.image = image;
		int height = image.getHeight();
		int width = image.getWidth();
		this.instanceColors = colors;
		this.siz = new Dimension(width, height);
		this.bounds = new Rectangle(-width / 2, -height / 2, width, height);

		this.ref = new ProjectionReference();

		this.graph = (Graphics2D) this.image.getGraphics();
		this.graph.setBackground(this.instanceColors.sky);
		this.orbitsPlotter = new OrbitsPlotter(this.graph);
		this.linePlotter = new PathPlotter(this.graph, this.siz);
	}

	public void setColors(ColorSchema schema) {
		this.instanceColors = schema;
		this.graph.setBackground(this.instanceColors.sky);
	}

	public boolean isPaintPlanetOsculatoryOrbit() {
		return this.paintPlanetOsculatoryOrbit;
	}

	public void setPaintPlanetOsculatoryOrbit(boolean planetOsculatory) {
		this.paintPlanetOsculatoryOrbit = planetOsculatory;
	}

	public boolean isPaintSpacecraftOsculatoryOrbit() {
		return this.paintSpacecraftOsculatoryOrbit;
	}

	public void setPaintSpacecraftOsculatoryOrbit(boolean spacecraftOsculatory) {
		this.paintSpacecraftOsculatoryOrbit = spacecraftOsculatory;
	}

	public Image paintImage(List<NewtonSim> integrList) {

		if (this.paintBackground) {
			this.graph.setColor(this.instanceColors.sky);
			this.graph.fillRect(0, 0, this.bounds.width, this.bounds.height);
		} else {
			clearImage();
		}

		if (integrList.size() > 0) {
			this.linePlotter.getParam().set(1, null, this.ref);
			calcCenterPoint(integrList.get(0));

			if (this.paintPlanetOsculatoryOrbit
					|| this.paintSpacecraftOsculatoryOrbit) {
				paintOsculatoryOrbits(integrList.get(0));
			}

			if (this.paintSpacecraftPath) {
				for (NewtonSim integr : integrList) {
					paintSpacecraftPath(integr);
				}
			}
		}
		return this.image;
	}

	public void clearImage() {
		Composite x = this.graph.getComposite();
		this.graph.setComposite(AlphaComposite.getInstance(
				AlphaComposite.CLEAR, 0.0f));
		this.graph.fillRect(0, 0, this.bounds.width, this.bounds.height);
		this.graph.setComposite(x);
	}

	public void paintSpacecraftPath(NewtonSim integ) {
		Color color = this.instanceColors.path;
		paintSpacecraftPath(integ, color);
	}

	public void paintSpacecraftPath(NewtonSim integ, Color color) {
		this.graph.setColor(color);

		double min, max;
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		for (Event ev : integ.getEvents()) {
			double t = ev.getDate();
			if (t < min) {
				min = t;
			}
			if (t > max) {
				max = t;
			}
		}

		if (min < Double.MAX_VALUE) {

			max += 50;
			try {
				SimulatorPlotter<NewtonSim> simPlotter;

				integ.timeIntegration(min - integ.getAbsoluteTime());
				integ.setEpochNow();
				integ.setParamMaxStep(10D);
				simPlotter = new SimulatorPlotter<NewtonSim>(this.linePlotter,
						integ, max - min);
				simPlotter.paint();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void paintOsculatoryOrbits(NewtonSim integ) {
		Color color = this.instanceColors.orbits;
		paintOsculatoryOrbits(integ, color);
	}

	public void paintOsculatoryOrbits(NewtonSim integ, Color color) {
		calcCenterPoint(integ);

		this.graph.setColor(color);
		Point[] plotPoint = new Point[integ.bodies];
		for (int i = 0; i < integ.bodies; i++) {
			plotPoint[i] = new Point();
			this.ref.project(i, integ.position, this.centerPoint, plotPoint[i]);
		}

		final OsculatoryElements[] keplerElements = integ.getKeplerElements();
		integ.fillKeplerElements(keplerElements);
		this.orbitsPlotter.paintOrbits(this.bounds, plotPoint, this.ref,
				keplerElements, this.centerPoint,
				this.paintSpacecraftOsculatoryOrbit);
	}

	public void calcCenterPoint(NewtonSim integ) {
		int body = 1;
		for (int i = 0; i < 3; i++) {
			this.centerPoint[i] = integ.position[3 * body + i];
		}
	}

	public BufferedImage getImageCopy(NewtonSim integr) {
		return getImageCopy(Collections.singletonList(integr));
	}

	public BufferedImage getImageCopy(List<NewtonSim> integr) {
		WritableRaster raster = this.image.getRaster()
				.createCompatibleWritableRaster();

		paintImage(integr);
		copyImageData(raster);

		BufferedImage newImage = new BufferedImage(this.image.getColorModel(),
				raster, true, null);
		return newImage;

	}

	public void copyImageData(WritableRaster raster) {
		raster.setDataElements(0, 0, this.image.getRaster());
	}

	public void setColors(Map<String, Integer> loadColorSchema) {
		this.instanceColors = new ColorSchema();
		this.instanceColors.setSchema(loadColorSchema);
		this.graph.setBackground(this.instanceColors.sky);
	}

	public double getWidth() {
		return this.bounds.getWidth();
	}

	public double getHeight() {
		return this.bounds.getHeight();
	}

	public boolean isPaintSpacecraftPath() {
		return this.paintSpacecraftPath;
	}

	public void setPaintSpacecraftPath(boolean paintSpacecraftPath) {
		this.paintSpacecraftPath = paintSpacecraftPath;
	}

	public boolean isPaintBackground() {
		return this.paintBackground;
	}

	public void setPaintBackground(boolean paintBackground) {
		this.paintBackground = paintBackground;
	}
}
