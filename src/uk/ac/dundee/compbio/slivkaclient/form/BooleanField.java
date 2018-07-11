package uk.ac.dundee.compbio.slivkaclient.form;

public class BooleanField extends FormField {

	private final Boolean initial;
	
	BooleanField(String name, boolean required, Boolean initial) {
		super(FieldType.BOOLEAN, name, required);
		this.initial = initial;
	}
	
	public Boolean getDefault() {
		return initial;
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
		if (!(value instanceof Boolean))
			throw fail("type", "Not a valid Boolean");
		Boolean val = (Boolean) value;
		if (required && !val)
			throw fail("required", "Field is required");
		return val ? "true" : "false";
	}
	
}