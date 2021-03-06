package uk.ac.dundee.compbio.slivkaclient;


import java.util.Collection;

/**
 * Class representing the exception thrown during form validation.
 * It is thrown by {@link SlivkaForm#submit()} if any of the fields throws a ValidationException.
 * THe collected ValidationException are passed to its constructor.
 *
 * @author mmwarowny
 * @see ValidationException
 */
public class FormValidationException extends Exception {

  private static final long serialVersionUID = -4704755112138413016L;
  private Collection<ValidationException> errors;

  FormValidationException(Collection<ValidationException> errors) {
    this.errors = errors;
  }

  public Collection<ValidationException> getErrors() {
    return errors;
  }
}
