package cerberus.core.files.tasks.impl;

import java.nio.file.Path;
import java.util.Collection;

import cerberus.core.files.FileManager;
import cerberus.core.files.tasks.FileManagerTaskingAdapter;
import cerberus.core.tasks.AsyncTasking;

public class FileManagerTaskingAdapterImpl implements FileManagerTaskingAdapter {

	@Override
	public AsyncTasking convert(FileManager filemanager) {
		return convert(filemanager, null);
	}

	@Override
	public AsyncTasking convert(FileManager filemanager, Collection<Path> files) {
		return FileManagerPerformTask.Factory.make(filemanager, files);
	}

}
