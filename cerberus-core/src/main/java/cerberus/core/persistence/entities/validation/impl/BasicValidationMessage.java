package cerberus.core.persistence.entities.validation.impl;

import cerberus.core.persistence.entities.validation.ValidationMessage;

public class BasicValidationMessage implements ValidationMessage {

	private String text = null;
	private boolean isForUser = false;

	public BasicValidationMessage(String message, boolean isForUser) {
		this.text = message;
		this.isForUser = isForUser;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public boolean isForUser() {
		return isForUser;
	}

}
