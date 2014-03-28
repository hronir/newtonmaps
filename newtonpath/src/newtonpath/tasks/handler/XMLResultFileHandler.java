package newtonpath.tasks.handler;

import java.io.File;
import java.util.Arrays;

import newtonpath.logging.KLogger;
import newtonpath.statemanager.OperationResult;
import newtonpath.tasks.NameMapper;
import newtonpath.ui.AproxObs;
import newtonpath.ui.XMLMapper;

public class XMLResultFileHandler extends XMLResultHandler {
	private final static KLogger LOGGER = KLogger
			.getLogger(XMLResultFileHandler.class);

	private final NameMapper nameMapper;
	private final String inputFile;

	public XMLResultFileHandler(String inputFile, NameMapper nameMapper) {
		this.inputFile = inputFile;
		this.nameMapper = nameMapper;
	}

	@Override
	public void saveResult(OperationResult x) {
		File outputFile = new File(this.nameMapper.getOutputName(
				this.inputFile, x) + ".xml");
		synchronized (this) {
			LOGGER.info("Save orbit to " + outputFile.getAbsolutePath());
			LOGGER.info(Arrays.toString(((AproxObs) x.getResultObject()).section.poincareEndVector));
			XMLMapper.getInstance().appendOperationResult(outputFile, x);
		}
	}
}