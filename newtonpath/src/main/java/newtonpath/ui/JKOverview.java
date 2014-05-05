package newtonpath.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import newtonpath.application.JKApplication;
import newtonpath.ui.widget.ResultListModel;

public class JKOverview extends JPanel implements ConfigurableComponent {
	private static final int HOVER_DISTANCE = 10;
	private static final int TILED_MAP_SCALE = 2;

	public static class ColorSchema extends JKSnapShot.ColorSchema {
		public Color selected = new Color(0xffCC0066);

		public ColorSchema() {
			this.orbits = new Color(0xffffffff);
			this.path = new Color(0xff99CC00);
			this.sky = new Color(0xffCCCCFF);
		}
	}

	public static ColorSchema colors = new ColorSchema();
	private ColorSchema instanceColors = colors;

	private final ResultListModel modelHistory;
	private final ListSelectionModel selectionModelHistory;
	private JKSnapShot backgroundSnapShot = null;
	private JKSnapShot pathSnapShot = null;
	private BufferedImage backgroundImage;
	private BufferedImage[] orbitsImages;

	private Point imageShift = new Point();

	private byte[] orbitMap;
	private int orbitMapWidth;
	private int orbitMapHeight;

	private int hoverOrbit;

	private ListDataListener dataListener = new ListDataListener() {

		public void intervalRemoved(ListDataEvent e) {
			redrawOrbits();
		}

		public void intervalAdded(ListDataEvent e) {
			redrawOrbits();
		}

		public void contentsChanged(ListDataEvent e) {
			redrawOrbits();
		}

	};

	private ListSelectionListener selectionListener = new ListSelectionListener() {

		public void valueChanged(ListSelectionEvent e) {
			repaint();
		}

	};

	private MouseAdapter mouseAdapter = new MouseAdapter() {
		@Override
		public void mouseMoved(MouseEvent e) {
			setHoverOrbit(getOrbitAt(e.getX(), e.getY()));
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			setHoverOrbit(getOrbitAt(e.getX(), e.getY()));
			selectHoverOrbit();
		}
	};

	public JKOverview(ResultListModel modelHistory,
			ListSelectionModel selectionModelHistory) {
		super();
		this.modelHistory = modelHistory;
		this.selectionModelHistory = selectionModelHistory;

		addMouseMotionListener(this.mouseAdapter);
		addMouseListener(this.mouseAdapter);

		register();

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				redrawOrbits();
			}

