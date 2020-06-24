package uk.ac.dundee.compbio.slivkaclient;


import org.json.JSONObject;

public final class BooleanField extends FormField {

  private final Boolean initial;

  private BooleanField(
      String name, String label, String description,
      boolean required, boolean multiple, Boolean initial
  ) {
    super(FieldType.BOOLEAN, name, label, description, required, multiple);
    this.initial = initial;
  }

  public static BooleanField newFromJson(JSONObject json) {
    return new BooleanField(
        json.getString("name"),
        json.getString("label"),
        json.getString("description"),
        json.getBoolean("required"),
        json.optBoolean("multiple", false),
        json.isNull("default") ? null : json.getBoolean("default")
    );
  }

  public Boolean getDefault() {
    return initial;
  }

}