package javajs.http;

public class HttpResponseException extends ClientProtocolException {

  private static final long serialVersionUID = -8481868921105838666L;
  
  private final int statusCode;
  private final String reasonPhrase;

  public HttpResponseException(int statusCode, String reasonPhrase, String message) {
    super(message);
    this.statusCode = statusCode;
    this.reasonPhrase = reasonPhrase;
  }  
  
  public HttpResponseException(int statusCode, String reasonPhrase) {
    this(statusCode, reasonPhrase, String.format("%d %s", statusCode, reasonPhrase));
  }
  
  public int getStatusCode() {
    return statusCode;
  }
  
  public String getReasonPhrase() {
    return reasonPhrase;
  }

}
