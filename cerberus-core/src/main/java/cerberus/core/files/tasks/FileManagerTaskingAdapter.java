package cerberus.core.files.tasks;

import java.nio.file.Path;
import java.util.Collection;

import cerberus.core.files.FileManager;
import cerberus.core.files.tasks.impl.FileManagerTaskingAdapterImpl;
import cerberus.core.tasks.AsyncTasking;

public interface FileManagerTaskingAdapter {

	public AsyncTasking convert(FileManager filemanager);
	
	public AsyncTasking convert(FileManager filemanager, Collection<Path> files);
	
	public static final class Factory {
		
		public static FileManagerTaskingAdapter make() {
			return new FileManagerTaskingAdapterImpl();
		}
		
	}
}
