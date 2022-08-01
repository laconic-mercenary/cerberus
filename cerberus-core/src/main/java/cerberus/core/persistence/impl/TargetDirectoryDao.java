package cerberus.core.persistence.impl;

import javax.persistence.EntityManager;

import cerberus.core.persistence.entities.TargetDirectory;

public class TargetDirectoryDao extends AbstractDao<TargetDirectory, Long> {

	private static final long serialVersionUID = 1817930686625169153L;

	public TargetDirectoryDao() {
	}

	public TargetDirectoryDao(EntityManager em, Class<TargetDirectory> clazz) {
		super(em, clazz);
	}
}
