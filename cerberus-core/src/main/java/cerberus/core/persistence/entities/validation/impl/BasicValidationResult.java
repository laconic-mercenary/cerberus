package cerberus.core.persistence.entities.validation.impl;

import cerberus.core.persistence.entities.validation.ValidationMessage;
import cerberus.core.persistence.entities.validation.ValidationResult;

public class BasicValidationResult implements ValidationResult {

	private boolean successful = false;

	private ValidationMessage message = null;

	public BasicValidationResult() {
	}

	public BasicValidationResult(boolean successful) {
		this.successful = successful;
	}

	public void makeSuccessful() {
		successful = true;
	}

	@Override
	public boolean isSuccessful() {
		return successful;
	}

	@Override
	public ValidationMessage getMessage() {
		return message;
	}

	public void setMessage(String message, boolean isForUser) {
		this.message = new BasicValidationMessage(message, isForUser);
	}
}
