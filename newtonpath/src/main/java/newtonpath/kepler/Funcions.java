package newtonpath.kepler;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;

public class Funcions {

	static double adamsBashforth_Beta[][] = {
			{ 1. },
			{ 3. / 2., -1. / 2. },
			{ 23. / 12., -16. / 12., 5. / 12. },
			{ 55. / 24., -59. / 24., 37. / 24., -9. / 24. },
			{ 1901. / 720., -2774. / 720., 2616. / 720., -1274. / 720.,
					251. / 720. } };

	static void adamsBashforth(double x[], int dim, double f1[], double f2[],
			double f3[], double f4[], int n, double pas) {
		double f[][] = new double[4][];
		f[0] = f1;
		f[1] = f2;
		f[2] = f3;
		f[3] = f4;
		int i, j;
		for (i = 0; i < dim; i++) {
			for (j = 0; j < n; j++) {
				x[i] += pas * adamsBashforth_Beta[n - 1][n - j - 1] * f[j][i];
			}
		}
	}

	static void copySubTable(double a[], int ia, double b[], int ib, int n) {
		int k;
		for (k = 0; k < n; k++) {
			b[k + ib] = a[k + ia];
		}
	}

	static void swd(double a[], int ia, double b[], int ib) {
		double c;
		c = a[ia];
		a[ia] = b[ib];
		b[ib] = c;
	}

	public static int gaussElim(double A[], double B[], int dim, double x[],
			double _tol) {
		int i, p, j, k;
		double m;
		for (i = 0; i < dim - 1; i++) {
			for (p = i; p < dim; p++) {
				if (Math.abs(A[p + i * dim]) > _tol) {
					break;
				}
			}
			if (p == dim) {
				continue;
			}
			if (p != i) {
				for (j = 0; j < dim; j++) {
					swd(A, p + j * dim, A, i + j * dim);
				}
				swd(B, p, B, i);
			}
			for (j = i + 1; j < dim; j++) {
				m = A[j + i * dim] / A[i + i * dim];
				B[j] -= m * B[i];
				for (k = i; k < dim; k++) {
					A[j + dim * k] -= m * A[i + dim * k];
				}
			}
		}
		if (Math.abs(A[dim * dim - 1]) < _tol) {
			if (Math.abs(B[dim - 1]) > _tol) {
				return 1;
			} else {
				x[dim - 1] = 1.;
			}
		} else {
			x[dim - 1] = B[dim - 1] / A[dim * dim - 1];
		}
		for (i = dim - 2; i >= 0; i--) {
			x[i] = B[i];
			for (j = i + 1; j < dim; j++) {
				x[i] -= A[i + dim * j] * x[j];
			}
			x[i] /= A[i + i * dim];
		}
		return 0;
	}

	static double scalard(double x[], double y[], int dim) {
		double m;
		int k;
		m = 0.;
		for (k = 0; k < dim; k++) {
			m += x[k] * y[k];
		}
		return m;
	}

	static double normaliserd(double x[], int dim) {
		double m;
		int k;
		m = Math.sqrt(scalard(x, x, dim));
		// TODO tolerancia
		if (m > 0) {
			for (k = 0; k < dim; k++) {
				x[k] /= m;
			}
		}
		return m;
	}

	public static double scal(double x[], double y[]) {
		double s;
		// Produit scalar de deux vecteur 3D
		s = x[0] * y[0] + x[1] * y[1] + x[2] * y[2];
		return s;
	}

	public static double scal(double x[], int ix, double y[], int iy) {
		double s;
		// Produit scalar de deux vecteur 3D
		s = x[0 + ix] * y[0 + iy] + x[1 + ix] * y[1 + iy] + x[2 + ix]
				* y[2 + iy];
		return s;
	}

	static void Vect(double x[], int ix, double y[], int iy, double result[]) {
		// Produit vectoriel de deux vecteurs 3D
		// Le resultat est gard�dans le vecteur *result
		result[0] = x[1 + ix] * y[2 + iy] - y[1 + iy] * x[2 + ix];
		result[1] = x[2 + ix] * y[0 + iy] - y[2 + iy] * x[0 + ix];
		result[2] = x[0 + ix] * y[1 + iy] - y[0 + iy] * x[1 + ix];
	}

	public static void Vect(double x[], double y[], double result[]) {
		// Produit vectoriel de deux vecteurs 3D
		// Le resultat est gard�dans le vecteur *result
		result[0] = x[1] * y[2] - y[1] * x[2];
		result[1] = x[2] * y[0] - y[2] * x[0];
		result[2] = x[0] * y[1] - y[0] * x[1];
	}

	public static double dist2(double x[], double y[]) {
		return dist2(x, 0, y, 0);
	}

	public static double dist2(double x[], int ix, double y[], int iy) {
		// Distance au carre entre deux vecteurs 3D
		double z0, z1, z2, d;
		z0 = x[0 + ix] - y[0 + iy];
		z1 = x[1 + ix] - y[1 + iy];
		z2 = x[2 + ix] - y[2 + iy];
		d = z0 * z0 + z1 * z1 + z2 * z2;
		return d;
	}

