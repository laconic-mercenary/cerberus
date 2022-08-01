package cerberus.core.commands.handlers;

import java.util.List;
import java.util.Map;

import cerberus.core.commands.Command;
import cerberus.core.commands.handlers.impl.DefaultCommandHandlerService;

public interface CommandHandlerService {

	public static final class Factory {

		public static CommandHandlerService make() {
			CommandHandlerService service = new DefaultCommandHandlerService();
			service.setDatabase(DefaultCommandHandlerService.database());
			return service;
		}

	}

	void setDatabase(
			Map<Command.Type, List<CommandHandler<? extends Command>>> database);

	// this should use the map specified in setDatabase()
	List<CommandHandler<? extends Command>> findByType(Command.Type commandType);

}
