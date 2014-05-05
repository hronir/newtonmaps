package newtonpath.ui.widget;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import newtonpath.statemanager.Observable;
import newtonpath.statemanager.ObservableArray;
import newtonpath.statemanager.Parameter;


public class MemberExpl extends JSplitPane {
	protected JTree tree;
	protected JTable table;
	protected MemberTreeModel treModel;
	protected final MemberTableModel tblModel;
	private final Map<Observable, Parameter> parameters;
	private Object integrator;

	public MemberExpl(Object integ, ObservableArray _rootArray) {
		initComponentTree();
		configListeners();
		this.integrator = integ;
		this.parameters = new HashMap<Observable, Parameter>();
		this.tree.setModel(this.treModel = new MemberTreeModel(_rootArray));
		this.treModel.reset(this.integrator);
		this.table.setModel(this.tblModel = new MemberTableModel(integ, this,
				null));
		this.tree.setSelectionRow(0);
		showHideTree();
	}

	public void resetObservableModel(ObservableArray _rootArray) {
		this.tree.setModel(this.treModel = new MemberTreeModel(_rootArray));
		reset(this.integrator);
	}

	public void setParameterString(Parameter _e, String _v) {
		_e.setValue(_v);
		this.parameters.put(_e.getObservable(), _e);
	}

	public Object getParameterValue(Observable _obs) {
		if (this.parameters.containsKey(_obs)) {
			return this.parameters.get(_obs).getValue();
		}
		return _obs.getValue(this.integrator);
	}

	public Collection<Parameter> getParameters() {
		return this.parameters.values();
	}

	private void initComponentTree() {
		JScrollPane scPropTree;
		javax.swing.JScrollPane scTree;
		scTree = new javax.swing.JScrollPane();
		this.tree = new javax.swing.JTree();
		scTree.setPreferredSize(new java.awt.Dimension(200, 80));
		scTree
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.tree.setModel(null);
		this.tree.setRootVisible(true);
		this.tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		scTree.setViewportView(this.tree);

		this.table = new JTable(null);

		scPropTree = new JScrollPane();
		scPropTree.setPreferredSize(new java.awt.Dimension(200, 150));
		scPropTree.setViewportView(this.table);

		setOrientation(JSplitPane.VERTICAL_SPLIT);
		setLeftComponent(scTree);
		setRightComponent(scPropTree);
	}

	public void reset(Object _integrador) {
		this.integrator = _integrador;
		this.parameters.clear();
		this.treModel.reset(this.integrator);
		this.tblModel.reset(this.integrator);
		this.tree.setSelectionRow(0);
		showHideTree();
	}

	public void showHideTree() {
		if (this.treModel.getChildCount(this.treModel.getRoot()) == 0) {
			setDividerLocation(0);
		}
	}

	public void refreshPropTable() {
		this.table.repaint();
	}

	public void configListeners() {
		this.tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) MemberExpl.this.tree
						.getLastSelectedPathComponent();
				MemberExpl.this.tblModel.setData(node == null ? null
						: MemberExpl.this.treModel.getObservablesVec(node));
				MemberExpl.this.tree.validate();
				MemberExpl.this.table.validate();
			}
		});
	}

	public Observable[] getSelection() {
		int i;
		Observable[] m = null;
		int x[] = this.table.getSelectedRows();

		if (x != null && x.length > 0) {
			m = new Observable[x.length];
			for (i = 0; i < x.length; i++) {
				m[i] = this.tblModel.elementAt(x[i]);
			}

		}
		return m;
	}
}
