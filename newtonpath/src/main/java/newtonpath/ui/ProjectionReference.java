package newtonpath.ui;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ProjectionReference extends Projection {
	final private double projArrayProj[][] = { { 1D, 0D, 0D, 0D },
			{ 0D, 1D, 0D, 0D }, { 0D, 0D, 1D, 0D }, { 0D, 0D, 0D, 1D } };
	private double rotation[][] = { { 1D, 0D, 0D, 0D }, { 0D, -1D, 0D, 0D },
			{ 0D, 0D, -1D, 0D }, { 0D, 0D, 0D, 1D } };
	private double zoom[][] = { { 5D, 0D, 0D, 0D }, { 0D, 5D, 0D, 0D },
			{ 0D, 0D, 5D, 0D }, { 0D, 0D, 0D, 1D } };
	private double focus = 200;

	public double[][] getRotation() {
		return this.rotation;
	}

	public void setRotation(double[][] rotation) {
		this.rotation = rotation;
		calcTransform();
	}

	public double getFocus() {
		return this.focus;
	}

	public void setFocus(double focus) {
		this.focus = focus;
		calcProjection();
		calcTransform();
	}

	public void setZoom(double[][] zoom) {
		this.zoom = zoom;
		calcTransform();
	}

	public double[][] getZoom() {
		return this.zoom;
	}

	final private double[][] auxProj1 = new double[][] { { 1D, 0D, 0D, 0D },
			{ 0D, 1D, 0D, 0D }, { 0D, 0D, 1D, 1D }, { 0D, 0D, 0D, 1D } };
	final private double[][] auxProj2 = new double[][] { { 1D, 0D, 0D, 0D },
			{ 0D, 1D, 0D, 0D }, { 0D, 0D, 1D, 0D }, { 0D, 0D, 0D, 1D } };
	private final static double identity[][] = { { 1D, 0D, 0D, 0D },
			{ 0D, 1D, 0D, 0D }, { 0D, 0D, 1D, 0D }, { 0D, 0D, 0D, 1D } };

	private final double[][] aux1 = newMap();
	private final double[][] aux2 = newMap();

	private final double auxZoom[][] = { { 1D, 0D, 0D, 0D },
			{ 0D, 1D, 0D, 0D }, { 0D, 0D, 1D, 0D }, { 0D, 0D, 0D, 1D } };

	private final double[][] auxTrans = new double[][] { { 1D, 0D, 0D, 0D },
			{ 0D, 1D, 0D, 0D }, { 0D, 0D, 1D, 0D }, { 0D, 0D, 0D, 1D } };

	public ProjectionReference() {
		calcProjection();
		calcTransform();
	}

	public void rotate(int ax, double angle) {
		rotationMatrix(ax, angle, this.aux2);
		applyTransformRot(this.aux2);
	}

	public void translate(double dx, double dy) {
		this.auxTrans[3][0] = dx;
		this.auxTrans[3][1] = dy;
		applyTransformTrans(this.auxTrans);
	}

	public void zoom(int i) {
		double f = 1.05;
		double g = 1;
		if (i < 0) {
			f = 1 / f;
			i = -i;
		}
		while (i > 0) {
			g *= f;
			i--;
		}

		zoom(g);
	}

	public void zoom(double g) {
		this.auxZoom[0][0] = this.auxZoom[1][1] = this.auxZoom[2][2] = g;
		applyTransformZoom(this.auxZoom);
	}

	public void aprox(int i) {
		double f = 1.2;
		double z = 1;
		if (i < 0) {
			f = 1 / f;
			i = -i;
		}
		while (i > 0) {
			this.focus /= f;
			z /= f;
			i--;
		}
		zoom(z);
		calcProjection();
		calcTransform();
	}

	private void calcTransform() {
		synchronized (this.invertMap) {
			this.invalidInvertMap = true;
			composeProjection3D(this.projArrayProj, this.rotation, this.aux1);
			composeProjection3D(this.zoom, this.aux1, this.projArray);
		}
	}

	private void applyTransformRot(double a[][]) {
		copyMap(this.rotation, this.aux1);
		composeProjection3D(a, this.aux1, this.rotation);
		calcTransform();
	}

	private void applyTransformZoom(double a[][]) {
		copyMap(this.zoom, this.aux1);
		composeProjection3D(a, this.aux1, this.zoom);
		calcTransform();
	}

	private void applyTransformTrans(double a[][]) {
		copyMap(this.zoom, this.aux1);
		composeProjection3D(a, this.aux1, this.zoom);
		calcTransform();
	}

	private void calcProjection() {
		final double projPar = 1000D;
		this.auxProj1[2][3] = 1D / projPar;
		this.auxProj2[3][2] = -projPar + this.focus;
		composeProjection3D(this.auxProj1, this.auxProj2, this.projArrayProj);
	}

	private void rotationMatrix(int axe, double angle, double[][] M) {
		double c, s;
		int k, l;
		copyMap(identity, M);
		c = Math.cos(angle);
		s = Math.sin(angle);
		k = (axe + 1) % 3;
		l = (axe + 2) % 3;
		M[k][k] = M[l][l] = c;
		M[k][l] = -(M[l][k] = s);
	}

	private static void composeProjection3D(double A[][], double B[][],
			double[][] result) {
		int i, j, k;
		for (i = 0; i < 4; i++) {
			for (k = 0; k < 4; k++) {
				result[i][k] = 0D;
				for (j = 0; j < 4; j++) {
					result[i][k] += A[j][k] * B[i][j];
				}
			}
		}
	}

	public void refresh() {
		calcProjection();
		calcTransform();
	}

	public void loadXml(Element e) {
		loadXmlArray(this.rotation, (Element) e
				.getElementsByTagName("rotation").item(0));
		this.focus = Double.parseDouble(((Element) e.getElementsByTagName(
				"focus").item(0)).getAttribute("value"));

		double z = Double.parseDouble(((Element) e.getElementsByTagName("zoom")
				.item(0)).getAttribute("value"));
		this.zoom[0][0] = this.zoom[1][1] = this.zoom[2][2] = z;
		loadXmlVector(this.zoom[3],
				(Element) e.getElementsByTagName("translation").item(0));
		calcProjection();
		calcTransform();
	}

	private static void loadXmlArray(double[][] arr, Element e) {
		NodeList l = e.getElementsByTagName("vector");
		for (int i = 0; i < arr.length; i++) {
			loadXmlVector(arr[i], (Element) l.item(i));
		}
	}

	private static void loadXmlVector(double[] ds, Element e) {
		NodeList l = e.getElementsByTagName("component");
		for (int i = 0; i < ds.length; i++) {
			ds[i] = Double.parseDouble(((Element) l.item(i))
					.getAttribute("value"));
		}
	}
}