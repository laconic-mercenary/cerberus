package cerberus.core.eventbus.notifications;

import java.io.Serializable;

public interface ResourceAvailableNotification<T> extends Serializable {

	T getResource();
	
}
