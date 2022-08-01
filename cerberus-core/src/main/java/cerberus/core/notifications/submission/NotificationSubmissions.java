package cerberus.core.notifications.submission;

import cerberus.core.notifications.submission.impl.EventBusNotificationSubmitter;

public class NotificationSubmissions {

	public static NotificationRequestSubmitter getDefaultSubmitter() {
		// designed to be factory-ish
		return new EventBusNotificationSubmitter();
	}

}
