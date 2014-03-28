package newtonpath.conic.drawing;

public class ConicPlotter {
	// Bresenham Ellipse AlgorithmBresenham Ellipse Algorithm
	//
	// CONIC 2D Bresenham-like conic drawer.
	// CONIC(Sx,Sy, Ex,Ey, A,B,C,D,E,F) draws the conic specified
	// by A x^2 + B x y + C y^2 + D x + E y + F = 0, between the
	// start point (Sx, Sy) and endpoint (Ex,Ey).

	// Author: Andrew W. Fitzgibbon (andrewfg@ed.ac.uk),
	// Machine Vision Unit,
	// Dept. of Artificial Intelligence,
	// Edinburgh University,
	// 
	// Date: 31-Mar-94
	// Version 2: 6-Oct-95
	// Bugfixes from Arne Steinarson <arst@ludd.luth.se>

	// A xx + B xy + C yy + D x + E y + F = 0

	protected int A;
	protected int B;
	protected int C;
	protected int D;
	protected int E;
	protected int F;

	private final static int DIAGx[] = { 999, 1, 1, -1, -1, -1, -1, 1, 1 };
	private final static int DIAGy[] = { 999, 1, 1, 1, 1, -1, -1, -1, -1 };
	private final static int SIDEx[] = { 999, 1, 0, 0, -1, -1, 0, 0, 1 };
	private final static int SIDEy[] = { 999, 0, 1, 1, 0, 0, -1, -1, 0 };

	private final static boolean DEBUG = false;

	private static boolean odd(int n) {
		return (n & 1) != 0;
	}

	private static int abs(int x) {
		return x < 0 ? -x : x;
	}

	public static int getOctant(int gx, int gy) {
		// Use gradient to identify octant.
		int upper = (abs(gx) > abs(gy)) ? 1 : 0;
		int retVal = 0;
		if (gx >= 0) {
			if (gy >= 0) {
				retVal = 4 - upper;
			} else {
				retVal = 1 + upper;
			}
		} else {
			if (gy > 0) {
				retVal = 5 + upper;
			} else {
				retVal = 8 - upper;
			}
		}
		return retVal;
	}

	protected void debugOctant(int i, int x, int y) {

	}

