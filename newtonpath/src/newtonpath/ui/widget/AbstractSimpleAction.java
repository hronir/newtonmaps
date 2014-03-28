package newtonpath.ui.widget;

import java.awt.Insets;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;

public abstract class AbstractSimpleAction extends AbstractAction implements
		SimpleAction {
	final public DefaultButtonModel model;
	final protected static Insets defaultButonMargin = new Insets(0, 3, 0, 3);

	public AbstractSimpleAction(DefaultButtonModel _model, String name,
			Icon icon) {
		super(name, icon);
		this.model = _model;
	}

	public AbstractSimpleAction(String name, Icon icon) {
		this(new DefaultButtonModel(), name, icon);
	}

	public AbstractSimpleAction(DefaultButtonModel _model, String name) {
		this(_model, name, null);
	}

	public AbstractSimpleAction(String name) {
		this(new DefaultButtonModel(), name);
	}

	public void setAction(AbstractButton x) {
		x.setAction(this);
		x.setModel(this.model);
	}

	public AbstractButton getNewButton() {
		AbstractButton b = new JButton();
		setAction(b);
		b.setMargin(defaultButonMargin);
		return b;
	}

	public AbstractButton getNewIconButton() {
		AbstractButton b = getNewButton();
		String t = b.getText();
		b.setText(null);
		b.setToolTipText(t);
		return b;
	}

	public JMenuItem getNewMenuItem() {
		JMenuItem b;
		setAction(b = new JMenuItem());
		return b;
	}
}
