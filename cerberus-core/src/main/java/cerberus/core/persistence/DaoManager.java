package cerberus.core.persistence;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import cerberus.core.persistence.entities.FileManager;
import cerberus.core.persistence.entities.FileManagerEntry;
import cerberus.core.persistence.entities.KeyValuePairProperty;
import cerberus.core.persistence.entities.NotificationActionEntry;
import cerberus.core.persistence.entities.PingInfo;
import cerberus.core.persistence.entities.TargetDirectory;
import cerberus.core.persistence.impl.AbstractDao;
import cerberus.core.persistence.impl.FileManagerDao;
import cerberus.core.persistence.impl.FileManagerEntryDao;
import cerberus.core.persistence.impl.KeyValuePropertiesDao;
import cerberus.core.persistence.impl.NotificationActionDao;
import cerberus.core.persistence.impl.PingInfoDao;
import cerberus.core.persistence.impl.TargetDirectoryDao;

public interface DaoManager {

	public static class Factory {

		// this entry can be found in the persistence.xml
		// which is in the resources directory of the cerberus-resources project
		public static final String PERSISTENCE_UNIT_1 = "h2JPA";

		public static final EntityManagerFactory EMF = Persistence
				.createEntityManagerFactory(PERSISTENCE_UNIT_1);

		private static Map<String, Class<?>> daoMap = null;
		static {
			// add any constituent entities to the map
			// format is (<ENTITY>,<DAO>)
			daoMap = new HashMap<>();
			daoMap.put(PingInfo.class.getName(), PingInfoDao.class);
			daoMap.put(KeyValuePairProperty.class.getName(),
					KeyValuePropertiesDao.class);
			daoMap.put(FileManagerEntry.class.getName(),
					FileManagerEntryDao.class);
			daoMap.put(FileManager.class.getName(), FileManagerDao.class);
			daoMap.put(TargetDirectory.class.getName(),
					TargetDirectoryDao.class);
			daoMap.put(NotificationActionEntry.class.getName(),
					NotificationActionDao.class);
		}

		public static <T extends Serializable, KEY> EntityDao<T, KEY> connect(
				Class<T> clazz) {
			AbstractDao<T, KEY> dao = null;
			try {
				dao = (AbstractDao<T, KEY>) daoMap.get(clazz.getName())
						.newInstance();
				dao.setClazzInfo(clazz);
				dao.setEntityManager(EMF.createEntityManager());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return dao;
		}
	}
}
