package uk.ac.dundee.compbio.slivkaclient;


import org.json.JSONObject;

public final class DecimalField extends FormField {

  private final Double initial;
  private final Double min;
  private final Double max;
  private final boolean minExc;
  private final boolean maxExc;

  private DecimalField(
      String name, String label, String description,
      boolean required, boolean multiple, Double initial,
      Double min, Double max, boolean minExc, boolean maxExc
  ) {
    super(FieldType.DECIMAL, name, label, description, required, multiple);
    this.initial = initial;
    this.min = min;
    this.max = max;
    this.minExc = minExc;
    this.maxExc = maxExc;
  }

  public static DecimalField newFromJson(JSONObject json) {
    return new DecimalField(
        json.getString("name"),
        json.getString("label"),
        json.getString("description"),
        json.getBoolean("required"),
        json.optBoolean("multiple", false),
        json.isNull("default") ? null : json.getDouble("default"),
        json.has("min") ? json.getDouble("min") : null,
        json.has("max") ? json.getDouble("max") : null,
        json.optBoolean("minExclusive", false),
        json.optBoolean("maxExclusive", false)
    );
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
}