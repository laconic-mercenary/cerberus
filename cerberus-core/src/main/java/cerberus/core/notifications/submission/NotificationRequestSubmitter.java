package cerberus.core.notifications.submission;

import cerberus.core.notifications.NotificationRequest;

public interface NotificationRequestSubmitter {

	void submit(NotificationRequest<?> request);

}
