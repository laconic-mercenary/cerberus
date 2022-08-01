package cerberus.core.commands.frontdoor.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import cerberus.core.commands.Command;
import cerberus.core.commands.CommandProcessor;
import cerberus.core.commands.SenderInformation;
import cerberus.core.commands.frontdoor.CommandReceptionHandler;
import cerberus.core.commands.gatekeeper.GateKeeper;
import cerberus.core.commands.handlers.CommandHandler;
import cerberus.core.commands.handlers.CommandHandlerService;

public class RequestCommandReceptionHandler implements
		CommandReceptionHandler<HttpServletRequest> {

	private static final Logger LOGGER = Logger
			.getLogger(RequestCommandReceptionHandler.class);

	private static final String HANDLE_MSG = "Request was received and will be handled...";

	@Override
	public void handle(HttpServletRequest request) {
		// this handles all command receptions
		LOGGER.info(HANDLE_MSG);

		GateKeeper<HttpServletRequest, SenderInformation> gk = GateKeeper.Factory
				.make(request);

		gk.processRx();

		if (gk.isRxProcessed()) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Request was processed.");

			if (gk.wasRxAccepted()) {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Request was accepted, getting commands...");

				SenderInformation info = gk.getResults();
				CommandProcessor<HttpServletRequest> processor = CommandProcessor.Factory
						.make(request, info);
				List<Command> commands = processor.getCommands();

				CommandHandlerService service = CommandHandlerService.Factory
						.make();

				if (commands.isEmpty()) {
					LOGGER.info("Received acceptable request, but no commands were found within it. Ignoring request.");
				} else {

					for (Command cmd : commands) {

						// in this case, the 'database' (map) of the service
						// should be set behind the scenes. as in, the following
						// operations already assume it's set.
						List<CommandHandler<? extends Command>> handlers = service
								.findByType(cmd.getCommandType());

						if (handlers.isEmpty()) {
							LOGGER.warn(String
									.format("Received a valid command (%s) - but no handlers were defined to handle it. ",
											cmd.getClass().getName()));
						} else {
							if (LOGGER.isDebugEnabled())
								LOGGER.debug(String
										.format("Found %d handlers for received command %s",
												handlers.size(), cmd.getClass()
														.getName()));

							for (CommandHandler handler : handlers) {
								if (LOGGER.isDebugEnabled())
									LOGGER.debug(String.format(
											"Handler %s to handle command %s",
											handler.getClass().getName(), cmd
													.getClass().getName()));

								// handle should be self-containing
								// particularly in terms of error handling
								handler.handle(cmd);
							}
						}
					}
				}

				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Finished handling request and commands.");
			} else {
				LOGGER.info("Request was rejected. Ignoring request.");
			}
		} else {
			LOGGER.warn("Request was not processed properly. Ignoring request.");
		}
	}
}
