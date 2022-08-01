package cerberus.core.files.impl.managers;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import cerberus.core.eventbus.CerberusEventBus;
import cerberus.core.persistence.KvpProperties;

import com.frontier.lib.time.TimeUtil;

public class CameraImageManager extends TimedManager implements PatternSettable {

	private static final Logger LOGGER = Logger
			.getLogger(CameraImageManager.class);

	private static final String KEY_MOVE_DIR = "MOVE_DIR";

	private static final String KEY_NEW_FILENAME_FMT = "NEW_FILENAME_FMT";

	private static final String MSG_HNDL_FILE_MVD_FMT = "File %s moved to %s";

	private static final String MSG_AUTO_CREATE_DIR_FMT = "Automatically created directory %s";

	private static final String MSG_EXTRACT_MSG_FMT = "Obtained '%s' for %s ";

	private static final String KEY_EXTRACT_FMT = "%s.%s";

	private static final Random RANDOM_GENERATOR = new Random();

	private static final int RANDOM_CEILING = 69931; // prime number

	public static class Factory {
		public static CameraImageManager make() {
			return new CameraImageManager();
		}
	}

	// if concerned about concurrency, these are not
	// changed once they are loaded in the loadProperties
	private String moveDirectory = null;
	private String newFilenameFormat = null;
	private DirectoryStream.Filter<Path> fileFilter = null;

	// this should be used almost in like a
	// singleton fashion - to avoid all the stuff that
	// happens in construction, all the time
	public CameraImageManager() {
		loadPropertiesDB();
		createDirectories();
	}

	// this is loaded on instantiation
	private void loadPropertiesDB() {
		LOGGER.info("Loading configuration...");
		moveDirectory = extract(KEY_MOVE_DIR.toLowerCase());
		newFilenameFormat = extract(KEY_NEW_FILENAME_FMT.toLowerCase());
		LOGGER.info("Loading complete");
	}

	// auto create directories if they don't exist
	private synchronized void createDirectories() {
		// automatically create the directories
		// move directory
		createDirectoryIfNotExists(this.moveDirectory);
	}

	private static void createDirectoryIfNotExists(String path) {
		Path directory = Paths.get(path);
		// isDirectory() also checks for existence...
		if (!Files.isDirectory(directory)) {
			try {
				// should be similar to mkdirs()
				Files.createDirectories(directory);
			} catch (Exception e) {
				String msg = String.format(
						"Attempt was made to create the directory '%s' but it failed."
								+ " This directory is required.", path);
				LOGGER.fatal(msg);
				throw new RuntimeException(msg);
			}

			LOGGER.info(String.format(MSG_AUTO_CREATE_DIR_FMT,
					directory.toString()));
		}
	}

	// database version
	// this is the expected format that the loaders of the database should be in
	// packagename.classname.property
	private static String extract(String key) {
		String value = null;
		key = String.format(KEY_EXTRACT_FMT,
				CameraImageManager.class.getName(), key);
		try {
			value = KvpProperties.findProperty(key);
			LOGGER.info(String.format(MSG_EXTRACT_MSG_FMT, value, key));
		} catch (Exception e) {
			LOGGER.fatal(String
					.format("Exception caught when extracting Camera Image Manager key '%s'.",
							key));
			e.printStackTrace();
		}
		if (value == null) {
			LOGGER.fatal(String.format("%s property '%s' is required.",
					CameraImageManager.class.getName(), key));
		}
		return value;
	}

	// must call the setFilterRegex before using this.
	// which should automatically be done by the
	// PersistenceFileManagerService
	@Override
	public DirectoryStream.Filter<Path> getFileFilter() {
		return this.fileFilter;
	}

	// remember, all the checking for whether there are files in the array and
	// if this TimedManager isReady() is done in the parent class, just HANDLE
	// the files here
	@Override
	protected void doHandleFiles(Collection<Path> files) {
		// no need to check array length, the service automatically does
		for (Path file : files) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Handling file %s", file.toString()));
			}

			Path src = file;
			Path target = null;
			try {
				// rename the file
				// but try to preserve the extension
				// and... do it faster
				target = determineNewFilenameFaster(moveDirectory, file,
						newFilenameFormat);

				// move to an area where the web application can
				// locate them and show them to the logged in user
				Files.move(src, target);

				// post image available to all subscribers
				// most likely to the ImageNotificationGateway
				CerberusEventBus.get().post(new ImageFileAvailable(target));

				// log
				LOGGER.info(String.format(MSG_HNDL_FILE_MVD_FMT,
						src.toString(), target.toString()));
			} catch (IOException e) {
				LOGGER.error(String.format(
						"Failed to move file '%s' from %s to %s", file
								.getFileName().toString(), src.getParent()
								.toString(), target != null ? target
								.getParent().toString() : "NULL"));
				e.printStackTrace();
				// Landing zone purge will take care of it
			}
		}
	}

	// the idea is that generating a timestamp + random number is faster than
	// checking if the file already exists, if by some chance it does already
	// exist (because of a whole bunch of requests simultaneously - unlikely)
	// then I'm willing to eat an IOException once, then return to normal,
	// performant file processing
	private static Path determineNewFilenameFaster(String moveDirectory,
			Path original, String strFormat) throws IOException {

		final String EXT = FilenameUtils.getExtension(original.toString());

		// the string format will come from the database - the timestamp is
		// added to know when the file was moved and the random stamp somewhat
		// helps ensure no conflicts will occur with existing files
		final String FILE_NAME = String.format(strFormat, random(), stamp(),
				EXT);

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Will use filename: " + FILE_NAME);

		return Paths.get(moveDirectory, FILE_NAME);
	}

	@Override
	public void setFilterRegex(String regex) {
		this.fileFilter = PatternSettable.Util.makeFilter(regex);
	}

	private static long stamp() {
		return TimeUtil.nowUTC().getTime();
	}

	private static long random() {
		return (long) RANDOM_GENERATOR.nextInt(RANDOM_CEILING);
	}
}
