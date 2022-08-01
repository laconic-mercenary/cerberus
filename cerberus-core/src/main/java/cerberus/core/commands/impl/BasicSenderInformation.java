package cerberus.core.commands.impl;

import cerberus.core.commands.SenderInformation;

public class BasicSenderInformation implements SenderInformation {

	private String name = null;

	private String address = null;

	public BasicSenderInformation(String name, String address) {
		this.name = name;
		this.address = address;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getAddress() {
		return this.address;
	}

	@Override
	public String toString() {
		return String.format("%s - %s", getName(), getAddress());
	}
}
