package newtonpath.ui.widget;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;


public abstract class AbstractRepeatAction extends
		AbstractSimpleAction implements MouseListener {
	final javax.swing.Timer autoRepeatTimer;

	public AbstractRepeatAction(DefaultButtonModel _model, String name,
			Icon icon) {
		super(_model, name, icon);
		autoRepeatTimer = new javax.swing.Timer(60, this);
		autoRepeatTimer.setInitialDelay(300);
	}

	public AbstractRepeatAction(String name, Icon icon) {
		this(new DefaultButtonModel(), name, icon);
	}

	public void mousePressed(MouseEvent e) {
		autoRepeatTimer.start();
	}

	public void mouseReleased(MouseEvent e) {
		autoRepeatTimer.stop();
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
		if (autoRepeatTimer.isRunning()) {
			autoRepeatTimer.stop();
		}
	}

	@Override
	public AbstractButton getNewButton() {
		AbstractButton b = super.getNewButton();
		b.addMouseListener(this);
		return b;
	}
}