package newtonpath.application;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyVetoException;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.RootPaneContainer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import newtonpath.kepler.Aprox;
import newtonpath.kepler.BodySystemRef;
import newtonpath.kepler.Funcions;
import newtonpath.kepler.eph.EphLoaderFile;
import newtonpath.logging.KLogger;
import newtonpath.sim.SimulatorEngine;
import newtonpath.statemanager.Operation;
import newtonpath.statemanager.OperationResult;
import newtonpath.ui.AproxObs;
import newtonpath.ui.Chrono;
import newtonpath.ui.ComponentConfiguration;
import newtonpath.ui.ConfigurableComponent;
import newtonpath.ui.ContentFactory;
import newtonpath.ui.JKActions;
import newtonpath.ui.JKOverview;
import newtonpath.ui.JKParameters;
import newtonpath.ui.JKParametersDialog;
import newtonpath.ui.JKPlot;
import newtonpath.ui.JKSnapShot;
import newtonpath.ui.JKTable;
import newtonpath.ui.JKTableSim;
import newtonpath.ui.JKView;
import newtonpath.ui.JKViewList;
import newtonpath.ui.NewtonSim;
import newtonpath.ui.OrbitsList;
import newtonpath.ui.XMLMapper;
import newtonpath.ui.graph.DataGraph;
import newtonpath.ui.splitter.JKMultiSplit;
import newtonpath.ui.splitter.JKMultiSplitGroup;
import newtonpath.ui.widget.AbstractColorSchema;
import newtonpath.ui.widget.AbstractSimpleAction;
import newtonpath.ui.widget.AbstractToggleAction;
import newtonpath.ui.widget.ActionOpenUniqueView;
import newtonpath.ui.widget.ActionOpenView;
import newtonpath.ui.widget.Console;
import newtonpath.ui.widget.JKInternalWindow;
import newtonpath.ui.widget.SimpleAction;
import newtonpath.ui.widget.orbit.LineRenderer;

import org.jdesktop.swingx.MultiSplitLayout.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 
 * @author oriol
 */
public class JKApplication {
	private static final KLogger LOGGER = KLogger
			.getLogger(JKApplication.class);

	public void setControlBoxVisible(boolean isControlBoxVisible) {
		this.splitPanels.setControlBoxVisible(isControlBoxVisible);
	}

	public boolean isControlBoxVisible() {
		return this.splitPanels.isControlBoxVisible();
	}

	public static class ColorScheme extends AbstractColorSchema {
		public Color splitter = new Color(0xFFFFFFFF);
		public Color panelBackground = new Color(0xFFcccccc);
	}

	public static ColorScheme colors = new ColorScheme();

	public static final Class<?>[] COLOR_SCHEMES = new Class<?>[] {
			DataGraph.class, JKSnapShot.class, JKPlot.class, OrbitsList.class,
			LineRenderer.class, JKApplication.class, JKOverview.class };
	protected DecimalFormat julianDateFormat = null;
	protected DateFormat calendarDateFormat = null;
	protected ChangeListener dateChange;

	protected Console console;

	protected JKViewList<AproxObs> viewList;
	protected SimulatorEngine<NewtonSim> simulator;
	protected final JKActions<AproxObs> actionPerformer;
	protected final JKMultiSplitGroup splitGroup;

	protected final BodySystemRef solarSystemSpacecraft;

	private static class SpeedRenderer extends JLabel implements
			ListCellRenderer {
		public SpeedRenderer() {
			setHorizontalAlignment(SwingConstants.RIGHT);
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setText(((Double) value).toString());
			return this;
		}
	}

