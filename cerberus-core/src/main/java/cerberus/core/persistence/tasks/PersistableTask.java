package cerberus.core.persistence.tasks;

import java.io.Serializable;

@Deprecated // consider revitilizing this when the need arises
public interface PersistableTask extends Serializable {
	void perform();
}
