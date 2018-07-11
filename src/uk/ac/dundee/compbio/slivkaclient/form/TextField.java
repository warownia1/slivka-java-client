package uk.ac.dundee.compbio.slivkaclient.form;

public class TextField extends FormField {
	
	public final String initial;
	public final Integer maxLength;
	public final Integer minLength;
	
	TextField(String name, boolean required,
			String initial, Integer minLength, Integer maxLength) {
		super(FieldType.TEXT, name, required);
		this.initial = initial;
		this.minLength = minLength;
		this.maxLength = maxLength;
	}
	
	@Override
	public String validate(Object value) throws ValidationException {
		if (value == null)
			value = initial;
		if (value == null)
			if (required)
				throw fail("required", "Field is required");
			else
				return null;
		if (!(value instanceof String))
			throw fail("type", "Not a valid String");
		String val = (String) value;
		if (minLength != null && val.length() < minLength)
			throw fail("min length", "String is too short");
		if (maxLength != null && val.length() > maxLength)
			throw fail("max length", "String is too long");
		return val;
	}
	
}