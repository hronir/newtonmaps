/**
 * 
 */
package newtonpath.ui;

import newtonpath.kepler.events.EventPosition;

public class NewtonPlotterParam {
	protected EventPosition centerToEvent = null;
	protected int centerToBody = -1;
	protected final Projection reference = new Projection();

	public void set(NewtonPlotterParam par) {
		NewtonPlotterParam p = par;
		this.centerToEvent = p.centerToEvent;
		this.centerToBody = p.centerToBody;
		this.reference.copyFrom(p.reference);
	}

	public void set(int _centerToBody, EventPosition _centerToEvent,
			Projection reference) {
		this.centerToEvent = _centerToEvent;
		this.centerToBody = _centerToBody;
		this.reference.copyFrom(reference);
	}
}