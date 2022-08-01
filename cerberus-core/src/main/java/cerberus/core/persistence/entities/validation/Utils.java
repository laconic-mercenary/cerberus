package cerberus.core.persistence.entities.validation;

import cerberus.core.persistence.entities.validation.impl.BasicValidationResult;

import com.frontier.lib.validation.TextValidator;

final public class Utils {

	private static final String REQUIRED_MSG_FMT = "Entry: %s is required.";

	private static final String OVERFLOW_MSG_FMT = "Entry: %s must be less than %d in length: '%s'";

	public static ValidationResult checkStringField(String entry,
			String entryName, boolean allowEmpty, int maxLength) {
		BasicValidationResult vr = new BasicValidationResult();
		if (TextValidator.isEmptyStr(entry)) {
			if (!allowEmpty)
				vr.setMessage(String.format(REQUIRED_MSG_FMT, entryName), true);
			else
				vr.makeSuccessful();
		} else {
			if (TextValidator.isOverflowing(entry, maxLength)) {
				vr.setMessage(String.format(OVERFLOW_MSG_FMT, entryName,
						maxLength, entry), true);
			} else {
				vr.makeSuccessful();
			}
		}
		return vr;
	}

	public static ValidationResult checkNumericRange(long entry,
			String entryName, long min, long max) {
		BasicValidationResult vr = new BasicValidationResult();
		if (entry < min || entry > max) {
			vr.setMessage(String.format(
					"Entry: %s (%d) must be between %d and %d", entryName,
					entry, min, max), true);
		} else {
			vr.makeSuccessful();
		}
		return vr;
	}

	public static ValidationResult checkNotNull(Object entry, String entryName) {
		BasicValidationResult vr = new BasicValidationResult();
		if (entry == null)
			vr.setMessage(String.format("%s is required", entryName), true);
		else
			vr.makeSuccessful();
		return vr;
	}
	

	public static boolean isEqualSafe(Object obj1, Object obj2) {
		if (obj1 == null)
			return false;
		if (obj2 == null)
			return false;
		return obj2.equals(obj1);
	}

	public static int safeHashcode(Object obj) {
		return obj == null ? 0 : obj.hashCode();
	}

}
