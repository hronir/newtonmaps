package newtonpath.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import newtonpath.kepler.Aprox;
import newtonpath.kepler.BodySystemRef;
import newtonpath.kepler.Funcions;
import newtonpath.kepler.Funcions.Action;
import newtonpath.kepler.Funcions.Parameter;
import newtonpath.kepler.Funcions.Write;
import newtonpath.kepler.Newton;
import newtonpath.kepler.Poincare;
import newtonpath.kepler.events.AproximationEvent;
import newtonpath.kepler.events.Event;
import newtonpath.logging.KLogger;
import newtonpath.member.MemberItemRoot;
import newtonpath.statemanager.ObservableArray;
import newtonpath.statemanager.Operation;
import newtonpath.statemanager.StateManager;

public class AproxObs extends Aprox implements StateManager {
	private static final KLogger LOGGER;
	static {
		LOGGER = KLogger.getLogger(Aprox.class);
	}

	public final BodySystemRef systemRef;

	@Write
	@Parameter({ "Flyby" })
	public double flyByCriterion = 0.05;

	public AproxObs(BodySystemRef _sys) {
		super(_sys.getMasses(), _sys.getCentralBody());
		this.systemRef = _sys;
	}

	public static AproxObs getInstance(BodySystemRef _sys) throws Exception {
		AproxObs r = new AproxObs(_sys);
		r.section.poincareStartBody = r.systemRef.ear();
		r.section.poincareEndBody = r.systemRef.jup();
		double today = Funcions.julianDay(new Date());
		LOGGER.info("Loading ephemeris...");
		r.loadEpoch(2455690D);
		LOGGER.info("Initializing epoch...");
		r.getIntegrator().initPos();
		r.setEpochNow();
		r.aproxInitialTime = today;
		r.getIntegrator().setInitStep();
		LOGGER.info("Initializing reference...");
		r.section.setAllBases(r.section.poincareStartBody);
		r.planetLaunch();
		return r;
	}

	public ObservableArray getParameters() {
		return new MemberItemRoot(this.getClass(), Funcions.settingsAnnotations);
	}

	public ObservableArray getObservables() {
		return new MemberItemRoot(this.getClass(),
				Funcions.inspectionAnnotations);
	}

	public Operation[] getOperations() {
		return new MemberItemRoot(this.getClass(), Funcions.actionAnnotations)
				.getOperations(this);
	}

	@Override
	public AproxObs clone() {
		AproxObs r = new AproxObs(this.systemRef);
		copyState(this, r);
		return r;
	}

	private List<Event> events = null;

	public List<Event> getEvents() {
		if (this.events == null) {
			List<Event> r = new ArrayList<Event>();
			if (this.section.poincareStartPoint != null) {
				r.add(new AproximationEvent(this.section.poincareStartVector[0]
						+ getIntegrator().epoch, this.section,
						this.systemRef.descriptions,
						this.section.poincareStartBody,
						this.section.poincareStartPoint));
			}
			if (this.section.poincareEndPoint != null) {
				r.add(new AproximationEvent(this.section.poincareEndVector[0]
						+ getIntegrator().epoch, this.section,
						this.systemRef.descriptions,
						this.section.poincareEndBody,
						this.section.poincareEndPoint));
			}

			double[] flyBy = new double[this.section.integrator.bodies];

			Newton n = new Newton(this.section.integrator.M);
			n.setPosition(this.section.poincareStartVector[0],
					this.section.poincareStartPoint);
			try {
				n.checkFlyBy(this.flyByCriterion,
						this.section.poincareEndVector[0]
								- this.section.poincareStartVector[0], flyBy);

				Poincare passBy = null;
				for (int i = 1; i < this.section.integrator.bodies; i++) {
					if (i != this.section.poincareStartBody
							&& !Double.isNaN(flyBy[i])) {
						if (passBy == null) {
							passBy = new Poincare(this.section.integrator.M);
							passBy.copyState(this.section, passBy);
						}
						passBy.poincareEndBody = i;
						passBy.poincareMinTime = flyBy[i] - 10D;
						passBy.poincareMaxTime = flyBy[i] + 10D;
						if (passBy.poincareMap() == 0) {
							r.add(new AproximationEvent(
									passBy.poincareEndVector[0]
											+ passBy.integrator.epoch,
									this.section, this.systemRef.descriptions,
									passBy.poincareEndBody,
									passBy.poincareEndPoint));
						} else {
							System.err
									.println("Error finding pass-by event for body: "
											+ i);
						}

					}
				}
				this.events = r;
			} catch (Exception e) {
				this.events = Collections.emptyList();
				e.printStackTrace();
			}
		}
		return this.events;
	}

	public double loadEpoch(double jd) throws Exception {
		double vcdm[] = new double[3];
		double cdm[] = new double[3];
		int i, j;

		getIntegrator().epoch = this.systemRef.loadEpoch(jd,
				getIntegrator().epochPosition);
		this.section.epoch = getIntegrator().epoch;
		getIntegrator().pos_cdm(getIntegrator().epochPosition, cdm);
		getIntegrator().vitesse_cdm(getIntegrator().epochPosition, vcdm);

		for (i = 0; i < this.systemRef.planetId.length; i++) {
			for (j = 0; j < 3; j++) {
				getIntegrator().epochPosition[i * 3 + j] -= cdm[j];
				getIntegrator().epochPosition[getIntegrator().firstSpeedPos + i
						* 3 + j] -= vcdm[j];
			}
		}
		return getIntegrator().epoch;
	}

	public double startDateTime() {
		double val = Double.MAX_VALUE;
		for (int i = 0; i < this.events.size(); i++) {
			double d = this.events.get(i).getDate();
			if (d < val) {
				val = d;
			}
		}
		return val;
	}

	public double endDateTime() {
		double val = Double.MIN_VALUE;
		for (int i = 0; i < this.events.size(); i++) {
			double d = this.events.get(i).getDate();
			if (d > val) {
				val = d;
			}
		}
		return val;
	}

	@Override
	@Action
	@newtonpath.kepler.Funcions.Operation("Flyby")
	public int distanceApproximation() throws Exception {
		int v = super.distanceApproximation();
		if (v != 0) {
			throw new OperationException(v);
		}
		return v;
	}

	@Action
	@newtonpath.kepler.Funcions.Operation("Normalize endpoint base")
	public void normalizeEndPointBase() throws Exception {
		this.section.normalizeEndPointHorizontalBase();
		this.section.poincareMap();
		this.section.toStartPosition();
	}
}
