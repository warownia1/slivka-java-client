package uk.ac.dundee.compbio.slivkaclient;

/**
 * Class representing an exception thrown during the field validation.
 * It is thrown by {@link FormField#validate(Object)} if any of the field constraints is violated.
 * It contains the error code, error message and the field that raised the error.
 *
 * @author Mateusz Warowny
 */
public class ValidationException extends Exception {

  private static final long serialVersionUID = -8887478266840943090L;
  private final String code;
  private final String message;
  private final FormField field;

  public ValidationException(FormField field, String code, String message) {
    super(String.format("%s(%s): %s", field.getName(), code, message));
    this.field = field;
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public FormField getField() {
    return field;
  }

  public String toString() {
    return String.format("%s(%s): %s", field.getName(), code, message);
  }
}
