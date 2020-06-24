package uk.ac.dundee.compbio.slivkaclient;


import org.json.JSONObject;

public final class TextField extends FormField {

  private final String initial;
  private final Integer maxLength;
  private final Integer minLength;

  private TextField(
      String name, String label, String description, boolean required,
      boolean multiple, String initial, Integer minLength, Integer maxLength
  ) {
    super(FieldType.TEXT, name, label, description, required, multiple);
    this.initial = initial;
    this.minLength = minLength;
    this.maxLength = maxLength;
  }

  public static TextField newFromJson(JSONObject json) {
    return new TextField(
        json.getString("name"),
        json.getString("label"),
        json.getString("description"),
        json.getBoolean("required"),
        json.optBoolean("multiple", false),
        json.isNull("default") ? null : json.getString("default"),
        json.has("minLength") ? json.getInt("minLength") : null,
        json.has("maxLength") ? json.getInt("maxLength") : null
    );
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