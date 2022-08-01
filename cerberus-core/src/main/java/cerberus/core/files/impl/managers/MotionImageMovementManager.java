package cerberus.core.files.impl.managers;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import cerberus.core.notifications.NotificationRequest;
import cerberus.core.notifications.NotificationRequestor;
import cerberus.core.notifications.submission.NotificationRequestSubmitter;
import cerberus.core.notifications.submission.NotificationSubmissions;
import cerberus.core.persistence.KvpProperties;
import cerberus.core.persistence.util.Keys;

import com.frontier.lib.time.TimeUtil;

public class MotionImageMovementManager extends TimedManager implements
		PatternSettable, NotificationRequestor<Path> {

	private static final Logger LOGGER = Logger
			.getLogger(MotionImageMovementManager.class);

	public static final String KEY_TARGET_IMAGE_DIRECTORY = "target_image_dir";

	private static final String MSG_TRANSPORT_FILES_FMT = "Will transport %d motion files to target DIR: %s";

	// the decimal formatters are FIRST=random stamp, SECOND=time stamp
	private static final String TARGET_DIR_FMT = "%s%s%s-%d-%d.%s";

	private static final String FILE_NAME_PFX = "Motion-Snapshot-RX";

	private static final int RANDOM_BOUND = 87872;

	// this will likely need to sync up with a database property
	// somewhere
	private static final String NOTIFYKEY_MOTION_SNAPSHOT_READY = "MOTION_SNAPSHOT_READY";

	private Path targetImageDirectory = null;

	private Filter<Path> fileFilter = null;

	public MotionImageMovementManager() {
		String targetPath = null;
		String key = Keys.make(this.getClass(), KEY_TARGET_IMAGE_DIRECTORY);

		try {
			targetPath = KvpProperties.findProperty(key);
		} catch (Exception e) {
			LOGGER.error("Exception trapped when querying KVP property: " + key);
			e.printStackTrace();
			return;
		}

		LOGGER.info(String.format(
				"Found configured target motion image directory '%s'",
				targetPath));

		checkDirectory(Paths.get(targetPath));
	}

	private void checkDirectory(Path targetDir) {
		if (Files.isDirectory(targetDir)) {
			this.targetImageDirectory = targetDir;
		} else {
			LOGGER.warn(String
					.format("Specified target directory %s is not a directory or does not exist. Creating...",
							targetDir.toString()));
			try {
				this.targetImageDirectory = Files.createDirectories(targetDir);
			} catch (IOException e) {
				LOGGER.error("Exception trapped when attempting to create the directory. I will be disabled.");
				e.printStackTrace();
				this.targetImageDirectory = null;
			}

			if (this.targetImageDirectory != null) {
				LOGGER.info("Successfully created the directory.");
			}
		}
	}

	public void setTargetImageDirectory(Path path) {
		this.targetImageDirectory = path;
	}

	@Override
	protected void doHandleFiles(Collection<Path> files) {
		if (this.targetImageDirectory == null) {
			// errors prevented us from determining our target
			// directory...
			LOGGER.warn(String
					.format("Configuration errors prevent me from operating on %d files",
							files.size()));
		} else {

			if (!files.isEmpty()) {
				LOGGER.info(String.format(MSG_TRANSPORT_FILES_FMT,
						files.size(), this.targetImageDirectory.toString()));

				// going to forego the existence check for the file for
				// performance reasons
				for (Path file : files) {
					try {
						final String PARENT_DIR = this.targetImageDirectory
								.toAbsolutePath().toString();
						final String EXT = FilenameUtils.getExtension(file
								.toString());

						Path targetFile = Paths.get(String.format(
								TARGET_DIR_FMT, PARENT_DIR,
								IOUtils.DIR_SEPARATOR, FILE_NAME_PFX,
								randomStamp(), timeStamp(), EXT));

						Files.move(file, targetFile);

						// notify anyone interested in knowing that a motion
						// snapshot is ready
						submitNotificationRequest(targetFile);
					} catch (Exception e) {
						LOGGER.error(String
								.format("Exception trapped when moving file %s to directory %s.",
										file.toString(),
										this.targetImageDirectory.toString()));
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public Filter<Path> getFileFilter() {
		return this.fileFilter;
	}

	@Override
	public void setFilterRegex(String regex) {
		this.fileFilter = PatternSettable.Util.makeFilter(regex);
	}

	private static long timeStamp() {
		return TimeUtil.nowUTC().getTime();
	}

	private static long randomStamp() {
		return (long) new Random().nextInt(RANDOM_BOUND);
	}

	@Override
	public void submitNotificationRequest(Path dataToPass) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("submitting notification request for motion file: "
					+ dataToPass.toString());
		}
		// would like for this to be a fairly un-intrusive operation
		// processing-wise, as there are other files to process
		NotificationRequestSubmitter submitter = NotificationSubmissions
				.getDefaultSubmitter();
		NotificationRequest<Path> request = NotificationRequest.Factory.make(
				this.getClass(), NOTIFYKEY_MOTION_SNAPSHOT_READY, dataToPass);
		submitter.submit(request);
	}
}
