package newtonpath.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.SwingUtilities;

import newtonpath.kepler.events.EventPosition;

public class BufferdPathPlotter extends PathPlotter {

	DoubleBufferedImage<NewtonPlotterParam> buf;

	private final Runnable repaintComponents;

	public BufferdPathPlotter(Runnable notification, Dimension size, Color sky,
			Color line) {
		this(new DoubleBufferedImage<NewtonPlotterParam>(parameterManager,
				newImage(sky, line, size)), notification, size, sky, line);
	}

	private BufferdPathPlotter(DoubleBufferedImage<NewtonPlotterParam> buf,
			Runnable notification, Dimension size, Color sky, Color line) {
		super((Graphics2D) buf.getGraphics(), size);
		this.repaintComponents = notification;

		this.buf = buf;
		this.graph = (Graphics2D) buf.getGraphics();
		this.graph
				.setBackground(new Color(buf.getColorModel().getColorSpace(),
						sky.getComponents(buf.getColorModel().getColorSpace(),
								null), 0));
		this.graph.setColor(line);
	}

	public void setGraphics(Dimension size, Color sky, Color line) {
		this.buf = new DoubleBufferedImage<NewtonPlotterParam>(
				parameterManager, newImage(sky, line, size));
		this.graph = (Graphics2D) this.buf.getGraphics();
		this.graph.setBackground(new Color(this.buf.getColorModel()
				.getColorSpace(), sky.getComponents(this.buf.getColorModel()
				.getColorSpace(), null), 0));
		this.graph.setColor(line);
		setGraphics(this.graph, size);

	}

	@Override
	public void afterUpdate(NewtonSim int1, boolean valid) {
		this.buf.validate(valid);
		if (valid) {
			SwingUtilities.invokeLater(this.repaintComponents);
		}
	}

	@Override
	public void beforeUpdate(NewtonSim int1) {
		this.buf.prepare(int1);
		this.graph.clearRect(0, 0, this.buf.getWidth(), this.buf.getHeight());
		super.beforeUpdate(int1);
	}

	public Image getImage() {
		return this.buf.getImage();
	}

	@Override
	public void set(int toBody, EventPosition toEvent, Projection p) {
		super.set(toBody, toEvent, p);
		this.buf.setParam(getParam());
	}

	public static BufferedImage newImage(Color skyColor, Color lineColor,
			Dimension d) {
		return new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
	}

}
