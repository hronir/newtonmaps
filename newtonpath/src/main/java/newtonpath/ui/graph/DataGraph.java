package newtonpath.ui.graph;


import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Iterator;

import newtonpath.kepler.Funcions.Write;
import newtonpath.ui.NewtonSim;
import newtonpath.ui.PlotterView;
import newtonpath.ui.graph.DataView.GraphRange;
import newtonpath.ui.widget.AbstractColorSchema;


public class DataGraph {
	public static class ColorSchema extends AbstractColorSchema {
		@Write
		public Color border = new Color(0x222222);
		@Write
		public Color tickSmall = new Color(0x22aa22);
		@Write
		public Color tickBig = new Color(0x2222aa);
		@Write
		public Color tickGrid = new Color(0x88ffffff, true);
	}

	private static final Color graphColor[] = { new Color(0xA4374C),
			new Color(0x2E5B92), new Color(0xB7188C), new Color(0x925717),
			new Color(0x131092), new Color(0x0E9249), new Color(0x620E92),
			new Color(0x005D92), new Color(0x509203), new Color(0xC79E1F) };

	public static final ColorSchema colors = new ColorSchema();

	private double scaleX, scaleY, deltaX, deltaY;
	public GraphRange graphRange;
	protected DataView<? extends DataPoint>[] dataView;

	public DataGraph() {
		this(null);
	}

	public DataGraph(DataView<? extends DataPoint> _dataview) {
		super();
		if (_dataview == null) {
			this.dataView = new DataView<?>[0];
		} else {
			this.dataView = new DataView<?>[] { _dataview };
		}
		this.scaleX = this.scaleY = 0;
		this.graphRange = new GraphRange();
		this.graphRange.x.min = this.graphRange.y.min = 0D;
		this.graphRange.x.max = this.graphRange.y.max = 1D;

	}

	public void setData(DataView<? extends DataPoint> _d) {
		this.dataView = new DataView<?>[1];
		this.dataView[0] = _d;
	}

	public void setData(int i, DataView<? extends DataPoint> _d) {
		this.dataView[i] = _d;
	}

	public void addData(DataView<? extends DataPoint> _d) {
		int i;
		DataView<? extends DataPoint>[] temp;
		temp = this.dataView;
		this.dataView = new DataView<?>[temp.length + 1];
		for (i = 0; i < temp.length; i++) {
			this.dataView[i] = temp[i];
		}
		this.dataView[temp.length] = _d;
		temp = null;
	}

	public void paint(Graphics g, Rectangle r) {
		Iterator<Integer> itGap;
		int i, nextGap;
		double p[] = new double[2];
		int x1, x2, y1, y2;
		int d;

		synchronized (this) {
			for (d = 0; d < this.dataView.length; d++) {
				x1 = y1 = 0;
				itGap = this.dataView[d].getGaps().iterator();
				nextGap = 0;
				g.setColor(graphColor[d % graphColor.length]);
				g.fillRect(d * 10, 20, 8, 8);
				for (i = 0; i < this.dataView[d].size(); i++) {
					this.dataView[d].getPoint(i, p);
					x2 = (int) (p[0] * this.scaleX + this.deltaX);
					y2 = (int) (p[1] * this.scaleY + this.deltaY);
					if (i != nextGap) {
						g.drawLine(x1, y1, x2, y2);
					} else {
						if (itGap.hasNext()) {
							nextGap = itGap.next().intValue();
						}
					}
					x1 = x2;
					y1 = y2;
				}
			}
			final double tickSpaceX = this.graphRange.x
					.calculateTickUnit(r.width / 7);
			final double tickSpaceY = this.graphRange.y
					.calculateTickUnit(r.height / 7);

			double startValue = tickSpaceX
					* Math.ceil(this.graphRange.x.min / tickSpaceX);
			double topValue = tickSpaceX
					* Math.floor(this.graphRange.x.max / tickSpaceX);
			int module = (int) ((this.graphRange.x.min / tickSpaceX) % 10D);
			for (double pos = startValue; pos <= topValue; pos += tickSpaceX) {
				int n = (int) (pos * this.scaleX + this.deltaX);
				int size;
				module = module % 10;
				size = module == 0 ? r.height : module == 5 ? 8 : 3;
				g.setColor(module == 0 ? this.colors.tickGrid
						: module == 5 ? this.colors.tickBig
								: this.colors.tickSmall);
				g.drawLine(r.x + n, r.y, r.x + n, r.y + size);
				if (module != 0) {
					g.drawLine(r.x + n, r.y + r.height - size, r.x + n, r.y
							+ r.height);
				}
				module++;
			}
			startValue = tickSpaceY
					* Math.ceil(this.graphRange.y.min / tickSpaceY);
			topValue = tickSpaceY
					* Math.floor(this.graphRange.y.max / tickSpaceY);
			module = (int) ((this.graphRange.y.min / tickSpaceY) % 10D);
			for (double pos = startValue; pos <= topValue; pos += tickSpaceY) {
				int n = (int) (pos * this.scaleY + this.deltaY);
				int size;
				module = module % 10;
				size = module == 0 ? r.width : module == 5 ? 8 : 3;
				g.setColor(module == 0 ? this.colors.tickGrid
						: module == 5 ? this.colors.tickBig
								: this.colors.tickSmall);
				g.drawLine(r.x, r.y + n, r.x + size, r.y + n);
				if (module != 0) {
					g.drawLine(r.x + r.width - size, r.y + n, r.x + r.width,
							r.y + n);
				}
				module++;
			}
			g.setColor(this.colors.border);
			g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
		}
	}

