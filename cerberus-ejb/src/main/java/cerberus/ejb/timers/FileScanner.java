package cerberus.ejb.timers;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Schedule;
import javax.ejb.Schedules;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import cerberus.ejb.async.AsyncFileProcessor;

@Singleton(description = "1 instance of a file scanner timed ejb")
public class FileScanner {

	private static final Logger LOGGER = Logger.getLogger(FileScanner.class);

	// everything that's injected (far as I know) needs getter/setter
	@Inject
	private AsyncFileProcessor fileProcessor = null;

	public AsyncFileProcessor getFileProcessor() {
		return fileProcessor;
	}

	public void setFileProcessor(AsyncFileProcessor fileProcessor) {
		this.fileProcessor = fileProcessor;
	}

	@PostConstruct
	public void initialize() {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Constructed");
	}

	@PreDestroy
	public void destroy() {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Destroyed");
	}

	// every 2 seconds
	@Schedules({ @Schedule(hour = "*", minute = "*", second = "*/2", persistent = false) })
	public void execute() {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Executing file scan...");

		// asynchronous invocations
		getFileProcessor().executeAsync();
	}
}
