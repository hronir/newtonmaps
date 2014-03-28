package newtonpath.statemanager;

public interface Observable {

	public String getDescription();

	public double getDoubleValue(Object _o);

	public String getStringValue(Object _o);

	public Object getValue(Object _o);

	public Class<?> getValueType();

	public Parameter getParameter();
}
