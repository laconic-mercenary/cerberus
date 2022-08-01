package cerberus.core.notifications;

public interface NotificationRequestor<T> {

	void submitNotificationRequest(T dataToPass);

}
