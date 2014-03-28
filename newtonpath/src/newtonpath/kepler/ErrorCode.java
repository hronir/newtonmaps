package newtonpath.kepler;

public enum ErrorCode {
	Ok(0), IterationOverFlow(13), GradientUnderFlow(14), InvalidModulus(10), StepUnderflow(
			11), SectionDomain(9), UnefinedSectionDifferential(3), SingularSectionEndpoint(
			2);

	private int errorNumber;

	ErrorCode(int errorNumber) {
		this.errorNumber = errorNumber;
	}

	public int getErrorNumber() {
		return this.errorNumber;
	}

	public static ErrorCode getError(int number) {
		for (ErrorCode x : ErrorCode.values()) {
			if (x.errorNumber == number) {
				return x;
			}
		}
		return null;
	}

	public static String errorDescription(int errorNumber) {
		ErrorCode err = getError(errorNumber);
		if (err == null) {
			return "Undefined(" + errorNumber + ")";
		} else {
			return err.toString() + "(" + errorNumber + ")";
		}
	}
}
