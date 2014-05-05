package newtonpath.ui.widget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class Console extends JPanel implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3674696636637679540L;
	private final PipedInputStream pin = new PipedInputStream();
	private final PipedInputStream pin2 = new PipedInputStream();
	private Thread reader;
	private Thread reader2;
	private boolean quit;
	private JTextArea textArea;
	private JScrollPane scroll;
	private final PrintStream pOldOut;
	private final PrintStream pOldOut2;

	public Console() {
		setPreferredSize(new Dimension(400, 300));
		this.textArea = new JTextArea();
		setLayout(new BorderLayout());
		this.scroll = new JScrollPane(this.textArea);
		add(this.scroll, BorderLayout.CENTER);
		this.textArea.setEditable(false);
		this.pOldOut = System.out;
		this.pOldOut2 = System.err;
		try {
			PipedOutputStream pout = new PipedOutputStream(this.pin);
			System.setOut(new PrintStream(pout, true));
		} catch (java.io.IOException io) {
			this.textArea.append("Couldn't redirect STDOUT to this console\n"
					+ io.getMessage());
		} catch (SecurityException se) {
			this.textArea.append("Couldn't redirect STDOUT to this console\n"
					+ se.getMessage());
		}

		try {
			PipedOutputStream pout2 = new PipedOutputStream(this.pin2);
			System.setErr(new PrintStream(pout2, true));
		} catch (java.io.IOException io) {
			this.textArea.append("Couldn't redirect STDERR to this console\n"
					+ io.getMessage());
		} catch (SecurityException se) {
			this.textArea.append("Couldn't redirect STDERR to this console\n"
					+ se.getMessage());
		}

		this.quit = false; // signals the Threads that they should exit

		// Starting two separate threads to read from the PipedInputStreams
		//
		this.reader = new Thread(this);
		this.reader.setDaemon(true);
		this.reader.start();
		//
		this.reader2 = new Thread(this);
		this.reader2.setDaemon(true);
		this.reader2.start();

	}

	public synchronized void run() {
		try {
			while (Thread.currentThread() == this.reader) {
				try {
					this.wait(100);
				} catch (InterruptedException ie) {
				}
				if (this.pin.available() != 0) {
					String input = readLine(this.pin);
					this.pOldOut.print(input);
					this.textArea.append(input);
					JScrollBar vbar = this.scroll.getVerticalScrollBar();
					if (vbar != null) {
						vbar.setValue(vbar.getMaximum());
					}
					repaint();
				}
				if (this.quit) {
					return;
				}
			}

			while (Thread.currentThread() == this.reader2) {
				try {
					this.wait(100);
				} catch (InterruptedException ie) {
				}
				if (this.pin2.available() != 0) {
					String input = readLine(this.pin2);
					this.pOldOut2.print(input);
					this.textArea.append(input);
					JScrollBar vbar = this.scroll.getVerticalScrollBar();
					if (vbar != null) {
						vbar.setValue(vbar.getMaximum());
					}
					repaint();
				}
				if (this.quit) {
					return;
				}
			}
		} catch (Exception e) {
			this.textArea.append("\nConsole reports an Internal error.");
			this.textArea.append("The error is: " + e);
		}
	}

	public synchronized String readLine(PipedInputStream in) throws IOException {
		String input = "";
		do {
			int available = in.available();
			if (available == 0) {
				break;
			}
			byte b[] = new byte[available];
			in.read(b);
			input = input + new String(b, 0, b.length);
		} while (!input.endsWith("\n") && !input.endsWith("\r\n") && !this.quit);
		return input;
	}

}
