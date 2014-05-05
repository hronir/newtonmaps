/*
 * ConicPlotter.java
 *
 * Created on 8 de noviembre de 2007, 18:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newtonpath.conic.test2;

/**
 * 
 * @author oriol
 */
public class ConicPlotter {
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

	int numpoints, maxnumpoints = 1000;

	double matrix[][];

	int A;
	int B;
	int C;
	int D;
	int E;
	int F;

	// User supplies this plot routine, which does the work.

	Object stderr;

	void fprintf(Object out, String fmt) {
	};

	void fprintf(Object out, String fmt, int A) {
	};

	void fprintf(Object out, String fmt, int A, int B) {
	};

	void fprintf(Object out, String fmt, int A, int B, int C) {
	};

	void fprintf(Object out, String fmt, int A, int B, int C, int D) {
	};

	void fprintf(Object out, String fmt, int A, int B, int C, int D, int E) {
	};

	void fprintf(Object out, String fmt, int A, int B, int C, int D, int E,
			int F) {
	};

	static int DIAGx[] = { 999, 1, 1, -1, -1, -1, -1, 1, 1 };
	static int DIAGy[] = { 999, 1, 1, 1, 1, -1, -1, -1, -1 };
	static int SIDEx[] = { 999, 1, 0, 0, -1, -1, 0, 0, 1 };
	static int SIDEy[] = { 999, 0, 1, 1, 0, 0, -1, -1, 0 };

	final static boolean DEBUG = false;

	int odd(int n) {
		return n & 1;
	}

	int abs(int i) {
		return i < 0 ? -i : i;
	}

	int getoctant(int gx, int gy) {
		// Use gradient to identify octant.
		int upper = abs(gx) > abs(gy) ? 1 : 0;
		if (gx >= 0) {
			if (gy >= 0) {
				return 4 - upper;
			} else {
				// Down
				return 1 + upper;
			}
		} else // Left
		if (gy > 0) {
			return 5 + upper;
		} else {
			// Down
			return 8 - upper;
		}
	}

