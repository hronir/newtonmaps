package newtonpath.ui;

import newtonpath.kepler.ErrorCode;

public class OperationException extends Exception {
	private final int errorNumber;

	public OperationException(int errorNumber) {
		this(errorNumber, null);
	}

	public OperationException(int errorNumber, Throwable cause) {
		this("Operation Exception: " + ErrorCode.errorDescription(errorNumber),
				errorNumber, cause);
	}

	public OperationException(String description, int errorNumber,
			Throwable cause) {
		super(description, cause);
		this.errorNumber = errorNumber;
	}

	public int getErrorNumber() {
		return errorNumber;
	}

}
