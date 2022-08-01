package cerberus.core.persistence.entities.validation.impl;

import com.frontier.lib.validation.TextValidator;

import cerberus.core.persistence.entities.KeyValuePairProperty;
import cerberus.core.persistence.entities.validation.ValidationResult;
import cerberus.core.persistence.entities.validation.Validator;

public class KeyValuePairPropertyValidator implements
		Validator<KeyValuePairProperty> {

	private static final String MSG_REQUIRED_FIELD_FMT = "%s is required for %s";

	private static final String MSG_OVERFLOW_FMT = "%s must be less than %d characters in length.";

	@Override
	public ValidationResult validate(KeyValuePairProperty target) {
		BasicValidationResult result = new BasicValidationResult();
		if (check(target.getKeyEntry(), "KeyEntry",
				KeyValuePairProperty.KEY_ENTRY_MAX_LENTH, result)) {
			if (check(target.getValueEntry(), "ValueEntry",
					KeyValuePairProperty.VALUE_ENTRY_MAX_LENGTH, result)) {
				result.makeSuccessful();
			}
		}
		return result;
	}

	private static boolean check(String property, String name, int maxLen,
			BasicValidationResult result) {
		boolean success = false;
		if (TextValidator.isEmptyStr(property)) {
			result.setMessage(String.format(MSG_REQUIRED_FIELD_FMT, name,
					KeyValuePairProperty.class.getName()), false);
		} else {
			if (TextValidator.isOverflowing(property, maxLen)) {
				result.setMessage(
						String.format(MSG_OVERFLOW_FMT, name, maxLen), false);
			} else {
				success = true;
			}
		}
		return success;
	}

}
