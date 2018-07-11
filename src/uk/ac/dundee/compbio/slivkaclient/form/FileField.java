package uk.ac.dundee.compbio.slivkaclient.form;

import uk.ac.dundee.compbio.slivkaclient.FileHandler;

public class FileField extends FormField {

	FileField(String name, boolean required) {
		super(FieldType.FILE, name, required);
	}

	@Override
	public String validate(Object value) throws ValidationException {
		if (value == null)
			if (required)
				throw fail("required", "Field is required");
			else
				return null;
		if (!(value instanceof FileHandler))
			throw fail("type", "Invalid value type");
		FileHandler wrapper = (FileHandler) value;
		return wrapper.getID();
	}
	
}