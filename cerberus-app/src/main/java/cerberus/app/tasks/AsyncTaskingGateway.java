package cerberus.app.tasks;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;

import cerberus.core.eventbus.CerberusEventBus;
import cerberus.core.tasks.AsyncTasking;

import com.google.common.eventbus.Subscribe;

/**
 * App scoped bean to help offset certain tasks that may take a while and do not
 * necessary need to be atomic with reference to other operations. Many of the
 * async taskings will likely come from the EJB that handles all the file
 * managers. Those file managers will be annotated with the AsyncEligible
 * annotation
 */
@ManagedBean(eager = true)
@ApplicationScoped
public class AsyncTaskingGateway implements Serializable {

	private static final long serialVersionUID = -9170650015445843344L;

	private static final Logger LOGGER = Logger
			.getLogger(AsyncTaskingGateway.class);

	private static final String MSG_TASK_FAILED_FMT = "Exception trapped when performing task '%s'";

	private static final String MSG_PERFORMING_FMT = "Starting asynchronous task: %s";

	private static final String MSG_FINISHED_FMT = "Finished asynchronous task: %s";

	private static final String MSG_PERFORMED_FMT = "Async task %s was posted but was already performed. Ignoring.";

	// maybe this is slighty faster than having a 'false' literal?
	// with optimizations - probably not - oh well
	private static final boolean NOT_PERFORMED = false;

	@PostConstruct
	public void initialize() {
		LOGGER.info(String.format("Registering %s to event bus", this
				.getClass().getName()));
		CerberusEventBus.get().register(this);
	}

	/**
	 * most of the taskings will likely be file manager-based tasks
	 */
	@Subscribe
	public void handleTasking(AsyncTasking tasking) {
		// first check if it was already performed
		// it's not a big deal if it was - sometimes this may be a design
		// decision
		if (NOT_PERFORMED == tasking.isPerformed()) {
			LOGGER.info(String.format(MSG_PERFORMING_FMT, tasking.getClass()
					.getName()));
			try {
				tasking.perform();
				LOGGER.info(String.format(MSG_FINISHED_FMT, tasking.getClass()
						.getName()));
			} catch (Exception e) {
				// need to catch the exception here as anything above this will
				// be container managed classes handling the exception
				LOGGER.error(String.format(MSG_TASK_FAILED_FMT, tasking
						.getClass().getName()));

				// dump to the server log
				e.printStackTrace();
			}
		} else {
			// torn between info/warn
			// maybe warn so that it's noticeable in case this was not by design
			LOGGER.warn(String.format(MSG_PERFORMED_FMT, tasking.getClass()
					.getName()));
		}
	}
}
