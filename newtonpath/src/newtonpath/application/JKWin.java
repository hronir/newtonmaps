package newtonpath.application;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import newtonpath.ui.ConfigurationManager;
import newtonpath.ui.widget.AbstractSimpleAction;

import org.xml.sax.InputSource;

/**
 * 
 * @author oriol
 */
public class JKWin extends JFrame {
	private JKApplication application;

	public JKWin(boolean mdi) {
		super();

	}

	protected void startApplication(boolean mdi, InputSource colorsStream,
			InputStream layoutStream, InputSource[] orbitInputSource) {
		if (colorsStream != null) {
			ConfigurationManager.loadColors(colorsStream);
		}

		this.application = new JKApplication();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		this.application.actionRestartMDI = new AbstractSimpleAction(
				"Restart in MDI mode") {

			public void actionPerformed(ActionEvent e) {
				startNewSession(true, null);
				JKWin.this.dispose();
			};
		};
		this.application.actionRestartNormal = new AbstractSimpleAction(
				"Restart in normal mode") {

			public void actionPerformed(ActionEvent e) {
				startNewSession(false, null);
				JKWin.this.dispose();
			};
		};

		this.application.buildInterface(mdi, this);
		this.application.setControlBoxVisible(true);

		setJMenuBar(this.application.mbMenu);

		this.application.mbMenu.getMenu(0).add(
				this.application.getActionSaveOrbit());

		this.application.mbMenu.getMenu(0).add(
				this.application.getActionSaveLayout());

		this.application.mbMenu.getMenu(0).add(
				this.application.getActionSaveColors());

		setPreferredSize(new java.awt.Dimension(800, 600));

		if (layoutStream != null) {
			this.application.loadLayout(layoutStream);
		}

		pack();

		if (orbitInputSource != null) {
			this.application.loadOrbits(orbitInputSource);
		}
	}

	public static void main(String args[]) {
		startNewSession(false, args);
	}

	protected static void startNewSession(boolean _mdi, String[] orbitFileNames) {
		java.awt.EventQueue.invokeLater(new SessionStart(_mdi, orbitFileNames));
	}

	static class SessionStart implements Runnable {
		private static final String DEFAULT_LAYOUT_FILE = "data/layout.xml";
		private static final String DEFAULT_ORBITS_FILE = "data/orbits.xml";
		private static final String DEFAULT_COLORS_FILE = "data/colors.xml";
		private final boolean mdi;
		private final String[] orbitFileNames;

		public SessionStart(boolean _mdi, String[] orbitFileNames) {
			this.mdi = _mdi;
			this.orbitFileNames = orbitFileNames;
		}

		public void run() {
			JKWin winApp = new JKWin(this.mdi);

			if (System.getSecurityManager() == null) {
				startAppLocal(winApp, this.orbitFileNames);
			} else {
				startAppUrl(winApp);
			}

			winApp.setVisible(true);
		}

		private void startAppLocal(JKWin winApp, String[] files) {
			File f;
			InputSource colorsStream = null;
			f = new File(DEFAULT_COLORS_FILE);
			if (f.canRead()) {
				try {
					colorsStream = new InputSource(new FileReader(f));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}

			String[] names;

			if (files.length == 0) {
				names = new String[] { DEFAULT_ORBITS_FILE };
			} else {
				names = files;
			}

			List<InputSource> orbitInputSourceList = new ArrayList<InputSource>();
			InputSource[] orbitInputSource = null;

			for (String path : names) {
				f = new File(path);
				if (f.canRead()) {
					try {
						orbitInputSourceList.add(new InputSource(
								new FileReader(f)));
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
				}
			}
			if (orbitInputSourceList.size() > 0) {
				orbitInputSource = orbitInputSourceList
						.toArray(new InputSource[orbitInputSourceList.size()]);
			}

			FileInputStream layoutStream = null;
			f = new File(DEFAULT_LAYOUT_FILE);
			if (f.canRead()) {
				try {
					layoutStream = new FileInputStream(f);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}

			winApp.startApplication(this.mdi, colorsStream, layoutStream,
					orbitInputSource);
		}

		private void startAppUrl(JKWin winApp) {

			URL f;
			InputSource colorsStream = null;
			try {
				f = new URL(System.getProperty("javaws.newtonmaps.colors"));
				colorsStream = new InputSource(f.openConnection()
						.getInputStream());
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			InputSource orbitInputSource[] = null;
			try {
				f = new URL(System.getProperty("javaws.newtonmaps.orbits"));
				orbitInputSource = new InputSource[] { new InputSource(f
						.openConnection().getInputStream()) };
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			InputStream layoutStream = streamFromProperty("javaws.newtonmaps.layout");

			winApp.startApplication(this.mdi, colorsStream, layoutStream,
					orbitInputSource);
		}
	}

	protected static InputStream streamFromProperty(String propertyName) {
		InputStream layoutStream = null;
		String property = System.getProperty(propertyName);
		if (property != null) {
			try {
				URL f = new URL(property);
				layoutStream = f.openConnection().getInputStream();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return layoutStream;
	}

}
