package uk.ac.dundee.compbio.slivkaclient.form;

@SuppressWarnings("serial")
public class ValidationException extends Exception {

	public final String code;
	public final String message;
	public final FormField field;
	
	public ValidationException(FormField field, String code, String message) {
		super(String.format("%s(%s): %s", field.getName(), code, message));
		this.field = field;
		this.code = code;
		this.message = message;
	}
	
	public String toString() {
		return String.format("%s(%s): %s", field.getName(), code, message);
	}
}
