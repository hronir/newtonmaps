package newtonpath.ui.graph;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.DefaultButtonModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

public class MultiDimPoint extends DataPoint {
	final double y[];

	public MultiDimPoint(double _y[]) {
		super();
		this.y = _y;
	}

	public static class DataDimProjection extends DataProjection<MultiDimPoint> {
		private final int index;

		public DataDimProjection(int _index) {
			this.index = _index;
		}

		@Override
		public double value(MultiDimPoint p) {
			return p.y[this.index];
		}
	}

	public static class ActionSetData extends AbstractAction {
		public static ActionSetData[] dataActionArray(String[] _varNames,
				DataChooseGraph _logGraph, int axe) {
			int i;
			ActionSetData[] actionsArray;

			actionsArray = new ActionSetData[_varNames.length];

			for (i = 0; i < _varNames.length; i++) {
				actionsArray[i] = new ActionSetData(_varNames[i], _logGraph,
						axe, actionsArray, i);
			}
			return actionsArray;
		}

		private static final long serialVersionUID = -7241865370563703843L;
		final private DataChooseGraph logGraph;
		final private int axe;
		final private int index;
		final DefaultButtonModel model;
		final private ActionSetData dataAction[];

		public ActionSetData(String _name, DataChooseGraph _logGraph, int _axe,
				ActionSetData[] _dataAction, int _i) {
			super(_name);
			this.dataAction = _dataAction;
			this.axe = _axe;
			this.logGraph = _logGraph;
			this.index = _i;
			this.model = new DefaultButtonModel();
		}

		public void actionPerformed(ActionEvent e) {
			int i;
			for (i = 0; i < this.dataAction.length; i++) {
				this.dataAction[i].model
						.setSelected(this.dataAction[i] == this);
			}
			this.logGraph.setAxe(this.axe, this.index);
			if (this.logGraph != null) {
				// this.logGraph.autoScale();
				// this.logGraph.repaint();
			}
		}
	}

	public static class DataChooseGraph extends DataGraph {
		private static final long serialVersionUID = 1L;
		private final JPopupMenu mnRight;
		private final MultiDimPoint.DataDimProjection axes[];
		private final ActionSetData[] xDataActionArray;
		private final ActionSetData[] yDataActionArray;
		private final DataSet<MultiDimPoint> data;
		private final String[] componentNames;

		public DataChooseGraph(String[] _names, DataSet<MultiDimPoint> d) {
			this(_names, d, new MultiDimPoint.DataDimProjection(0),
					new MultiDimPoint.DataDimProjection(0));
		}

		public DataChooseGraph(String[] _names, DataSet<MultiDimPoint> d,
				MultiDimPoint.DataDimProjection _x,
				MultiDimPoint.DataDimProjection _y) {
			super(new DataView<MultiDimPoint>(d, _x, _y));
			this.data = d;
			this.axes = new MultiDimPoint.DataDimProjection[2];
			this.axes[0] = _x;
			this.axes[1] = _y;
			this.componentNames = _names;
			this.xDataActionArray = ActionSetData.dataActionArray(
					this.componentNames, this, 0);
			this.yDataActionArray = ActionSetData.dataActionArray(
					this.componentNames, this, 1);

			this.mnRight = new JPopupMenu();
			JMenu mnData[] = new JMenu[2];
			this.mnRight.add(mnData[0] = new JMenu("X"));
			this.mnRight.add(mnData[1] = new JMenu("Y"));
			int k;

			for (k = 0; k < this.xDataActionArray.length; k++) {
				JCheckBoxMenuItem m = new JCheckBoxMenuItem();
				m.setAction(this.xDataActionArray[k]);
				m.setModel(this.xDataActionArray[k].model);
				mnData[0].add(m);
				m = new JCheckBoxMenuItem();
				m.setAction(this.yDataActionArray[k]);
				m.setModel(this.yDataActionArray[k].model);
				mnData[1].add(m);
			}

			// addMouseListener(new MouseAdapter() {
			//
			// @Override
			// public void mousePressed(final MouseEvent evt) {
			// if (!triggerPopup(evt)) {
			// }
			// }
			//
			// @Override
			// public void mouseReleased(final MouseEvent evt) {
			// triggerPopup(evt);
			// }
			// });
		}

		void setAxe(int axe, int index) {
			this.axes[axe] = new MultiDimPoint.DataDimProjection(index);
			if (this.axes[0] != null && this.axes[1] != null) {
				setData(new DataView<MultiDimPoint>(this.data, this.axes[0],
						this.axes[1]));
			}
		}

		boolean triggerPopup(final MouseEvent evt) {
			boolean bTrigger;
			bTrigger = evt.isPopupTrigger();
			if (bTrigger) {
				this.mnRight.show(evt.getComponent(), evt.getX(), evt.getY());
			}
			return bTrigger;
		}
	}

}