	public void draw(int xs, int ys, int xe, int ye, int minX, int maxX,
			int minY, int maxY, boolean endPoint, int octants) {
		int iA, iB, iC, iD, iE, iF;
		iA = this.A * 4;
		iB = this.B * 4;
		iC = this.C * 4;
		iD = this.D * 4;
		iE = this.E * 4;
		iF = this.F * 4;

		if (DEBUG) {
			System.err.println("draw -- " + iA + " " + iB + " " + iC + " " + iD
					+ " " + iE + " " + iF);
		}

		// Translate start point to origin...
		iF = iA * xs * xs + iB * xs * ys + iC * ys * ys + iD * xs + iE * ys
				+ iF;
		iD = iD + 2 * iA * xs + iB * ys;
		iE = iE + iB * xs + 2 * iC * ys;

		// Work out starting octant
		int octant = getOctant(iD, iE);

		int dxS = SIDEx[octant];
		int dyS = SIDEy[octant];
		int dxD = DIAGx[octant];
		int dyD = DIAGy[octant];

		int d, u, v;
		d = u = v = 0;
		switch (octant) {
		case 1:
			d = iA + iB / 2 + iC / 4 + iD + iE / 2 + iF;
			u = iA + iB / 2 + iD;
			v = u + iE;
			break;
		case 2:
			d = iA / 4 + iB / 2 + iC + iD / 2 + iE + iF;
			u = iB / 2 + iC + iE;
			v = u + iD;
			break;
		case 3:
			d = iA / 4 - iB / 2 + iC - iD / 2 + iE + iF;
			u = -iB / 2 + iC + iE;
			v = u - iD;
			break;
		case 4:
			d = iA - iB / 2 + iC / 4 - iD + iE / 2 + iF;
			u = iA - iB / 2 - iD;
			v = u + iE;
			break;
		case 5:
			d = iA + iB / 2 + iC / 4 - iD - iE / 2 + iF;
			u = iA + iB / 2 - iD;
			v = u - iE;
			break;
		case 6:
			d = iA / 4 + iB / 2 + iC - iD / 2 - iE + iF;
			u = iB / 2 + iC - iE;
			v = u - iD;
			break;
		case 7:
			d = iA / 4 - iB / 2 + iC + iD / 2 - iE + iF;
			u = -iB / 2 + iC - iE;
			v = u + iD;
			break;
		case 8:
			d = iA - iB / 2 + iC / 4 + iD - iE / 2 + iF;
			u = iA - iB / 2 + iD;
			v = u - iE;
			break;
		default:
			throw new RuntimeException("FUNNY OCTANT");
		}

		int k1sign = dyS * dyD;
		int k1 = 2 * (iA + k1sign * (iC - iA));
		int Bsign = dxD * dyD;
		int k2 = k1 + Bsign * iB;
		int k3 = 2 * (iA + iC + Bsign * iB);

		// Work out gradient at endpoint
		int gxe = xe - xs;
		int gye = ye - ys;
		int gx = 2 * iA * gxe + iB * gye + iD;
		int gy = iB * gxe + 2 * iC * gye + iE;

		int octantcount;
		if (endPoint) {
			octantcount = getOctant(gx, gy) - octant;
			if (octantcount < 0) {
				octantcount = octantcount + 8;
			} else if (octantcount == 0) {
				if ((xs > xe && dxD > 0) || (ys > ye && dyD > 0)
						|| (xs < xe && dxD < 0) || (ys < ye && dyD < 0)) {
					octantcount += 8;
				}
			}
		} else {
			octantcount = octants;
		}


		int x = xs;
		int y = ys;

		while (octantcount > 0
				&& (x >= minX && x <= maxX && y >= minY && y <= maxY)) {
			debugOctant(octant, x, y);
			if (odd(octant)) {
				while (2 * v <= k2
						&& (x >= minX && x <= maxX && y >= minY && y <= maxY)) {
					// Plot this point
					plot(x, y);

					// Are we inside or outside?
					if (DEBUG) {
						System.err.println("x = " + x + " y = " + y + " d = "
								+ d);
					}

					if (d < 0) { // Inside
						x = x + dxS;
						y = y + dyS;
						u = u + k1;
						v = v + k2;
						d = d + u;
					} else { // outside
						x = x + dxD;
						y = y + dyD;
						u = u + k2;
						v = v + k3;
						d = d + v;
					}
				}

				d = d - u + v / 2 - k2 / 2 + 3 * k3 / 8;
				// error (^) in Foley and van Dam p 959,
				// "2nd ed, revised 5th printing"
				u = -u + v - k2 / 2 + k3 / 2;
				v = v - k2 + k3 / 2;
				k1 = k1 - 2 * k2 + k3;
				k2 = k3 - k2;
				int tmp = dxS;
				dxS = -dyS;
				dyS = tmp;
			} else { // Octant is even
				while (2 * u < k2
						&& (x >= minX && x <= maxX && y >= minY && y <= maxY)) {
					// Plot this point
					plot(x, y);

					// Are we inside or outside?
					if (d > 0) { // Outside
						x = x + dxS;
						y = y + dyS;
						u = u + k1;
						v = v + k2;
						d = d + u;
					} else { // Inside
						x = x + dxD;
						y = y + dyD;
						u = u + k2;
						v = v + k3;
						d = d + v;
					}
				}
				int tmpdk = k1 - k2;
				d = d + u - v + tmpdk;
				v = 2 * u - v + tmpdk;
				u = u + tmpdk;
				k3 = k3 + 4 * tmpdk;
				k2 = k1 + tmpdk;

				int tmp = dxD;
				dxD = -dyD;
				dyD = tmp;
			}

			octant = (octant & 7) + 1;
			octantcount--;
		}

		// Draw final octant until we reach the endpoint
		if (DEBUG) {
			System.err.println("-- " + octant + " (final) -----------------\n");
		}
		if (endPoint) {
			debugOctant(octant, x, y);
			if (odd(octant)) {
				while (2 * v <= k2
						&& (x >= minX && x <= maxX && y >= minY && y <= maxY)) {
					// Plot this point
					plot(x, y);
					if (x == xe && y == ye) {
						break;
					}
					if (DEBUG) {
						System.err.println("x = " + x + " y = " + y + " d = "
								+ d);
					}

					// Are we inside or outside?
					if (d < 0) { // Inside
						x = x + dxS;
						y = y + dyS;
						u = u + k1;
						v = v + k2;
						d = d + u;
					} else { // outside
						x = x + dxD;
						y = y + dyD;
						u = u + k2;
						v = v + k3;
						d = d + v;
					}
				}

			} else { // Octant is even
				while ((2 * u < k2)
						&& (x >= minX && x <= maxX && y >= minY && y <= maxY)) {
					// Plot this point
					plot(x, y);
					if (x == xe && y == ye) {
						break;
					}
					if (DEBUG) {
						System.err.println("x = " + x + " y = " + y + " d = "
								+ d);
					}

					// Are we inside or outside?
					if (d > 0) { // Outside
						x = x + dxS;
						y = y + dyS;
						u = u + k1;
						v = v + k2;
						d = d + u;
					} else { // Inside
						x = x + dxD;
						y = y + dyD;
						u = u + k2;
						v = v + k3;
						d = d + v;
					}
				}
			}
		}
	}

	protected void plot(int x, int y) {
		System.out.println(x + " " + y);
	}

	public ConicPlotter() {
		this.A = this.B = this.C = this.D = this.E = this.F = 0;
	}

	public void assign(int A_, int B_, int C_, int D_, int E_, int F_) {
		this.A = A_;
		this.B = B_;
		this.C = C_;
		this.D = D_;
		this.E = E_;
		this.F = F_;
	}

	private static int rnd(double x) {
		return (x >= 0.0) ? (int) (x + 0.5) : (int) (x - 0.5);
	}

	public void assign(double scale, double A_, double B_, double C_,
			double D_, double E_, double F_) {
		assign(rnd(A_ * scale), rnd(B_ * scale), rnd(C_ * scale), rnd(D_
				* scale), rnd(E_ * scale), rnd(F_ * scale));
	}

}
