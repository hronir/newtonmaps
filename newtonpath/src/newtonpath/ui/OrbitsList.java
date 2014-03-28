package newtonpath.ui;

import java.awt.Color;
import java.awt.Component;
import java.io.ObjectStreamException;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JScrollPane;

import newtonpath.application.JKApplication;
import newtonpath.ui.widget.AbstractColorSchema;


public class OrbitsList extends JScrollPane implements ConfigurableComponent {

	public static class ColorScheme extends AbstractColorSchema {
		public Color background = new Color(0xe0e0e0);
		public Color border = new Color(0x333333);
	}

	public final static ColorScheme colors = new ColorScheme();

	public OrbitsList(JKActions<?> actionPerformer) {
		setBorder(BorderFactory.createLineBorder(colors.border, 1));
		final JList theList = actionPerformer.getNewHistoryList();
		theList.setBackground(colors.background);
		setViewportView(theList);
	}

	public ComponentConfiguration getReplacement() throws ObjectStreamException {
		return new OrbitsListConfiguration();
	}

	public static class OrbitsListConfiguration implements
			ComponentConfiguration {
		public Component getComponent(JKApplication context)
				throws ObjectStreamException {
			final OrbitsList orbitsList = new OrbitsList(context
					.getActionPerformer());
			return orbitsList;
		}
	}
}
