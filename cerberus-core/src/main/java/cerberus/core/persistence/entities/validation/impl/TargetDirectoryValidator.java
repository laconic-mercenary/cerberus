package cerberus.core.persistence.entities.validation.impl;

import cerberus.core.persistence.entities.TargetDirectory;
import cerberus.core.persistence.entities.validation.Utils;
import cerberus.core.persistence.entities.validation.ValidationResult;
import cerberus.core.persistence.entities.validation.Validator;

public class TargetDirectoryValidator implements Validator<TargetDirectory> {

	@Override
	public ValidationResult validate(TargetDirectory target) {
		return Utils.checkStringField(target.getAbsolutePath(), "path", false,
				TargetDirectory.ABSOLUTE_PATH_LENGTH);
	}

}
