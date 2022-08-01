package cerberus.core.persistence.util;

public final class Keys {

	public static final String KEY_CLASS_PROP_FMT = "%s.%s";

	public static String make(Class<?> clazz, String property) {
		return make(clazz.getName(), property);
	}

	public static String make(String prefix, String property) {
		return String.format(KEY_CLASS_PROP_FMT, prefix, property);
	}

}
