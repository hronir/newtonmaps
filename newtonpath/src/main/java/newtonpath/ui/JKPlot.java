package newtonpath.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import newtonpath.application.JKApplication;
import newtonpath.images.Images;
import newtonpath.kepler.OsculatoryElements;
import newtonpath.kepler.bodies.Body;
import newtonpath.kepler.bodies.Planet;
import newtonpath.kepler.bodies.Spacecraft;
import newtonpath.kepler.events.Event;
import newtonpath.kepler.events.EventPosition;
import newtonpath.sim.SimulatorEngine;
import newtonpath.ui.widget.AbstractColorSchema;
import newtonpath.ui.widget.AbstractRepeatAction;
import newtonpath.ui.widget.AbstractSimpleAction;
import newtonpath.ui.widget.AbstractToggleAction;

import org.w3c.dom.Element;

/**
 * 
 * @author oriol
 */
public class JKPlot extends JPanel implements JKView<NewtonSim>, Serializable,
		ConfigurableComponent {

	public AbstractRepeatAction actionZoomIn = new AbstractRepeatAction(
			"Zoom in", Images.iconPLUS) {
		public void actionPerformed(ActionEvent e) {
			doZoom(1);
		}
	};

	public AbstractRepeatAction actionZoomOut = new AbstractRepeatAction(
			"Zoom out", Images.iconMINUS) {
		public void actionPerformed(ActionEvent e) {
			doZoom(-1);
		}
	};

	public AbstractRepeatAction rotateAction(final int axis, final int qty,
			ImageIcon icon, String description) {
		return new AbstractRepeatAction(description, icon) {
			public void actionPerformed(ActionEvent e) {
				doRotate(axis, qty);
			}
		};
	}

	protected AbstractButton newControlButton(AbstractRepeatAction h) {
		AbstractButton b = h.getNewIconButton();
		b.setContentAreaFilled(false);
		b.setRolloverEnabled(true);
		return b;
	}

	private final class Controls extends JPanel {
		public Controls() {
			final int siz = 12;
			final int cols = 3;
			final int rows = 3;
			final int margin = 2;
			setOpaque(false);
			setLayout(new GridLayout(rows, cols, margin, margin));
			setSize(rows * siz + margin * (rows - 1), cols * siz + margin
					* (cols - 1));

			add(newControlButton(JKPlot.this.actionZoomIn));
			add(newControlButton(rotateAction(0, 1, Images.iconNORTH, "Turn up")));
			add(newControlButton(JKPlot.this.actionZoomOut));

			add(newControlButton(rotateAction(1, -1, Images.iconWEST,
					"Turn right")));
			add(newControlButton(rotateAction(0, -1, Images.iconSOUTH,
					"Turn down")));
			add(newControlButton(rotateAction(1, 1, Images.iconEAST,
					"Turn right")));

			add(Box.createGlue());
			add(Box.createGlue());
			add(Box.createGlue());
		}
	}

	public static class ColorScheme extends AbstractColorSchema {
		public Color path = new Color(0xD5698B);
		public Color sky = new Color(0xD9E0E4);
		public Color osculatoryOrbits = new Color(0x888DBF);

		public Color flyby = new Color(0X909080);
		public Color planet = new Color(0x355F38);
		public Color sun = new Color(0xaa6638);
		public Color spacecraft = new Color(0x0000dd);

		Color[] solarSystem = { this.sun, this.planet, this.planet,
				this.planet, this.planet, this.planet, this.planet,
				this.planet, this.planet, this.planet };

		private void refreshSolarSystem() {
			this.solarSystem[0] = colors.sun;
			for (int i = 1; i < this.solarSystem.length; i++) {
				this.solarSystem[i] = colors.planet;
			}
		}

		@Override
		public void setSchema(Map<String, Integer> val) {
			super.setSchema(val);
			refreshSolarSystem();
		}
	}

	public static final ColorScheme colors = new ColorScheme();

	transient protected SimulatorEngine<NewtonSim> parent;

	transient protected Point lastDragPoint = null;

	protected boolean drawLine = false;
	protected boolean drawOrbits = false;
	protected boolean drawPoints = false;
	protected boolean drawOsculatoryOrbit = false;

	transient protected Point deltaImg = new Point();
	transient protected Point centerImg = new Point();
	transient protected Point deltaPlot = new Point();

	transient protected NewtonSim integrador;

	protected ProjectionReference ref;

	protected BufferdPathPlotter linePlotter;

	protected BufferedImage imgOsc;
	private Graphics2D grafOsc;

	private Point plotPoint[] = null, eventPos[] = null;

	private int centerToPlanet = 0;
	private int centerToBody = -1;
	private int centerToEv = -1;

	private final double centerPoint[] = new double[3];

	private ObjectSymbol planetSymbol[];
	private EventSymbol eventSymbol[];

	EventPosition[] eventPositions = new EventPosition[0];

	final static Dimension planetComponentSize = new Dimension(15, 15);
	final static Dimension planetSymbolSize = new Dimension(7, 7);
	final static Point planetSymbolDelta = new Point(
			-planetComponentSize.width / 2, -planetComponentSize.height / 2);
	final static Point planetSymbolPoint = new Point(
			(planetComponentSize.width - planetSymbolSize.width) / 2,
			(planetComponentSize.height - planetSymbolSize.height) / 2);

	final static Dimension spacecraftComponentSize = new Dimension(15, 15);
	final static Dimension spacecraftSymbolSize = new Dimension(5, 5);
	final static Point spacecraftSymbolDelta = new Point(
			-spacecraftComponentSize.width / 2,
			-spacecraftComponentSize.height / 2);
	final static Point spacecraftSymbolPoint = new Point(
			(spacecraftComponentSize.width - spacecraftSymbolSize.width) / 2,
			(spacecraftComponentSize.height - spacecraftSymbolSize.height) / 2);

	final static Dimension eventSymbolSize = new Dimension(9, 9);
	final static Point eventSymbolPoint = new Point(
			(planetComponentSize.width - eventSymbolSize.width) / 2,
			(planetComponentSize.height - eventSymbolSize.height) / 2);

	final Vector<JMenuItem> externalMenuItems;
	private final JMenu mnBody;
	final JPopupMenu mnRight;
	final JMenuItem mnCenter;

	private final MouseInputAdapter mouseEventHandler;

	ObjectSymbol currentSymbol = null;

	OrbitsPlotter orbitsPlotter = null;

	public static abstract class ObjectSymbol extends Component {

		protected Point delta, symbolPoint;
		protected Dimension componentSize, symbolSize;

		public ObjectSymbol(Dimension componentSize, Dimension symbolSize,
				Point delta, Point symbolPoint) {
			this.delta = delta;
			this.componentSize = componentSize;
			this.symbolSize = symbolSize;
			this.symbolPoint = symbolPoint;
			setSize(componentSize);
		}

		public void setPosition(int x, int y) {
			setLocation(x + this.delta.x, y + this.delta.y);
		}

		public abstract void preparePopupMenu(Vector<JMenuItem> mnu);

		public abstract void setCenter();

		public abstract String getDescription();
	}

	public class PlanetSymbol extends ObjectSymbol {
		private final int body;
		private final Planet descriptor;
		private Color planetColor;

		public PlanetSymbol(int _body, Planet _descriptor) {
			super(planetComponentSize, planetSymbolSize, planetSymbolDelta,
					planetSymbolPoint);
			this.body = _body;
			this.descriptor = _descriptor;
			this.planetColor = colors.solarSystem[_descriptor.getIndex()];
		}

		protected PlanetSymbol(int _body, Dimension componentSize,
				Dimension symbolSize, Point delta, Point symbolPoint,
				Planet _descriptor) {
			super(componentSize, symbolSize, delta, symbolPoint);
			this.descriptor = _descriptor;
			this.body = _body;
		}

		@Override
		public void preparePopupMenu(Vector<JMenuItem> mnu) {
		}

		@Override
		public void setCenter() {
			setCenterToBody(this.body);
		}

		@Override
		public void paint(Graphics g) {
			g.setColor(this.planetColor);
			g.fillOval(this.symbolPoint.x, this.symbolPoint.y,
					this.symbolSize.width, this.symbolSize.height);
		}

		@Override
		public String getDescription() {
			return this.descriptor.toString();
		}
	}

	public class SpaceCraftSymbol extends ObjectSymbol {
		private final int body;
		private final Body descriptor;

		public SpaceCraftSymbol(int _body, Body _descriptor) {
			super(spacecraftComponentSize, spacecraftSymbolSize,
					spacecraftSymbolDelta, spacecraftSymbolPoint);
			this.descriptor = _descriptor;
			this.body = _body;

		}

		@Override
		public void paint(Graphics g) {
			g.setColor(colors.spacecraft);
			g.fillOval(this.symbolPoint.x, this.symbolPoint.y,
					this.symbolSize.width, this.symbolSize.height);
		}

		@Override
		public void setCenter() {
			setCenterToBody(this.body);
		}

		@Override
		public String getDescription() {
			return this.descriptor.toString();
		}

		@Override
		public void preparePopupMenu(Vector<JMenuItem> mnu) {
		}
	}

	public class EventSymbol extends ObjectSymbol {

		private static final long serialVersionUID = 4944629331439383961L;
		final EventPosition eventPosition;
		final int index;

		public EventSymbol(int i) {
			super(planetComponentSize, eventSymbolSize, planetSymbolDelta,
					eventSymbolPoint);
			setSize(this.componentSize);
			this.index = i;
			this.eventPosition = JKPlot.this.eventPositions[i];
		}

		@Override
		public void paint(Graphics g) {
			g.setColor(colors.flyby);
			g.drawOval(this.symbolPoint.x, this.symbolPoint.y,
					this.symbolSize.width, this.symbolSize.height);
		}

		@Override
		public void setCenter() {
			setCenterToEvent(this.index);
		}

		@Override
		public void preparePopupMenu(Vector<JMenuItem> mnu) {
			JKPlot.this.parent.prepareContextMenu(mnu,
					this.eventPosition.getEvent());
		}

		@Override
		public String getDescription() {
			return this.eventPosition.getEvent().getDescription();
		}
	}

	public JKPlot() {
		this(null);
	}

	public JKPlot(SimulatorEngine<NewtonSim> _parent) {
		super();
		this.parent = _parent;
		this.externalMenuItems = new Vector<JMenuItem>();
		this.mnRight = new javax.swing.JPopupMenu();
		this.mnBody = new javax.swing.JMenu();

		this.mouseEventHandler = new MouseInputAdapter() {

			@Override
			public void mousePressed(java.awt.event.MouseEvent evt) {
				JKPlot.this.lastDragPoint = null;
				JKPlot.this.currentSymbol = null;
				Component subc = getComponentAt(evt.getPoint());
				if (subc != null && subc instanceof ObjectSymbol) {
					JKPlot.this.currentSymbol = (ObjectSymbol) subc;
				}

				JKPlot.this.actionCenterTo
						.setEnabled(JKPlot.this.currentSymbol != null);
				JKPlot.this.mnCenter.setVisible(JKPlot.this.actionCenterTo
						.isEnabled());

				if (JKPlot.this.currentSymbol != null) {
					JKPlot.this.actionCenterTo.putValue(
							Action.NAME,
							"Center to: "
									+ JKPlot.this.currentSymbol
											.getDescription());
				} else {
					JKPlot.this.actionCenterTo.putValue(Action.NAME,
							"Center to");
				}

				if (!triggerPopup(evt)) {
					switch (evt.getButton()) {
					case MouseEvent.BUTTON1:
						JKPlot.this.lastDragPoint = evt.getPoint();
						break;
					default:
					}
				}
			}

			@Override
			public void mouseReleased(java.awt.event.MouseEvent evt) {
				if (JKPlot.this.lastDragPoint == null) {
					triggerPopup(evt);
				}
			}

			@Override
			public void mouseDragged(java.awt.event.MouseEvent evt) {
				if (JKPlot.this.lastDragPoint != null) {
					int dx, dy;
					if (JKPlot.this.lastDragPoint != null) {
						dx = evt.getX()
								- (int) JKPlot.this.lastDragPoint.getX();
						dy = evt.getY()
								- (int) JKPlot.this.lastDragPoint.getY();
						if (dx != 0 || dy != 0) {
							if (evt.isControlDown()) {
								JKPlot.this.ref.translate(dx, dy);
							} else {
								JKPlot.this.ref.rotate(0, -dy * 0.01);
								JKPlot.this.ref.rotate(1, dx * 0.01);
							}
							changeTransform();

							if (JKPlot.this.drawOrbits) {
								plotOrbits();
							}
							repaint();
						}
					}
					JKPlot.this.lastDragPoint = evt.getPoint();
				}
			}
		};

		addMouseMotionListener(this.mouseEventHandler);
		addMouseListener(this.mouseEventHandler);

		this.mnRight.add(this.actionDrawLine.getNewMenuItem());
		this.mnRight.add(this.actionDrawOrbit.getNewMenuItem());
		this.mnRight.add(this.actionDrawOsculatoryOrbit.getNewMenuItem());
		this.mnRight.add(this.actionContactPoints.getNewMenuItem());
		this.mnRight.add(this.mnCenter = this.actionCenterTo.getNewMenuItem());

		this.mnBody.setText("Body");
		this.mnRight.addPopupMenuListener(new PopupMenuListener() {

			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				int i;
				for (i = 0; i < JKPlot.this.externalMenuItems.size(); i++) {
					JKPlot.this.mnRight.remove(JKPlot.this.externalMenuItems
							.elementAt(i));
				}
				JKPlot.this.externalMenuItems.clear();
				JKPlot.this.parent.finishContextMenu();
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				JKPlot.this.parent.prepareContextMenu(
						JKPlot.this.externalMenuItems, null);
				if (JKPlot.this.currentSymbol != null) {
					JKPlot.this.currentSymbol
							.preparePopupMenu(JKPlot.this.externalMenuItems);
					if (JKPlot.this.externalMenuItems != null) {
						int i;
						for (i = 0; i < JKPlot.this.externalMenuItems.size(); i++) {
							JKPlot.this.mnRight
									.add(JKPlot.this.externalMenuItems
											.elementAt(i));
						}
					}
				}
				JKPlot.this.mnRight.validate();
			}
		});

		setLayout(null);
		setPreferredSize(new Dimension(400, 300));
		setForeground(new java.awt.Color(255, 255, 255));

		addMouseWheelListener(new java.awt.event.MouseWheelListener() {

			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
				int i = evt.getWheelRotation();

				if (evt.isControlDown()) {
					JKPlot.this.ref.aprox(i);
				} else if (evt.isShiftDown()) {
					JKPlot.this.ref.rotate(2, 0.03 * i);
				} else {
					JKPlot.this.ref.zoom(i);
				}
				changeTransform();

				if (JKPlot.this.drawOrbits) {
					plotOrbits();
				}
				repaint();
			}
		});

		addAncestorListener(new javax.swing.event.AncestorListener() {

			public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
			}

			public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
				if (JKPlot.this.parent != null) {
					JKPlot.this.parent.addView(JKPlot.this);
					JKPlot.this.parent.addPlotView(JKPlot.this.linePlotter);
					JKPlot.this.parent.restartPlotter();
				}
			}

			public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
				if (JKPlot.this.parent != null) {
					JKPlot.this.parent.removeView(JKPlot.this);
					JKPlot.this.parent.removePlotView(JKPlot.this.linePlotter);
				}
			}
		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				initializeGraphics();

				JKPlot.this.deltaImg.x = (getWidth() - JKPlot.this.imgOsc
						.getWidth()) / 2;
				JKPlot.this.deltaImg.y = (getHeight() - JKPlot.this.imgOsc
						.getHeight()) / 2;
				JKPlot.this.deltaPlot.x = JKPlot.this.centerImg.x
						+ JKPlot.this.deltaImg.x;
				JKPlot.this.deltaPlot.y = JKPlot.this.centerImg.y
						+ JKPlot.this.deltaImg.y;
				setPositions();

				JKPlot.this.parent.restartPlotter();

				if (JKPlot.this.integrador != null && JKPlot.this.drawOrbits) {
					plotOrbits();
				}

				repaint();
				super.componentResized(e);
			}
		});

		this.ref = new ProjectionReference();

		this.linePlotter = new BufferdPathPlotter(new Runnable() {

			public void run() {
				JKPlot.this.repaint();
			}
		}, new Dimension(100, 100), colors.sky, colors.path);

		initializeGraphics();

		JPanel controls = new Controls();
		add(controls);
		controls.setLocation(2, 2);

	}

	protected void initializeGraphics() {
		int wdt = getWidth();
		int hgt = getHeight();

		wdt = wdt / 100 * 100 + 100;
		hgt = hgt / 100 * 100 + 100;
		if (wdt <= 100) {
			wdt = 100;
		}
		if (hgt <= 100) {
			hgt = 100;
		}

		if (this.linePlotter.buf.getWidth() != wdt
				|| this.linePlotter.buf.getHeight() != hgt) {
			this.linePlotter.setGraphics(new Dimension(wdt, hgt), colors.sky,
					colors.path);
		}

		if (this.imgOsc == null || this.imgOsc.getWidth() != wdt
				|| this.imgOsc.getHeight() != hgt) {
			this.imgOsc = BufferdPathPlotter.newImage(colors.sky,
					colors.osculatoryOrbits, new Dimension(wdt, hgt));
			this.grafOsc = (Graphics2D) this.imgOsc.getGraphics();
			this.grafOsc.setBackground(new Color(this.imgOsc.getColorModel()
					.getColorSpace(), colors.sky.getComponents(this.imgOsc
					.getColorModel().getColorSpace(), null), 0));
			this.grafOsc.setColor(colors.osculatoryOrbits);
			this.centerImg.x = this.imgOsc.getWidth() / 2;
			this.centerImg.y = this.imgOsc.getHeight() / 2;

			this.orbitsPlotter = new OrbitsPlotter(this.grafOsc);

			resetPlotterParam();
		}

		setBackground(colors.sky);
	}

	public void reset() {
		int i;
		rebuildEventPoints();
		this.currentSymbol = null;
		this.actionCenterTo.setEnabled(false);
		if (this.planetSymbol != null) {
			for (i = 0; i < this.planetSymbol.length; i++) {
				remove(this.planetSymbol[i]);
			}
			this.planetSymbol = null;
		}
		if (this.integrador != null) {
			this.plotPoint = new Point[this.integrador.bodies];
			this.planetSymbol = new ObjectSymbol[this.integrador.bodies];
			for (i = 0; i < this.plotPoint.length; i++) {
				this.plotPoint[i] = new Point();
				if (this.integrador.systemRef.descriptions[i].getClass()
						.equals(Spacecraft.class)) {
					this.planetSymbol[i] = new SpaceCraftSymbol(i,
							this.integrador.systemRef.descriptions[i]);
				} else {
					this.planetSymbol[i] = new PlanetSymbol(i,
							(Planet) this.integrador.systemRef.descriptions[i]);
				}
				add(this.planetSymbol[i]);
			}
			this.eventPos = new Point[this.eventPositions.length];
			for (i = 0; i < this.eventPos.length; i++) {
				this.eventPos[i] = new Point();
			}
			initGraphics();
			updateView(this.integrador);
		} else {
			this.eventPos = new Point[0];
		}
		paintOrbits();
		repaint();
	}

	public void resetView(NewtonSim integrador) {
		int j;
		this.integrador = integrador;
		if (this.centerToEv >= this.eventPositions.length) {
			this.centerToEv = -1;
			this.centerToPlanet = 0;
			this.centerToBody = 0;
		}
		if (this.centerToPlanet >= 0 && integrador != null
				&& integrador.systemRef.planetPos[this.centerToPlanet] >= 0) {
			this.centerToBody = integrador.systemRef.planetPos[this.centerToPlanet];
		}

		Vector<EventPosition> vPhPos = new Vector<EventPosition>();

		for (Event ev : integrador.getEvents()) {
			EventPosition lPhenPos[] = ev.getPoints();
			if (lPhenPos != null) {
				for (j = 0; j < lPhenPos.length; j++) {
					vPhPos.add(lPhenPos[j]);
				}
			}
		}
		this.eventPositions = new EventPosition[vPhPos.size()];
		vPhPos.toArray(this.eventPositions);

		resetPlotterParam();

		reset();
	}

	void resetPlotterParam() {
		this.linePlotter.set(this.centerToBody,
				this.centerToEv >= 0 ? this.eventPositions[this.centerToEv]
						: null, this.ref);
	}

	void rebuildEventPoints() {
		int k;
		Vector<EventSymbol> x = new Vector<EventSymbol>();

		if (this.eventSymbol != null) {
			for (k = 0; k < this.eventSymbol.length; k++) {
				remove(this.eventSymbol[k]);
			}
		}

		if (this.eventPos != null && this.drawPoints) {
			for (k = 0; k < this.eventPositions.length; k++) {
				EventSymbol s = new EventSymbol(k);
				x.add(s);
				add(s);
				if (s.eventPosition.getFromBody() == null
						&& this.drawOrbits
						|| this.centerToBody >= 0
						&& s.eventPosition.getFromBody() == this.integrador.systemRef.descriptions[this.centerToBody]) {
					s.setVisible(true);
				} else {
					s.setVisible(false);
				}
			}
		}
		this.eventSymbol = x.toArray(new EventSymbol[x.size()]);
	}

	private void calcCenter() {
		int i;
		if (this.centerToEv >= 0) {
			this.eventPositions[this.centerToEv].evalPosition(this.centerPoint,
					this.integrador.position);
		} else {
			for (i = 0; i < 3; i++) {
				this.centerPoint[i] = this.integrador.position[3
						* this.centerToBody + i];
			}
		}
	}

	public void updateView(NewtonSim integrador) {
		int i;
		calcCenter();
		for (i = 0; i < integrador.bodies; i++) {
			this.ref.project(i, integrador.position, this.centerPoint, this.plotPoint[i]);
		}
		if (this.drawOrbits) {
			paintOrbits();
		}
		int k;
		double xx[] = new double[3];
		for (k = 0; k < this.eventPositions.length; k++) {
			this.eventPositions[k].evalPosition(xx, integrador.position);
			this.ref.project(0, xx, this.centerPoint, this.eventPos[k]);
		}
		setPositions();
		repaint();
	}

	void setPositions() {
		int k;
		if (this.planetSymbol != null) {
			for (k = 0; k < this.planetSymbol.length; k++) {
				Point p = this.plotPoint[k];
				this.planetSymbol[k].setPosition(p.x + this.deltaPlot.x, p.y
						+ this.deltaPlot.y);
			}
		}
		if (this.eventSymbol != null) {
			for (k = 0; k < this.eventSymbol.length; k++) {
				Point p = this.eventPos[this.eventSymbol[k].index];
				this.eventSymbol[k].setPosition(p.x + this.deltaPlot.x, p.y
						+ this.deltaPlot.y);
			}
		}
	}

	void paintOrbits() {
		clearOrbits();
		if (this.drawOrbits) {
			plotOrbits();
		}
	}

	private void clearOrbits() {
		this.grafOsc.clearRect(0, 0, this.imgOsc.getWidth(),
				this.imgOsc.getHeight());
	}

	protected void changeTransform() {
		initGraphics();
		updateView(this.integrador);
		resetPlotterParam();
		this.parent.restartPlotter();
	}

	private void initGraphics() {
		this.parent.stopPlotter();
		if (this.drawOrbits) {
			this.grafOsc.clearRect(0, 0, this.imgOsc.getWidth(),
					this.imgOsc.getHeight());
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (this.drawOrbits) {
			g.drawImage(this.imgOsc, this.deltaImg.x, this.deltaImg.y, null);
		}

		if (this.drawLine) {
			Image img = this.linePlotter.getImage();
			if (img != null) {
				g.drawImage(img, this.deltaImg.x, this.deltaImg.y, null);
			}
		}
	}

	void plotOrbits() {
		final Point imageCenter = this.centerImg;
		final boolean paintOsculatoryOrbit = this.drawOsculatoryOrbit;
		final Point[] bodyPoint = this.plotPoint;
		final Projection reference = this.ref;
		final OsculatoryElements[] keplerElements = this.integrador.keplerElements;
		final Rectangle coordinateRect = new Rectangle(-imageCenter.x,
				-imageCenter.y, 2 * imageCenter.x, 2 * imageCenter.y);
		final double[] centerCoordinates = this.centerPoint;

		this.orbitsPlotter.paintOrbits(coordinateRect, bodyPoint, reference,
				keplerElements, centerCoordinates, paintOsculatoryOrbit);
	}

	boolean triggerPopup(java.awt.event.MouseEvent evt) {
		boolean bTrigger;
		bTrigger = evt.isPopupTrigger();
		if (bTrigger) {
			this.mnRight.show(evt.getComponent(), evt.getX(), evt.getY());
		}
		return bTrigger;
	}

	void setCenterToBody(int centerToBody) {
		this.centerToEv = -1;
		this.centerToBody = centerToBody;
		if (centerToBody >= 0 && this.integrador != null
				&& this.integrador.systemRef.planetId[centerToBody] >= 0) {
			this.centerToPlanet = this.integrador.systemRef.planetId[centerToBody];
		} else {
			this.centerToPlanet = -1;
		}
		resetPlotterParam();
		reset();
	}

	void setCenterToEvent(int _ev) {
		this.centerToBody = -1;
		this.centerToPlanet = -1;
		this.centerToEv = _ev;
		resetPlotterParam();
		reset();
	}

	final AbstractToggleAction actionDrawLine = new AbstractToggleAction(
			"Spacecraft trajectory") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6093012529988738131L;

		public void actionPerformed(ActionEvent e) {
			JKPlot.this.drawLine = this.model.isSelected();
			repaint();
		}
	};

	final AbstractToggleAction actionDrawOrbit = new AbstractToggleAction(
			"Planet orbits") {
		private static final long serialVersionUID = 4505439470823307956L;

		public void actionPerformed(ActionEvent e) {
			JKPlot.this.drawOrbits = this.model.isSelected();
			rebuildEventPoints();
			paintOrbits();
			updateView(JKPlot.this.integrador);
		}
	};

	final AbstractToggleAction actionDrawOsculatoryOrbit = new AbstractToggleAction(
			"Osculatory spacecraft orbit") {

		public void actionPerformed(ActionEvent e) {
			JKPlot.this.drawOsculatoryOrbit = this.model.isSelected();
			rebuildEventPoints();
			paintOrbits();
			updateView(JKPlot.this.integrador);
		}
	};

	final AbstractToggleAction actionContactPoints = new AbstractToggleAction(
			"Contact points") {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5518719305275212874L;

		public void actionPerformed(java.awt.event.ActionEvent evt) {
			JKPlot.this.drawPoints = this.model.isSelected();
			rebuildEventPoints();
			updateView(JKPlot.this.integrador);
		}
	};

	final AbstractSimpleAction actionCenterTo = new AbstractSimpleAction(
			"Center to") {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6313955700606198441L;

		public void actionPerformed(java.awt.event.ActionEvent evt) {
			if (JKPlot.this.currentSymbol != null) {
				JKPlot.this.currentSymbol.setCenter();
			}
		}
	};

	public static JKPlot loadXml(Element element,
			SimulatorEngine<NewtonSim> _parent) {
		JKPlot p = new JKPlot(_parent);
		p.drawLine = "1".equals(element.getAttribute("path"));
		p.drawOrbits = "1".equals(element.getAttribute("orbits"));
		p.drawOsculatoryOrbit = "1".equals(element
				.getAttribute("osculatoryOrbit"));

		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jk.ConfigurableComponent#getReplacement()
	 */

	public ComponentConfiguration getReplacement() throws ObjectStreamException {
		EncodedReplacement val = new EncodedReplacement();
		val.drawLine = this.drawLine;
		val.drawOrbits = this.drawOrbits;
		val.drawOsculatoryOrbit = this.drawOsculatoryOrbit;
		val.drawPoints = this.drawPoints;
		val.setName(getName());
		val.setReference(this.ref);
		return val;
	}

	public static class EncodedReplacement implements Serializable,
			ComponentConfiguration {
		protected boolean drawLine = false;
		protected boolean drawOrbits = false;
		protected boolean drawPoints = false;
		protected boolean drawOsculatoryOrbit = false;
		protected String name = "";
		protected ProjectionReference reference = new ProjectionReference();

		public Component getComponent(JKApplication context)
				throws ObjectStreamException {
			JKPlot plot = new JKPlot();
			plot.drawLine = this.drawLine;
			plot.drawOrbits = this.drawOrbits;
			plot.drawOsculatoryOrbit = this.drawOsculatoryOrbit;
			plot.drawPoints = this.drawPoints;
			plot.setName(this.name);

			plot.actionDrawLine.model.setSelected(this.drawLine);
			plot.actionDrawOrbit.model.setSelected(this.drawOrbits);
			plot.actionDrawOsculatoryOrbit.model
					.setSelected(this.drawOsculatoryOrbit);
			plot.actionContactPoints.model.setSelected(this.drawPoints);
			plot.ref = this.reference;
			plot.ref.refresh();
			plot.resetPlotterParam();
			plot.setSimulatorEngine(context.getSimulator());
			return plot;
		}

		public boolean getDrawLine() {
			return this.drawLine;
		}

		public boolean isDrawOrbits() {
			return this.drawOrbits;
		}

		public boolean isDrawPoints() {
			return this.drawPoints;
		}

		public boolean isDrawOsculatoryOrbit() {
			return this.drawOsculatoryOrbit;
		}

		public void setDrawLine(boolean drawLine) {
			this.drawLine = drawLine;
		}

		public void setDrawOrbits(boolean drawOrbits) {
			this.drawOrbits = drawOrbits;
		}

		public void setDrawPoints(boolean drawPoints) {
			this.drawPoints = drawPoints;
		}

		public void setDrawOsculatoryOrbit(boolean drawOsculatoryOrbit) {
			this.drawOsculatoryOrbit = drawOsculatoryOrbit;
		}

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setReference(ProjectionReference reference) {
			this.reference = reference;
		}

		public Projection getReference() {
			return this.reference;
		}

	}

	public void setParent(JKViewList<NewtonSim> parent) {
	}

	public void setSimulatorEngine(SimulatorEngine<NewtonSim> parent) {
		this.parent = parent;
	}

	public void doZoom(int z) {
		JKPlot.this.ref.zoom(z);
		changeTransform();

		if (JKPlot.this.drawOrbits) {
			plotOrbits();
		}
		repaint();
	}

	public void doRotate(int axis, int qty) {
		JKPlot.this.ref.rotate(axis, qty * 0.01);
		changeTransform();

		if (JKPlot.this.drawOrbits) {
			plotOrbits();
		}
		repaint();

	}
}
