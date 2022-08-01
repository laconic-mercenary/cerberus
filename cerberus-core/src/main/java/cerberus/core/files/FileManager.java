package cerberus.core.files;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Those that implment this class will likely be used in situations that require
 * performance to be very good. As in, we need to doHandle() and then get out so
 * that other FMs can do their work.
 */
public interface FileManager {

	DirectoryStream.Filter<Path> getFileFilter();

	void handleFiles(Collection<Path> files);

	boolean isRequiringFiles();

	boolean isEnabled();

	void setEnabled(boolean isEnabled);

	void setRequiringFiles(boolean isRequiringFiles);

	public static class Factory {
		public static FileManager make(String className, boolean isEnabled,
				boolean isRequiringFiles) {
			FileManager fm = null;
			try {
				fm = (FileManager) Class.forName(className).newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			fm.setEnabled(isEnabled);
			fm.setRequiringFiles(isRequiringFiles);
			return fm;
		}
	}
}
