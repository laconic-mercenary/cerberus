package cerberus.core.commands;

import static com.frontier.lib.validation.ObjectValidator.raiseIfNull;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import cerberus.core.commands.impl.processors.HttpCommandProcessor;

public interface CommandProcessor<T> {

	public static class Factory {
		public static CommandProcessor<HttpServletRequest> make(
				HttpServletRequest request, SenderInformation sender) {
			raiseIfNull(request);
			return new HttpCommandProcessor(request, sender);
		}
	}

	public T getParameters();

	public List<Command> getCommands();

}
