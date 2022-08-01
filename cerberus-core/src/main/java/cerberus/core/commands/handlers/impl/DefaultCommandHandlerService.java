package cerberus.core.commands.handlers.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cerberus.core.commands.Command;
import cerberus.core.commands.Command.Type;
import cerberus.core.commands.handlers.CommandHandler;
import cerberus.core.commands.handlers.CommandHandlerService;

import com.frontier.lib.validation.ObjectValidator;

public class DefaultCommandHandlerService implements CommandHandlerService {

	private static final Logger LOGGER = Logger
			.getLogger(DefaultCommandHandlerService.class);

	private static final String MSG_DB_CREATED = "Total command handler entries in database: %d - (the actual number of handlers may be larger - map).";

	private Map<Command.Type, List<CommandHandler<? extends Command>>> database = null;

	@Override
	public void setDatabase(
			Map<Command.Type, List<CommandHandler<? extends Command>>> database) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Setting database");

		checkDatabase(database);
		this.database = database;
	}

	@Override
	public List<CommandHandler<? extends Command>> findByType(
			Command.Type commandType) {
		checkDatabase(this.database);
		return this.database.get(commandType);
	}

	public static Map<Command.Type, List<CommandHandler<? extends Command>>> database() {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Building database...");

		// later on, consider making this configurable?
		Map<Type, List<CommandHandler<? extends Command>>> db = new HashMap<>();

		List<CommandHandler<? extends Command>> pingList = new LinkedList<>();
		pingList.add(CommandHandler.Factory.makePingHandler());

		List<CommandHandler<? extends Command>> motionList = new LinkedList<>();
		motionList.add(CommandHandler.Factory.makeMotionHandler());

		db.put(Command.Type.PING, pingList);
		db.put(Command.Type.MOTION, motionList);

		LOGGER.info(String.format(MSG_DB_CREATED, db.size()));

		return db;
	}

	private static void checkDatabase(Map<?, ?> database) {
		ObjectValidator.raiseIfNull(database, "database");
		if (database.isEmpty()) {
			LOGGER.error("Nothing in the command handler database");
			throw new IllegalStateException(
					"Database must contain at least 1 entry.");
		}
	}
}
