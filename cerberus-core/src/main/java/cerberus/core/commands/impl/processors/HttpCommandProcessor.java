package cerberus.core.commands.impl.processors;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import cerberus.core.commands.Command;
import cerberus.core.commands.CommandProcessor;
import cerberus.core.commands.SenderInformation;

public class HttpCommandProcessor implements
		CommandProcessor<HttpServletRequest> {

	private static final Logger LOGGER = Logger
			.getLogger(HttpCommandProcessor.class);

	private final static String KEY_PING_CMD = "ping";

	private final static String KEY_MOTION_CMD = "motion";

	private static final String MSG_CMD_RX_FMT = "'%s' command was received and will be handled.";

	private HttpServletRequest parameters = null;
	private SenderInformation sender = null;

	public HttpCommandProcessor(HttpServletRequest request,
			SenderInformation sender) {
		this.parameters = request;
		this.sender = sender;
	}

	@Override
	public HttpServletRequest getParameters() {
		return parameters;
	}

	@Override
	public List<Command> getCommands() {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Getting commands received from " + sender.toString());

		List<Command> cmds = new LinkedList<>();
		Map<String, String[]> map = getParameters().getParameterMap();

		parse(map, cmds);

		return cmds;
	}

	private void parse(Map<String, String[]> map, List<Command> cmds) {
		// support multiple commands

		// PING
		if (map.containsKey(KEY_PING_CMD)) {
			LOGGER.info(String.format(MSG_CMD_RX_FMT, KEY_PING_CMD));
			// shouldn't need anything else for a ping command
			Command cmd = Command.Factory.makePing(sender);
			cmds.add(cmd);
		}

		// MOTION
		if (map.containsKey(KEY_MOTION_CMD)) {
			LOGGER.info(String.format(MSG_CMD_RX_FMT, KEY_MOTION_CMD));
			Command cmd = Command.Factory.makeMotion(sender);
			cmds.add(cmd);
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug(String.format("Fetched %d commands", cmds.size()));
	}
}
