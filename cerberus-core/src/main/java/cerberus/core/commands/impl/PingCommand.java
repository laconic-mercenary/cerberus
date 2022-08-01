package cerberus.core.commands.impl;

import cerberus.core.commands.Command;
import cerberus.core.commands.SenderInformation;

public class PingCommand implements Command {
	private SenderInformation senderInformation = null;

	public PingCommand(SenderInformation sender) {
		this.senderInformation = sender;
	}

	@Override
	public Type getCommandType() {
		return Type.PING;
	}

	@Override
	public SenderInformation getSender() {
		return this.senderInformation;
	}

	@Override
	public void setSender(SenderInformation sender) {
		this.senderInformation = sender;
	}
}
