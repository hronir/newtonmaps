package newtonpath.ui.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import newtonpath.statemanager.Observable;
import newtonpath.statemanager.ObservableArray;


public class MemberTreeModel extends DefaultTreeModel {
	private final HashMap<DefaultMutableTreeNode, List<ObservableArray>> nodeMembers;
	private final HashMap<DefaultMutableTreeNode, List<Observable>> nodeObservables;
	private final ObservableArray rootArray;

	public MemberTreeModel(ObservableArray _rootArray) {
		super(null);
		this.nodeMembers = new HashMap<DefaultMutableTreeNode, List<ObservableArray>>();
		this.nodeObservables = new HashMap<DefaultMutableTreeNode, List<Observable>>();
		this.rootArray = _rootArray;
	}

	public void reset(Object integrador) {
		setRoot(buildPropTree(integrador));
	}

	private DefaultMutableTreeNode buildPropTree(Object integrador) {
		this.nodeMembers.clear();

		DefaultMutableTreeNode rootNode = null;
		if (integrador != null) {
			rootNode = getNode(this.rootArray, integrador);
		}
		return rootNode;
	}

	private DefaultMutableTreeNode getNode(ObservableArray memb, Object _o) {
		DefaultMutableTreeNode ret = null;
		DefaultMutableTreeNode n;
		Object o;
		ObservableArray[] sub;

		o = _o;

		ret = new DefaultMutableTreeNode(memb);
		if (o != null) {
			sub = memb.getArrays(o);
			if (sub != null) {
				for (ObservableArray subitem : sub) {
					n = getNode(subitem, o);
					if (n != null) {
						ret.add(n);
					}
				}
			}
			Observable[] obs = memb.getComponents(o);
			if (obs != null) {
				this.nodeObservables.put(ret, new ArrayList<Observable>(Arrays
						.asList(obs)));
			}
		}
		return ret;
	}

	public ObservableArray[] getSubMembers(DefaultMutableTreeNode v) {
		ObservableArray[] resul = null;
		if (this.nodeMembers.containsKey(v)) {
			List<ObservableArray> subMemb = this.nodeMembers.get(v);
			resul = subMemb.toArray(new ObservableArray[subMemb.size()]);
		}
		return resul;
	}

	public List<ObservableArray> getSubMembersVec(DefaultMutableTreeNode v) {
		if (this.nodeMembers.containsKey(v)) {
			return this.nodeMembers.get(v);
		}
		return null;
	}

	public List<Observable> getObservablesVec(DefaultMutableTreeNode v) {
		if (this.nodeObservables.containsKey(v)) {
			return this.nodeObservables.get(v);
		}
		return null;
	}

	@Override
	public boolean isLeaf(Object node) {
		return false;
	}
}
