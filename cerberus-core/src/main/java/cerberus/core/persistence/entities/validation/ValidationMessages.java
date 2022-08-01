package cerberus.core.persistence.entities.validation;

public class ValidationMessages {

	public static String required(String targetName) {
		return String.format("%s is required.", targetName);
	}

	public static String overflowingStr(String targetName, long maxlength) {
		return String.format("%s must be less than or equal to %d characters.",
				targetName, maxlength);
	}
}
