package newtonpath.tasks;

import newtonpath.kepler.ErrorCode;
import newtonpath.ui.AproxObs;
import newtonpath.ui.OperationException;

public class BatchException extends OperationException {
	private final String fileName;
	private final AproxObs orbit;

	public BatchException(int errorNumber, String fileName,AproxObs incompleteOrbitResult, Throwable cause) {
		super(ErrorCode.errorDescription(errorNumber) + " at " + fileName,
				errorNumber, cause);
		orbit=incompleteOrbitResult;
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
	
	public AproxObs getOrbit() {
		return orbit;
	}
}
