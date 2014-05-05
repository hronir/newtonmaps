package newtonpath.conic.test2;

import java.awt.Dimension;

import javax.swing.JFrame;

public class testConic extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 639578240209711823L;

	public testConic() {
		setLayout(new java.awt.BorderLayout());
		add(new testConicPane(), java.awt.BorderLayout.CENTER);

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("testConic");
		setMinimumSize(new Dimension(800, 600));
		setPreferredSize(new Dimension(800, 600));
		setResizable(false);
		pack();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new testConic().setVisible(true);
			}
		});

	}

}
