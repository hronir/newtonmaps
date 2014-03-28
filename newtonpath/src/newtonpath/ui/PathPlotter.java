package newtonpath.ui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

import newtonpath.kepler.events.EventPosition;

public class PathPlotter implements PlotterView<NewtonSim> {

	private Point oldPlotPoint[] = null, plotPoint[] = null;
	private boolean isValidPos[] = null;
	private final double centerPoint[] = new double[3];

	protected Graphics2D graph;
	private Dimension size;

	private final Point centerImg = new Point();
	private final NewtonPlotterParam param = new NewtonPlotterParam();

	public static ParameterManager<NewtonPlotterParam> parameterManager = new ParameterManager<NewtonPlotterParam>() {

		public NewtonPlotterParam getObject() {
			return new NewtonPlotterParam();
		}

		public NewtonPlotterParam getCopy(NewtonPlotterParam original) {
			NewtonPlotterParam o = new NewtonPlotterParam();
			o.set(original);
			return o;
		}

		public void copyTo(NewtonPlotterParam origin,
				NewtonPlotterParam destination) {
			destination.set(origin);

		}
	};

	public PathPlotter(Graphics2D g, Dimension size) {
		this.graph = g;
		this.size = size;
		this.centerImg.x = this.size.width / 2;
		this.centerImg.y = this.size.height / 2;
	}

	public void setGraphics(Graphics2D g, Dimension size) {
		this.graph = g;
		this.size = size;
		this.centerImg.x = this.size.width / 2;
		this.centerImg.y = this.size.height / 2;
	}

	private void calcCenter(NewtonSim integrador) {
		int i;
		if (getParam().centerToEvent != null) {
			getParam().centerToEvent.evalPosition(this.centerPoint,
					integrador.position);
		} else {
			for (i = 0; i < 3; i++) {
				this.centerPoint[i] = integrador.position[3
						* getParam().centerToBody + i];
			}
		}
	}

	public NewtonPlotterParam getParam() {
		return this.param;
	}

	public void set(int toBody, EventPosition toEvent, Projection proj) {
		this.param.set(toBody, toEvent, proj);
	}

	public void updatePoint(NewtonSim integrador) {
		int i;
		if (integrador != null && this.centerPoint != null
				&& this.plotPoint != null) {
			for (i = 0; i < 1; i++) {
				getParam().reference.project(i, integrador.position, this.centerPoint, this.plotPoint[i]);
				if (this.plotPoint[i].x > Integer.MIN_VALUE) {
					if (this.isValidPos[i]
							&& !this.oldPlotPoint[i].equals(this.plotPoint[i])) {

						if (this.oldPlotPoint[i].y > this.plotPoint[i].y) {
							this.graph.drawLine(this.plotPoint[i].x
									+ this.centerImg.x, this.plotPoint[i].y
									+ this.centerImg.y, this.oldPlotPoint[i].x
									+ this.centerImg.x, this.oldPlotPoint[i].y
									+ this.centerImg.y);
						} else {
							this.graph.drawLine(this.oldPlotPoint[i].x
									+ this.centerImg.x, this.oldPlotPoint[i].y
									+ this.centerImg.y, this.plotPoint[i].x
									+ this.centerImg.x, this.plotPoint[i].y
									+ this.centerImg.y);
						}
					}

					this.oldPlotPoint[i].x = this.plotPoint[i].x;
					this.oldPlotPoint[i].y = this.plotPoint[i].y;
					this.isValidPos[i] = true;
				} else {
					this.isValidPos[i] = false;
				}
			}
		}
	}

	public void reset(NewtonSim _int) {
		this.isValidPos = new boolean[_int.bodies];
		if (this.plotPoint == null || this.plotPoint.length != _int.bodies) {
			this.plotPoint = new Point[_int.bodies];
			this.oldPlotPoint = new Point[_int.bodies];
			for (int i = 0; i < this.plotPoint.length; i++) {
				this.oldPlotPoint[i] = new Point();
				this.plotPoint[i] = new Point();
			}
		}
		calcCenter(_int);
	}

	public void afterUpdate(NewtonSim int1, boolean valid) {
	}

	public void beforeUpdate(NewtonSim int1) {
		reset(int1);
	}

}