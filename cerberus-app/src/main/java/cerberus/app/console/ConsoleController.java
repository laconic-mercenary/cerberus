package cerberus.app.console;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import cerberus.core.persistence.KvpProperties;
import cerberus.core.persistence.entities.PingInfo;
import cerberus.core.persistence.util.Keys;

/* 
 * The purpose of a controller is to hold UI events. The session scoped
 * beans are meant to be re-used, but Controllers can be UI specific and hold
 * things such as information on how to post messages to the UI
 * */

@Named
@ViewScoped
public class ConsoleController implements Serializable {

	private static final long serialVersionUID = -1818465805523216975L;

	private static final Logger LOGGER = Logger
			.getLogger(ConsoleController.class);

	/**
	 * port that the monitor client is listening on to receive image requests
	 */
	private static final Integer DEFAULT_MONITOR_PORT = 8888;

	private static final String KEY_MONITOR_CLIENT_LISTENING_PORT = Keys.make(
			"cerberus.app", "monitor_client_listing_port");

	private static final String KEY_MONITOR_CLIENT_ADDRESS_FORMAT = Keys.make(
			"cerberus.app", "monitor_client_address_format");

	private static final String MSG_PING_RX = "A ping has been received from a monitor client.";

	private static final String MSG_FAILED_TO_SEND = "Failed to send request to %s : %d";

	private static final String DEFAULT_CLIENT_ADDR_FMT = "http://%s:%d/";

	@PostConstruct
	public void initialize() {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Initializing");

		// the monitor client port is configurable
		this.monitorPort = DEFAULT_MONITOR_PORT.intValue();
		String listeningPortStr = KvpProperties.findProperty(
				KEY_MONITOR_CLIENT_LISTENING_PORT,
				DEFAULT_MONITOR_PORT.toString());

		try {
			this.monitorPort = Integer.valueOf(listeningPortStr);
			LOGGER.info(String.format("Expecting MONITOR CLIENT PORT of %d",
					this.monitorPort));
		} catch (NumberFormatException e) {
			LOGGER.warn(String
					.format("Monitor client port was not in a valid format: %s. Using default %d.",
							listeningPortStr, DEFAULT_MONITOR_PORT.intValue()));
			this.monitorPort = DEFAULT_MONITOR_PORT.intValue();
		}

		// get the format of the monitor client url
		this.monitorAddrFmt = KvpProperties.findProperty(
				KEY_MONITOR_CLIENT_ADDRESS_FORMAT, DEFAULT_CLIENT_ADDR_FMT);
		LOGGER.info(String.format("MONITOR CLIENT address format will be %s",
				this.monitorAddrFmt));
	}

	@Inject
	private ImageRequestBean imageRequestBean = null;

	@Inject
	private PingTrackerBean pingTrackerBean = null;

	private int monitorPort = DEFAULT_MONITOR_PORT.intValue();

	private String monitorAddrFmt = DEFAULT_CLIENT_ADDR_FMT;

	public ImageRequestBean getImageRequestBean() {
		return imageRequestBean;
	}

	public void setImageRequestBean(ImageRequestBean imageRequestBean) {
		this.imageRequestBean = imageRequestBean;
	}

	public PingTrackerBean getPingTrackerBean() {
		return pingTrackerBean;
	}

	public void setPingTrackerBean(PingTrackerBean pingTrackerBean) {
		this.pingTrackerBean = pingTrackerBean;
	}

	//
	// Actions

	// when the request button is clicked
	public void requestImagesAction(PingInfo pingInfo) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug(String.format("Sending image request action %s : %d",
					pingInfo.getAddress(), this.monitorPort));

		try {
			getImageRequestBean().sendImageRequestAsync(
					String.format(this.monitorAddrFmt, pingInfo.getAddress(),
							this.monitorPort));
		} catch (Exception e) {
			// this may be a string format issue
			// or an async send issue
			final String msg = String.format(MSG_FAILED_TO_SEND,
					pingInfo.getAddress(), this.monitorPort);
			LOGGER.error(msg);
			postUserMessage(msg, FacesMessage.SEVERITY_ERROR);
		}
	}

	public void notifyUserPingRx() {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("notifyUserPingRx() invoked");

		postUserMessage(MSG_PING_RX, FacesMessage.SEVERITY_INFO);
	}

	private static final void postUserMessage(String message,
			FacesMessage.Severity severity) {
		// null posts to the growl (global)
		FacesContext.getCurrentInstance().addMessage(
				null,
				new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Console Notification", message));
	}
}
