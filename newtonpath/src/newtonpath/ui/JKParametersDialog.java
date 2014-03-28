package newtonpath.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class JKParametersDialog extends JDialog {
	final JKParameters parameters;

	public JKParametersDialog(JKParameters _param) {
		this.parameters = _param;
		setTitle(this.parameters.getOperation().toString());
		setLayout(new BorderLayout());
		add(this.parameters, BorderLayout.CENTER);
		setPreferredSize(new Dimension(300, 400));
		setModal(true);

		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout(FlowLayout.LEFT));
		JButton btnOk = new JButton("Ok");
		btnOk.setDefaultCapable(true);
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JKParametersDialog.this.parameters.doOperation();
				dispose();
			}
		});
		bar.add(btnOk);
		add(bar, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(btnOk);
		pack();
	}
}
