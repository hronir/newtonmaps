package org.newtonmaps.conicplot;

public class QuadricSection {

	static public int section(ConicMatrix cm, double y, double minX,
			double maxX, double[] table) {
		double a, b, c;
		a = cm.getCoefficient(0, 0);
		b = 2D * (y * cm.getCoefficient(0, 1) + cm.getCoefficient(0, 2));
		c = y * y * cm.getCoefficient(1, 1) + 2D * y * cm.getCoefficient(1, 2)
				+ cm.getCoefficient(2, 2);
		return section(a, b, c, minX, maxX, table);
	}

	static private int section(double _a, double _b, double _c, double _min,
			double _max, double[] _table) {
		if (_a == 0) {
			if (_b == 0) {
				return saveSection0();
			}
			double t = -_c / _b;
			if (t < _min || t > _max) {
				return saveSection0();
			}
			return saveSection1(_table, t);
		}

		double b, c;
		b = _b / _a;
		c = _c / _a;
		double vert = -b / 2;
		if (vert * vert + b * vert + c > 0) {
			return saveSection0();
		}
		if (vert < _min) {
			if (_max * _max + b * _max + c > 0) {
				return saveSection1(_table, newton(b, c, _max));
			}
			return saveSection0();
		}
		if (vert > _max) {
			if (_min * _min + b * _min + c > 0) {
				return saveSection1(_table, newton(b, c, _min));
			}
			return saveSection0();
		}

		double vMin, vMax;
		vMin = _min * _min + b * _min + c;
		vMax = _max * _max + b * _max + c;
		if (vMin < 0 && vMax < 0) {
			return saveSection0();
		}
		if (vMin < 0) {
			return saveSection1(_table, newton(b, c, _max));
		}
		if (vMax < 0) {
			return saveSection1(_table, newton(b, c, _min));
		}
		double t1 = newton(b, c, (vert - _min > _max - vert) ? _min : _max);
		double t2 = vert - (t1 - vert);
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
		return saveSection2(_table, t1, t2);
	}

	static private double newton(double _b, double _c, double _init) {
		double t, dT;
		t = _init;
		int iter = 20;
		while (iter > 0) {
			double d = 2 * t + _b;
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

	static private int saveSection0() {
		return 0;
	}

	static private int saveSection1(double[] _table, double _v1) {
		_table[0] = _v1;
		return 1;
	}

	static private int saveSection2(double[] _table, double _v1, double _v2) {
		_table[0] = _v1;
		_table[1] = _v2;
		return 2;
	}
}
