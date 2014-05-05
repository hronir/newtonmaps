/**
 * 
 */
package newtonpath.conic.test;

import java.awt.Rectangle;

import newtonpath.conic.drawing.ConicMatrix;
import newtonpath.conic.drawing.HyperbolaPlotter;


class HyperbolaTester extends HyperbolaPlotter {
	private byte[] imageTest;
	private Rectangle imageTestRectangle;
	public int overlap, outOfArea;

	@Override
	public void plot(int x, int y) {
		if (x < this.imageTestRectangle.x
				|| x > this.imageTestRectangle.x
						+ this.imageTestRectangle.width
				|| y < this.imageTestRectangle.y
				|| y > this.imageTestRectangle.y
						+ this.imageTestRectangle.height) {
			this.outOfArea++;
		} else {
			int index = (x - this.imageTestRectangle.x)
					+ (y - this.imageTestRectangle.y)
					* this.imageTestRectangle.width;
			if (this.imageTest[index] == 99) {
				this.overlap++;
			} else {
				this.imageTest[index] = 99;
			}
		}
	}

	public void prepareTest(ConicMatrix _c, Rectangle _r) {
		this.imageTestRectangle = _r;
		this.overlap = this.outOfArea = 0;
		this.imageTest = new byte[_r.width * _r.height];
		normalizeAndAssign(100D, _c);
		for (int x = _r.x; x < _r.x + _r.width; x++) {
			for (int y = _r.y; y < _r.y + _r.height; y++) {
				int index = (x - _r.x) + (y - _r.y)
						* this.imageTestRectangle.width;
				long v = eval(x, y);
				this.imageTest[index] = (v == 0) ? 0
						: ((v > 0) ? 1 : (byte) -1);
			}
		}
	}

	public int missingFrontierPixels() {
		byte last, current;
		int numErr = 0;
		Rectangle r = this.imageTestRectangle;
		for (int x = r.x; x < r.x + r.width; x++) {
			last = this.imageTest[(x - r.x)];
			for (int y = r.y; y < r.y + r.height; y++) {
				int index = (x - r.x) + (y - r.y)
						* this.imageTestRectangle.width;
				current = this.imageTest[index];
				if (current != last) {
					if (current != 99 && last != 99) {
						numErr++;
					}
				}
				last = current;
			}
		}
		for (int y = r.y; y < r.y + r.height; y++) {
			last = this.imageTest[(y - r.y) * this.imageTestRectangle.width];
			for (int x = r.x; x < r.x + r.width; x++) {
				int index = (x - r.x) + (y - r.y)
						* this.imageTestRectangle.width;
				current = this.imageTest[index];
				if (current != last) {
					if (current != 99 && last != 99) {
						numErr++;
					}
				}
				last = current;
			}
		}
		return numErr;
	}

	@Override
	protected void debugOctant(int i, int x, int y) {
		// System.out.println("Octant " + i);
	}

	@Override
	protected void debugBranch(int x, int y) {
		//
		// System.out.println("Branch\t" + (x - this.imageTestRectangle.x) +
		// "\t"
		// + (y - this.imageTestRectangle.y));
	}
}