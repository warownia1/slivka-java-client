package uk.ac.dundee.compbio.slivkaclient.form;

import java.util.Collection;

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
