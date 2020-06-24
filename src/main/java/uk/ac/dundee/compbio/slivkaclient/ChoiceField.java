package uk.ac.dundee.compbio.slivkaclient;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ChoiceField extends FormField {

  private final String initial;
  private final Collection<String> choices;

  private ChoiceField(String name, String label, String description, boolean required,
              boolean multiple, String initial, Collection<String> choices) {
    super(FieldType.CHOICE, name, label, description, required, multiple);
    this.initial = initial;
    this.choices = Collections.unmodifiableCollection(choices);
  }

  static ChoiceField newFromJson(JSONObject json) {
    JSONArray choicesArray = json.getJSONArray("choices");
    ArrayList<String> choices = new ArrayList<>(choicesArray.length());
    for (Object obj: choicesArray) {
      choices.add((String) obj);
    }
    return new ChoiceField(
        json.getString("name"),
        json.getString("label"),
        json.getString("description"),
        json.getBoolean("required"),
        json.optBoolean("multiple", false),
        json.isNull("default") ? null : json.getString("default"),
        choices
    );
  }

  public String getDefault() {
    return initial;
  }

  public Collection<String> getChoices() {
    return choices;
  }

}