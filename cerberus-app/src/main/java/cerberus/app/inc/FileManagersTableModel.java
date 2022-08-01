package cerberus.app.inc;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;

import cerberus.core.persistence.DaoManager;
import cerberus.core.persistence.EntityDao;
import cerberus.core.persistence.entities.FileManager;

@Named
@ViewScoped
public class FileManagersTableModel implements Serializable {

	private static final long serialVersionUID = 9016287563295514711L;

	private static final Logger LOGGER = Logger
			.getLogger(FileManagersTableModel.class);

	// this will effectively re-query after each
	// refresh of the page
	public List<FileManager> getFileManagers() {
		List<FileManager> results = Collections.emptyList();
		try (EntityDao<FileManager, ?> dao = DaoManager.Factory
				.connect(FileManager.class)) {
			results = dao.findAll();
		} catch (Exception e) {
			LOGGER.error("Failed to query for a list of file manager entries.");
			e.printStackTrace();
		}
		return results;
	}
}
