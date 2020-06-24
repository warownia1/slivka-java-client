package uk.ac.dundee.compbio.slivkaclient;


import org.json.JSONObject;

public final class IntegerField extends FormField {

  private final Integer initial;
  private final Integer max;
  private final Integer min;

  private IntegerField(
      String name, String label, String description, boolean required,
      boolean multiple, Integer initial, Integer min, Integer max
  ) {
    super(FieldType.INTEGER, name, label, description, required, multiple);
    this.initial = initial;
    this.min = min;
    this.max = max;
  }

  public static IntegerField newFromJson(JSONObject json) {
    return new IntegerField(
        json.getString("name"),
        json.getString("label"),
        json.getString("description"),
        json.getBoolean("required"),
        json.optBoolean("multiple", false),
        json.isNull("default") ? null : json.getInt("default"),
        json.has("min") ? json.getInt("min") : null,
        json.has("max") ? json.getInt("max") : null
    );
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