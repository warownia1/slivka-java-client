package uk.ac.dundee.compbio.slivkaclient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javajs.http.HttpClient;

public class HttpClientStub implements HttpClient {

  private Iterator<HttpRequestMock> requests = null;

  public HttpClientStub() {
  }

  public void setRequests(Iterable<HttpRequestMock> requests) {
    this.requests = requests.iterator();
  }

  private HttpRequest getNextRequest(URI uri, String method) {
    var req = requests.next();
    req.setMethod(method);
    req.setUri(uri);
    return req;
  }

  @Override
  public HttpRequest get(URI uri) {
    return getNextRequest(uri, "GET");
  }

  @Override
  public HttpRequest head(URI uri) {
    return getNextRequest(uri, "HEAD");
  }

  @Override
  public HttpRequest post(URI uri) {
    return getNextRequest(uri, "POST");
  }

  @Override
  public HttpRequest put(URI uri) {
    return getNextRequest(uri, "PUT");
  }

  @Override
  public HttpRequest delete(URI uri) {
    return getNextRequest(uri, "DELETE");
  }

  public static class HttpRequestMock implements HttpClient.HttpRequest {

    private String method = "GET";
    private URI uri;
    private final List<Map.Entry<String, String>> headers = new ArrayList<>();
    private final List<Map.Entry<String, String>> stringParams = new ArrayList<>();
    private final List<Map.Entry<String, Object>> fileParams = new ArrayList<>();
    private HttpClient.HttpResponse response = null;

    HttpRequestMock() {
    }

    @Override
    public String getMethod() {
      return method;
    }

    void setMethod(String method) {
      this.method = method;
    }

    @Override
    public URI getUri() {
      return uri;
    }

    void setUri(URI uri) {
      this.uri = uri;
    }

    @Override
    public HttpRequest addHeader(String name, String value) {
      headers.add(new SimpleImmutableEntry<>(name, value));
      return this;
    }

    public List<Map.Entry<String, String>> getHeaders() {
      return headers;
    }

    @Override
    public HttpRequest addQueryParameter(String name, String value) {
      return this;
    }

    @Override
    public HttpRequest clearQueryParameters(String name) {
      return this;
    }

    @Override
    public HttpRequest addFormPart(String name, String value) {
      stringParams.add(Map.entry(name, value));
      return this;
    }

    public List<Map.Entry<String, String>> getFormParts() {
      return stringParams;
    }

    @Override
    public HttpRequest clearFormParts(String name) {
      for (var it = stringParams.iterator(); it.hasNext();) {
        var entry = it.next();
        if (entry.getKey().equals(name)) {
          it.remove();
        }
      }
      return this;
    }

    @Override
    public HttpRequest addFilePart(
        String name, File file, String contentType, String fileName
    ) {
      fileParams.add(Map.entry(name, file));
      return this;
    }

    @Override
    public HttpRequest addFilePart(
        String name, InputStream stream, String contentType, String fileName
    ) {
      fileParams.add(Map.entry(name, stream));
      return this;
    }

    public List<Map.Entry<String, Object>> getFiles() {
      return fileParams;
    }

    @Override
    public HttpResponse execute() throws IOException {
      if (response == null) {
        var response = new HttpResponseMock();
        this.response = response;
        response.setStatusCode(404);
        response.setContentLocation("/response/404-body.json");
      }
      return this.response;
    }

    @Override
    public void executeAsync(
        Consumer<HttpResponse> success,
        BiConsumer<HttpResponse, ? super IOException> failure,
        BiConsumer<HttpResponse, ? super IOException> always
    ) {
      // TODO Auto-generated method stub
    }

    void setResponse(HttpResponse response) {
      this.response = response;
    }

  }

  public static class HttpResponseMock implements HttpClient.HttpResponse {

    private int statusCode = 500;
    private Map<String, String> headers = new HashMap<>();
    private String contentLocation = null;

    public HttpResponseMock() {
    }

    @Override
    public int getStatusCode() {
      return statusCode;
    }

    public void setStatusCode(int statusCode) {
      this.statusCode = statusCode;
    }

    @Override
    public Map<String, String> getHeaders() {
      return headers;
    }

    public void setHeader(String name, String value) {
      headers.put(name, value);
    }

    @Override
    public InputStream getContent() throws IOException {
      if (contentLocation != null) {
        return getClass().getResourceAsStream(contentLocation);
      } else {
        return new InputStream() {
          @Override
          public int read() {
            return -1;
          }
        };
      }
    }

    @Override
    public String getText() throws IOException {
      return new String(getContent().readAllBytes());
    }

    public void setContentLocation(String location) {
      contentLocation = location;
    }

  }

}
