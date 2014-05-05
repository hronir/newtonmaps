package newtonpath.conic.drawing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

public class HyperbolaPlotter extends ConicPlotter {
	public void assign(ConicMatrix _t) {
		this.assign(1D, _t);
	}

	public void assign(double _factor, ConicMatrix _t) {
		assign(_factor, _t.getCoefficient(0, 0),
				(_t.getCoefficient(1, 0) * 2D), _t.getCoefficient(1, 1), (_t
						.getCoefficient(2, 0) * 2D),
				(_t.getCoefficient(2, 1) * 2D), _t.getCoefficient(2, 2));
	}

	public void normalizeAndAssign(double _norm, ConicMatrix _t) {
		double n = _t.getMaxNorm();
		if (n > 0) {
			assign(_norm / n, _t);
		} else {
			assign(1D, _t);
		}

	}

	@Override
	public void assign(int A_, int B_, int C_, int D_, int E_, int F_) {
		super.assign(A_, B_, C_, D_, E_, F_);
		if (!isConvexGradient()) {
			invertSign();
		}
	}

	private void invertSign() {
		this.A = -this.A;
		this.B = -this.B;
		this.C = -this.C;
		this.D = -this.D;
		this.E = -this.E;
		this.F = -this.F;
	}

	@Override
	public void draw(int xs, int ys, int xe, int ye, int minX, int maxX,
			int minY, int maxY, boolean endPoint, int octants) {

		int a, b, c, d, e, f;

		a = this.A;
		b = this.B;
		c = this.C;
		d = this.D;
		e = this.E;
		f = this.F;
		// if (! isConvexGradient()) {
		// A = -A;
		// B = -B;
		// C = -C;
		// D = -D;
		// E = -E;
		// F = -F;
		// }
		super.draw(xs, ys, xe, ye, minX, maxX, minY, maxY, endPoint, octants);
		this.A = a;
		this.B = b;
		this.C = c;
		this.D = d;
		this.E = e;
		this.F = f;
	}

	public final void getPositiveOrientedTangentVector(int xs, int ys, Point v) {
		getGradient(xs, ys, v);
		turnRightVector(v);
	}

	public void getGradient(int xs, int ys, Point v) {
		v.x = 2 * this.A * xs + this.B * ys + this.D;
		v.y = this.B * xs + 2 * this.C * ys + this.E;
	}

	public int xSection(int x, int minY, int maxY, int _offset) {
		float a, b, c;
		a = this.C;
		b = this.B * (float) x + this.E;
		c = this.A * x * (float) x + this.D * (float) x + this.F;
		this.borderSectionX[_offset] = this.borderSectionX[_offset + 1] = x;
		final int retVal = section(a, b, c, minY, maxY, this.borderSectionY,
				_offset);
		return retVal;
	}

	public int ySection(int y, int minX, int maxX, int _offset) {
		float a, b, c;
		a = this.A;
		b = this.B * (float) y + this.D;
		c = this.C * y * (float) y + this.E * (float) y + this.F;
		this.borderSectionY[_offset] = this.borderSectionY[_offset + 1] = y;
		final int retVal = section(a, b, c, minX, maxX, this.borderSectionX,
				_offset);
		return retVal;
	}

	private int saveSection0(int _offset) {
		this.borderSection[_offset] = false;
		this.borderSection[_offset + 1] = false;
		return 0;
	}

	private int saveSection1(int _offset, int[] _table, int _v1) {
		_table[_offset] = _v1;
		this.borderSection[_offset] = true;
		this.borderSection[_offset + 1] = false;
		return 1;
	}

	private int saveSection2(int _offset, int[] _table, int _v1, int _v2) {
		_table[_offset] = _v1;
		_table[_offset + 1] = _v2;
		this.borderSection[_offset] = true;
		this.borderSection[_offset + 1] = true;
		return 2;
	}

