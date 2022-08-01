package cerberus.app.pings;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

import cerberus.app.images.ImageNotificationGateway;
import cerberus.core.eventbus.CerberusEventBus;
import cerberus.core.persistence.entities.MotionInfo;
import cerberus.core.persistence.entities.PingInfo;

import com.google.common.eventbus.Subscribe;

//eager=true means 'bake' this bean right away
//it appears that it only works for app-scoped beans
//and not session-scoped, the reason we do this is 
// so that the CerberusEventBus has something in memory to post to
// if we tried to post to a session bean, it would receive it only 
// if it got baked (i.e., user logged in). Otherwise nothing gets it.
// app scoped beans persist between sessions.

/**
 * this will be the common gateway for 'commands' received from the monitor
 * clients
 */
@ManagedBean(eager = true)
@ApplicationScoped
public class CommandNotificationGateway implements Serializable {

	private static final long serialVersionUID = 7867515034290212393L;

	private static final Logger LOGGER = Logger
			.getLogger(CommandNotificationGateway.class);

	private static final String MSG_MOTION_RX = "Motion information received from event bus";

	// cannot have 2 websocket endpoints on the same console?
	private static final String PING_SOCKET_EP = ImageNotificationGateway.WEB_SOCKET_EP;

	private static final String PING = "ping";

	// WARNING: this delimeter must sync up with the client side
	// it is expected to know this when this push is received by the browser
	private static final String DELIMETER = "|";

	// this will just be in the thing I send out so that the
	// client knows to differentiate it from other pushes
	private static final String MOTION = "motion";

	@PostConstruct
	public void initialize() {
		LOGGER.info("Registering with event bus");
		CerberusEventBus.get().register(this);
	}

	@PreDestroy
	public void shutdown() {
		LOGGER.info("Unregistering with event bus");
		CerberusEventBus.get().unregister(this);
	}

	@Subscribe
	public void motionReceived(MotionInfo motionInfo) {
		LOGGER.info(MSG_MOTION_RX);

		// at this point, the information should be validated
		// ok to send
		String toSend = makeSendableString(Arrays.asList(MOTION,
				motionInfo.getMachineName(), motionInfo.getMachineAddress()));

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Data to push to client is: " + toSend);

		EventBus bus = EventBusFactory.getDefault().eventBus();

		// in this case, I'm pushing a delimited string to the client
		// javascript - which is expected to parse this information
		// and handle it accordingly
		bus.publish(PING_SOCKET_EP, toSend);
	}

	@Subscribe
	public synchronized void pingReceived(PingInfo pingInfo) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Ping received from event bus");

		EventBus bus = EventBusFactory.getDefault().eventBus();
		bus.publish(PING_SOCKET_EP, PING);
	}

	public String getPingSocketEp() {
		return PING_SOCKET_EP;
	}

	private static String makeSendableString(List<String> values) {
		StringBuilder sb = new StringBuilder();
		for (String value : values) {
			sb.append(value);
			sb.append(DELIMETER);
		}

		if (sb.toString().endsWith(DELIMETER))
			sb.deleteCharAt(sb.lastIndexOf(DELIMETER));

		return sb.toString();
	}
}
