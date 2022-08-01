package cerberus.core.persistence.impl;

import javax.persistence.EntityManager;

import cerberus.core.persistence.entities.KeyValuePairProperty;

// see DaoManager
public class KeyValuePropertiesDao extends
		AbstractDao<KeyValuePairProperty, String> {

	private static final long serialVersionUID = -3618994390548792955L;

	public KeyValuePropertiesDao() {
	}

	public KeyValuePropertiesDao(EntityManager em,
			Class<KeyValuePairProperty> clazz) {
		super(em, clazz);
	}
}
