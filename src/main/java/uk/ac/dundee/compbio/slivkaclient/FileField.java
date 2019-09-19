package uk.ac.dundee.compbio.slivkaclient;


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
    if (!(value instanceof RemoteFile))
      throw fail("type", "Invalid value type");
    RemoteFile wrapper = (RemoteFile) value;
    return wrapper.getUUID();
  }

  @Override
  public Object getDefault() {
    return null;
  }

  public Object valueOf(String value) {
    throw new RuntimeException("Cannot create file field value form string");
  }
}