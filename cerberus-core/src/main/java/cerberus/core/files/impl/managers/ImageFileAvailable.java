package cerberus.core.files.impl.managers;

import java.nio.file.Path;

import cerberus.core.eventbus.notifications.ResourceAvailableNotification;

/**
 * this is what's posted to the event bus when an image file is available and
 * ready for the ImageServlet and whomever else is concerned (subscribed)
 */
public class ImageFileAvailable implements ResourceAvailableNotification<Path> {

	private static final long serialVersionUID = -5674637190161108608L;

	private Path image = null;

	public ImageFileAvailable(Path file) {
		setImage(file);
	}

	@Override
	public Path getResource() {
		return getImage();
	}

	public Path getImage() {
		return image;
	}

	public void setImage(Path image) {
		this.image = image;
	}

}
