package cerberus.core.persistence.entities.validation.impl;

import cerberus.core.persistence.entities.FileManager;
import cerberus.core.persistence.entities.validation.Utils;
import cerberus.core.persistence.entities.validation.ValidationResult;
import cerberus.core.persistence.entities.validation.Validator;

public class FileManagerVisitor implements Validator<FileManager> {

	public static final class Factory {

		public static Validator<FileManager> make() {
			return new FileManagerVisitor();
		}

	}
	
	private FileManagerVisitor() { }

	@Override
	public ValidationResult validate(FileManager target) {
		ValidationResult vr = Utils.checkStringField(target.getDescription(),
				"Description", true, FileManager.DESCRIPTION_MAXLEN);
		if (vr.isSuccessful()) {
			vr = Utils.checkStringField(target.getInterval(), "Interval", true,
					FileManager.INTERVAL_MAXLEN);

			if (vr.isSuccessful()) {
				vr = Utils.checkStringField(target.getParameters(),
						"Parameters", true, FileManager.PARAMETERS_MAXLEN);

				if (vr.isSuccessful()) {
					vr = Utils.checkNotNull(target.getFileManagerEntry(),
							"FileManagerEntry");

					if (vr.isSuccessful()) {

						vr = Utils.checkNotNull(target.getTargetDirectory(),
								"TargetDirectory");
					}
				}
			}
		}
		return vr;
	}

}
