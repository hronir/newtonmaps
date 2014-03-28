package newtonpath.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class KLogger {
	private static final KLogger INSTANCE = new KLogger();
	private static final Logger DEFAULT_LOGGER;
	protected static DateFormat dateFormatter = new SimpleDateFormat(
			"yyyyMMdd HH:mm:ss.SSS");
	static {
		Formatter formatter = new Formatter() {
			@Override
			public synchronized String format(LogRecord record) {

				return dateFormatter.format(new Date(record.getMillis())) + " "
						+ record.getThreadID()
						+ record.getLevel().getLocalizedName() + " ["
						+ record.getMessage() + "] "
						+ record.getSourceClassName() + "."
						+ record.getSourceMethodName() + "\n";
			}
		};

		DEFAULT_LOGGER = Logger.getAnonymousLogger();
		StreamHandler h = new StreamHandler(System.out, formatter);
		DEFAULT_LOGGER.addHandler(h);
		try {
			DEFAULT_LOGGER.setLevel(Level.ALL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static KLogger getLogger(Class<?> clazz) {
		return INSTANCE;
	}

	private KLogger() {

	}

	public void info(String obj) {
		LogRecord r = new LogRecord(Level.INFO, obj);
		r.setSourceClassName(Thread.currentThread().getStackTrace()[2]
				.getClassName());

		DEFAULT_LOGGER.log(r);
	}

	public void error(String obj) {
		LogRecord r = new LogRecord(Level.SEVERE, obj);
		r.setSourceClassName(Thread.currentThread().getStackTrace()[2]
				.getClassName());

		DEFAULT_LOGGER.log(r);
	}

	public void debug(String obj) {
		LogRecord r = new LogRecord(Level.FINEST, obj);
		r.setSourceClassName(Thread.currentThread().getStackTrace()[2]
				.getClassName());

		DEFAULT_LOGGER.log(r);
	}

}
