package cerberus.core.files.impl.managers.filtered;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cerberus.core.files.impl.managers.TimedManager;

public abstract class FilteredTimedManager extends TimedManager {

	private List<Filter> filters = Collections.emptyList();

	public void addFilter(Filter filter) {
		if (filters.isEmpty())
			filters = new LinkedList<>();
		filters.add(filter);
	}

	protected Collection<Path> doFiltering(Collection<Path> files) {
		Collection<Path> newList = files;
		for (Filter filter : filters)
			newList = filter.doFilter(newList);
		return newList;
	}

	@Override
	protected void doHandleFiles(Collection<Path> files) {
		Collection<Path> filteredFiles = doFiltering(files);
		doHandle(filteredFiles);
	}

	abstract protected void doHandle(Collection<Path> filteredFiles);
}
