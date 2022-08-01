package cerberus.core.persistence.impl;

import javax.persistence.EntityManager;

import cerberus.core.persistence.entities.FileManagerEntry;

// this isn't used directly, see the DaoManager for details
public class FileManagerEntryDao extends AbstractDao<FileManagerEntry, Long> {

	private static final long serialVersionUID = -126799427838778548L;

	public FileManagerEntryDao() {
	}

	public FileManagerEntryDao(EntityManager em, Class<FileManagerEntry> clazz) {
		super(em, clazz);
	}
}
