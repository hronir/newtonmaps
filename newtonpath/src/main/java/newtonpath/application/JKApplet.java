package newtonpath.application;

import java.io.InputStream;
import java.net.URL;

import javax.swing.JApplet;


import newtonpath.logging.KLogger;
import newtonpath.ui.ConfigurationManager;

import org.xml.sax.InputSource;

/**
 * 
 * @author oriol
 */
public class JKApplet extends JApplet {
	private static final KLogger LOGGER = KLogger.getLogger(JKApplet.class);

	private JKApplication application;

	private String getParameter(String name, String defaultValue) {
		String val = getParameter(name);
		if (val == null) {
			val = defaultValue;
		}
		return val;
	}

	@Override
	public void init() {
		super.init();
		URL url = null;

		String colorsFile = getParameter("colors", null);
		if (colorsFile != null && colorsFile.trim().length() > 0) {
			try {
				url = getDocumentBase().toURI().resolve(colorsFile).toURL();
				InputStream characterStream = url.openConnection()
						.getInputStream();
				ConfigurationManager
						.loadColors(new InputSource(characterStream));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		LOGGER.info(" Start application instance...");
		this.application = new JKApplication();
		LOGGER.info(" ...done starting application instance.");

		LOGGER.info("Building interface...");
		this.application.buildInterface(false, this);
		LOGGER.info(" ...done building interface");

		final String layoutFile = getParameter("layout", null);
		if (layoutFile != null && layoutFile.trim().length() > 0) {
			try {
				url = getDocumentBase().toURI().resolve(layoutFile).toURL();
				InputStream characterStream = url.openConnection()
						.getInputStream();
				this.application.loadLayout(characterStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void start() {
		super.start();
		URL url = null;
		try {
			final String orbitsFile = getParameter("orbits", null);
			if (orbitsFile != null && orbitsFile.trim().length() > 0) {
				url = getDocumentBase().toURI().resolve(orbitsFile).toURL();
				this.application.loadOrbits(url);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
