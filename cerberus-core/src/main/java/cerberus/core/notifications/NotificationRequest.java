package cerberus.core.notifications;

import java.io.Serializable;

public interface NotificationRequest<T> extends Serializable {

	String getKey();

	Class<? extends NotificationRequestor<T>> getRequestorClass();

	T getData();

	public static final class Factory {

		public static <T> NotificationRequest<T> make(
				final Class<? extends NotificationRequestor<T>> requestorClass,
				final String key, final T data) {

			return new NotificationRequest<T>() {

				private static final long serialVersionUID = -2601683994381162244L;

				@Override
				public String getKey() {
					return key;
				}

				@Override
				public Class<? extends NotificationRequestor<T>> getRequestorClass() {
					return requestorClass;
				}

				@Override
				public T getData() {
					return data;
				}
			};
		}
	}
}
