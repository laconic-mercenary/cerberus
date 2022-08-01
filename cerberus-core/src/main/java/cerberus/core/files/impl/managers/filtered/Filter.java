package cerberus.core.files.impl.managers.filtered;

import java.nio.file.Path;
import java.util.Collection;

public interface Filter {

	public Collection<Path> doFilter(Collection<Path> files);

}
