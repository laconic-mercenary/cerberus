package cerberus.core.files.tasks.impl;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import cerberus.core.files.FileManager;
import cerberus.core.tasks.AsyncEligible;
import cerberus.core.tasks.AsyncTasking;

public class FileManagerPerformTask implements AsyncTasking {

	private static final long serialVersionUID = -3651099085835909091L;

	private static final Logger LOGGER = Logger
			.getLogger(FileManagerPerformTask.class);

	private static final String MSG_COMPLETED_FMT = "Async tasked manager %s has completed task. Manager required files: %s";

	private static final String MSG_NOT_ENABLED_FMT = "FileManager %s is marked as %s - but is not currently enabled. Will not perform the handle task.";

	public static final class Factory {

		public static FileManagerPerformTask make(FileManager manager,
				Collection<Path> files) {
			return new FileManagerPerformTask(manager, files);
		}

	}

	private FileManager fileManager = null;

	private Collection<Path> files = null;

	private FileManagerPerformTask(FileManager manager) {
		this(manager, Collections.emptyList());
	}

	private FileManagerPerformTask(FileManager manager, Collection<Path> files) {
		this.fileManager = manager;
		this.files = files;
	}

	@Override
	public void perform() throws Exception {
		if (fileManager.isEnabled()) {
			fileManager.handleFiles(files);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format(MSG_COMPLETED_FMT, fileManager
						.getClass().getName(), fileManager.isRequiringFiles()));
			}
		} else {
			LOGGER.info(String.format(MSG_NOT_ENABLED_FMT, fileManager
					.getClass().getName(), AsyncEligible.class.getSimpleName()));
		}
		// null will indicate that it is performed
		fileManager = null;
	}

	@Override
	public boolean isPerformed() {
		return (fileManager == null);
	}

}