			@Override
			public void componentShown(ComponentEvent e) {
				register();
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				unregister();
			}
		});
	}

	protected int getOrbitAt(int x, int y) {
		int xx = (x - this.imageShift.x) / JKOverview.TILED_MAP_SCALE;
		int yy = (y - this.imageShift.y) / JKOverview.TILED_MAP_SCALE;
		if (xx >= 0 && xx < this.orbitMapWidth && yy >= 0
				&& yy < this.orbitMapHeight) {
			return this.orbitMap[xx + this.orbitMapWidth * yy] - 1;
		}
		return -1;
	}

	public void register() {
		this.modelHistory.addListDataListener(this.dataListener);
		this.selectionModelHistory
				.addListSelectionListener(this.selectionListener);

	}

	public void unregister() {
		this.modelHistory.removeListDataListener(this.dataListener);
		this.selectionModelHistory
				.removeListSelectionListener(this.selectionListener);
	}

	protected void redrawOrbits() {
		if (createSnapshot()) {
			Graphics2D backGroundGraphics = this.backgroundImage
					.createGraphics();

			List<NewtonSim> l = new ArrayList<NewtonSim>();
			NewtonSim sim = null;
			for (int i = 0; i < this.orbitsImages.length; i++) {
				sim = NewtonSim.getCopy((AproxObs) this.modelHistory
						.getElementAt(i).getResultObject());
				l.add(sim);
			}

			if (l.size() > 0) {
				this.backgroundSnapShot.paintImage(l);

				for (int i = 0; i < l.size(); i++) {
					NewtonSim s = l.get(i);
					Image img = this.pathSnapShot.paintImage(Collections
							.singletonList(s));
					this.pathSnapShot.copyImageData(this.orbitsImages[i]
							.getRaster());

					backGroundGraphics.drawImage(img, 0, 0, null);
				}
			}

			fillTiles();

			resetHoverOrbit();

			repaint();
		}
		calculateShift();
	}

	protected boolean createSnapshot() {
		boolean modif = false;
		if (this.backgroundSnapShot == null
				|| this.backgroundSnapShot.getWidth() < getWidth()
				|| this.backgroundSnapShot.getHeight() < getHeight()) {

			this.backgroundImage = new BufferedImage(getWidth() + 5,
					getHeight() + 5, BufferedImage.TYPE_4BYTE_ABGR);

			this.backgroundSnapShot = new JKSnapShot(this.backgroundImage);
			this.backgroundSnapShot.setPaintSpacecraftOsculatoryOrbit(false);
			this.backgroundSnapShot.setPaintPlanetOsculatoryOrbit(true);
			this.backgroundSnapShot.setPaintSpacecraftPath(false);
			this.backgroundSnapShot.instanceColors = this.instanceColors;

			calculateShift();

			modif = true;
			this.orbitsImages = null;
		}
		if (this.orbitsImages == null
				|| this.orbitsImages.length != this.modelHistory.getSize()) {

			byte[] mapR = new byte[] {
					(byte) this.instanceColors.path.getRed(), -127 };
			byte[] mapG = new byte[] {
					(byte) this.instanceColors.path.getGreen(), -127 };
			byte[] mapB = new byte[] {
					(byte) this.instanceColors.path.getBlue(), -127 };
			IndexColorModel cm = new IndexColorModel(8, mapR.length, mapR,
					mapG, mapB, 1);

			BufferedImage img = new BufferedImage(
					this.backgroundImage.getWidth(),
					this.backgroundImage.getHeight(),
					BufferedImage.TYPE_BYTE_INDEXED, cm);

			this.pathSnapShot = new JKSnapShot(img);
			this.pathSnapShot.instanceColors = this.instanceColors;
			this.pathSnapShot.setPaintSpacecraftOsculatoryOrbit(false);
			this.pathSnapShot.setPaintPlanetOsculatoryOrbit(false);
			this.pathSnapShot.setPaintBackground(false);

			this.orbitsImages = new BufferedImage[this.modelHistory.getSize()];

			Color col = this.instanceColors.selected;
			byte[] mapRed = new byte[] { (byte) col.getRed(), -127 };
			byte[] mapGreen = new byte[] { (byte) col.getGreen(), -127 };
			byte[] mapBlue = new byte[] { (byte) col.getBlue(), -127 };
			cm = new IndexColorModel(8, 2, mapRed, mapGreen, mapBlue, 1);

			for (int i = 0; i < this.orbitsImages.length; i++) {

				this.orbitsImages[i] = new BufferedImage(
						this.backgroundImage.getWidth(),
						this.backgroundImage.getHeight(),
						BufferedImage.TYPE_BYTE_INDEXED, cm);

			}

			this.orbitMapWidth = this.backgroundImage.getWidth()
					/ JKOverview.TILED_MAP_SCALE;
			this.orbitMapHeight = this.backgroundImage.getHeight()
					/ JKOverview.TILED_MAP_SCALE;

			this.orbitMap = new byte[this.orbitMapWidth * this.orbitMapHeight];

			modif = true;
		}
		return modif;
	}

	private void fillTiles() {
		for (int i = HOVER_DISTANCE; i > 0; i--) {
			for (int x = 0; x < this.backgroundImage.getWidth(); x++) {
				for (int y = 0; y < this.backgroundImage.getHeight(); y++) {
					int c = this.orbitsImages.length - 1;
					while (c >= 0
							&& this.orbitsImages[c].getRaster().getSample(x, y,
									0) != 0) {
						c--;
					}
					if (c >= 0) {
						int xx = x / JKOverview.TILED_MAP_SCALE;
						int yy = y / JKOverview.TILED_MAP_SCALE;
						for (int j = -i + 1; j < i; j++) {
							colorize(xx + i - 1, yy + j, c + 1);
							colorize(xx - i + 1, yy + j, c + 1);
							colorize(xx + j, yy + i - 1, c + 1);
							colorize(xx + j, yy - i + 1, c + 1);
						}
					}
				}
			}
		}
	}

	public void calculateShift() {
		this.imageShift.x = (getWidth() - this.backgroundImage.getWidth(null)) / 2;
		this.imageShift.y = (getHeight() - this.backgroundImage.getHeight(null)) / 2;
	}

	private void colorize(int x, int y, int c) {
		if ((x >= 0) && (x < this.orbitMapWidth) && (y >= 0)
				&& (y < this.orbitMapHeight)) {
			this.orbitMap[x + (y * this.orbitMapWidth)] = (byte) c;
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (this.backgroundImage != null) {

			g.drawImage(this.backgroundImage, this.imageShift.x,
					this.imageShift.y, null);

			int sel = this.selectionModelHistory.getMinSelectionIndex();

			paintThickerOrbit(sel, g, 0);

			if (this.hoverOrbit >= 0) {
				paintThickerOrbit(this.hoverOrbit, g, 1);
			}
		}
	}

	private void paintThickerOrbit(int sel, Graphics g, final int w) {
		if (sel >= 0 && sel < this.orbitsImages.length) {
			for (int i = -w; i <= w; i++) {
				for (int j = -w; j <= w; j++) {
					if ((i * i) + (j * j) <= (w * w)) {
						g.drawImage(this.orbitsImages[sel], this.imageShift.x
								+ i, this.imageShift.y + j, null);
					}
				}
			}
		}
	}

	public void resetHoverOrbit() {
		this.hoverOrbit = -1;
	}

	public void setHoverOrbit(int i) {
		if (this.hoverOrbit != i) {
			this.hoverOrbit = i;
			repaint();
		}
	}

	public void selectHoverOrbit() {
		if (JKOverview.this.hoverOrbit >= 0) {
			JKOverview.this.selectionModelHistory.setSelectionInterval(
					JKOverview.this.hoverOrbit, JKOverview.this.hoverOrbit);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jk.ConfigurableComponent#getReplacement()
	 */

	public ComponentConfiguration getReplacement() throws ObjectStreamException {
		EncodedReplacement val = new EncodedReplacement();

		return val;
	}

	public static class EncodedReplacement implements Serializable,
			ComponentConfiguration {
		public Component getComponent(JKApplication context)
				throws ObjectStreamException {
			return new JKOverview(context.getActionPerformer()
					.getModelHistory(), context.getActionPerformer()
					.getSelectionModelHistory());
		}
	}

}
