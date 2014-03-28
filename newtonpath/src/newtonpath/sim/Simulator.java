package newtonpath.sim;

public interface Simulator {

	public void initPos();

	public int oneStep() throws Exception;

	public int oneStep(double t) throws Exception;

	public double getTime();

	public void setTimeStep(double ts);

	public double getTimeStep();

	public void postProcess();

	public double getParamMaxStep();

	public void setParamMaxStep(double s);

	public void setEpochNow();

	public double getEpoch();

	public void setState(Object _o);

	public Simulator getCopy();

	public void timeIntegration(double _dtemps) throws Exception;

}
