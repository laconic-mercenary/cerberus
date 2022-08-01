package cerberus.core.files.impl.managers.purge;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.apache.log4j.Logger;

import cerberus.core.files.impl.managers.PatternSettable;
import cerberus.core.files.impl.managers.TimedManager;
import cerberus.core.tasks.AsyncEligible;

@AsyncEligible
public class FilesPurge extends TimedManager implements PatternSettable,
		Serializable {

	private static final long serialVersionUID = 4035838378277593303L;

	private static final Logger LOGGER = Logger.getLogger(FilesPurge.class);

	public static final String MSG_PURGE_STARTED_FMT = "PURGE [%s:%s] started...";

	public static final String MSG_PURGED_FILE = "PURGED file '%s'";

	private static final String MSG_FILE_NO_EXISTS_FMT = "File [%s] does not exist, will not remove.";

	public static final String MSG_PURGE_COMPLETE_FMT = "PURGE [%s:%s] completed. %d files purged.";

	private static final String MSG_PURGE_FMT = "Will purge %d files in directory '%s'";

	// public static final String IMAGE_PATTERN =
	// "([^\\s]+(\\.(?i)(jpg|png|gif|bmp|tif|jpeg))$)";

	private DirectoryStream.Filter<Path> fileFilter = null;

	public FilesPurge() {
	}

	public FilesPurge(int days, int hours, int minutes, int seconds) {
		setTimeInterval(days, hours, minutes, seconds);
		calculateNextTargetTime();
	}

	// a prerequisite for this to work is the setFilterRegex
	// unless this is overridden in a super class
	public DirectoryStream.Filter<Path> getFileFilter() {
		return this.fileFilter;
	}

	@Override
	public void setFilterRegex(String regex) {
		this.fileFilter = PatternSettable.Util.makeFilter(regex);
	}

	// reusable purge method
	public static int purgeFiles(Collection<Path> files) {
		int filesPurged = 0;

		if (!files.isEmpty()) {

			LOGGER.info(String.format(MSG_PURGE_FMT, files.size(), files
					.iterator().next().getParent().toString()));

			for (Path file : files) {
				if (Files.exists(file) && !Files.isDirectory(file)) {
					try {
						Files.delete(file);
						++filesPurged;
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug(String.format(MSG_PURGED_FILE,
									file.toString()));
						}
					} catch (IOException e) {
						LOGGER.error("Failed to delete file: "
								+ file.toString());
						e.printStackTrace();
					}
				} else {
					if (!Files.isDirectory(file)) {
						// well that's weird...
						// maybe it was deleted in the split second between then and now
						LOGGER.warn(String.format(MSG_FILE_NO_EXISTS_FMT,
								file.toString()));
					}
				}
			}
		}
		return filesPurged;
	}

	@Override
	protected void doHandleFiles(Collection<Path> files) {
		LOGGER.info(String.format(MSG_PURGE_STARTED_FMT, this.getClass()
				.getName(), this.hashCode()));

		int filesPurged = purgeFiles(files);

		LOGGER.info(String.format(MSG_PURGE_COMPLETE_FMT, this.getClass()
				.getName(), this.hashCode(), filesPurged));
	}
}
