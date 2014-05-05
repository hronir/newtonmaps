package newtonpath.ui;

import java.awt.event.ActionEvent;

import newtonpath.sim.Simulator;
import newtonpath.sim.SimulatorEngine;
import newtonpath.statemanager.Observable;
import newtonpath.statemanager.StateManager;
import newtonpath.ui.splitter.JKMultiSplitGroup;
import newtonpath.ui.widget.AbstractSimpleAction;

public class JKTableSim<E extends Simulator & StateManager> extends JKTable<E> {

	public JKTableSim(E integrador, SimulatorEngine<E> parent,
			JKMultiSplitGroup splits) {
		super(integrador, parent, splits);
		this.tbNorth.add(this.actionGraph.getNewButton());

	}

	protected SimulatorEngine<E> getSimulatorEngine() {
		return (SimulatorEngine<E>) this.parent;
	}

	private final AbstractSimpleAction actionGraph = new AbstractSimpleAction(
			"Graph") {
		private static final long serialVersionUID = 2971554760389888363L;

		
		public void actionPerformed(ActionEvent e) {
			if (JKTableSim.this.splits != null) {
				int i;
				Observable memberList[];
				memberList = new Observable[JKTableSim.this.propTable
						.getRowCount()];
				for (i = 0; i < memberList.length; i++) {
					memberList[i] = JKTableSim.this.propTable.elementAt(i);
				}

				JKGraphMember<E> newGraph = new JKGraphMember<E>(memberList);
				newGraph.register(getSimulatorEngine());
				JKTableSim.this.splits.startSelectAndSplit(newGraph);
			}
		}
	};

}
