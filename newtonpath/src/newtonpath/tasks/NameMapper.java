package newtonpath.tasks;

import newtonpath.statemanager.OperationResult;

public class NameMapper {
	private final String extension;

	public NameMapper(String extension) {
		super();
		this.extension = extension;
	}

	public String getOutputName(String inputName, OperationResult o) {
		String baseName = inputName.replaceAll("\\.xml$", "");
		return baseName + this.extension;
	}
}
