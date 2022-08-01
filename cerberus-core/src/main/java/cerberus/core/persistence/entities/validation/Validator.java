package cerberus.core.persistence.entities.validation;

// this can actually be a generic validator,
// doesn't necessarily need to be used for entities only
public interface Validator<T> {

	ValidationResult validate(T target);
}