	private int section(float _a, float _b, float _c, int _min, int _max,
			int[] _table, int _offset) {
		if (_a == 0) {
			if (_b == 0) {
				return saveSection0(_offset);
			}
			int t = (int) (-_c / _b);
			if (t < _min || t > _max) {
				return saveSection0(_offset);
			}
			return saveSection1(_offset, _table, t);
		}

		float b, c;
		b = _b / _a;
		c = _c / _a;
		float vert = -b / 2;
		if (vert * vert + b * vert + c > 0) {
			return saveSection0(_offset);
		}
		if (vert < _min) {
			if (_max * _max + b * _max + c > 0) {
				return saveSection1(_offset, _table, (int) newton(b, c, _max));
			}
			return saveSection0(_offset);
		}
		if (vert > _max) {
			if (_min * _min + b * _min + c > 0) {
				return saveSection1(_offset, _table, (int) newton(b, c, _min));
			}
			return saveSection0(_offset);
		}

		float vMin, vMax;
		vMin = _min * _min + b * _min + c;
		vMax = _max * _max + b * _max + c;
		if (vMin < 0 && vMax < 0) {
			return saveSection0(_offset);
		}
		if (vMin < 0) {
			return saveSection1(_offset, _table, (int) newton(b, c, _max));
		}
		if (vMax < 0) {
			return saveSection1(_offset, _table, (int) newton(b, c, _min));
		}
		float t1 = newton(b, c, (vert - _min > _max - vert) ? _min : _max);
		float t2 = vert - (t1 - vert);
		if (t1 > _max) {
			t1 = _max;
		}
		if (t2 > _max) {
			t2 = _max;
		}
		if (t1 < _min) {
			t1 = _min;
		}
		if (t2 < _min) {
			t2 = _min;
		}
		return saveSection2(_offset, _table, (int) t1, (int) t2);
	}

	private float newton(float _b, float _c, float _init) {
		float t, dT;
		t = _init;
		int iter = 20;
		while (iter > 0) {
			float d = 2 * t + _b;
			if (Math.abs(d) < 1e-6) {
				throw new RuntimeException("Number Overflow");
			}
			dT = -(t * t + _b * t + _c) / d;
			t += dT;
			if (Math.abs(dT) < 0.0025) {
				return t;
			}
			iter--;
		}
		throw new RuntimeException("Number Overflow");
	}

	private final int borderSectionX[] = new int[8];
	private final int borderSectionY[] = new int[8];
	private final boolean borderSection[] = new boolean[8];

	public Collection<Point> getIntersections(Rectangle _r) {
		int minX, maxX, minY, maxY;
		minX = _r.x;
		maxX = _r.x + _r.width - 1;
		minY = _r.y;
		maxY = _r.y + _r.height - 1;
		ArrayList<Point> retVal = new ArrayList<Point>(8);
		calculateRectangleIntersec(minX, maxX, minY, maxY);
		for (int i = 0; i < 8; i++) {
			final int x = this.borderSectionX[i];
			final int y = this.borderSectionY[i];
			if (this.borderSection[i]) {
				retVal.add(new Point(x, y));
			}
		}
		return retVal;
	}

