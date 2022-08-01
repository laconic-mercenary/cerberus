package cerberus.core.notifications.submission.impl;

import cerberus.core.eventbus.CerberusEventBus;
import cerberus.core.notifications.NotificationRequest;
import cerberus.core.notifications.submission.NotificationRequestSubmitter;

public class EventBusNotificationSubmitter implements
		NotificationRequestSubmitter {

	@Override
	public void submit(final NotificationRequest<?> request) {
		CerberusEventBus.get().post(new NotificationAsyncTasking(request));
	}

}