	public void setXScale(double x_min, double x_max, Rectangle r) {
		this.scaleX = (r.width) / (x_max - x_min);
		this.deltaX = -this.scaleX * x_min;
	}

	public void setYScale(double y_min, double y_max, Rectangle r) {
		this.scaleY = (r.height) / (y_min - y_max);
		this.deltaY = -this.scaleY * y_max;
	}

	public void autoScaleX(Rectangle r) {
		int d;
		if (this.dataView.length == 0) {
			this.graphRange.x.max = 1;
			this.graphRange.x.min = 0;
		} else {
			this.graphRange.x.max = this.dataView[0].x.dataRange.max;
			this.graphRange.x.min = this.dataView[0].x.dataRange.min;
			for (d = 1; d < this.dataView.length; d++) {
				this.graphRange.x.include(this.dataView[d].x.dataRange);
			}
		}
		this.graphRange.x.normalize();
		setXScale(this.graphRange.x.min, this.graphRange.x.max, r);
	}

	public void autoScaleY(Rectangle r) {
		int d;
		if (this.dataView.length == 0) {
			this.graphRange.y.max = 1;
			this.graphRange.y.min = 0;
		} else {
			this.graphRange.y.max = this.dataView[0].y.dataRange.max;
			this.graphRange.y.min = this.dataView[0].y.dataRange.min;
			for (d = 1; d < this.dataView.length; d++) {
				this.graphRange.y.include(this.dataView[d].y.dataRange);
			}
		}
		this.graphRange.y.normalize();
		setYScale(this.graphRange.y.min, this.graphRange.y.max, r);
	}

	public void autoScale(Rectangle r) {
		int d;
		if (this.dataView.length == 0) {
			this.graphRange.x.max = 1;
			this.graphRange.x.min = 0;
			this.graphRange.y.max = 1;
			this.graphRange.y.min = 0;
		} else {
			this.dataView[0].calcRange();
			this.graphRange.x.max = this.dataView[0].x.dataRange.max;
			this.graphRange.x.min = this.dataView[0].x.dataRange.min;
			this.graphRange.y.max = this.dataView[0].y.dataRange.max;
			this.graphRange.y.min = this.dataView[0].y.dataRange.min;
			for (d = 1; d < this.dataView.length; d++) {
				this.dataView[d].calcRange();
				this.graphRange.x.include(this.dataView[d].x.dataRange);
				this.graphRange.y.include(this.dataView[d].y.dataRange);
			}
		}
		this.graphRange.x.normalize();
		setXScale(this.graphRange.x.min, this.graphRange.x.max, r);
		this.graphRange.y.normalize();
		setYScale(this.graphRange.y.min, this.graphRange.y.max, r);
	}

	public void recalculateScale(Rectangle r) {
		setXScale(DataGraph.this.graphRange.x.min,
				DataGraph.this.graphRange.x.max, r);
		setYScale(DataGraph.this.graphRange.y.min,
				DataGraph.this.graphRange.y.max, r);
	}

	public static class DataGraphComponent extends Component implements
			PlotterView<NewtonSim> {
		final protected DataGraph graph;
		final protected Rectangle paintBounds;

		public DataGraphComponent(DataGraph theGraph) {
			this.graph = theGraph;
			this.paintBounds = new Rectangle(0, 0, 0, 0);
			addComponentListener(new ComponentAdapter() {

				@Override
				public void componentResized(ComponentEvent e) {
					DataGraphComponent.this.paintBounds.setSize(getWidth(),
							getHeight());
					DataGraphComponent.this.graph
							.recalculateScale(DataGraphComponent.this.paintBounds);
					repaint();
				}

				@Override
				public void componentShown(ComponentEvent e) {
					DataGraphComponent.this.paintBounds.setSize(getWidth(),
							getHeight());
					DataGraphComponent.this.graph
							.recalculateScale(DataGraphComponent.this.paintBounds);
				}
			});
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			this.graph.paint(g, this.paintBounds);
		}

		public void updatePoint(NewtonSim int1) {
		}

		public void beforeUpdate(NewtonSim int1) {
			this.setIgnoreRepaint(true);
		}

		public void afterUpdate(NewtonSim int1, boolean valid) {
			DataGraphComponent.this.graph
					.recalculateScale(DataGraphComponent.this.paintBounds);
			this.setIgnoreRepaint(false);
			repaint();
		}
	}
}
