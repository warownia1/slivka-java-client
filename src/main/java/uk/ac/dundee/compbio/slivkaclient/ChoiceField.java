package uk.ac.dundee.compbio.slivkaclient;


import java.util.Collection;
import java.util.Collections;

public class ChoiceField extends FormField {

  private final String initial;
  private final Collection<String> choices;

  ChoiceField(String name, String label, String description, boolean required, 
              boolean multiple, String initial, Collection<String> choices) {
    super(FieldType.CHOICE, name, label, description, required, multiple);
    this.initial = initial;
    this.choices = Collections.unmodifiableCollection(choices);
  }

  public String getDefault() {
    return initial;
  }

  public Collection<String> getChoices() {
    return choices;
  }

  @Override
  public String validate(Object value) throws ValidationException {
    if (value == null)
      value = initial;
    if (value == null) {
      if (required)
        throw fail("required", "Field is required");
      else
        return null;
    }

    final Object val = value;
    if (!choices.stream().anyMatch(choice -> choice.equals(val))) {
      throw fail("invalid choice", String.format("\"%s\" is not a valid choice", value));
    }
    return value.toString();
  }

  @Override
  public String valueOf(String value) {
    return value;
  }
}