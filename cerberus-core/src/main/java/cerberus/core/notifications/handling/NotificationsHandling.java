package cerberus.core.notifications.handling;

import cerberus.core.notifications.handling.impl.PersistenceRequestHandler;

public class NotificationsHandling {

	public static NotificationRequestHandler getDefaultHandler() {
		// act like a factory - keeping it mostly stateless
		return new PersistenceRequestHandler();
	}

}
