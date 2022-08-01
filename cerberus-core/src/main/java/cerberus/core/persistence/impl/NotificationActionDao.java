package cerberus.core.persistence.impl;

import javax.persistence.EntityManager;

import cerberus.core.persistence.entities.NotificationActionEntry;

/**
 * this will have to be used like so:
 * try (NotificationActionDao dao = DaoManager.Factory.connect(NotificationActionEntry.class)) {
 *   ...
 * }
 * instead of 
 * try (EntityDao<> dao = DaoManager.Factory.connect(NotificationActionEntry.class)) {
 *   ...
 * }
 */
public class NotificationActionDao extends
		AbstractDao<NotificationActionEntry, Long> {

	private static final long serialVersionUID = 7044952343433237123L;

	private static final String SELECT_BY_CLASS_AND_KEY = "select n from %s n where n.requestorClassname = %s and n.requestorKey = %s";

	public NotificationActionDao() {
	}

	public NotificationActionDao(EntityManager em,
			Class<NotificationActionEntry> clazz) {
		super(em, clazz);
	}

	public NotificationActionEntry findByClassAndKey(String classname,
			String key) {
		NotificationActionEntry result = null;
		// TODO
		return result;
	}
}
