package cerberus.core.notifications.submission.impl;

import org.apache.log4j.Logger;

import cerberus.core.notifications.NotificationAction;
import cerberus.core.notifications.NotificationRequest;
import cerberus.core.notifications.handling.NotificationRequestHandler;
import cerberus.core.notifications.handling.NotificationsHandling;
import cerberus.core.tasks.AsyncTasking;

public class NotificationAsyncTasking implements AsyncTasking {

	private static final long serialVersionUID = 6747849163700387014L;

	private static final String MSG_FMT_PERFORM_START = "Performing task %s";

	private static final String MSG_FMT_PERFORM_FINISH = "Finished task %s";

	private static final Logger LOGGER = Logger
			.getLogger(NotificationAsyncTasking.class);

	private NotificationRequest<?> notificationRequest = null;

	public NotificationAsyncTasking(NotificationRequest<?> request) {
		this.notificationRequest = request;
	}

	@Override
	public void perform() throws Exception {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("perform() invoked");

		NotificationRequestHandler handler = NotificationsHandling
				.getDefaultHandler();

		NotificationAction<?> action = handler
				.requestToAction(this.notificationRequest);

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Finished fetching notification action");

		// null is a good indication that it was not found
		if (action != null) {
			// assumes that all the necessary data has already been passed over
			// to this action

			LOGGER.info(String.format(MSG_FMT_PERFORM_START, action.getClass()
					.getName()));
			// do it
			action.performNotification();

			// set our finished/performed flag
			notificationRequest = null;

			LOGGER.info(String.format(MSG_FMT_PERFORM_FINISH, action.getClass()
					.getName()));
		} else {
			LOGGER.warn("No notification action was retrieved - ignoring notification");
		}
	}

	@Override
	public boolean isPerformed() {
		return notificationRequest == null;
	}

}
