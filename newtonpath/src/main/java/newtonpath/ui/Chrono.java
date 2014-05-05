package newtonpath.ui;

public class Chrono {
	private long time;
	private long totalTime;
	private final String name;

	public Chrono() {
		this(null);
	}

	public Chrono(String s) {
		this.time = System.currentTimeMillis();
		this.name = s;
	}

	public Chrono start() {
		this.time = System.currentTimeMillis();
		return this;
	}

	public Chrono pause() {
		this.totalTime += System.currentTimeMillis() - this.time;
		this.time = 0;
		return this;
	}

	public long end() {
		if (this.time != 0) {
			pause();
		}
		return this.totalTime;
	}

	public void report() {
		System.out.print(end());
		System.out.print("\t");
		System.out.println(this.name);
	}

}
