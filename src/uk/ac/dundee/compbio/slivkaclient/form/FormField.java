package uk.ac.dundee.compbio.slivkaclient.form;

import java.util.Collection;
import java.util.Collections;


public abstract class FormField {
	
	public final FieldType type;
	public final boolean required;
	private final String name;
	
	public FormField(FieldType type, String name, boolean required) {
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


class DecimalField extends FormField {

	public final Double min;
	public final Double max;
	public final boolean minExc;
	public final boolean maxExc;
	public final Double initial;

	public DecimalField(String name, boolean required, Double initial,
			Double min, Double max, boolean minExc, boolean maxExc) {
		super(FieldType.DECIMAL, name, required);
		this.initial = initial;
		this.min = min;
		this.max = max;
		this.minExc = minExc;
		this.maxExc = maxExc;
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
		if (!(value instanceof Double))
			throw fail("type", "Not a valid double");
		Double val = (Double) value;
		if (max != null) {
			if (maxExc && val >= max)
				throw fail("max", "Value is too large");
			if (!maxExc && val > max)
				throw fail("max", "Value is too large");
		}
		if (min != null) {
			if (minExc && val <= min)
				throw fail("min", "Value is too small");
			if (!minExc && val < min)
				throw fail("min", "Value is too small");
		}
		return val.toString();
	}
	
}


class TextField extends FormField {
	
	public final String initial;
	public final Integer maxLength;
	public final Integer minLength;
	
	public TextField(String name, boolean required,
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

	
class BooleanField extends FormField {

	public final Boolean initial;
	
	public BooleanField(String name, boolean required, Boolean initial) {
		super(FieldType.BOOLEAN, name, required);
		this.initial = initial;
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


class ChoiceField extends FormField {
	
	public final String initial;
	public final Collection<String> choices;
	
	public ChoiceField(String name, boolean required, String initial, 
			Collection<String> choices) {
		super(FieldType.CHOICE, name, required);
		this.initial = initial;
		this.choices = Collections.unmodifiableCollection(choices);
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
		found: {
			for (String choice : choices) {
				if (choice.equals(value))
					break found;
			}
			throw fail("invalid choice", String.format("\"%s\" is not a valid choice", value));
		}
		return value.toString();
	}
	
}


class FileField extends FormField {

	public FileField(String name, boolean required) {
		super(FieldType.FILE, name, required);
	}

	@Override
	public String validate(Object value) {
		return null;
	}
	
}