	public static double dist(double x[], double y[]) {
		// Distance entre deux vecteurs 3D
		return Math.sqrt(dist2(x, y));
	}

	public static double dist(double x[], int ix, double y[], int iy) {
		// Distance entre deux vecteurs 3D
		return Math.sqrt(dist2(x, ix, y, iy));
	}

	public static void normaliser(double v[]) {
		double mod;
		mod = Math.sqrt(scal(v, v));
		v[0] = v[0] / mod;
		v[1] = v[1] / mod;
		v[2] = v[2] / mod;
	}

	public static void reference(double v[], double r[], double e1[],
			double e2[], double e3[]) {

		double mod_e1, mod_e2;
		double v_e1;

		e1[0] = r[0];
		e1[1] = r[1];
		e1[2] = r[2];

		mod_e1 = Math.sqrt(Funcions.scal(e1, e1));
		e1[0] = e1[0] / mod_e1;
		e1[1] = e1[1] / mod_e1;
		e1[2] = e1[2] / mod_e1;

		e2[0] = v[0];
		e2[1] = v[2];
		e2[1] = v[2];
		v_e1 = Funcions.scal(v, e1);
		e2[0] -= v_e1 * e1[0];
		e2[1] -= v_e1 * e1[1];
		e2[2] -= v_e1 * e1[2];
		mod_e2 = Math.sqrt(Funcions.scal(e2, e2));
		e2[0] = e2[0] / mod_e2;
		e2[1] = e2[1] / mod_e2;
		e2[2] = e2[2] / mod_e2;

		Funcions.Vect(e1, e2, e3);

	}

	public static double[] refenlinia(double x[][]) {
		double r[] = new double[9];
		int i, j;
		for (i = 0; i < 3; i++) {
			for (j = 0; j < 3; j++) {
				r[i * 3 + j] = x[i][j];
			}
		}
		return r;
	}

	public static double[] refenlinia4x4(double x[][]) {
		double r[] = new double[16];
		int i, j;
		for (i = 0; i < 4; i++) {
			for (j = 0; j < 4; j++) {
				r[i * 4 + j] = x[i][j];
			}
		}
		return r;
	}

	public static double applySymMatForm(double A[][], double x[], double y[]) {
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

	public static void printMap(double A[][]) {
		int i, j;
		for (i = 0; i < 3; i++) {
			for (j = 0; j < 3; j++) {
				System.out.print(A[j][i]);
				System.out.print("\t");
			}
			System.out.println(A[3][i]);
		}
		System.out.println();
	}

	public static double[] applyTriagMatrix(double A[][], double x[]) {
		double y[] = new double[A.length];
		int i, j;
		for (i = 0; i < A.length; i++) {
			y[i] = 0d;
			for (j = i; j < A.length; j++) {
				y[i] += A[j][i] * x[j];
			}
		}
		return y;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Action {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Inspect {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Write {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.METHOD })
	public @interface Read {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Parameter {
		String[] value() default {};
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Operation {
		String value() default "";
	}

	@SuppressWarnings("unchecked")
	public final static Class<Annotation> inspectionAnnotations[] = new Class[] {
			Funcions.Write.class, Funcions.Inspect.class, Funcions.Read.class };
	@SuppressWarnings("unchecked")
	public static final Class<Annotation> actionAnnotations[] = new Class[] { Funcions.Action.class };
	@SuppressWarnings("unchecked")
	public static final Class<Annotation> settingsAnnotations[] = new Class[] { Funcions.Write.class };

	public final static void printVector(double v[]) {
		int j;
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		DecimalFormat formatter = new DecimalFormat("0.00000000E000", dfs);
		for (j = 0; j < v.length; j++) {
			System.out.print(formatter.format(v[j]));
			System.out.print(" ");
		}
		System.out.println();
	}

	public static double[][] newTriangularMatrix(int _dim) {
		double x[][];
		x = new double[_dim][];
		int i;
		for (i = 0; i < _dim; i++) {
			x[i] = new double[i + 1];
		}
		return x;
	}

	public static Date calDate(double julianDay) {
		return new Date(
				(long) Math.floor((julianDay - 2440587.5) * 24D * 3600000D));
	}

	public static double julianDay(Date _d) {
		return 2440587.5D + _d.getTime() / (24D * 3600000D);
	}

	public static double[][] newMap(int d1, int d2) {
		double x[][];
		x = new double[d1][];
		int i;
		for (i = 0; i < d1; i++) {
			x[i] = new double[d2];
		}
		return x;
	}

	public static void invertMap3D(double A[][], double result[][])
			throws Exception {
		int i;
		// double result[][]=newMap(3, 3);
		double z[] = new double[3];
		double a[];
		for (i = 0; i < 3; i++) {
			z[0] = z[1] = z[2] = 0D;
			z[i] = 1D;
			a = refenlinia(A);
			gaussElim(a, z, 3, result[i], 1.e-8D);
		}
		// return result;
	}

}
