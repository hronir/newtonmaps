package newtonpath.conic.test2;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import newtonpath.kepler.Funcions;


public class testConicPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4443209561536532544L;
	double ax[][] = { { -10, 150, 0 }, { 0, 130, 0 } };
	Point centerImg = new Point(400, 300);
	Graphics2D grafOsc;
	private BufferedImage imgOsc;
	double axefac = 1.3;

	public testConicPane() {
		this.ax[1][0] = this.axefac * this.ax[0][1];
		this.ax[1][1] = -this.axefac * this.ax[0][0];
		this.imgOsc = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
		this.grafOsc = (Graphics2D) this.imgOsc.getGraphics();
		plotConic();
	}

	private double[] applyInvTriagMatrix(double A[][], double _x[]) {
		double x[] = _x.clone();
		int n = x.length;
		double y[] = new double[n];
		int j;
		while (n > 0) {
			n--;
			if (Math.abs(A[n][n]) > 1.E-16) {
				y[n] = x[n] / A[n][n];
				for (j = 0; j < n; j++) {
					x[j] -= A[n][j] * y[n];
				}
			} else {
				return null;
			}
		}
		return y;
	}

	private double applySymMatForm(double A[][], double x[], double y[]) {
		double result = 0D;
		int i, j, d;
		d = x.length;
		for (i = 0; i < d; i++) {
			result += A[i][i] * x[i] * y[i];
			for (j = 0; j < i; j++) {
				result += A[i][j] * (x[i] * y[j] + x[j] * y[i]);
			}
		}
		return result;
	}

	private void factorizeLDL(double a[][], double l[][], double d[]) {
		int dim, i, r, k;
		double aux;
		dim = d.length;
		for (i = 0; i < dim; i++) {
			d[i] = 0D;
			for (k = 0; k <= i; k++) {
				l[i][k] = 0D;
			}
		}

		for (k = 0; k < dim; k++) {
			d[k] = a[k][k];
			for (r = 0; r < k; r++) {
				d[k] -= l[k][r] * l[k][r] * d[r];
			}
			l[k][k] = 1D;
			for (i = k + 1; i < dim; i++) {
				aux = 0D;
				for (r = 0; r < k; r++) {
					aux += l[i][r] * d[r] * l[k][r];
				}
				l[i][k] = (a[i][k] - aux) / d[k];
			}

		}
	}

	// float xA,xB,xC,xD,xE,xF;

	public void plotConic() {

		this.grafOsc.setBackground(new Color(0, 0, 0, 0));
		this.grafOsc.clearRect(0, 0, this.imgOsc.getWidth(), this.imgOsc
				.getHeight());

		int i, j, k;
		AffineTransform nat = new AffineTransform();
		AffineTransform oldTrans = this.grafOsc.getTransform();

		Color oldcolor = this.grafOsc.getColor();
		this.grafOsc.setColor(Color.lightGray);

		double cf[][] = Funcions.newTriangularMatrix(3);
		for (i = 0; i < 2; i++) {
			double aux2[] = new double[3];
			double a2 = Funcions.scal(this.ax[i], this.ax[i]);
			aux2[0] = (this.ax[i][0]) / a2;
			aux2[1] = (this.ax[i][1]) / a2;
			aux2[2] = 0;
			for (j = 0; j < 3; j++) {
				for (k = 0; k <= j; k++) {
					cf[j][k] += (i != 0 ? (aux2[j] * aux2[k])
							: -(aux2[j] * aux2[k]));
				}
			}
		}
		cf[2][2] -= 1D;
		double U[][] = Funcions.newTriangularMatrix(3);
		double diag[] = new double[3];

		factorizeLDL(cf, U, diag);

		double d[] = new double[2];
		d[0] = (1D / Math.sqrt(Math.abs(diag[0])));
		d[1] = (1D / Math.sqrt(Math.abs(diag[1])));

		double axe1[] = { 1D, 0D, 0D };
		double axe2[] = { 0D, 1D, 0D };
		double axe3[] = { 0D, 0D, 1D };

		axe1 = applyInvTriagMatrix(U, axe1);
		axe2 = applyInvTriagMatrix(U, axe2);
		axe3 = applyInvTriagMatrix(U, axe3);

		nat.setTransform(axe1[0], axe1[1], axe2[0], axe2[1], axe3[0]
				+ this.centerImg.x, axe3[1] + this.centerImg.y);

		this.grafOsc.setTransform(nat);
		this.grafOsc.setColor(Color.RED);
		// grafOsc.drawOval(-(int)d[0],-(int)d[1],2*(int)d[0],2*(int)d[1]);

		this.grafOsc.setTransform(oldTrans);
		this.grafOsc.translate(this.centerImg.x, this.centerImg.y);
		this.grafOsc.setColor(Color.BLUE);
		ConicPlotter cpl;
		double x[] = new double[3];
		double y[] = new double[3];

		x[0] = (-d[0] + 0D * U[1][0] - U[2][0] + U[1][0] * U[2][1]);
		x[1] = (-0D - U[2][1]);
		x[2] = 1D;
		y[0] = (d[0] + 0D * U[1][0] - U[2][0] + U[1][0] * U[2][1]);
		y[1] = (-0D - U[2][1]);
		y[2] = 1D;

		System.out.println(diag[0]);
		System.out.println(diag[1]);
		System.out.println(diag[2]);

		System.err.println(applySymMatForm(cf, x, x));
		System.err.println(applySymMatForm(cf, y, y));

		System.out.println(y[0]);
		System.out.println(y[1]);
		System.out.println(x[0]);
		System.out.println(x[1]);

		// float fact=5000000f;
		cpl = new ConicPlotter(cf);
		cpl = new ConicPlotter(cf) {
			@Override
			void plot(int x, int y) {
				// System.err.println("plot("+
				// Integer.toString(x)+","+Integer.toString(y)+")");
				testConicPane.this.grafOsc.fillRect(x
						- (int) testConicPane.this.ax[1][0], y
						- (int) testConicPane.this.ax[1][1], 1, 1);
				// System.err.println(xA*x*x+xB*x*y+xC*y*y+xD*x+xE*y+xF);
				if (this.numpoints-- < 0) {
					throw new RuntimeException();
				}
			}
		};
		try {
			cpl.draw((int) x[0], (int) x[1], (int) y[0], (int) y[1]);
		} catch (RuntimeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			cpl.draw((int) y[0], (int) y[1], (int) x[0], (int) x[1]);
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.grafOsc.setColor(Color.LIGHT_GRAY);
		x[2] = 1D;
		for (x[0] = -400; x[0] < 400; x[0] += 2) {
			for (x[1] = -300; x[1] < 300; x[1] += 2) {
				y[0] = x[0] + (int) this.ax[1][0];
				y[1] = x[1] + (int) this.ax[1][1];

				if (applySymMatForm(cf, y, y) < 0) {
					this.grafOsc.fillRect((int) x[0], (int) x[1], 1, 1);
				}
			}
		}

		this.grafOsc.setTransform(oldTrans);
		this.grafOsc.setColor(oldcolor);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.drawImage(this.imgOsc, 0, 0, null);

	}
}
