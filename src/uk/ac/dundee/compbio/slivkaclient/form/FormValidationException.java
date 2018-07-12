package uk.ac.dundee.compbio.slivkaclient.form;

import java.util.Collection;

/**
 * Class representing the exception thrown during form validation.
 * It is thrown by {@link Form#submit()} if any of the fields throws a ValidationException.
 * THe collected ValidationException are passed to its constructor.
 * 
 * @author mmwarowny
 * @see ValidationException
 */
@SuppressWarnings("serial")
public class FormValidationException extends Exception {
	private Collection<ValidationException> errors;

	public FormValidationException(Collection<ValidationException> errors) {
		this.errors = errors;
	}
	
	public Collection<ValidationException> getErrors() {
		return errors;
	}
}
