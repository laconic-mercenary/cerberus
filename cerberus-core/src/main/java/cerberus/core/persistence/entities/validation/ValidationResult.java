package cerberus.core.persistence.entities.validation;

public interface ValidationResult {
	
	boolean isSuccessful();

	ValidationMessage getMessage();
}
