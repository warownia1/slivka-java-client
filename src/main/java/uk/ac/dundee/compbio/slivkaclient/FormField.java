package uk.ac.dundee.compbio.slivkaclient;


public abstract class FormField {

  private final FieldType type;
  protected final boolean required;
  private final String name;
  private final String label;
  private final String description;

  FormField(FieldType type, String name, String label,
            String description, boolean required) {
    this.type = type;
    this.name = name;
    this.required = required;
    this.label = label;
    this.description = description;
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

  /**
   * Tests if the value is valid for that field.
   * <p>
   * FormField subclasses override this method to provide specialised value checks.
   * Values passed to the form are checked by each field's validate method.
   *
   * @param value Value to be compared against field constraints
   * @return String to be submitted
   * @throws ValidationException If the value violates any of the field constraints.
   */
  public abstract String validate(Object value) throws ValidationException;
  
  public abstract Object valueOf(String value);

  protected ValidationException fail(String code, String message) {
    return new ValidationException(this, code, message);
  }

  public String toString() {
    return String.format("%s:%s", name, type.toString());
  }
}