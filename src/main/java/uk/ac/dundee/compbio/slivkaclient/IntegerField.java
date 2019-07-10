package uk.ac.dundee.compbio.slivkaclient;


public class IntegerField extends FormField {

  private final Integer initial;


  private final Integer max;
  private final Integer min;

  IntegerField(String name, String label, String description,
               boolean required, Integer initial, Integer min, Integer max) {
    super(FieldType.INTEGER, name, label, description, required);
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

  @Override
  public Integer getDefault() {
    return initial;
  }

  public Integer getMax() {
    return max;
  }

  public Integer getMin() {
    return min;
  }
}