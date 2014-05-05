/**
 * 
 */
package newtonpath.ui;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import newtonpath.conic.drawing.ConicMatrix;
import newtonpath.conic.drawing.ConicPlotter;
import newtonpath.conic.drawing.HyperbolaPlotter;
import newtonpath.kepler.Funcions;
import newtonpath.kepler.OsculatoryElements;

public class OrbitsPlotter {
	protected final Point imageCenter;
	protected final Graphics2D bufferGraphics;

	private final Point gradient = new Point();
	private final ConicMatrix conicMatrix = new ConicMatrix();

	protected final HyperbolaPlotter hyperbolaPlotter = new HyperbolaPlotter() {
		@Override
		protected void plot(int x, int y) {
			OrbitsPlotter.this.bufferGraphics.fillRect(
					OrbitsPlotter.this.imageCenter.x + x,
					OrbitsPlotter.this.imageCenter.y + y, 1, 1);
		}
	};

	public OrbitsPlotter(Graphics2D bufferGraphics) {
		this.bufferGraphics = bufferGraphics;
		this.imageCenter = new Point();
	}

	public void paintOrbits(Rectangle coordinateRect, final Point[] positions,
			final Projection reference,
			final OsculatoryElements[] keplerElements,
			final double[] centerCoordinates, final boolean paintOsculatoryOrbit) {
		int body, i;
		double inv[][] = null;
		OsculatoryElements[] el;
		double relPosCOM[] = new double[4];

		this.imageCenter.setLocation(coordinateRect.width / 2,
				coordinateRect.height / 2);

		relPosCOM[3] = 1D;
		try {
			inv = reference.getInvertMap();
			el = keplerElements;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		AffineTransform nat = new AffineTransform();
		AffineTransform oldTrans = this.bufferGraphics.getTransform();
		for (body = paintOsculatoryOrbit ? 0 : 1; body < el.length; body++) {

			if (el[body].excentricity < 1D) {

				double lj;

				double centerFromFocus[] = new double[3];

				lj = Funcions.scal(el[body].angularMomVec, inv[2]) / inv[3][3];
				if (Math.abs(lj) > 1e-6D) {
					double cf[][] = Funcions.newTriangularMatrix(3);
					double m[][] = { { 0 }, { -1, 0 }, { -1, -1, -1 } };

					for (i = 0; i < 3; i++) {
						relPosCOM[i] = el[body].center[i]
								+ el[body].excentricity * el[body].majorVec[i]
								- centerCoordinates[i];
						centerFromFocus[i] = el[body].center[i]
								- centerCoordinates[i];
					}
					relPosCOM[3] = 1D;
					double[] xx = new double[4];
					reference.applyProjection(relPosCOM, xx);
					relPosCOM = xx;

					if (relPosCOM[2] < 0) {
						double projRef[][] = { { 0, 0, 0, 0 }, { 0, 0, 0, 0 },
								{ 0, 0, 0, 0 }, { 0, 0, 0, 0 }, };

						double[] p1, p2, p3, p4;

						p1 = new double[] {
								centerFromFocus[0] + el[body].majorVec[0],
								centerFromFocus[1] + el[body].majorVec[1],
								centerFromFocus[2] + el[body].majorVec[2], 1D };
						p2 = new double[] {
								centerFromFocus[0] + el[body].minorVec[0],
								centerFromFocus[1] + el[body].minorVec[1],
								centerFromFocus[2] + el[body].minorVec[2], 1D };
						p3 = new double[] { centerFromFocus[0],
								centerFromFocus[1], centerFromFocus[2], 1D };

						p4 = new double[] { p1[0] + p2[0] + p3[0],
								p1[1] + p2[1] + p3[1], p1[2] + p2[2] + p3[2],
								p1[3] + p2[3] + p3[3], };

						reference.applyProjectionLin(p1, projRef[0]);
						reference.applyProjectionLin(p2, projRef[1]);
						reference.applyProjectionLin(p3, projRef[2]);
						reference.applyProjectionLin(p4, projRef[3]);

						projRef[0][2] = projRef[0][3];
						projRef[1][2] = projRef[1][3];
						projRef[2][2] = projRef[2][3];
						projRef[3][2] = projRef[3][3];

						p4[0] = projRef[3][0];
						p4[1] = projRef[3][1];
						p4[2] = projRef[3][2];

						double[] fact = new double[3];
						double[] a = Funcions.refenlinia(projRef);
						Funcions.gaussElim(a, p4, 3, fact, 1.e-8D);
						for (int ii = 0; ii < 3; ii++) {
							for (int jj = 0; jj < 3; jj++) {
								projRef[ii][jj] *= fact[ii];
							}
						}

						double invProjRef[][] = null;
						try {
							invProjRef = Funcions.newMap(3, 3);
							Funcions.invertMap3D(projRef, invProjRef);
							for (int ii = 0; ii < 3; ii++) {
								for (int jj = 0; jj <= ii; jj++) {
									cf[ii][jj] = evalConic(m, invProjRef[ii],
											invProjRef[jj]);
								}
							}
						} catch (Exception ex) {
							throw new RuntimeException(ex);
						}
						if (cf[2][2] > 0D) {
							for (int ii = 0; ii < 3; ii++) {
								for (int jj = 0; jj <= ii; jj++) {
									cf[ii][jj] = -cf[ii][jj];
								}
							}
						}

						boolean pivotar = cf[0][0] < cf[1][1];

						if (pivotar) {
							pivotar(cf);
						}

						double U[][] = Funcions.newTriangularMatrix(3);
						double diag[] = new double[3];

						factorizeLDL(cf, U, diag);

						if ((diag[0] > 0) == (diag[1] > 0)) {
							double d[] = new double[2];
							d[0] = (1D / Math.sqrt(-diag[0] / diag[2]));
							d[1] = (1D / Math.sqrt(-diag[1] / diag[2]));

							double axe1[] = new double[3];
							double axe2[] = new double[3];
							double axe3[] = new double[3];

							applyInvTriangMatrix(U, setVector(1D, 0D, 0D), axe1);
							applyInvTriangMatrix(U, setVector(0D, 1D, 0D), axe2);
							applyInvTriangMatrix(U, setVector(0D, 0D, 1D), axe3);

							if (pivotar) {
								swap(axe1, 0, 1);
								swap(axe2, 0, 1);
								swap(axe3, 0, 1);
							}
							try {
								nat.setTransform(axe1[0], axe1[1], axe2[0],
										axe2[1], axe3[0] + this.imageCenter.x,
										axe3[1] + this.imageCenter.y);
								this.bufferGraphics.setTransform(nat);
								this.bufferGraphics.drawOval(-(int) d[0],
										-(int) d[1], 2 * (int) d[0],
										2 * (int) d[1]);
							} finally {
							}
						} else {
							if (pivotar) {
								pivotar(cf);
							}

							nat.setTransform(1, 0, 0, 1, 0, 0);

							this.bufferGraphics.setTransform(nat);
							this.conicMatrix.setValues(cf);
							try {
								this.hyperbolaPlotter.normalizeAndAssign(100D,
										this.conicMatrix);
								this.hyperbolaPlotter.getGradient(
										positions[i].x, positions[i].y,
										this.gradient);
								this.hyperbolaPlotter.drawHyperbola(
										coordinateRect, ConicPlotter.getOctant(
												this.gradient.x,
												this.gradient.y));
							} catch (Exception e) {

							}
						}
					}
				}
			}
		}
		this.bufferGraphics.setTransform(oldTrans);
	}

	private static double[] setVector(double... v) {
		return v;
	}

	private static void pivotar(double[][] coeficients) {
		double mem = coeficients[0][0];
		coeficients[0][0] = coeficients[1][1];
		coeficients[1][1] = mem;

		mem = coeficients[2][0];
		coeficients[2][0] = coeficients[2][1];
		coeficients[2][1] = mem;
	}

	private static double evalConic(double[][] cf, double[] xx, double yy[]) {
		double v;
		v = 0;
		for (int kk = 0; kk < 3; kk++) {
			for (int ll = 0; ll < 3; ll++) {
				v += xx[kk] * (kk < ll ? cf[ll][kk] : cf[kk][ll]) * yy[ll];
			}
		}
		return v;
	}

	private static void swap(double[] v, int i, int j) {
		double mem = v[i];
		v[i] = v[j];
		v[j] = mem;
	}

	private static void applyInvTriangMatrix(double _A[][], double _x[],
			double[] _result) {
		double xtemp[] = _x.clone();
		int n = xtemp.length;
		int j;
		while (n > 0) {
			n--;
			if (Math.abs(_A[n][n]) > 1.E-16) {
				_result[n] = xtemp[n] / _A[n][n];
				for (j = 0; j < n; j++) {
					xtemp[j] -= _A[n][j] * _result[n];
				}
			}
		}
	}

	private static void factorizeLDL(double a[][], double l[][], double d[]) {
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
}