	int draw(int xs, int ys, int xe, int ye) {
		prepCoef();
		this.numpoints = this.maxnumpoints;
		this.A *= 4;
		this.B *= 4;
		this.C *= 4;
		this.D *= 4;
		this.E *= 4;
		this.F *= 4;

		if (DEBUG) {
			fprintf(this.stderr, "draw -- %d %d %d %d %d %d\n", this.A, this.B,
					this.C, this.D, this.E, this.F);
		}

		// Translate start point to origin...
		this.F = this.A * xs * xs + this.B * xs * ys + this.C * ys * ys
				+ this.D * xs + this.E * ys + this.F;
		this.D = this.D + 2 * this.A * xs + this.B * ys;
		this.E = this.E + this.B * xs + 2 * this.C * ys;

		// Work out starting octant
		int octant = getoctant(this.D, this.E);

		int dxS = SIDEx[octant];
		int dyS = SIDEy[octant];
		int dxD = DIAGx[octant];
		int dyD = DIAGy[octant];

		int d, u, v;
		switch (octant) {
		case 1:
			d = this.A + this.B / 2 + this.C / 4 + this.D + this.E / 2 + this.F;
			u = this.A + this.B / 2 + this.D;
			v = u + this.E;
			break;
		case 2:
			d = this.A / 4 + this.B / 2 + this.C + this.D / 2 + this.E + this.F;
			u = this.B / 2 + this.C + this.E;
			v = u + this.D;
			break;
		case 3:
			d = this.A / 4 - this.B / 2 + this.C - this.D / 2 + this.E + this.F;
			u = -this.B / 2 + this.C + this.E;
			v = u - this.D;
			break;
		case 4:
			d = this.A - this.B / 2 + this.C / 4 - this.D + this.E / 2 + this.F;
			u = this.A - this.B / 2 - this.D;
			v = u + this.E;
			break;
		case 5:
			d = this.A + this.B / 2 + this.C / 4 - this.D - this.E / 2 + this.F;
			u = this.A + this.B / 2 - this.D;
			v = u - this.E;
			break;
		case 6:
			d = this.A / 4 + this.B / 2 + this.C - this.D / 2 - this.E + this.F;
			u = this.B / 2 + this.C - this.E;
			v = u - this.D;
			break;
		case 7:
			d = this.A / 4 - this.B / 2 + this.C + this.D / 2 - this.E + this.F;
			u = -this.B / 2 + this.C - this.E;
			v = u + this.D;
			break;
		case 8:
			d = this.A - this.B / 2 + this.C / 4 + this.D - this.E / 2 + this.F;
			u = this.A - this.B / 2 + this.D;
			v = u - this.E;
			break;
		default:
			fprintf(this.stderr, "FUNNY OCTANT\n");
			throw new RuntimeException("FUNNY OCTANT");
		}

		int k1sign = dyS * dyD;
		int k1 = 2 * (this.A + k1sign * (this.C - this.A));
		int Bsign = dxD * dyD;
		int k2 = k1 + Bsign * this.B;
		int k3 = 2 * (this.A + this.C + Bsign * this.B);

		// Work out gradient at endpoint
		int gxe = xe - xs;
		int gye = ye - ys;
		int gx = 2 * this.A * gxe + this.B * gye + this.D;
		int gy = this.B * gxe + 2 * this.C * gye + this.E;

		int octantcount = getoctant(gx, gy) - octant;
		if (octantcount < 0) {
			octantcount = octantcount + 8;
		} else if (octantcount == 0) {
			if ((xs > xe && dxD > 0) || (ys > ye && dyD > 0)
					|| (xs < xe && dxD < 0) || (ys < ye && dyD < 0)) {
				octantcount += 8;
			}
		}

		if (DEBUG) {
			fprintf(this.stderr, "octantcount = %d\n", octantcount);
		}

		int x = xs;
		int y = ys;

		while (octantcount > 0) {
			if (DEBUG) {
				fprintf(this.stderr, "-- %d -------------------------\n",
						octant);
			}

			if (odd(octant) != 0) {
				while (2 * v <= k2) {
					// Plot this point
					plot(x, y);

					// Are we inside or outside?
					if (DEBUG) {
						fprintf(this.stderr, "x = %3d y = %3d d = %4d\n", x, y,
								d);
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
				while (2 * u < k2) {
					// Plot this point
					plot(x, y);
					if (DEBUG) {
						fprintf(this.stderr, "x = %3d y = %3d d = %4d\n", x, y,
								d);
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
			fprintf(this.stderr, "-- %d (final) -----------------\n", octant);
		}

		if (odd(octant) != 0) {
			while (2 * v <= k2) {
				// Plot this point
				plot(x, y);
				if (x == xe && y == ye) {
					break;
				}
				if (DEBUG) {
					fprintf(this.stderr, "x = %3d y = %3d d = %4d\n", x, y, d);
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
			while ((2 * u < k2)) {
				// Plot this point
				plot(x, y);
				if (x == xe && y == ye) {
					break;
				}
				if (DEBUG) {
					fprintf(this.stderr, "x = %3d y = %3d d = %4d\n", x, y, d);
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

		return 1;
	}

	void plot(int x, int y) {
		fprintf((Object) null, "plot(%d, %d)\n", x, y);
	}

	/*
	 * ConicPlotter() { assign(0,0,0,0,0,0); } ConicPlotter(int A, int B, int C,
	 * int D, int E, int F) { assign(A,B,C,D,E,F); }
	 * 
	 * public ConicPlotter(float A_, float B_, float C_, float D_, float E_,
	 * float F_){ this((int) A_, (int) B_, (int) C_, (int) D_, (int) E_, (int)
	 * F_); } void assign(int A_, int B_, int C_, int D_, int E_, int F_) { A =
	 * A_; B = B_; C = C_; D = D_; E = E_; F = F_; }
	 * 
	 * int rnd(double x) { return (x>=0.0)?(int)(x + 0.5):(int)(x - 0.5); }
	 * 
	 * void assignf(double scale, double A_, double B_, double C_, double D_,
	 * double E_, double F_) { A = rnd(A_ * scale); B = rnd(B_ * scale); C =
	 * rnd(C_ * scale); D = rnd(D_ * scale); E = rnd(E_ * scale); F = rnd(F_ *
	 * scale); }
	 */
	double fact = 80000000;

	public ConicPlotter(double _mat[][]) {
		this.matrix = _mat;
	}

	private void prepCoef() {
		this.A = (int) (this.fact * this.matrix[0][0]);
		this.B = (int) (this.fact * 2D * this.matrix[1][0]);
		this.C = (int) (this.fact * this.matrix[1][1]);
		this.D = (int) (this.fact * 2D * this.matrix[2][0]);
		this.E = (int) (this.fact * 2D * this.matrix[2][1]);
		this.F = (int) (this.fact * this.matrix[2][2]);
	}

}
