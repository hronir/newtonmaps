/*
 * JKPanel.java
 *
 * Created on 10 de octubre de 2007, 19:54
 */

package newtonpath.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.AncestorListener;

import newtonpath.application.JKApplication;
import newtonpath.kepler.Funcions;
import newtonpath.member.MemberItemRoot;
import newtonpath.statemanager.Observable;
import newtonpath.statemanager.OperationResult;
import newtonpath.statemanager.StateManager;
import newtonpath.ui.splitter.JKMultiSplitGroup;
import newtonpath.ui.widget.AbstractSimpleAction;
import newtonpath.ui.widget.AbstractToggleAction;
import newtonpath.ui.widget.MemberExpl;
import newtonpath.ui.widget.MemberTableModel;

/**
 * 
 * @author oriol
 */
public class JKTable<E extends StateManager> extends JPanel implements
		JKView<E>, ConfigurableComponent {

	private static final long serialVersionUID = 7164985673878728362L;
	private E integrador;
	protected MemberTableModel propTable;
	protected JKViewList<E> parent;
	final protected JKMultiSplitGroup splits;

	private javax.swing.JSplitPane splHor;
	private javax.swing.JScrollPane scTable;
	protected JToolBar tbNorth;
	private final MemberExpl membTree;
	private JPanel rightPane;
	private JTable tblProp;

	public JKTable(E _integrador, JKViewList<E> _parent,
			JKMultiSplitGroup _splits) {
		super();
		this.integrador = _integrador;
		this.parent = _parent;
		this.splits = _splits;
		this.membTree = new MemberExpl(this.integrador,
				this.integrador.getObservables());
		initComponent();
		configListeners();
		this.propTable = new MemberTableModel(this.integrador, null);
		this.tblProp.setModel(this.propTable);
		showHideTree(true);
	}

	
	public void updateView(E integrador) {
		refreshPropTable();
	}

	
	public void resetView(E _integrador) {
		this.integrador = _integrador;
		this.membTree.reset(this.integrador);
		this.propTable.reset(this.integrador);
		refreshPropTable();
	}

	private boolean showTree = true;

	protected void showHideTree(boolean _visible) {
		this.showTree = _visible;
		showHideTree();
	}

	protected void showHideTree() {
		if (this.showTree) {
			remove(this.rightPane);
			this.splHor.setRightComponent(this.rightPane);
			add(this.splHor, java.awt.BorderLayout.CENTER);
		} else {
			remove(this.splHor);
			this.splHor.setRightComponent(null);
			add(this.rightPane, java.awt.BorderLayout.CENTER);
		}
		validate();
	}

	private void refreshPropTable() {
		this.tblProp.repaint();
		this.membTree.refreshPropTable();
	}

	void addPropSel() {
		Observable p[] = this.membTree.getSelection();
		int i;
		if (p != null) {
			for (i = 0; i < p.length; i++) {
				this.propTable.addRow(p[i]);
			}
		}
	}

	private void initComponent() {

		this.splHor = new javax.swing.JSplitPane();
		this.rightPane = new javax.swing.JPanel();
		this.scTable = new javax.swing.JScrollPane();
		this.tblProp = new javax.swing.JTable();
		this.tbNorth = new javax.swing.JToolBar();

		setLayout(new java.awt.BorderLayout());

		setPreferredSize(new java.awt.Dimension(200, 100));
		this.scTable.setViewportView(this.tblProp);

		this.rightPane.setLayout(new java.awt.BorderLayout());
		this.rightPane.add(this.scTable, java.awt.BorderLayout.CENTER);

		this.splHor.setOneTouchExpandable(true);
		this.splHor.setRightComponent(this.rightPane);
		this.splHor.setLeftComponent(this.membTree);

		add(this.splHor, java.awt.BorderLayout.CENTER);

		this.tbNorth.setFloatable(false);

		this.tbNorth.add(this.actionTree.getNewButton());
		this.tbNorth.add(this.actionAdd.getNewButton());

		add(this.tbNorth, java.awt.BorderLayout.NORTH);
	}

	private void configListeners() {
		addAncestorListener(new AncestorListener() {
			
			public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
			}

			
			public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
				if (JKTable.this.parent != null) {
					JKTable.this.parent.addView(JKTable.this);
					resetView(JKTable.this.parent.getInteg());
				}
				showHideTree();
			}

			
			public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
				if (JKTable.this.parent != null) {
					JKTable.this.parent.removeView(JKTable.this);
				}
			}
		});
	}

	private final AbstractToggleAction actionTree = new AbstractToggleAction(
			"Tree", true) {
		private static final long serialVersionUID = 4677040923362411443L;

		
		public void actionPerformed(ActionEvent e) {
			showHideTree(this.model.isSelected());
		}
	};
	private final AbstractSimpleAction actionAdd = new AbstractSimpleAction(
			"Add") {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7727869744775057776L;

		
		public void actionPerformed(ActionEvent e) {
			addPropSel();
		}
	};

	
	public void setParent(newtonpath.ui.JKViewList<E> parent) {
		this.parent = parent;
	};

	
	public ComponentConfiguration getReplacement() throws ObjectStreamException {
		List<Observable> members = new ArrayList<Observable>();
		for (int j = 0; j < this.propTable.getRowCount(); j++) {
			members.add(this.propTable.elementAt(j));
		}
		return new EncodedReplacement(members);
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
					.addObservables(null, new MemberItemRoot(AproxObs.class,
							Funcions.inspectionAnnotations), context
							.getViewList().getInteg());

			JKTable<AproxObs> newTable = new JKTable<AproxObs>(context
					.getActionPerformer().getIntegrator(),
					context.getViewList(), context.getSplitGroup());

			Map<String, Observable> obsByName = new HashMap<String, Observable>();
			for (Observable o : avaliableObservables) {
				obsByName.put(o.getDescription(), o);
			}
			for (String m : this.members) {
				newTable.propTable.addRow(obsByName.get(m));
			}

			newTable.showHideTree(false);

			return newTable;
		}
	}
}
