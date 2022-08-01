package cerberus.app;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import cerberus.core.eventbus.CerberusEventBus;

import com.google.common.eventbus.Subscribe;

/**
 * The entry and exit point for the cerberus application
 */
public class CerberusMain implements ServletContextListener {

	private static final Logger LOGGER = Logger.getLogger(CerberusMain.class);

	/**
	 * Fires when the application is undeployed
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		LOGGER.info("Shutting down event bus");

		CerberusEventBus.get().dispose();

		LOGGER.info("CERBERUS has been shutdown");
	}

	/**
	 * This should be the first thing that fires when this application is
	 * deployed.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		LOGGER.info("CERBERUS starting...");

		LOGGER.info("Registering with event bus");
		CerberusEventBus.get().register(this);
		LOGGER.info("Performing bus test...");
		CerberusEventBus.get().post(Boolean.TRUE);
	}

	@Subscribe
	public void rxTest(Boolean testFlag) {
		LOGGER.info("Received test event bus post. TestSuccessful="
				+ testFlag.booleanValue());
	}
}
