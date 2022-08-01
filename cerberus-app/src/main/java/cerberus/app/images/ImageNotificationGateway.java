package cerberus.app.images;

import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

import cerberus.core.eventbus.CerberusEventBus;
import cerberus.core.files.impl.managers.ImageFileAvailable;

import com.google.common.eventbus.Subscribe;

// eager=true means 'bake' this bean right away
// it appears that it only works for app-scoped beans
// and not session-scoped

/**
 * This receives messages from the cerberus event bus and posts them to the
 * prime faces push web socket
 */
@ManagedBean(eager = true)
@ApplicationScoped
public class ImageNotificationGateway implements Serializable {

	public static final class ImageSendOffEvent implements Serializable {

		private static final long serialVersionUID = 1807644704256098439L;

		private int sendOffAmount = 1;

		public ImageSendOffEvent(int amount) {
			this.sendOffAmount = amount;
		}

		public int getSendOffAmount() {
			return sendOffAmount;
		}

		public void setSendOffAmount(int sendOffAmount) {
			this.sendOffAmount = sendOffAmount;
		}
	}

	private static final long serialVersionUID = 8837946878215424972L;

	// web socket end point
	public static final String WEB_SOCKET_EP = "/image-update";

	private static final Logger LOGGER = Logger
			.getLogger(ImageNotificationGateway.class);

	private static final int MAX_QUEUE_SIZE = 50;

	private static final int TRIM_AMOUNT = 3;

	private static final long DELAY = 700L; // in millis

	@PostConstruct
	public void initialize() {
		LOGGER.info("Registering " + getClass().getSimpleName()
				+ " to event bus");
		CerberusEventBus.get().register(this);
	}

	private Queue<ImageFileAvailable> imagesQueue = new LinkedBlockingQueue<>();

	private AtomicBoolean isLocked = new AtomicBoolean(false);

	@PreDestroy
	public void destroy() {
		LOGGER.info("Unregistering " + getClass().getSimpleName());
		CerberusEventBus.get().unregister(this);
	}

	// received from the CameraImageManager (cerberus.core.files.impl.managers)
	@Subscribe
	public void imageAvailable(ImageFileAvailable imageFile) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("imageAvailable() triggered");

		imageAvailableHandle1(imageFile);
	}

	private void imageAvailableHandle2(ImageFileAvailable imageFile) {
		synchronized (imagesQueue) {
			imagesQueue.add(imageFile);
		}
		if (!isLocked.get()) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug(String.format(
						"Posting new ImageSendOffEvent from Thread %d", Thread
								.currentThread().getId()));

			CerberusEventBus.get().post(new ImageSendOffEvent(1));
		}
	}

	private void imageAvailableHandle1(ImageFileAvailable imageFile) {
		int sendOffAmount = 1;
		synchronized (imagesQueue) {
			if (imagesQueue.size() >= MAX_QUEUE_SIZE) {
				// this is fine but let's trim off some extra ones
				if (LOGGER.isDebugEnabled())
					LOGGER.debug(String
							.format("Queue size reached maximum allowed size of %d. Dequeueing images...",
									imagesQueue.size()));

				sendOffAmount = TRIM_AMOUNT;
			}

			// and ultimately enqueue the image file
			imagesQueue.add(imageFile);
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug(String.format(
					"Posting new ImageSendOffEvent from Thread %d", Thread
							.currentThread().getId()));

		// go handle an image or 2
		CerberusEventBus.get().post(new ImageSendOffEvent(sendOffAmount));
	}

	public String getWebsocketEndpoint() {
		return WEB_SOCKET_EP;
	}

	@Subscribe
	public void postAnImageWhenReady(ImageSendOffEvent imageReadyEvent) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("ImageSendOffEvent received");

		postAnImageHandle1(imageReadyEvent);
	}

	private void postAnImageHandle2(ImageSendOffEvent imageReadyEvent) {
		synchronized (imagesQueue) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("synchronized imagesQueue");

			isLocked.set(true);
			EventBus eventBus = EventBusFactory.getDefault().eventBus();

			while (!imagesQueue.isEmpty()) {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("posting images");

				postImage(imagesQueue.poll(), eventBus);

				if (LOGGER.isDebugEnabled())
					LOGGER.debug("delaying...");

				delay();
			}

			isLocked.set(false);

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("unsynchronizing imagesQueue");
		}
	}

	private void postAnImageHandle1(ImageSendOffEvent imageReadyEvent) {
		if (imageReadyEvent.getSendOffAmount() > 0
				&& imagesQueue.size() >= imageReadyEvent.getSendOffAmount()) {

			synchronized (imagesQueue) {

				if (LOGGER.isDebugEnabled())
					LOGGER.debug("synchronized imagesQueue");

				int amount = imageReadyEvent.getSendOffAmount();
				EventBus eventBus = EventBusFactory.getDefault().eventBus();

				while (amount-- > 0) {

					if (LOGGER.isDebugEnabled())
						LOGGER.debug(String.format(
								"Delaying thead %d for %d ms", Thread
										.currentThread().getId(), DELAY));

					// delay the thread to prevent the ImageServlet from being
					// overwhelmed
					delay();

					// push to web socket
					if (LOGGER.isDebugEnabled())
						LOGGER.debug("Dequeue-ing next image and pushing to web socket");

					postImage(imagesQueue.poll(), eventBus);
				}
			}
		}
	}

	private static void delay() {
		try {
			Thread.sleep(DELAY);
		} catch (InterruptedException e) {
			LOGGER.error(String.format(
					"Thread interrupt exception trapped on thread: %d", Thread
							.currentThread().getId()));
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	private static void postImage(ImageFileAvailable imageFile,
			EventBus eventBus) {
		final String name = imageFile.getResource().getFileName().toString();
		if (LOGGER.isDebugEnabled())
			LOGGER.debug(String.format("Publishing Image File with name: %s",
					name));
		eventBus.publish(WEB_SOCKET_EP, name);
	}
}
