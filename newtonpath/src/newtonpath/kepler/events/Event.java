/**
 * 
 */
package newtonpath.kepler.events;

public abstract class Event {
	protected final double date;
	public double[] position;

	public Event(double _date) {
		this.date = _date;
		this.position = null;
	}

	public Event(double _date, double _position[]) {
		this.date = _date;
		if (_position != null) {
			this.position = _position.clone();
		} else {
			this.position = null;
		}
	}

	public abstract String getDescription();

	public abstract EventPosition[] getPoints();

	public double getDate() {
		return this.date;
	}
}