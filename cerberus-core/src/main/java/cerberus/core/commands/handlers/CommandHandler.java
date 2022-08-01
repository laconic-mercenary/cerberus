package cerberus.core.commands.handlers;

import cerberus.core.commands.Command;
import cerberus.core.commands.handlers.impl.MotionCommandHandler;
import cerberus.core.commands.handlers.impl.PingCommandHandler;

public interface CommandHandler<T extends Command> {

	public static final class Factory {

		public static CommandHandler<? extends Command> makePingHandler() {
			return new PingCommandHandler();
		}

		public static CommandHandler<? extends Command> makeMotionHandler() {
			return new MotionCommandHandler();
		}

	}

	void handle(T command);

}
