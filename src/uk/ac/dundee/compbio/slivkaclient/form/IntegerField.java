package uk.ac.dundee.compbio.slivkaclient.form;

public class IntegerField extends FormField {

	public final Integer initial;
	public final Integer max;
	public final Integer min;
	
	IntegerField(String name, boolean required, 
			Integer initial, Integer min, Integer max) {
		super(FieldType.INTEGER, name, required);
		this.initial = initial;
		this.min = min;
		this.max = max;
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
		if (!(value instanceof Integer))
			throw fail("type", "Not a valid integer");
		Integer val = (Integer) value;
		if (max != null && val > max)
			throw fail("max", "Value is too large");
		if (min != null && val < min)
			throw fail("min", "Value is too small");
		return val.toString();
	}	
	
}