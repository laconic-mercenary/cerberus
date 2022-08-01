package cerberus.app.motion;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import cerberus.app.servlets.ImageServlet;
import cerberus.core.files.impl.managers.MotionImageMovementManager;
import cerberus.core.persistence.KvpProperties;
import cerberus.core.persistence.util.Keys;

import com.frontier.lib.validation.TextValidator;

@Named
@ViewScoped
// as opposed to javax.faces.bean.ViewScoped
// which is a VERY !!!IMPORTANT!!! distinction - it must be
// javax.faces.view.ViewScoped, otherwise treats
// it like a request scoped bean
public class MotionSnapshotDialogController implements Serializable {

	private static final long serialVersionUID = -2959018431580375869L;

	private static final Logger LOGGER = Logger
			.getLogger(MotionSnapshotDialogController.class);

	// get the directory where the motion images are stored
	private static final String KEY_TARGET_DIR = Keys.make(
			MotionImageMovementManager.class,
			MotionImageMovementManager.KEY_TARGET_IMAGE_DIRECTORY);

	// get the directory where the images are soaked up by the ImageServlet to
	// be displayed by the user
	private static final String KEY_IMAGE_DIR = Keys.make(ImageServlet.class,
			ImageServlet.KEY_IMAGE_DIR.toLowerCase());

	// part of a kvp property
	private static final String KEY_ATTEMPT_HARDLINK = "attempt_hardlink";

	// complete kvp key
	private static final String KEY_HARDLINK = Keys.make(
			MotionSnapshotBean.class, KEY_ATTEMPT_HARDLINK);

	// not all operating systems will support the hardlink
	private static final Boolean DEFAULT_ATTEMPT_HARDLINK = false;

	private static final String USERMSG_SNAPSHOTS_SUCCESS = "Moved %d snapshots.";

	private static final String USERMSG_SNAPSHOTS_NONE = "No motion snapshots were moved.";

	private static final String USERMSG_SNAPSHOTS_ERROR = "Error occurred when attempting to move snapshots.";

	private static final String USERMSG_TITLE = "Notification";

	// this fires once, and only once: when the bean is loaded (when the dialog
	// shows)
	@PostConstruct
	public void initialize() {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("initialize()");

		boolean attemptHardlink = Boolean.valueOf(KvpProperties.findProperty(
				KEY_HARDLINK, DEFAULT_ATTEMPT_HARDLINK.toString()));

		String targetDirectory = extract(KEY_TARGET_DIR);
		if (targetDirectory != null && isValidDir2(targetDirectory)) {
			// got the target motion images dir
			LOGGER.info(String.format("Found target directory '%s'",
					targetDirectory));

			String imageDirectory = extract(KEY_IMAGE_DIR);

			if (imageDirectory != null && isValidDir2(imageDirectory)) {
				// got the images dir, good to go
				LOGGER.info(String.format("Found image directory '%s'",
						imageDirectory));

				getSnapshotBean().setAttemptHardlink(attemptHardlink);
				getSnapshotBean().setTargetMotionDirectory(Paths.get(targetDirectory));
				getSnapshotBean().setTargetImagesDirectory(Paths.get(imageDirectory));

				determineTotalSnapshot();
			}
		}
	}

	@Inject
	private MotionSnapshotBean snapshotBean = null;

	public MotionSnapshotBean getSnapshotBean() {
		return snapshotBean;
	}

	public void setSnapshotBean(MotionSnapshotBean snapshotBean) {
		this.snapshotBean = snapshotBean;
	}

	private void determineTotalSnapshot() {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("dialogShowAction() triggered");

		// reset the Yes/No radio for deleting
		getSnapshotBean().setDeleteOnXfer(false);

		// find snapshots in directory
		try {
			getSnapshotBean().determineTotalSnapshots();
		} catch (Exception e) {
			// send message to the user
			postMessage(
					"Error occurred in determining the number of total motion snapshots available.",
					FacesMessage.SEVERITY_WARN, true);
		}
	}

	public void submitAction() {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("submitAction triggered");

		int moved = 0;
		try {
			moved = getSnapshotBean().moveSnapshotsToImages();
		} catch (Exception e) {
			moved = -1;
			LOGGER.error("Exception trapped when attempting to move snapshots");
			e.printStackTrace();
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug(String.format("Moved %d snapshot images to image dir",
					moved));

		if (moved == 0) {
			postMessage(USERMSG_SNAPSHOTS_NONE, FacesMessage.SEVERITY_WARN,
					false);
		} else {
			if (moved == -1) {
				postMessage(String.format(USERMSG_SNAPSHOTS_ERROR),
						FacesMessage.SEVERITY_ERROR, false);
			} else {
				postMessage(String.format(USERMSG_SNAPSHOTS_SUCCESS, moved),
						FacesMessage.SEVERITY_INFO, false);
			}
		}

		determineTotalSnapshot();
	}

	private static String extract(String key) {
		String result = null;
		try {
			result = KvpProperties.findProperty(key);
		} catch (Exception e) {
			LOGGER.error(String.format(
					"Exception trapped when extracting KVP property %s", key));
			e.printStackTrace();
		}
		return result;
	}

	private static void postMessage(String message,
			FacesMessage.Severity severity, boolean failValidation) {

		FacesContext ctx = FacesContext.getCurrentInstance();

		if (failValidation)
			ctx.validationFailed();

		if (LOGGER.isDebugEnabled())
			LOGGER.debug(String.format(
					"POSTING USER MESSAGE: %s - (validation_failed=%s)",
					message, failValidation));

		// null will target the growl
		ctx.addMessage(null, new FacesMessage(severity, USERMSG_TITLE, message));
	}

	private static boolean isValidDir2(String directoryStr) {
		boolean isValid = false;
		if (!TextValidator.isEmptyStr(directoryStr)) {
			Path path = Paths.get(directoryStr);
			if (Files.isDirectory(path)) {
				isValid = true;
			} else {
				LOGGER.error(String.format(
						"Directory '%s' is not a directory or does not exist.",
						directoryStr));
			}
		}
		return isValid;
	}
}
