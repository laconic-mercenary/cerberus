package cerberus.core.tasks;

import java.io.Serializable;

public interface AsyncTasking extends Serializable {

	void perform() throws Exception;
	
	boolean isPerformed();
	
}
