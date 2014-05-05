package newtonpath.ui.widget;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import newtonpath.statemanager.Observable;


public class MemberTableModel extends AbstractTableModel {
	private List<Observable> data;
	private Object integrator;
	private final MemberExpl expl;

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return Object.class;
	}

	public MemberTableModel(Object integ, MemberExpl _expl) {
		this(integ, _expl, new ArrayList<Observable>());
	}

	public MemberTableModel(Object integ, MemberExpl _expl,
			ArrayList<Observable> _data) {
		super();
		this.data = _data;
		this.integrator = integ;
		this.expl = _expl;
	}

	public void setData(List<Observable> _data) {
		this.data = _data;
		fireTableDataChanged();
	}

	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return this.data == null ? 0 : this.data.size();
	}

	public void clear() {
		this.data.clear();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Observable m;
		m = this.data.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return m.getDescription();
		case 1:
			if (this.expl != null) {
				return this.expl.getParameterValue(m);
			}
			if (this.integrator != null) {
				return m.getValue(this.integrator);
			}
		}
		return null;
	}

	public void addRow(Observable m) {
		int i = this.data.size();
		this.data.add(m);
		fireTableRowsInserted(i, i);
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return (col == 1 && this.expl != null && this.data != null);
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (col == 1 && this.expl != null) {
			this.expl.setParameterString(this.data.get(row).getParameter(),
					value.toString());
		}
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
		case 0:
			return "Field";
		case 1:
			return "Value";
		}
		return "";
	}

	public void reset(Object _integrador) {
		this.integrator = _integrador;
	}

	public Observable elementAt(int arg0) {
		return this.data.get(arg0);
	}
}
