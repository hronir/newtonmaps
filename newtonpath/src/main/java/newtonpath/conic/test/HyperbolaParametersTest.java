package newtonpath.conic.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import newtonpath.conic.drawing.ConicMatrix;


public class HyperbolaParametersTest extends JFrame implements Runnable {
	private final JPanel tb, c;
	private final Coef spCoef[][];
	private final Coef factor;
	private final ConicMatrix matrix;
	private final JList lstParams;
	private final JSpinner nbPoint;

	BufferedImage img;

	public static class Coef extends JSpinner {

		public Coef() {
			super(new SpinnerNumberModel(0D, -1e6D, 1e6D, 10D));
			setPreferredSize(new Dimension(70, 24));
		}
	}

	protected void refreshMatrix() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j <= i; j++) {
				this.matrix
						.setCoefficient(i, j, ((Double) this.factor.getValue())
								.doubleValue()
								* ((Double) this.spCoef[i][j].getValue())
										.doubleValue());
			}
		}
	}

	protected void doTest() {

		if (this.lstParams.getSelectedIndex() >= 0) {
			TestParameters pt = (TestParameters) this.lstParams
					.getSelectedValue();

			pt.printDescription();
			pt.testHyperbola(System.out);

			Rectangle r = pt.rect;
			HyperbolaImagePlotter p = new HyperbolaImagePlotter(this.img, r,
					Color.YELLOW.getRGB());
			this.matrix.setValues(pt.matrix.getValues());

			p.normalizeAndAssign(100000D, this.matrix);
			p.plotContent(this.img, r);
			p.drawHyperbola(r, -1);

			for (Point point : p.getIntersections(r)) {
				System.out.println(point.x + "\t" + point.y + "\t"
						+ p.testFrontier(point.x, point.y));
			}

			this.c.setPreferredSize(new Dimension(this.img.getWidth() * 2,
					this.img.getHeight() * 2));
			this.c.repaint();
			showMatrix();
		}
	}

	public HyperbolaParametersTest() {
		this.img = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);

		this.matrix = new ConicMatrix();
		this.matrix.setValues(new double[] { 1, 0, -2, 20, 0, 123 });

		this.tb = new JPanel();
		BoxLayout loTb = new BoxLayout(this.tb, BoxLayout.X_AXIS);
		this.tb.setLayout(loTb);
		JButton b;
		this.tb.add(b = new JButton("Test"));

		b.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				doTest();
			}
		});

		// tb.add(cmbParams=new JComboBox());

		this.nbPoint = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
		this.nbPoint.setValue(new Integer(1));
		this.tb.add(this.nbPoint);

		this.factor = new Coef();
		this.factor.setValue(new Double(1D));
		this.tb.add(this.factor);

		this.spCoef = new Coef[3][];
		for (int i = 0; i < 3; i++) {
			this.spCoef[i] = new Coef[i + 1];
			for (int j = 0; j <= i; j++) {
				this.spCoef[i][j] = new Coef();
				this.tb.add(this.spCoef[i][j]);
				this.spCoef[i][j].setValue(new Double(this.matrix
						.getCoefficient(i, j)));
			}
		}
		this.tb.add(Box.createHorizontalGlue());

		JScrollPane sc;
		this.c = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(HyperbolaParametersTest.this.img, 0, 0,
						HyperbolaParametersTest.this.img.getWidth() * 2,
						HyperbolaParametersTest.this.img.getHeight() * 2, null);
			}
		};
		this.c.setPreferredSize(new Dimension(800, 800));
		sc = new JScrollPane(this.c);
		sc.setPreferredSize(new Dimension(1100, 500));

		setLayout(new BorderLayout());
		add(this.tb, BorderLayout.NORTH);
		add(sc, BorderLayout.CENTER);

		this.lstParams = new JList();
		this.lstParams.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				doTest();
			}
		});
		add(new JScrollPane(this.lstParams), BorderLayout.WEST);

		pack();

		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new HyperbolaParametersTest());
	}

	public void run() {
		scan();
		doTest();
		setVisible(true);
	}

	private void showMatrix() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j <= i; j++) {
				this.spCoef[i][j].setValue(new Double(this.matrix
						.getCoefficient(i, j)));
			}
		}
	}

	HashMap<String, TestParameters> params = new HashMap<String, TestParameters>(
			1024 * 16);

	Pattern reId = Pattern.compile("^\\[([0-9_]+)\\] *(.*) *$");
	Pattern reMatrix = Pattern
			.compile(" *(Matrix:)? *drawing.ConicMatrix\\( *([\\-\\+0-9\\.E]+) *, *([\\-\\+0-9\\.E]+) *, *([\\-\\+0-9\\.E]+) *, *([\\-\\+0-9\\.E]+) *, *([\\-\\+0-9\\.E]+) *, *([\\-\\+0-9\\.E]+) *\\) *");
	Pattern reRect = Pattern
			.compile(" *(Rectangle *\\: *Rectangle|Rectangle|Rectangle\\: java.awt.Rectangle)? *[\\(\\[] *([\\-\\+0-9\\.E]+) *, *([\\-\\+0-9\\.E]+) *, *([\\-\\+0-9\\.E]+) *, *([\\-\\+0-9\\.E]+) *[\\)\\]]");

	private void readLine(String _line) {
		Matcher m = this.reId.matcher(_line);
		if (m.matches()) {
			String k = m.group(1);
			TestParameters p = null;
			if (this.params.containsKey(k)) {
				p = this.params.get(k);
			} else {
				this.params.put(k, p = new TestParameters(k));
			}
			String l = m.group(2);
			// System.out.println(l);
			m = this.reMatrix.matcher(l);
			if (m.matches()) {
				int ij = 0;
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j <= i; j++) {
						p.matrix.setCoefficient(i, j, Double.parseDouble(m
								.group(ij + 2)));
						ij++;
					}
				}
			} else if ((m = this.reRect.matcher(l)).matches()) {
				p.rect.setBounds(Integer.parseInt(m.group(2)), Integer
						.parseInt(m.group(3)), Integer.parseInt(m.group(4)),
						Integer.parseInt(m.group(5)));
			} else {
				p.error = (p.error == null ? "" : p.error) + l + "\n";
			}
		}
	}

	// private boolean fileExists(String _id){
	// return (new File("["+_id+"].png")).isFile();
	// }

	private void scan(String _filename) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(_filename));
		String s;
		while ((s = r.readLine()) != null) {
			readLine(s);
		}
		r.close();

		for (TestParameters t : this.params.values()) {
			t.printDescription();
		}
	}

	private void scan() {
		for (File f : new File(".").listFiles()) {
			if (f.getName().endsWith(".txt")) {
				try {
					scan(f.getAbsolutePath());
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
		}

		Iterator<TestParameters> it = this.params.values().iterator();
		while (it.hasNext()) {
			TestParameters p = it.next();
			if (p.rect.width <= 1 || p.rect.height <= 1
					|| p.testHyperbola(null)) {
				it.remove();
			}
		}

		initCombo();
	}

	private void initCombo() {
		TestParameters[] list = this.params.values().toArray(
				new TestParameters[this.params.size()]);
		Arrays.sort(list, new Comparator<TestParameters>() {
			public int compare(TestParameters o1, TestParameters o2) {
				return o1.id.compareTo(o2.id);
			}
		});
		DefaultComboBoxModel m = new DefaultComboBoxModel(list);
		this.lstParams.setModel(m);
		this.lstParams.setSelectedIndex(0);
	}
}
