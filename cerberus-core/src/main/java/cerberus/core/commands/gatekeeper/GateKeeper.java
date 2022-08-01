package cerberus.core.commands.gatekeeper;

import javax.servlet.http.HttpServletRequest;

import cerberus.core.commands.SenderInformation;
import cerberus.core.commands.gatekeeper.impl.RequestGateKeeper;

public interface GateKeeper<RX, TX> {

	public static final class Factory {

		public static GateKeeper<HttpServletRequest, SenderInformation> make(
				HttpServletRequest request) {
			return new RequestGateKeeper(request);
		}

	}

	void processRx();

	boolean isRxProcessed();

	boolean wasRxAccepted();

	RX getRx();

	TX getResults();
}
