package cerberus.core.commands.frontdoor;

import javax.servlet.http.HttpServletRequest;

import cerberus.core.commands.frontdoor.impl.RequestCommandReceptionHandler;

public interface CommandReceptionHandler<RX> {
	
	public static final class Factory {

		public static CommandReceptionHandler<HttpServletRequest> make() {
			return new RequestCommandReceptionHandler();
		}

	}

	void handle(RX rx);
}
