package newtonpath.tasks;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import newtonpath.logging.KLogger;
import newtonpath.tasks.handler.ImageFileHandler;
import newtonpath.tasks.handler.ResultHandler;
import newtonpath.tasks.handler.XMLResultFileHandler;
import newtonpath.tasks.handler.XMLResultHandler;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class BatchRunner {
	private final static KLogger LOGGER = KLogger.getLogger(BatchRunner.class);

	private static enum Option {
		f {
			@Override
			public void apply(BatchRunner obj, List<String> options) {

			};
		};
		public abstract void apply(BatchRunner obj, List<String> options);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		BatchRunner instance = new BatchRunner();

		List<String> argList = new ArrayList<String>(Arrays.asList(args));
		while (argList.size() > 0) {
			String arg = argList.remove(0);

			if (arg.startsWith("-")) {
				for (int i = 1; i < arg.length(); i++) {
					Option.valueOf(arg.substring(i, i + 1)).apply(instance,
							argList);
				}
			}
		}

		if (args.length == 0) {
			instance.loadTasks(new InputSource(System.in), null);
		} else {
			for (String fileName : args) {
				instance.loadTasks(new InputSource(new FileReader(fileName)),
						fileName);
			}
		}

		instance.run();
	}

	private final List<Task> taskList = new ArrayList<Task>();

	private NameMapper nameMapper = new NameMapper(".res");

	public List<Task> getTaskList() {
		return this.taskList;
	}

	public void run() throws Exception {
		ExecutorService ex = Executors.newFixedThreadPool(Runtime.getRuntime()
				.availableProcessors());

		final CountDownLatch counter = new CountDownLatch(this.taskList.size());

		for (final Task calc : this.taskList) {
			LOGGER.info("Submit task " + calc.getReference());
			ex.submit(new Runnable() {
				public void run() {
					try {
						calc.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
					counter.countDown();
				}
			});
		}

		counter.await();
		System.exit(0);
	}

	public void loadTasks(InputSource inputSource, String baseFilename) {

		List<Task> result = new ArrayList<Task>();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Element documentElement = dBuilder.parse(inputSource)
					.getDocumentElement();
			if ("tasks".equals(documentElement.getNodeName())) {
				NodeList handlers = documentElement
						.getElementsByTagName("handler");
				String hType = null;
				Element handlerDescriptor = null;
				if (handlers.getLength() > 0) {
					handlerDescriptor = (Element) handlers.item(0);
					hType = handlerDescriptor.getAttribute("type");
				}

				ResultHandler rh = getHandler(hType, baseFilename,
						handlerDescriptor);

				NodeList l = documentElement.getElementsByTagName("task");
				for (int i = 0; i < l.getLength(); i++) {
					String reference = getReference(baseFilename, i);
					try {
						Element e = (Element) l.item(i);
						result.add(BatchComputation.loadBatchTask(e, reference,
								rh));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			this.taskList.addAll(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getReference(String baseFilename, int itemNumber) {
		String reference = null;
		if (baseFilename == null) {
			reference = "<INPUT>:" + (itemNumber + 1);
		} else {
			reference = baseFilename + ":" + (itemNumber + 1);
		}
		return reference;
	}

	public ResultHandler getHandler(String type, String baseFilename, Element el) {
		HandlerSelector x = HandlerSelector.xml;
		if (type != null) {
			try {
				x = HandlerSelector.valueOf(type);
			} catch (Exception e) {
				LOGGER.error("Handler \"" + type
						+ "\" not found. Fallback to default.");
			}
		}
		return x.getHandler(baseFilename, this.nameMapper, el);
	}

	public enum HandlerSelector {
		xml {
			@Override
			public ResultHandler getHandler(String baseFilename,
					NameMapper nameMapper, Element el) {
				ResultHandler rh;
				if (baseFilename == null) {
					rh = new XMLResultHandler();
				} else {
					rh = new XMLResultFileHandler(baseFilename, nameMapper);
				}
				return rh;
			}
		},
		png {
			@Override
			public ResultHandler getHandler(String baseFilename,
					NameMapper nameMapper, Element el) {
				String file = baseFilename;
				if (file == null) {
					file = "output";
				}
				return ImageFileHandler.loadImageFileHandler(baseFilename,
						nameMapper, el);
			}
		};
		public abstract ResultHandler getHandler(String baseFilename,
				NameMapper nameMapper, Element el);
	}
}
