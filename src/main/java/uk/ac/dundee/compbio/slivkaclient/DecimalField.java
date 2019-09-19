package uk.ac.dundee.compbio.slivkaclient;


public class DecimalField extends FormField {

  private final Double min;
  private final Double max;
  private final boolean minExc;
  private final boolean maxExc;
  private final Double initial;

  DecimalField(String name, String label, String description,
               boolean required, Double initial,
               Double min, Double max, boolean minExc, boolean maxExc) {
    super(FieldType.DECIMAL, name, label, description, required);
    this.initial = initial;
    this.min = min;
    this.max = max;
    this.minExc = minExc;
    this.maxExc = maxExc;
  }

  public Double getMin() {
    return min;
  }

  public Double getMax() {
    return max;
  }

  public boolean isMinExclusive() {
    return minExc;
  }

  public boolean isMaxExclusive() {
    return maxExc;
  }

  public Double getDefault() {
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

  @Override
  public Double valueOf(String value) {
    return value != null ? Double.valueOf(value) : null;
  }
}