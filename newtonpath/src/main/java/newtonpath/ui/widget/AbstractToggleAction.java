package newtonpath.ui.widget;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToggleButton.ToggleButtonModel;

public abstract class AbstractToggleAction extends AbstractSimpleAction
		implements ToggleAction {
	private static ToggleButtonModel newToggleButtonModel(boolean initialValue) {
		ToggleButtonModel m = new ToggleButtonModel();
		m.setSelected(initialValue);
		return m;
	}

	public AbstractToggleAction(String name, Icon icon) {
		super(new ToggleButtonModel(), name, icon);
	}

	public AbstractToggleAction(String name, Icon icon, boolean initialValue) {
		super(newToggleButtonModel(initialValue), name, icon);
	}

	public AbstractToggleAction(String name) {
		super(new ToggleButtonModel(), name);
	}

	public AbstractToggleAction(String name, boolean initialValue) {
		super(newToggleButtonModel(initialValue), name);
	}

	@Override
	public AbstractButton getNewButton() {
		AbstractButton b;
		b = new JToggleButton();
		setAction(b);
		b.setMargin(defaultButonMargin);
		return b;
	}

	@Override
	public JMenuItem getNewMenuItem() {
		JMenuItem b;
		setAction(b = new javax.swing.JCheckBoxMenuItem());
		return b;
	}

	@Override
	public AbstractButton getNewIconButton() {
		AbstractButton b = getNewButton();
		String t = b.getText();
		b.setText(null);
		b.setToolTipText(t);
		return b;
	}
}
