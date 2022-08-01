package cerberus.core.persistence.entities.validation.impl;

import cerberus.core.persistence.entities.FileManagerEntry;
import cerberus.core.persistence.entities.validation.Utils;
import cerberus.core.persistence.entities.validation.ValidationResult;
import cerberus.core.persistence.entities.validation.Validator;

public class FileManagerEntryValidator implements Validator<FileManagerEntry> {

	public static final class Factory {

		public static Validator<FileManagerEntry> make() {
			return new FileManagerEntryValidator();
		}

	}

	private FileManagerEntryValidator() {
	}

	@Override
	public ValidationResult validate(FileManagerEntry target) {
		ValidationResult result = Utils.checkStringField(target.getClassName(),
				"ClassName", false, FileManagerEntry.CLASSNAME_MAXLEN);
		return result;
	}

}
