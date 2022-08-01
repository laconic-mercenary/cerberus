package cerberus.core.commands.impl;

import cerberus.core.commands.Command;
import cerberus.core.commands.SenderInformation;

// as of now, this doesn't need additional info
public class MotionCommand implements Command {

	private SenderInformation sender = null;

	public MotionCommand(SenderInformation sender) {
		this.sender = sender;
	}

	@Override
	public Type getCommandType() {
		return Command.Type.MOTION;
	}

	@Override
	public SenderInformation getSender() {
		return this.sender;
	}

	@Override
	public void setSender(SenderInformation sender) {
		this.sender = sender;
	}
}
