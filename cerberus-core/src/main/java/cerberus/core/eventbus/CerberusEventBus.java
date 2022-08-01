package cerberus.core.eventbus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.eventbus.AsyncEventBus;

/**
 * This will a a message bus for many of the cerberus components Of note is that
 * for a class to receive a message, I think it needs to be in memory, which is
 * why many of the subscribers are ApplicationScoped managed beans
 */
public class CerberusEventBus {

	public static final String ID = CerberusEventBus.class.getName();

	// probably don't need anything extravagant
	// small thread pool should do
	private static final int THREAD_POOL_SIZE = 4;

	private static final int THREAD_POOL_MAX_SIZE = 8;

	private static final long KEEP_ALIVE = 10000L;

	private static final Logger LOGGER = Logger
			.getLogger(CerberusEventBus.class);

	private static CerberusEventBus singleton = null;

	private AsyncEventBus bus = null;

	private Map<String, Object> subscribers = null;

	public synchronized static CerberusEventBus get() {
		if (singleton == null) {
			singleton = new CerberusEventBus();
			singleton.configure();
			singleton.subscribers = new HashMap<>();
			LOGGER.info("Configured and ready");
		}
		return singleton;
	}

	public synchronized void post(Serializable message) {
		bus.post(message);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Posted message: " + message.getClass().getName());
		}
	}

	public synchronized void register(Object object) {
		if (object != null) {
			bus.register(object);
			subscribers.put(object.getClass().getName(), object);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Successfully subscribed: "
						+ object.getClass().getName());
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Object passed to register() was NULL");
			}
		}
	}

	public void unregister(Object object) {
		if (object != null) {
			final String key = object.getClass().getName();
			if (subscribers.containsKey(key)) {
				bus.unregister(object);
				subscribers.remove(key);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Successfully unregistered: "
							+ object.getClass().getName());
				}
			} else {
				LOGGER.warn("Attempt made to unregister an non-registered object: "
						+ object.getClass().getName());
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Object passed to unregister() was NULL");
			}
		}
	}

	public void dispose() {
		synchronized (subscribers) {
			List<String> keys = new ArrayList<>(subscribers.keySet().size());
			keys.addAll(subscribers.keySet());
			for (String key : keys) {
				
				// in using this kind of for loop, you CANNOT
				// modify the map while in the loop
				// hence copying over to a list first...
				
				unregister(subscribers.get(key));
			}
		}
		LOGGER.info("disposed");
	}

	private synchronized void configure() {
		ExecutorService service = new ThreadPoolExecutor(
		/* corePoolSize */THREAD_POOL_SIZE,
		/* maximumPoolSize */THREAD_POOL_MAX_SIZE,
		/* keepAliveTime */KEEP_ALIVE, TimeUnit.MILLISECONDS,
		/* workQueue */new LinkedBlockingQueue<Runnable>(),
		/* handler */new ThreadPoolExecutor.DiscardPolicy());
		bus = new AsyncEventBus(ID, service);
	}
}
