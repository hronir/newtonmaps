package newtonpath.ui;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

public class DoubleBufferedImage<E> {

	private volatile E paramNew;
	private volatile boolean paramChanged = false;
	private volatile BufferedImage imgCpy;
	private volatile BufferedImage imgView;
	private volatile boolean imageChanged = false;
	private volatile boolean imageOk = false;
	private final Object synchronizer = new Object();
	private final BufferedImage imgBkg;

	private final E param;
	private final ParameterManager<E> manager;

	public DoubleBufferedImage(ParameterManager<E> newManager, BufferedImage _bk) {
		super();
		this.manager = newManager;
		this.paramNew = this.manager.getObject();
		this.param = this.manager.getObject();
		this.imgBkg = _bk;
		this.imgCpy = new BufferedImage(_bk.getColorModel(), _bk.getRaster()
				.createCompatibleWritableRaster(), _bk.isAlphaPremultiplied(),
				null);
		this.imgView = new BufferedImage(_bk.getColorModel(), _bk.getRaster()
				.createCompatibleWritableRaster(), _bk.isAlphaPremultiplied(),
				null);
	}

	public void setParam(E par) {
		synchronized (this.paramNew) {
			this.manager.copyTo(par, this.paramNew);
			this.paramChanged = true;
			this.imageOk = false;
		}
	}

	public void prepare(NewtonSim _int) {
		if (this.paramChanged) {
			synchronized (this.paramNew) {
				this.manager.copyTo(this.paramNew, this.param);
				this.paramChanged = false;
			}
		}
	}

	public void validate(boolean _valid) {
		if (_valid) {
			synchronized (this.synchronizer) {
				this.imgCpy.setData(this.imgBkg.getData());
				this.imageOk = this.imageChanged = true;
			}
		}
	}

	public Image getImage() {
		Image r = null;
		if (this.imageOk) {
			if (this.imageChanged) {
				synchronized (this.synchronizer) {
					this.imgCpy.copyData(this.imgView.getRaster());
					r = this.imgView;
				}
			} else {
				r = this.imgView;
			}
		}
		return r;
	}

	public ColorModel getColorModel() {
		return this.imgBkg.getColorModel();
	}

	public Graphics getGraphics() {
		return this.imgBkg.getGraphics();
	}

	public int getWidth() {
		return this.imgBkg.getWidth();
	}

	public int getHeight() {
		return this.imgBkg.getHeight();
	}

	public E getParam() {
		return this.param;
	}
}