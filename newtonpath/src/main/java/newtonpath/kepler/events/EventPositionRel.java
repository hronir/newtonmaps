/**
 * 
 */
package newtonpath.kepler.events;

import newtonpath.kepler.Newton;
import newtonpath.kepler.bodies.Body;
import newtonpath.kepler.bodies.Planet;

public class EventPositionRel extends EventPosition {
	final private Planet fromBody;
	private final int bodyPosition;

	public EventPositionRel(Event _ph, Newton _integ, Body[] _descriptions,
			double _position[], int _body, int _fromBody) {
		super(_ph, _integ, _descriptions, _position, _body);
		int i;
		this.fromBody = (Planet) _descriptions[_fromBody];
		this.bodyPosition = _fromBody;
		for (i = 0; i < 3; i++) {
			this.position[i] -= _position[3 * _fromBody + i];
			this.velocity[i] -= _position[_integ.firstSpeedPos + 3 * _fromBody
					+ i];
		}
	}

	private EventPositionRel(Body body, Planet fromBody, int bodyPosition,
			double[] position, double[] velocity, Event _ev) {
		super(body, position, velocity, _ev);
		this.fromBody = fromBody;
		this.bodyPosition = bodyPosition;

	}

	@Override
	public Body getFromBody() {
		return this.fromBody;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new EventPositionRel(this.body, this.fromBody,
				this.bodyPosition, this.position, this.velocity, this.event);
	}

	@Override
	public void evalPosition(double result[], double positions[]) {
		int i;
		for (i = 0; i < 3; i++) {
			result[i] = this.position[i] + positions[this.bodyPosition * 3 + i];
		}
	}
}