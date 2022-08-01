package cerberus.core.commands;

import static com.frontier.lib.validation.ObjectValidator.raiseIfNull;
import cerberus.core.commands.impl.MotionCommand;
import cerberus.core.commands.impl.PingCommand;

public interface Command {

	public static class Factory {
		static public Command makePing(SenderInformation sender) {
			raiseIfNull(sender);
			return new PingCommand(sender);
		}

		static public Command makeMotion(SenderInformation sender) {
			raiseIfNull(sender);
			return new MotionCommand(sender);
		}
	}

	public Type getCommandType();

	public SenderInformation getSender();

	public void setSender(SenderInformation sender);

	public enum Type {
		PING, MOTION
	}
}
