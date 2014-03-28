package newtonpath.ui.widget;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractColorSchema {

	public Map<String, Integer> getSchema() {
		HashMap<String, Integer> val = new HashMap<String, Integer>();
		for (Field f : this.getClass().getFields()) {
			if (f.getType().equals(Color.class)) {
				try {
					val.put(f.getName(), ((Color) f.get(this)).getRGB());
				} catch (Exception e) {
					System.err.println("Invalid field access for "
							+ this.getClass().getName() + "." + f.getName());
				}
			}
		}
		return val;
	}

	public void setSchema(Map<String, Integer> val) {
		for (Field f : this.getClass().getFields()) {
			if (f.getType().equals(Color.class)) {
				Integer col=null;
				col = val.get(f.getName());
				if (col != null) {
					try {
						f.set(this, new Color(col, true));
					} catch (Exception e) {
						System.err
								.println("Invalid field access for "
										+ this.getClass().getName() + "."
										+ f.getName());
					}
				}
			}
		}
	}
}
