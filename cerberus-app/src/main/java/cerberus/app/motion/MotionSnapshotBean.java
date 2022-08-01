package cerberus.app.motion;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import cerberus.core.eventbus.CerberusEventBus;
import cerberus.core.files.impl.managers.ImageFileAvailable;

import com.frontier.lib.time.TimeUtil;

@Named
@SessionScoped
public class MotionSnapshotBean implements Serializable {

	private static final long serialVersionUID = -6235779023113453569L;

	private static final Logger LOGGER = Logger
			.getLogger(MotionSnapshotBean.class);

	public static final int MAX_ALLOWED_SNAPSHOT_XFERS = 10;

	public static final int MIN_ALLOWED_SNAPSHOT_XFERS = 1;

	private static final String MSG_HL_COUNT = "Using HARDLINK to handle %d files.";

	private static final String MSG_MV_COUNT = "Attempting to move %d files.";

	private static final String MSG_DELETING = "Deleting source file.";

	private static final String TGT_FILE_FMT = "%s%s%s-%d-%d.%s";

	private static final String FILE_PREFIX = "Motion-Snapshot-Ready";

	private static final int RANDOM_BOUND = 87872;

	private static final SortFilesLatestFirst SORT_LATEST = new SortFilesLatestFirst();

	private static final class SortFilesLatestFirst implements Comparator<Path> {

		@Override
		public int compare(Path file1, Path file2) {
			FileTime file1Stamp = null;
			try {
				file1Stamp = Files.getLastModifiedTime(file1);
			} catch (IOException e) {
				LOGGER.warn("Trapped exception when attempting to get LAST MODIFIED TIME of FILE: "
						+ file1.toString());
				e.printStackTrace();
				return 0;
			}

			FileTime file2Stamp = null;
			try {
				file2Stamp = Files.getLastModifiedTime(file2);
			} catch (IOException e) {
				LOGGER.warn("Trapped exception when attempting to get LAST MODIFIED TIME of FILE: "
						+ file2.toString());
				e.printStackTrace();
				return 0;
			}

			// we want the youngest files up in front of the list
			// to switch to the oldest at the front,
			// simple reverse the two: file1Stamp.compareTo(file2Stamp);
			return file2Stamp.compareTo(file1Stamp);
		}
	}

	private Path targetMotionDirectory = null;

	private Path targetImagesDirectory = null;

	private int totalSnapshots = -1;

	private int snapshotsToMove = 1;

	private boolean attemptHardlink = false;

	private boolean deleteOnXfer = false;

	public int getMaxAllowedSnapshotXfers() {
		return MAX_ALLOWED_SNAPSHOT_XFERS;
	}

	public int getMinAllowedSnapshotXfers() {
		return MIN_ALLOWED_SNAPSHOT_XFERS;
	}

	public Path getTargetMotionDirectory() {
		return targetMotionDirectory;
	}

	public void setTargetMotionDirectory(Path targetMotionDirectory) {
		this.targetMotionDirectory = targetMotionDirectory;
	}

	public Path getTargetImagesDirectory() {
		return targetImagesDirectory;
	}

	public void setTargetImagesDirectory(Path targetImagesDirectory) {
		this.targetImagesDirectory = targetImagesDirectory;
	}

	public int getSnapshotsToMove() {
		return snapshotsToMove;
	}

	public void setSnapshotsToMove(int snapshotsToMove) {
		this.snapshotsToMove = snapshotsToMove;
	}

	public int getTotalSnapshots() {
		return this.totalSnapshots;
	}

	public boolean isAttemptHardlink() {
		return attemptHardlink;
	}

	public void setAttemptHardlink(boolean attemptHardlink) {
		this.attemptHardlink = attemptHardlink;
	}

	public boolean isDeleteOnXfer() {
		return deleteOnXfer;
	}

	public void setDeleteOnXfer(boolean deleteOnXfer) {
		this.deleteOnXfer = deleteOnXfer;
	}

	public boolean isReady() {
		return (getTargetImagesDirectory() != null && getTargetMotionDirectory() != null);
	}

