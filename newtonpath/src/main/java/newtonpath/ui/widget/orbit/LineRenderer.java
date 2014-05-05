/**
 * 
 */
package newtonpath.ui.widget.orbit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Shape;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import newtonpath.images.Images;
import newtonpath.kepler.Funcions;
import newtonpath.kepler.events.Event;
import newtonpath.kepler.events.EventPosition;
import newtonpath.statemanager.OperationResult;
import newtonpath.ui.AproxObs;
import newtonpath.ui.JKSnapShot;
import newtonpath.ui.NewtonSim;
import newtonpath.ui.widget.AbstractColorSchema;

@SuppressWarnings("serial")
public class LineRenderer extends JPanel implements ListCellRenderer {
	public static class ColorScheme extends AbstractColorSchema {
		public Color background = new Color(0xFFFFFFFF);
		public Color date = new Color(0xFF000000);
		public Color border = new Color(0xFFcccccc);
		public Color borderSelected = new Color(0xFF666666);
		public Color borderFocused = new Color(0xFF000000);
		public Color timeBackground = new Color(0xFFdddddd);
		public Color timeForeground = new Color(0xFF333333);
		public Color description = new Color(0xFF000000);
	}

	public static ColorScheme colors = new ColorScheme();

	private final int borderWidth = 2;
	private final int baseline;
	private final int height;
	private final Map<OperationResult, Image> imageCache;
	private Image currentItemImage;
	private final DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

	private double timeStart = Double.NaN, timeEnd = Double.NaN;

	private final int c1pos = 65;

	private OperationResult data;
	private boolean selected;
	private boolean focused;
	private final int dateWidth;

	private final JLabel[] planetFlag;

	private final JKSnapShot snapShot;

	final Font descriptionFont;

	public LineRenderer(Font f, int _height) {
		super();
		setBackground(colors.background);
		this.setSize(200, _height);
		FontMetrics _metrics = getFontMetrics(f);
		setFont(f);
		this.descriptionFont = f.deriveFont(Font.BOLD);
		this.baseline = _metrics.getAscent() + this.borderWidth;
		this.height = _metrics.getHeight() + (2 * this.borderWidth);
		this.dateWidth = _metrics.stringWidth("00/00/0000");
		this.planetFlag = new JLabel[11];
		setLayout(null);

		ImageIcon[] icons = new ImageIcon[] { null, null, Images.iconMER,
				Images.iconVEN, Images.iconEAR, Images.iconMAR, Images.iconJUP,
				Images.iconSAT, Images.iconURA, Images.iconNEP, Images.iconPLU };
		for (int i = 0; i < this.planetFlag.length; i++) {
			this.planetFlag[i] = new JLabel(icons[i], SwingConstants.CENTER);
			this.planetFlag[i].setBounds(2, (i - 2) * 6, 7, 7);

			add(this.planetFlag[i]);
		}
		this.snapShot = new JKSnapShot(50, 50);
		this.snapShot.getRef().zoom(0.05);
		this.imageCache = new IdentityHashMap<OperationResult, Image>();
	}

	@Override
	protected void paintComponent(Graphics _g) {
		AproxObs aprox = (AproxObs) this.data.getResultObject();
		double missionStart = aprox.startDateTime();
		double missionEnd = aprox.endDateTime();

		Graphics g = _g.create();
		Shape oldClip = g.getClip();

		for (int i = 0; i < this.planetFlag.length; i++) {
			this.planetFlag[i].setVisible(false);
		}

		for (Event ev : aprox.getEvents()) {
			for (EventPosition evPos : ev.getPoints()) {
				int i = aprox.systemRef.getPlanetIndex(evPos.getBody());
				if (i > 0) {
					this.planetFlag[i].setVisible(true);
				}
			}
		}
		// Fons
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());

		// Miniatura
		if (this.currentItemImage != null) {
			g.drawImage(this.currentItemImage, 10, 0, null);
		}

		// Gantt
		int timeWidth = getWidth() - this.c1pos - 3;

