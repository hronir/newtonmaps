/**
 * 
 */
package newtonpath.kepler.events;

import newtonpath.kepler.Poincare;
import newtonpath.kepler.bodies.Body;
import newtonpath.kepler.bodies.Planet;
import newtonpath.kepler.bodies.Spacecraft;

public class AproximationEvent extends Event {
	private final Spacecraft spaceCraft;
	private final Planet planet;
	private final EventPosition planetPos;
	private final EventPosition relPos;

	public AproximationEvent(double date, Poincare _p, Body[] _descriptions,
			int _from_body, double[] _position) {
		super(date, _position);

		this.spaceCraft = (Spacecraft) _descriptions[0];
		this.planet = (Planet) _descriptions[_from_body];
		this.planetPos = new EventPosition(this, _p.integrator, _descriptions,
				_position, _from_body);
		this.relPos = new EventPosition(this, _p.integrator, _descriptions,
				_position, 0);
	}

	@Override
	public String getDescription() {
		return "Aprox. " + this.spaceCraft.toString() + "-"
				+ this.planet.toString() + " " + Double.toString(this.date);
	}

	@Override
	public EventPosition[] getPoints() {

		EventPosition retVal[];
		retVal = new EventPosition[2];
		retVal[0] = this.planetPos;
		retVal[1] = this.relPos;
		return retVal;
	}
}