	public JKApplication() {
		this.console = null;// new Console();
		LOGGER.info("Building solar system...");

		LOGGER.info("Building actions...");

		this.solarSystemSpacecraft = BodySystemRef.getSolarSystemSpacecraft();
		this.solarSystemSpacecraft.setEphLoader(new EphLoaderFile());

		this.actionOpenNewWindow = new ActionOpenNewViewMDI("New window",
				getViewContentFactory());
		this.actionOpenConsole = newActionOpenUV("Console",
				new ContentFactory() {

					public Container getContent() {
						return JKApplication.this.console;
					}
				});
		this.actionOpenProperties = newActionOpenV("Properties",
				new ContentFactory() {

					public Container getContent() {
						return new JKTable<AproxObs>(
								JKApplication.this.actionPerformer
										.getIntegrator(),
								JKApplication.this.viewList, null);
					}
				});
		this.actionOpenView = newActionOpenV("New view",
				getViewContentFactory());
		this.actionOpenInspector = newActionOpenV("Inspector",
				new ContentFactory() {

					public Container getContent() {
						return new JKTableSim<NewtonSim>(
								JKApplication.this.simulator.getInteg(),
								JKApplication.this.simulator,
								JKApplication.this.splitGroup);
					}
				});
		this.actionRemoveView = new AbstractSimpleAction("Remove pane") {

			public void actionPerformed(ActionEvent e) {
				JKApplication.this.splitGroup.startSelectAndRemove();
			};
		};

		this.actionOpenOrbitList = newActionOpenV("Orbits",
				new ContentFactory() {

					public Container getContent() {
						JScrollPane scroll = new OrbitsList(
								JKApplication.this.actionPerformer);
						return scroll;

					}
				});

		this.actionOpenOverview = newActionOpenV("Overview",
				new ContentFactory() {

					public Container getContent() {
						Container scroll = new JKOverview(
								JKApplication.this.actionPerformer
										.getModelHistory(),
								JKApplication.this.actionPerformer
										.getSelectionModelHistory());
						return scroll;

					}
				});

		this.julianDateFormat = new DecimalFormat();
		this.julianDateFormat.applyPattern("0.00");
		this.calendarDateFormat = new SimpleDateFormat("dd/MM/yyyy");

		LOGGER.info("Building simulator engine...");
		this.simulator = new SimulatorEngine<NewtonSim>();

		LOGGER.info("Building simulator view...");
		this.simulator.addView(new JKView<NewtonSim>() {
			private final StringBuffer sb = new StringBuffer();

			public void updateView(NewtonSim integrator) {
				double ad;
				ad = integrator.getAbsoluteTime();
				this.sb.setLength(0);
				this.sb.append(JKApplication.this.julianDateFormat
						.format(integrator.getAbsoluteTime()));
				JKApplication.this.lblDate.setText(this.sb.toString());
				JKApplication.this.lblDateCal
						.setText(JKApplication.this.calendarDateFormat
								.format(Funcions.calDate(ad)));
				JKApplication.this.txtAbsDate.getModel();
				JKApplication.this.txtAbsDate
						.removeChangeListener(JKApplication.this.dateChange);
				JKApplication.this.txtAbsDate.setValue(new Double(integrator
						.getTime()));
				JKApplication.this.txtAbsDate
						.addChangeListener(JKApplication.this.dateChange);
			}

			public void resetView(NewtonSim integrador) {
				updateView(integrador);
			}

			public void setParent(JKViewList<NewtonSim> parent) {
			}
		});

		LOGGER.info("Building executor...");
		this.actionPerformer = new JKActions<AproxObs>() {
			@Override
			public void selectionChanged(AproxObs x) {
				super.selectionChanged(x);
				NewtonSim xx = NewtonSim.getCopy(x);
				JKApplication.this.simulator.setSimulation(xx);
				JKApplication.this.viewList.resetView(x);
			}

			@Override
			public void stateChanged() {
				JKApplication.this.lblRunning.setVisible(isRunning());
			}

			@Override
			public AproxObs newIntegrator() {
				try {
					final AproxObs value = AproxObs
							.getInstance(JKApplication.this.solarSystemSpacecraft);
					return value;

				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		LOGGER.info("Building splitter...");
		this.splitGroup = new JKMultiSplitGroup();

		this.viewList = new JKViewList<AproxObs>();
		this.viewList.resetView(this.actionPerformer.getIntegrator());
		LOGGER.info("Done");
	}

	public ContentFactory getViewContentFactory() {
		return new ContentFactory() {

			public Container getContent() {
				return new JKPlot(JKApplication.this.simulator);
			}
		};
	}

	protected void addInternalFrame(JInternalFrame frame, boolean _max) {
		frame.setVisible(true);

		this.desktop.add(frame);
		try {
			frame.setSelected(true);
			if (_max) {
				frame.setMaximum(true);
			}
		} catch (java.beans.PropertyVetoException e) {
		}
	}

	protected void setSpeed(Double d) {
		this.simulator.setSpeed(d / 1000D);
	}

	private static AbstractButton sizeButton(AbstractButton b) {
		b.setMaximumSize(new Dimension(16, 16));
		return b;
	}

	protected void buildInterface(boolean _mdi, RootPaneContainer container) {

		this.desktop = new JDesktopPane();
		this.lblDateCal = new JTextField();
		this.lblDate = new JTextField();
		this.txtAbsDate = new JSpinner();
		JPanel toolBar = new JPanel();
		this.mbMenu = new JMenuBar();
		this.mnWindow = new JMenu();
		this.mnSim = new JMenu();

		this.desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

		if (_mdi) {
			container.getContentPane().add(this.desktop,
					java.awt.BorderLayout.CENTER);
		}

		toolBar.setBackground(colors.panelBackground);

		toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.X_AXIS));
		toolBar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

		toolBar.add(sizeButton(this.simulator.actionSimPlayForward
				.getNewIconButton()));
		toolBar.add(sizeButton(this.simulator.actionSimPlayPause
				.getNewIconButton()));
		toolBar.add(sizeButton(this.simulator.actionSimPlayBackwards
				.getNewIconButton()));
		toolBar.add(sizeButton(this.simulator.actionSimReset.getNewIconButton()));

		toolBar.add(Box.createHorizontalStrut(15));
		toolBar.add(new JLabel("Speed:"));
		toolBar.add(Box.createHorizontalStrut(8));

		JComboBox speed = new JComboBox(new Double[] { 1D, 2D, 3D, 4D, 5D, 10D,
				20D, 50D, 100D, 200D, 500D, 1000D });
		speed.setMaximumRowCount(20);
		speed.setEditable(false);
		speed.setSelectedItem(100D);
		speed.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setSpeed((Double) e.getItem());
				}
			}
		});
		speed.setRenderer(new SpeedRenderer());

		speed.setMaximumSize(new Dimension(65, 22));
		speed.setPreferredSize(speed.getMaximumSize());

		toolBar.add(speed);
		toolBar.add(Box.createHorizontalStrut(8));
		toolBar.add(new JLabel("days/s."));

		toolBar.add(Box.createHorizontalStrut(15));
		toolBar.add(new JLabel("Date:"));
		toolBar.add(Box.createHorizontalStrut(8));
		this.lblDateCal.setEditable(false);
		this.lblDateCal.setHorizontalAlignment(SwingConstants.CENTER);
		this.lblDateCal.setMaximumSize(new java.awt.Dimension(80, 25));
		this.lblDateCal.setOpaque(false);
		this.lblDateCal.setPreferredSize(new java.awt.Dimension(80, 19));
		this.lblDateCal.setToolTipText("UTC");
		toolBar.add(this.lblDateCal);

		toolBar.add(Box.createHorizontalStrut(2));
		this.lblDate.setEditable(false);
		this.lblDate.setHorizontalAlignment(SwingConstants.CENTER);
		this.lblDate.setMaximumSize(new java.awt.Dimension(90, 25));
		this.lblDate.setOpaque(false);
		this.lblDate.setPreferredSize(new java.awt.Dimension(60, 19));
		this.lblDate.setToolTipText("Julian Day");
		toolBar.add(this.lblDate);

		toolBar.add(Box.createHorizontalStrut(2));
		this.txtAbsDate.setMaximumSize(new java.awt.Dimension(60, 25));
		this.txtAbsDate.setPreferredSize(new java.awt.Dimension(30, 19));
		this.txtAbsDate.setToolTipText("Mission age");

		Double value = new Double(0);
		Double min = new Double(-10000D);
		Double max = new Double(10000D);
		Double step = new Double(1D);

		SpinnerNumberModel absDateModel = new SpinnerNumberModel(value, min,
				max, step);
		this.txtAbsDate.setModel(absDateModel);

		this.txtAbsDate.setEditor(new JSpinner.NumberEditor(this.txtAbsDate,
				"#,###,##0.00"));

		toolBar.add(this.txtAbsDate);

		toolBar.add(Box.createHorizontalStrut(15));

		this.lblRunning = new JLabel();
		this.lblRunning.setForeground(Color.red);
		this.lblRunning.setText("Running...");
		this.lblRunning.setVisible(false);
		toolBar.add(this.lblRunning);

		container.getContentPane().add(toolBar, java.awt.BorderLayout.NORTH);

		this.mnWindow.setText("View");
		if (_mdi && this.actionOpenNewWindow != null) {
			this.mnWindow.add(this.actionOpenNewWindow.getNewMenuItem());
		}
		this.mnWindow.add(this.actionOpenProperties.getNewMenuItem());
		if (this.actionOpenConsole != null) {
			this.mnWindow.add(this.actionOpenConsole.getNewMenuItem());
		}
		this.mnWindow.add(this.actionOpenOrbitList.getNewMenuItem());
		this.mnWindow.add(this.actionOpenOverview.getNewMenuItem());

		this.mnWindow.add(this.actionRemoveView.getNewMenuItem());
		if (_mdi) {
			if (this.actionRestartNormal != null) {
				this.mnWindow.add(this.actionRestartNormal.getNewMenuItem());
			}
		} else {
			if (this.actionRestartMDI != null) {
				this.mnWindow.add(this.actionRestartMDI.getNewMenuItem());
			}
		}

		this.mbMenu.add(this.mnWindow);

		this.mnSim.setText("Simulation");

		this.mnSim.add(this.simulator.actionSimPlayForward.getNewMenuItem());
		this.mnSim.add(this.simulator.actionSimPlayPause.getNewMenuItem());
		this.mnSim.add(this.simulator.actionSimPlayBackwards.getNewMenuItem());
		this.mnSim.add(this.simulator.actionSimReset.getNewMenuItem());
		this.mnSim.addSeparator();
		this.mnSim.add(this.actionOpenView.getNewMenuItem());
		this.mnSim.add(this.actionOpenInspector.getNewMenuItem());

		this.mbMenu.add(this.mnSim);

		JMenu mnOperations = new JMenu("Operations");
		for (Operation op : this.actionPerformer.getIntegrator()
				.getOperations()) {
			mnOperations.add(getOperationMenuItem(op));
		}
		this.mbMenu.add(mnOperations);

		this.dateChange = new ChangeListener() {

			public void stateChanged(ChangeEvent evt) {
				if (JKApplication.this.simulator != null) {
					double d = ((Double) JKApplication.this.txtAbsDate
							.getValue()).doubleValue();
					try {
						JKApplication.this.simulator.goTo(d);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};

		MouseWheelListener dateWheel = new java.awt.event.MouseWheelListener() {

			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
				try {
					JKApplication.this.simulator.goToDelta(-(double) evt
							.getWheelRotation());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		this.txtAbsDate.addMouseWheelListener(dateWheel);
		this.lblDate.addMouseWheelListener(dateWheel);
		this.lblDateCal.addMouseWheelListener(dateWheel);

		this.simulator.setSimulation(NewtonSim.getCopy(this.actionPerformer
				.getIntegrator()));
		this.simulator.resetSimulation(false);

		setSpeed((Double) speed.getSelectedItem());

		if (_mdi) {
			this.actionOpenView.open();
		} else {
			this.splitPanels = new JKMultiSplit(new JKPlot(this.simulator));
			this.splitPanels.setBackground(colors.splitter);
			container.setGlassPane(this.splitPanels.getGlassPane());
			this.splitPanels.getGlassPane().setVisible(true);
			this.splitGroup.add(this.splitPanels);
			container.getContentPane().add(this.splitPanels,
					java.awt.BorderLayout.CENTER);
			this.splitPanels.getGlassPane().hideControlBox();
		}

	}

	private JDesktopPane desktop;
	protected JSpinner txtAbsDate;
	protected JTextField lblDate;
	protected JTextField lblDateCal;
	protected JMenuBar mbMenu;
	private JMenu mnSim;
	private JMenu mnWindow;
	protected JLabel lblRunning;
	protected JKMultiSplit splitPanels;

	protected abstract class AbstractActionOpenNewView extends
			AbstractSimpleAction implements ActionOpenView {
		private final ContentFactory contentFactory;

		public AbstractActionOpenNewView(String name) {
			super(name);
			this.contentFactory = null;
		}

		public AbstractActionOpenNewView(String name,
				ContentFactory _contentFactory) {
			super(name);
			this.contentFactory = _contentFactory;
		}

		public void actionPerformed(ActionEvent e) {
			open();
		}

		public void open() {
			doOpen();
		}

		abstract protected void doOpen();

		public Container getContent() {
			return this.contentFactory.getContent();
		};
	}

	private class ActionOpenNewViewSplitted extends AbstractActionOpenNewView {
		public ActionOpenNewViewSplitted(String name,
				ContentFactory _contentFactory) {
			super(name, _contentFactory);
		}

		@Override
		protected void doOpen() {
			JKApplication.this.splitGroup.startSelectAndSplit(this);
		}
	}

	public class ActionOpenNewViewMDI extends AbstractActionOpenNewView {
		public ActionOpenNewViewMDI(String name, ContentFactory _contentFactory) {
			super(name, _contentFactory);
		}

		@Override
		protected void doOpen() {
			addInternalFrame(new JKInternalWindow(getContent()), false);
		}
	}

	private abstract class AbstractActionOpenUniqueView extends
			AbstractToggleAction implements ActionOpenUniqueView {
		public AbstractActionOpenUniqueView(String name) {
			super(name);
		}

		public void actionPerformed(java.awt.event.ActionEvent evt) {
			if (this.model.isSelected()) {
				doOpen();
			} else {
				doClose();
			}
		}

		public void open() {
			this.model.setSelected(true);
			doOpen();
		}

		public void close() {
			this.model.setSelected(false);
			doClose();
		}

		abstract protected void doClose();

		abstract protected void doOpen();
	}

	private class ActionOpenUniqueViewMDI extends AbstractActionOpenUniqueView {
		private JKInternalWindow simpleWindowRef;
		private final ContentFactory contentFactory;

		public ActionOpenUniqueViewMDI(String name,
				ContentFactory _contentFactory) {
			super(name);
			this.contentFactory = _contentFactory;
		}

		@Override
		protected void doOpen() {
			if (this.simpleWindowRef == null) {
				this.simpleWindowRef = new JKInternalWindow(getContent());
				this.simpleWindowRef
						.addInternalFrameListener(new InternalFrameAdapter() {
							@Override
							public void internalFrameClosed(InternalFrameEvent e) {
								doClose();
								if (ActionOpenUniqueViewMDI.this.model
										.isSelected()) {
									ActionOpenUniqueViewMDI.this.model
											.setSelected(false);
								}
							}
						});
				addInternalFrame(this.simpleWindowRef, false);
			}
		}

		@Override
		protected void doClose() {
			if (this.simpleWindowRef != null) {
				try {
					this.simpleWindowRef.setClosed(true);
				} catch (PropertyVetoException e) {
					;
				}
			}
			this.simpleWindowRef = null;
		}

		public Container getContent() {
			return this.contentFactory.getContent();
		};
	}

	private ActionOpenUniqueView newActionOpenUV(String _name, ContentFactory _c) {
		ActionOpenUniqueView retVal = null;
		retVal = new ActionOpenUniqueViewMDI(_name, _c);
		return retVal;
	}

	private ActionOpenView newActionOpenV(String _name, ContentFactory _c) {
		ActionOpenView retVal;
		retVal = new ActionOpenNewViewSplitted(_name, _c);
		return retVal;
	}

	private JMenuItem getOperationMenuItem(final Operation _op) {
		JMenuItem retVal = new JMenuItem(_op.toString());
		retVal.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Operation op = _op;
				Aprox o = JKApplication.this.actionPerformer.getIntegrator()
						.clone();
				JKParameters par = new JKParameters(o, op,
						JKApplication.this.actionPerformer, op.getParameters(o));
				JKParametersDialog d = new JKParametersDialog(par);
				d.setVisible(true);
			}
		});
		return retVal;
	}

	private final ActionOpenView actionOpenConsole;
	private final ActionOpenView actionOpenProperties;
	private final ActionOpenView actionOpenView;
	private final ActionOpenView actionOpenInspector;
	private final SimpleAction actionRemoveView;
	protected ActionOpenView actionOpenNewWindow = null;
	protected SimpleAction actionRestartMDI = null;
	protected SimpleAction actionRestartNormal = null;
	private final ActionOpenView actionOpenOrbitList;
	private final ActionOpenView actionOpenOverview;

	private final SimpleAction actionSaveOrbit = new AbstractSimpleAction(
			"Save orbit") {

		public void actionPerformed(ActionEvent e) {
			OperationResult res = JKApplication.this.actionPerformer
					.getSelectedResult();
			if (res != null) {
				try {
					XMLMapper.getInstance().printOperationResult(res);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}

		}
	};

	private final SimpleAction actionSaveLayout = new AbstractSimpleAction(
			"Save layout") {

		public void actionPerformed(ActionEvent ev) {

			try {
				OutputStream out = System.out;
				XMLEncoder e = new XMLEncoder(out);
				Node model = JKApplication.this.splitPanels
						.getMultiSplitLayout().getModel();
				e.writeObject(model);

				Map<String, Component> childMap = JKApplication.this.splitPanels
						.getMultiSplitLayout().getChildMap();
				Map<String, ComponentConfiguration> m = new HashMap<String, ComponentConfiguration>();
				for (String key : childMap.keySet()) {
					Component c = childMap.get(key);
					if (c instanceof ConfigurableComponent) {
						m.put(key, ((ConfigurableComponent) c).getReplacement());
					}
				}
				e.writeObject(m);
				e.flush();

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	private final SimpleAction actionSaveColors = new AbstractSimpleAction(
			"Save colors") {

		public void actionPerformed(ActionEvent ev) {
			try {
				Map<String, Map<String, Integer>> colorSchema = new HashMap<String, Map<String, Integer>>();
				for (Class<?> c : COLOR_SCHEMES) {
					AbstractColorSchema s = (AbstractColorSchema) c.getField(
							"colors").get(null);
					colorSchema.put(c.getName(), s.getSchema());
				}

				XMLMapper.getInstance().saveDocument(
						XMLMapper.getInstance().saveColors(colorSchema),
						System.out);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	};

	public SimpleAction getActionSaveLayout() {
		return this.actionSaveLayout;
	}

	SimpleAction getActionSaveOrbit() {
		return this.actionSaveOrbit;
	}

	public void loadOrbits(File f) {
		FileReader characterStream = null;
		try {
			characterStream = new FileReader(f);
		} catch (FileNotFoundException e1) {
			System.err.println("Unable to read file " + f.getAbsolutePath());
		}
		if (characterStream != null) {
			loadOrbits(new InputSource(characterStream));
		}
	}

	public void loadOrbits(URL f) {
		InputStream characterStream = null;
		try {
			characterStream = f.openConnection().getInputStream();
		} catch (Exception e1) {
			System.err.println("Unable to read URL " + f.toString());
		}
		if (characterStream != null) {
			loadOrbits(new InputSource(characterStream));
		}
	}

	public void loadOrbits(InputSource... inputSources) {
		Chrono c = new Chrono("loadOrbits");
		try {
			final List<Element> elemList = new ArrayList<Element>();

			JKApplication.this.lblRunning.setText("Loading...");
			JKApplication.this.lblRunning.setVisible(true);

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();

			for (InputSource is : inputSources) {
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				final NodeList l = dBuilder.parse(is).getDocumentElement()
						.getElementsByTagName("result");
				for (int i = 0; i < l.getLength(); i++) {
					Element element = (Element) l.item(i);
					elemList.add(element);
				}
			}

			final Element first = elemList.size() > 0 ? elemList.remove(0)
					: null;

			final Runnable loadRefreshEnd = new Runnable() {
				public void run() {
					JKApplication.this.lblRunning
							.setVisible(JKApplication.this.actionPerformer
									.isRunning());
					JKApplication.this.splitPanels.repaint();
				}
			};

			final Runnable loadingRefresh = new Runnable() {

				public void run() {
					JKApplication.this.actionPerformer.refreshOrbitList();
				}
			};

			Runnable loadRefreshStart = new Runnable() {

				public void run() {
					for (Element element : elemList) {
						final OperationResult r = XMLMapper.getInstance()
								.loadOperationResult(element);
						addOrbitToList(r);
						SwingUtilities.invokeLater(loadingRefresh);
					}
					SwingUtilities.invokeLater(loadRefreshEnd);
				}
			};

			if (first != null) {
				final OperationResult r = XMLMapper.getInstance()
						.loadOperationResult(first);
				addOrbitToList(r);
				SwingUtilities.invokeLater(loadingRefresh);
				JKApplication.this.actionPerformer.setSelection(0);
			}

			new Thread(loadRefreshStart).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
		c.report();
	}

	public void printLayout() {
		System.out.println(this.splitPanels.getMultiSplitLayout().getModel()
				.toString());
	}

	public void loadLayout(InputStream stream) {
		Chrono c = new Chrono("loadLayout");
		XMLDecoder d = new XMLDecoder(new BufferedInputStream(stream));
		Node model = (Node) d.readObject();
		Map<?, ?> m = (Map<?, ?>) d.readObject();
		d.close();

		this.splitPanels.removeAll();

		try {
			for (Entry<?, ?> e : m.entrySet()) {
				Component component = ((ComponentConfiguration) e.getValue())
						.getComponent(this);
				this.splitPanels.add(component, e.getKey());
			}
			this.splitPanels.getMultiSplitLayout().setModel(model);
		} catch (ObjectStreamException e) {
			e.printStackTrace();
		}
		c.report();
	}

	public JKActions<AproxObs> getActionPerformer() {
		return this.actionPerformer;
	}

	public SimulatorEngine<NewtonSim> getSimulator() {
		return this.simulator;
	}

	public SimpleAction getActionSaveColors() {
		return this.actionSaveColors;
	}

	protected void addOrbitToList(final OperationResult r) {
		if (SwingUtilities.isEventDispatchThread()) {
			JKApplication.this.actionPerformer.addOrbit(r);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					public void run() {
						JKApplication.this.actionPerformer.addOrbit(r);
					}
				});
			} catch (Exception e) {
				// Sorry: The calculated orbit can not be added to
				// the list.
				e.printStackTrace();
			}
		}
	}

	public JKViewList<AproxObs> getViewList() {
		return this.viewList;
	}

	public JKMultiSplitGroup getSplitGroup() {
		return this.splitGroup;
	}
}