	public void determineTotalSnapshots() {
		int total = 0;
		Path motionDir = getTargetMotionDirectory();
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(motionDir)) {
			for (Path path : ds)
				if (Files.isRegularFile(path))
					++total;
		} catch (IOException e) {
			LOGGER.error(String
					.format("IOException trapped when getting contents of directory '%s'",
							motionDir.toString()));
			e.printStackTrace();
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug(String.format(
					"Determining total snapshots in %s. Found %d.",
					motionDir.toString(), total));

		this.totalSnapshots = total;
	}

	public int moveSnapshotsToImages() {
		return moveSnapshotsToImages(getSnapshotsToMove());
	}

	public int moveSnapshotsToImages(int amount) {
		List<Path> imagesMoved = Collections.emptyList();

		// check the range of the amount specified
		if (amount >= getMinAllowedSnapshotXfers()
				&& amount <= getMaxAllowedSnapshotXfers()) {

			// get all files
			// we are assuming here that all the files in this
			// directory are indeed the image files
			try (DirectoryStream<Path> ds = Files
					.newDirectoryStream(getTargetMotionDirectory())) {

				// build a list of the files
				List<Path> filesInMotionDir = new LinkedList<>();
				for (Path path : ds)
					if (Files.isRegularFile(path))
						filesInMotionDir.add(path);

				// sort them by latest
				Collections.sort(filesInMotionDir, SORT_LATEST);

				// perform the actual move/hardlink
				imagesMoved = new LinkedList<>();
				if (isAttemptHardlink())
					LOGGER.info(String.format(MSG_HL_COUNT, amount));
				else
					LOGGER.info(String.format(MSG_MV_COUNT, amount));

				int counter = 0;
				for (Path path : filesInMotionDir) {
					final String EXT = FilenameUtils.getExtension(path
							.toString());
					final String DIR = getTargetImagesDirectory().toString();
					
					// <prefix>-<randomIntStamp>-<utcTimestamp>.<ext>
					final String TARGET_PATH = String.format(TGT_FILE_FMT, DIR,
							IOUtils.DIR_SEPARATOR, FILE_PREFIX, randomStamp(),
							TimeUtil.nowUTC().getTime(), EXT);

					Path targetPath = Paths.get(TARGET_PATH);

					try {
						if (isAttemptHardlink()) {
							// perform the hardlink
							Files.createLink(targetPath, path);
						} else {
							// no link, just copy
							// replace any existing files (should be okay with
							// the random stamp)
							Files.copy(path, targetPath,
									StandardCopyOption.REPLACE_EXISTING);
						}
					} catch (IOException ioex) {
						LOGGER.error(String
								.format("Exception trapped when %s %s to %s.",
										isAttemptHardlink() ? "hardlinking"
												: "copying", path.toString(),
										targetPath.toString()));
						ioex.printStackTrace();
						continue; // attempt the next operation
					}

					// check if wanting to delete

					if (isDeleteOnXfer()) {
						LOGGER.info(String.format(MSG_DELETING));
						handleDelete(path);
					}

					// add to list
					imagesMoved.add(targetPath);

					if (++counter >= amount)
						break; // reached the specified limit

				} // end foreach path

				// notify the image servlet
				postImagesAvailable(imagesMoved);
			} catch (IOException e) {
				LOGGER.error(String
						.format("IOException trapped when getting contents of directory '%s'",
								getTargetMotionDirectory().toString()));
				e.printStackTrace();
			}

		} else {
			LOGGER.error(String.format(
					"Amount to move specified was not valid: %d", amount));
		}

		return imagesMoved.size();
	}

	private static void handleDelete(Path file) {
		try {
			Files.delete(file);
		} catch (IOException e) {
			LOGGER.error(String.format(
					"Exception trapped when attempting to delete file '%s'",
					file.toString()));
			e.printStackTrace();
		}
	}

	private static void postImagesAvailable(List<Path> imageList) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug(String.format("Posting %d IMAGE FILES to bus",
					imageList.size()));

		for (Path image : imageList)
			CerberusEventBus.get().post(new ImageFileAvailable(image));
	}

	private static int randomStamp() {
		return TimeUtil.randomInt(RANDOM_BOUND);
	}
}
