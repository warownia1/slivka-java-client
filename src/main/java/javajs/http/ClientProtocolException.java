package javajs.http;

import java.io.IOException;

public class ClientProtocolException extends IOException {

  private static final long serialVersionUID = 2500725227517025772L;

  public ClientProtocolException() {
    super();
  }

  public ClientProtocolException(String message) {
    super(message);
  }

  public ClientProtocolException(Throwable cause) {
    super(cause);
  }

  public ClientProtocolException(String message, Throwable cause) {
    super(message, cause);
  }

}
