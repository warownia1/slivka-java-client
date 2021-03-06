package uk.ac.dundee.compbio.slivkaclient;


public class TextField extends FormField {

  private final String initial;
  private final Integer maxLength;


  private final Integer minLength;

  TextField(String name, String label, String description, boolean required, 
            boolean multiple, String initial, Integer minLength, Integer maxLength) {
    super(FieldType.TEXT, name, label, description, required, multiple);
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
  
  @Override
  public String valueOf(String value) {
    return value;
  }

  @Override
  public String getDefault() {
    return initial;
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  public Integer getMinLength() {
    return minLength;
  }
}