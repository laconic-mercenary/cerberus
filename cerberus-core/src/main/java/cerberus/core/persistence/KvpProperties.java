package cerberus.core.persistence;

import org.apache.log4j.Logger;

import cerberus.core.persistence.entities.KeyValuePairProperty;

public class KvpProperties {

	private static final Logger LOGGER = Logger.getLogger(KvpProperties.class);

	public static String findProperty(String key) throws Exception {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug(String.format("Attempting to find property : '%s'",
					key));

		KeyValuePairProperty kvpProperty = null;
		String result = null;
		try (EntityDao<KeyValuePairProperty, String> dao = DaoManager.Factory
				.connect(KeyValuePairProperty.class)) {
			kvpProperty = dao.findByID(key);
		}
		if (kvpProperty != null) {
			result = kvpProperty.getValueEntry();
		} else {
			LOGGER.warn(String
					.format("Unable to find KVP Property : '%s'", key));
		}
		return result;
	}

	public static String findProperty(String key, String defaultValue) {
		String property = null;
		try {
			property = findProperty(key);
		} catch (Exception e) {
			LOGGER.warn(String.format(
					"Exception trapped when querying for KVP Key: '%s'", key));
			e.printStackTrace();
		}
		if (property == null) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug(String.format(
						"Failed to find property, using default value of %s",
						defaultValue));
			property = defaultValue;
		}
		return property;
	}

}
