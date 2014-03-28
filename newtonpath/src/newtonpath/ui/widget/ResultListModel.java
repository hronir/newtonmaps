package newtonpath.ui.widget;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import newtonpath.statemanager.OperationResult;


public class ResultListModel extends AbstractListModel {
	private final List<OperationResult> list;

	public ResultListModel() {
		this.list = new ArrayList<OperationResult>();
	}

	public boolean add(OperationResult _o) {
		boolean retVal = this.list.add(_o);
		int i = getSize() - 1;
		fireIntervalAdded(this, i, i);
		return retVal;
	}

	public OperationResult getElementAt(int index) {
		return this.list.get(index);
	}

	public int getSize() {
		return this.list.size();
	}

	public void clear() {
		int i = getSize();
		this.list.clear();
		if (i > 0) {
			fireIntervalRemoved(this, 0, i - 1);
		}
	}

	public OperationResult[] toArray() {
		return this.list.toArray(new OperationResult[this.list.size()]);
	}

	public void refresh(){
		fireContentsChanged(this, 0, list.size());
	}
}
