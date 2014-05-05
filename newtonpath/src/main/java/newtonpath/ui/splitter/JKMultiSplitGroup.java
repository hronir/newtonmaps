/**
 * 
 */
package newtonpath.ui.splitter;

import java.awt.Container;
import java.util.ArrayList;

import newtonpath.ui.ContentFactory;
import newtonpath.ui.splitter.JKMultiSplit.NodeSelectionAction;
import newtonpath.ui.splitter.JKMultiSplit.RemoveAction;
import newtonpath.ui.splitter.JKMultiSplit.SplitAction;


public class JKMultiSplitGroup {
	private ArrayList<JKMultiSplit> paneSet = null;
	private SplitAction clickSplit = new SplitAction();
	private RemoveAction clickRemove = new RemoveAction();

	public void startSelectAndSplit(ContentFactory cb) {
		this.clickSplit.prepareContentFactory(cb);
		startLeafSelection(this.clickSplit);
	}

	public void startSelectAndSplit(Container c) {
		this.clickSplit.prepareContainer(c);
		startLeafSelection(this.clickSplit);
	}

	public void startSelectAndRemove() {
		startLeafSelection(this.clickRemove);
	}

	public JKMultiSplitGroup() {
		this.paneSet = new ArrayList<JKMultiSplit>();
	}

	public boolean add(JKMultiSplit o) {
		o.setSplitGroup(this);
		return this.paneSet.add(o);
	}

	public boolean remove(JKMultiSplit o) {
		o.setSplitGroup(null);
		return this.paneSet.remove(o);
	}

	public void startLeafSelection(NodeSelectionAction _action) {
		for (JKMultiSplit el : this.paneSet) {
			el.startLeafSelection(_action);
		}
	}

	public void finishLeafSelection() {
		this.clickSplit.finish();
		for (JKMultiSplit el : this.paneSet) {
			el.finishLeafSelection();
		}
	}

	public void resetControlBoxLeaf(JKMultiSplit _split) {
		for (JKMultiSplit el : this.paneSet) {
			if (el != _split) {
				el.resetControlBoxLeaf(false);
			}
		}
	}

}