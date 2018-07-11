package uk.ac.dundee.compbio.slivkaclient.form;

public abstract class FormField {
	
	public final FieldType type;
	public final boolean required;
	private final String name;
	
	FormField(FieldType type, String name, boolean required) {
		this.type = type;
		this.name = name;
		this.required = required;
	}

	public final FieldType getType() {
		return type;
	}
	
	public final String getName() {
		return name;
	}
	
	public final boolean isRequired() {
		return required;
	}

	public abstract String validate(Object value) throws ValidationException;
	
	protected ValidationException fail(String code, String message) {
		return new ValidationException(this, code, message);
	}
	
	public String toString() {
		return String.format("%s:%s", name, type.toString());
	}
}