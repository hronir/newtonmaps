package newtonpath.ui;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import newtonpath.application.JKApplication;
import newtonpath.sim.Simulator;
import newtonpath.sim.SimulatorEngine;
import newtonpath.statemanager.Observable;
import newtonpath.statemanager.OperationResult;
import newtonpath.ui.graph.DataGraph;
import newtonpath.ui.graph.DataProjection;
import newtonpath.ui.graph.DataSet;
import newtonpath.ui.graph.DataView;
import newtonpath.ui.graph.MultiGraphPoint;


public class JKGraphMember<E extends Simulator> extends JPanel implements
		ConfigurableComponent {
	private static final Dimension SCALE_LEGEND_SIZE = new Dimension(100, 18);
	private final ArrayList<Observable> members;
	private final DataSet<MultiGraphPoint> data;
	private double arg[];
	protected final DataGraph graph;
	protected final DataGraph.DataGraphComponent graphComponent;
	private final Plotter plotter;
	private final JLabel scaleLabelX;
	private final JLabel scaleLabelY;

	public static class JKGraphMemberView extends DataView<MultiGraphPoint> {
		public JKGraphMemberView(DataSet<MultiGraphPoint> _data,
				DataProjection<MultiGraphPoint> _x,
				DataProjection<MultiGraphPoint> _y) {
			super(_data, _x, _y);
		}
	}

	public JKGraphMember(Observable[] memberList) {
		super(createLayout());
		this.data = new DataSet<MultiGraphPoint>(1000);
		this.members = new ArrayList<Observable>();
		this.graph = new DataGraph();
		this.plotter = new Plotter();
		this.scaleLabelX = createScaleLabel();
		this.scaleLabelY = createScaleLabel();
		this.scaleLabelX.setHorizontalTextPosition(SwingConstants.RIGHT);

		this.graphComponent = new DataGraph.DataGraphComponent(this.graph);

		addMembers(memberList);

		add(this.graphComponent, BorderLayout.CENTER);
		add(this.scaleLabelY, BorderLayout.NORTH);
		add(this.scaleLabelX, BorderLayout.SOUTH);
		add(Box.createHorizontalStrut(10), BorderLayout.WEST);
		add(Box.createHorizontalStrut(10), BorderLayout.EAST);
	}

	private static JLabel createScaleLabel() {
		JLabel val = new JLabel();
		val.setMinimumSize(SCALE_LEGEND_SIZE);
		val.setPreferredSize(SCALE_LEGEND_SIZE);
		return val;
	}

	private static BorderLayout createLayout() {
		final BorderLayout layout = new BorderLayout();

		return layout;
	}

	public void register(SimulatorEngine<E> eng) {
		eng.addPlotView(this.plotter);
		eng.addPlotView(this.graphComponent);
	}

	private void addMembers(Observable[] memberList) {
		int i;
		for (i = 0; i < memberList.length; i++) {
			this.members.add(memberList[i]);
			this.graph.addData(new JKGraphMemberView(this.data,
					new MultiGraphPoint.DataMultiXProjection(),
					new MultiGraphPoint.DataMultiYProjection(i)));
		}
		this.arg = new double[this.members.size()];
	}

	public void addPoint(E integ) {
		int i;
		for (i = 0; i < this.members.size(); i++) {
			this.arg[i] = this.members.get(i).getDoubleValue(integ);
		}
		this.data.addPoint(new MultiGraphPoint(integ.getTime(), this.arg));
	}

	public void refreshGraph() {
		this.graph.autoScale(getBounds());
		// this.graphComponent.repaint();
		refreshScaleLegend();

	}

	private void refreshScaleLegend() {
		this.scaleLabelX.setText("["
				+ Double.toString(this.graph.graphRange.x.min) + ","
				+ Double.toString(this.graph.graphRange.x.max) + "]");
		this.scaleLabelY.setText("["
				+ Double.toString(this.graph.graphRange.y.min) + ","
				+ Double.toString(this.graph.graphRange.y.max) + "]");
	}

	public void resetGraph(E integrador) {
		this.data.clear();
		// this.graphComponent.repaint();
		refreshScaleLegend();
	}

	protected class View implements JKView<E> {
		public void resetView(E integrador) {
			resetGraph(integrador);
		}

		public void updateView(E integ) {
			addPoint(integ);
			refreshGraph();
		}

		public void setParent(JKViewList<E> parent) {
		}
	}

	@SuppressWarnings("unchecked")
	protected class Plotter implements PlotterView<NewtonSim> {
		public void beforeUpdate(NewtonSim _int) {
			synchronized (JKGraphMember.this.graph) {
				resetGraph((E) _int);
			}
		}

		public void updatePoint(NewtonSim _int) {
			synchronized (JKGraphMember.this.graph) {
				addPoint((E) _int);
			}
		}

		public void afterUpdate(NewtonSim _int, boolean _valid) {
			synchronized (JKGraphMember.this.graph) {
				if (!_valid) {
					resetGraph((E) _int);
				}
				refreshGraph();
			}
		}
	}

	public ComponentConfiguration getReplacement() throws ObjectStreamException {
		return new EncodedReplacement(this.members);
	}

	public static class EncodedReplacement implements ComponentConfiguration {
		private List<String> members;

		public EncodedReplacement() {
			this.members = null;
		}

		public List<String> getMembers() {
			return this.members;
		}

		public void setMembers(List<String> members) {
			this.members = members;
		}

		public EncodedReplacement(List<Observable> members) {
			super();
			this.members = new ArrayList<String>();
			for (Observable o : members) {
				this.members.add(o.getDescription());
			}
		}

		public Component getComponent(JKApplication context)
				throws ObjectStreamException {

			List<Observable> avaliableObservables = OperationResult
					.addObservables(null, context.getSimulator().getInteg()
							.getObservables(), context.getSimulator()
							.getInteg());

			Map<String, Observable> obsByName = new HashMap<String, Observable>();

			for (Observable o : avaliableObservables) {
				obsByName.put(o.getDescription(), o);
			}

			Observable[] val = new Observable[this.members.size()];

			for (int i = 0; i < val.length; i++) {
				val[i] = obsByName.get(this.members.get(i));
			}
			JKGraphMember<NewtonSim> newGraph = new JKGraphMember<NewtonSim>(
					val);

			newGraph.register(context.getSimulator());

			return newGraph;
		}
	}
}
