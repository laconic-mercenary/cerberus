package cerberus.app.images;

import org.apache.log4j.Logger;
import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.annotation.Singleton;
import org.primefaces.push.impl.JSONDecoder;
import org.primefaces.push.impl.JSONEncoder;

@PushEndpoint(ImageNotificationGateway.WEB_SOCKET_EP)
@Singleton
public class ImageSocketEndpoint {

	private static final Logger LOGGER = Logger
			.getLogger(ImageSocketEndpoint.class);

	@OnMessage(encoders = { JSONEncoder.class }, decoders = { JSONDecoder.class })
	public String messageRx(String message) {
		// I guess the socket will not work without a pushendpoint
		// being defined. It also appears that returning the value
		// is also important for it to work. This was quite a change
		// from PF 4.0

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("RX socket message");
		return message;
	}

}
