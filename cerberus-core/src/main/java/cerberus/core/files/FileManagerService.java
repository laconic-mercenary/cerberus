package cerberus.core.files;

import java.nio.file.Path;
import java.util.Collection;

import cerberus.core.files.impl.PersistenceFileManagerService;

public interface FileManagerService {

	public Collection<Path> getTargetDirectories();

	public Collection<FileManager> getManagersFor(Path directory);

	public static class Factory {
		public static FileManagerService make() {
			// return new PropertiesFileManagerService();
			
			// the static initialization of this class will fire off
			// when this is FIRST instantiated. All subsequent 
			// instantiations will not re-fire all the static logic
			// otherwise that would be very im-performant
			return new PersistenceFileManagerService();
		}
	}
}
