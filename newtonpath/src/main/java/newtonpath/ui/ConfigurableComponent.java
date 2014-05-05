package newtonpath.ui;

import java.io.ObjectStreamException;

public interface ConfigurableComponent {

	public abstract ComponentConfiguration getReplacement() throws ObjectStreamException;

}