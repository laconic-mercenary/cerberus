package cerberus.core.persistence.impl;

import javax.persistence.EntityManager;

import cerberus.core.persistence.entities.FileManager;

public class FileManagerDao extends AbstractDao<FileManager, Long> {

	private static final long serialVersionUID = -1626321934515632303L;

	public FileManagerDao() {
	}

	public FileManagerDao(EntityManager em, Class<FileManager> clazz) {
		super(em, clazz);
	}
}
