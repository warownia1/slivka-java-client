package uk.ac.dundee.compbio.slivkaclient;


import org.json.JSONObject;

public abstract class FormField {

  protected final FieldType type;
  protected final boolean required;
  protected final String name;
  protected final String label;
  protected final String description;
  protected final boolean multiple;

  protected FormField(
      FieldType type, String name, String label,
      String description, boolean required, boolean multiple
  ) {
    this.type = type;
    this.name = name;
    this.required = required;
    this.label = label;
    this.description = description;
    this.multiple = multiple;
  }

  /**
   * Gets the type of the field.
   * <p>
   * The type can be used to perform type casting to more specialised
   * field classes. The field types and the corresponding classes are:
   * {@link FieldType#INTEGER}: {@link IntegerField},
   * {@link FieldType#DECIMAL}: {@link DecimalField},
   * {@link FieldType#TEXT}: {@link TextField},
   * {@link FieldType#BOOLEAN}: {@link BooleanField},
   * {@link FieldType#CHOICE}: {@link ChoiceField},
   * {@link FieldType#FILE}: {@link FileField}
   *
   * @return Field type
   * @see FieldType
   * @see IntegerField
   * @see DecimalField
   * @see TextField
   * @see BooleanField
   * @see ChoiceField
   * @see FileField
   */
  public final FieldType getType() {
    return type;
  }

  /**
   * Gets the name of the field.
   *
   * @return The name of the field.
   */
  public final String getName() {
    return name;
  }

  /**
   * Indicates whether the field is required.
   *
   * @return true if field is required; false if not.
   */
  public final boolean isRequired() {
    return required;
  }

  @Deprecated
  public final boolean hasMultipleValues() {
    return multiple;
  }

  public final boolean isMultivalued() {
    return multiple;
  }

  public abstract Object getDefault();

  /**
   * Gets the human readable field label.
   *
   * @return Field label.
   */
  public final String getLabel() {
    return label;
  }

  /**
   * Gets the detailed description of the field.
   *
   * @return Description of the field.
   */
  public final String getDescription() {
    return description;
  }

  public String toString() {
    return String.format("%s(%s)", getClass().getName(), name);
  }
}