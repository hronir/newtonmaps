/**
 * 
 */
package newtonpath.kepler.events;

import newtonpath.kepler.Newton;
import newtonpath.kepler.bodies.Body;

public class EventPosition {
	protected final Body body;
	protected final double position[];
	protected final double velocity[];
	protected final Event event;

	public EventPosition(Event _ev, Newton _integ, Body[] _descriptions,
			double _position[], int _body) {
		int i;
		this.event = _ev;
		this.position = new double[3];
		this.velocity = new double[3];
		this.body = _descriptions[_body];
		for (i = 0; i < 3; i++) {
			this.position[i] = _position[3 * _body + i];
			this.velocity[i] = _position[_integ.firstSpeedPos + 3 * _body + i];
		}
	}

	EventPosition(Body body, double[] position, double[] velocity, Event _ev) {
		super();
		this.body = body;
		this.position = position == null ? null : position.clone();
		this.velocity = velocity == null ? null : velocity.clone();
		this.event = _ev;
	}

	public Body getBody() {
		return this.body;
	}

	public void evalPosition(double result[], double positions[]) {
		int i;
		for (i = 0; i < 3; i++) {
			result[i] = this.position[i];
		}
	}

	public double[] getPosition() {
		return this.position;
	}

	public double[] getVelocity() {
		return this.velocity;
	}

	public Event getEvent() {
		return this.event;
	}

	public Body getFromBody() {
		return null;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new EventPosition(this.body, this.position, this.velocity,
				this.event);
	}
}