		int missionLeft = 0;
		int missionWidth = timeWidth;

		int timeTop = this.height;
		int timeHeight = getHeight() - timeTop;

		if (!Double.isNaN(this.timeStart) && !Double.isNaN(this.timeEnd)
				&& timeWidth > 5) {
			g.clipRect(this.c1pos, 0, timeWidth, getHeight());

			double left = missionStart;
			double right = missionEnd;
			left = (left - this.timeStart) / (this.timeEnd - this.timeStart);
			right = (right - this.timeStart) / (this.timeEnd - this.timeStart);
			if (left < 0D) {
				left = 0D;
			} else if (left > 1D) {
				left = 1D;
			}
			if (right < 0D) {
				right = 0D;
			} else if (right > 1D) {
				right = 1D;
			}
			if (right < left) {
				right = left;
			}
			missionLeft = (int) (timeWidth * left);
			missionWidth = (int) (timeWidth * (right - left));
			if (missionWidth < 3) {
				missionWidth = 3;
			}
			int barTop = timeTop + this.height;
			int barHeight = timeHeight - 2 * this.height;
			g.setColor(colors.timeBackground);
			g.fillRect(this.c1pos, barTop, timeWidth, barHeight);
			g.setColor(colors.timeForeground);
			g.fillRect(this.c1pos + missionLeft, barTop, missionWidth,
					barHeight);

			g.fillRect(this.c1pos + missionLeft, barTop - 2, 3, barHeight + 2);

			g.fillRect(this.c1pos + missionLeft + missionWidth - 3, barTop, 3,
					barHeight + 2);

			g.setColor(colors.date);

			int l = missionLeft - this.dateWidth / 2;
			if (l + this.dateWidth > timeWidth) {
				l = timeWidth - this.dateWidth;
			}
			if (l < 0) {
				l = 0;
			}
			g.drawString(
					this.dateFormatter.format(Funcions.calDate(missionStart)),
					this.c1pos + l, timeTop + this.baseline);

			l = missionLeft + missionWidth - this.dateWidth / 2;
			if (l + this.dateWidth > timeWidth) {
				l = timeWidth - this.dateWidth;
			}
			if (l < 0) {
				l = 0;
			}
			int h = timeHeight - this.height;
			g.drawString(
					this.dateFormatter.format(Funcions.calDate(missionEnd)),
					this.c1pos + l, timeTop + h + this.baseline);

		}

		g.setClip(oldClip);

		g.setColor(colors.description);
		g.setFont(this.descriptionFont);
		g.drawString(this.data.getDescription(), this.c1pos, this.baseline);

		// Vora
		if (this.selected) {
			g.setColor(this.focused ? colors.borderFocused
					: colors.borderSelected);
			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			if (this.focused) {
				g.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
			}
		} else {
			g.setColor(colors.border);
			g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
		}

		g.dispose();
		g = null;
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		this.selected = isSelected;
		this.focused = cellHasFocus;
		this.data = (OperationResult) value;

		if (!this.imageCache.containsKey(this.data)) {
			AproxObs aprox = (AproxObs) this.data.getResultObject();
			NewtonSim xx = NewtonSim.getCopy(aprox);
			this.currentItemImage = this.snapShot.getImageCopy(xx);
			this.imageCache.put(this.data, this.currentItemImage);
		} else {
			this.currentItemImage = this.imageCache.get(this.data);
		}
		return this;
	}

	public synchronized void setTimeBounds(double min, double max) {
		this.timeStart = min;
		this.timeEnd = max;
	}

	public void adjustOrbitList(final List<OperationResult> list) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (OperationResult r : list) {
			AproxObs resultObject = (AproxObs) r.getResultObject();
			double a = resultObject.startDateTime();
			double b = resultObject.endDateTime();
			if (a < min) {
				min = a;
			}
			if (b > max) {
				max = b;
			}
		}
		if (min < Double.MAX_VALUE) {
			setTimeBounds(min, max);
		}
	}

}