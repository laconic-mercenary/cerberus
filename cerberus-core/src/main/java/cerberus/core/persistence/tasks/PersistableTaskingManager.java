package cerberus.core.persistence.tasks;

import java.io.Serializable;
import java.util.Collection;

public interface PersistableTaskingManager extends Serializable {

	boolean schedule(long nextExecutableTimeUTC, PersistableTask task);

	Collection<PersistableTask> fetchReadyTasks();

}