	public boolean testFrontier(int x, int y) {
		boolean z, p, n;
		z = p = n = false;
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				long v = eval(x + i, y + j);
				if (v < 0) {
					n = true;
				} else if (v > 0) {
					p = true;
				} else {
					z = true;
				}
			}
		}
		return z || (p && n);
	}

	public void drawHyperbola(Rectangle _r, int octantBranch) {
		Point v = new Point();
		int minX, maxX, minY, maxY;
		minX = _r.x;
		maxX = _r.x + _r.width - 1;
		minY = _r.y;
		maxY = _r.y + _r.height - 1;

		int[] starts = new int[4];
		int[] ends = new int[4];
		int[] octant = new int[8];

		int nbStarts = 0, nbEnds = 0;

		calculateRectangleIntersec(minX, maxX, minY, maxY);
		for (int i = 0; i < 8; i++) {
			final int x = this.borderSectionX[i];
			final int y = this.borderSectionY[i];
			if (this.borderSection[i]) {
				getPositiveOrientedTangentVector(x, y, v);

				boolean internalBranch = true;
				if (internalBranch && x == minX && v.x <= 0) {
					internalBranch = false;
				}
				if (internalBranch && x == maxX && v.x >= 0) {
					internalBranch = false;
				}
				if (internalBranch && y == minY && v.y <= 0) {
					internalBranch = false;
				}
				if (internalBranch && y == maxY && v.y >= 0) {
					internalBranch = false;
				}

				turnLeftVector(v);
				octant[i] = getOctant(v.x, v.y);
				if (internalBranch) {
					starts[nbStarts] = i;
					nbStarts++;
				} else {
					ends[nbEnds] = i;
					nbEnds++;
				}
			}
		}
		if (nbStarts == nbEnds) {
			for (int i = 0; i < nbStarts; i++) {
				boolean selectedOctant = false;
				if (octantBranch <= 0) {
					selectedOctant = true;
				} else {
					int currentOctantStart = octant[starts[i]];
					int currentOctantEnd = currentOctantStart + 8;
					for (int j = 0; j < nbEnds; j++) {
						int aux = octant[ends[j]];
						if (aux < currentOctantStart) {
							aux += 8;
						}
						if (currentOctantEnd > aux) {
							currentOctantEnd = aux;
						}
					}
					int aux = octantBranch;
					if (aux < currentOctantStart) {
						aux += 8;
					}
					if (currentOctantEnd >= aux) {
						selectedOctant = true;
					}
				}
				if (selectedOctant) {
					final int x = this.borderSectionX[starts[i]];
					final int y = this.borderSectionY[starts[i]];
					draw(x, y, x, y, minX, maxX, minY, maxY, false, 8);
				}

			}
		}
	}

	protected void debugBranch(int x, int y) {

	}

	private void calculateRectangleIntersec(int minX, int maxX, int minY,
			int maxY) {
		xSection(minX, minY, maxY, 0);
		xSection(maxX, minY, maxY, 2);
		ySection(minY, minX, maxX, 4);
		ySection(maxY, minX, maxX, 6);
		for (int i = 0; i < 4; i++) {
			if (this.borderSection[i]) {
				for (int j = 4; j < 8; j++) {
					if (this.borderSection[j]
							&& (this.borderSectionX[i] == this.borderSectionX[j])
							&& (this.borderSectionY[i] == this.borderSectionY[j])) {
						this.borderSection[j] = false;
					}
				}
			}
		}
	}

	private static void turnRightVector(Point v) {
		int aux = v.x;
		v.x = -v.y;
		v.y = aux;
	}

	private static void turnLeftVector(Point v) {
		int aux = v.x;
		v.x = v.y;
		v.y = -aux;
	}

	public boolean checkPoint(int x, int y) {
		int i, j;
		boolean pos = false, neg = false;
		for (i = -1; i <= 1; i++) {
			for (j = -1; j <= 1; j++) {
				if (eval(x + i, y + j) > 0) {
					pos = true;
				} else {
					neg = true;
				}
			}
		}
		return (pos && neg);
	}

	public long eval(long x, long y) {
		return (this.A * x + this.B * y + this.D) * x + (this.C * y + this.E)
				* y + this.F;
	}

	public boolean isConvexGradient() {
		boolean retVal = false;
		float a1, b1, c1, a2, b2, c2, x, y;
		if (Math.abs(this.A) > (Math.abs(this.B) / 2)) {
			a1 = this.A;
			a2 = b1 = 0.5f * this.B;
			b2 = this.C;
			c1 = 0.5f * this.D;
			c2 = 0.5f * this.E;
		} else {
			a2 = this.A;
			a1 = b2 = 0.5f * this.B;
			b1 = this.C;
			c2 = 0.5f * this.D;
			c1 = 0.5f * this.E;
		}
		if (Math.abs(a1 * b2 - a2 * b1) < 1.e-18) {

			throw new SingularMatrixException();
		}

		float f = a2 / a1;
		b2 -= f * b1;
		c2 -= f * c1;
		y = -c2 / b2;
		x = -(c1 + y * b1) / a1;
		retVal = ((this.A * x + this.B * y + this.D) * x
				+ (this.C * y + this.E) * y + this.F) > 0;

		return retVal;
	}

	public void plotContent(BufferedImage _img, Rectangle _r) {
		Graphics gra = _img.getGraphics();
		gra.setColor(Color.WHITE);
		gra.fillRect(0, 0, _img.getWidth(), _img.getHeight());
		for (int x = 0; x < _r.width; x++) {
			for (int y = 0; y < _r.height; y++) {
				if (eval(x + _r.x, y + _r.y) < 0) {
					_img.setRGB(x, y, Color.lightGray.getRGB());
				} else {
					_img.setRGB(x, y, Color.darkGray.getRGB());
				}
			}
		}
	}

}
