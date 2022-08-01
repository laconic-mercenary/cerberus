package cerberus.core.notifications.handling;

import cerberus.core.notifications.NotificationAction;
import cerberus.core.notifications.NotificationRequest;

public interface NotificationRequestHandler {

	<T> NotificationAction<T> requestToAction(NotificationRequest<T> request);

}
