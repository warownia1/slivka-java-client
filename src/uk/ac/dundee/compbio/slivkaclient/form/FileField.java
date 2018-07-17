package uk.ac.dundee.compbio.slivkaclient.form;

import uk.ac.dundee.compbio.slivkaclient.FileHandler;

public class FileField extends FormField {

	FileField(String name, String label, String description, boolean required) {
		super(FieldType.FILE, name, label, description, required);
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

	@Override
	public Object getDefault() {
		return null;
	}
	
}