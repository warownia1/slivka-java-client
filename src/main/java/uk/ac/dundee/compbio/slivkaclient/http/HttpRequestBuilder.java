package uk.ac.dundee.compbio.slivkaclient.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public interface HttpRequestBuilder {

  public String getMethod();
  public HttpRequestBuilder addHeader(String name, String value);
  public HttpRequestBuilder addParameter(String name, String value);
  public HttpRequestBuilder addFile(String name, File file);
  public HttpRequestBuilder addFile(String name, InputStream stream);
  public HttpRequestBuilder doneCallback(Consumer<? super HttpResponse> consumer);
  public HttpRequestBuilder failCallback(Consumer<? super HttpResponse> consumer);
  public HttpRequestBuilder alwaysCallback(Consumer<? super HttpResponse> consumer);
  public HttpResponse execute() throws IOException;
}
