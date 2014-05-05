package newtonpath.ui.widget;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JMenuItem;

public interface SimpleAction extends Action {

	public void setAction(AbstractButton x);

	public AbstractButton getNewButton();

	public JMenuItem getNewMenuItem();

}
