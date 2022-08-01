package cerberus.core.notifications.handling.impl;

import org.apache.log4j.Logger;

import cerberus.core.notifications.NotificationAction;
import cerberus.core.notifications.NotificationRequest;
import cerberus.core.notifications.handling.NotificationRequestHandler;
import cerberus.core.persistence.DaoManager;
import cerberus.core.persistence.EntityDao;
import cerberus.core.persistence.entities.NotificationActionEntry;
import cerberus.core.persistence.impl.NotificationActionDao;

public class PersistenceRequestHandler implements NotificationRequestHandler {

	// try to avoid putting properties here...
	// unless necessary

	private static final Logger LOGGER = Logger
			.getLogger(PersistenceRequestHandler.class);

	@Override
	public <T> NotificationAction<T> requestToAction(
			NotificationRequest<T> request) {

		// fetch the notification action - based on class and key
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String
					.format("Fetching notification action - given REQUESTOR=%s and KEY=%s and DATA=%s",
							request.getClass().getName(), request.getKey(),
							request.getData() == null ? "(NONE)" : request
									.getData().getClass().getName()));
		}

		NotificationAction<T> action = queryAction(
				request.getClass().getName(), request.getKey());

		if (action != null) {
			// here is the point where types need to align perfectly..
			// worked beautifully...
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String
						.format("Notification Action '%s' was found when querying with class='%s' key='%s'. Passing over data.",
								action.getClass().getName(), request.getClass()
										.getName(), request.getKey()));
			}
			action.setPrerequisiteData(request.getData());
		} else {
			LOGGER.error(String
					.format("A Notification Action with class '%s' and key '%s' does not exist",
							request.getClass().getName(), request.getKey()));
		}

		return action;
	}

	static <T> NotificationAction<T> queryAction(String className, String key) {
		try (EntityDao<NotificationActionEntry, Long> dao = DaoManager.Factory
				.connect(NotificationActionEntry.class)) {

			NotificationActionDao ndao = (NotificationActionDao) dao;

			ndao.findByClassAndKey(className, key);
			
			// TODO...

		} catch (Exception e) {
			LOGGER.error("");
			e.printStackTrace();
		}
		return null;
	}
}
