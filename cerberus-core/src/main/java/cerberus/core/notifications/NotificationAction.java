package cerberus.core.notifications;

import java.io.Serializable;

public interface NotificationAction<T> extends Serializable {

	void setPrerequisiteData(T data);

	void performNotification();

}
