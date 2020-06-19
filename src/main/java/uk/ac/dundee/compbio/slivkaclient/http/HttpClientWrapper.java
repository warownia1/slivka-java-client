package uk.ac.dundee.compbio.slivkaclient.http;

import java.net.URI;

public interface HttpClientWrapper {

  public HttpRequestBuilder get(URI uri);
  public HttpRequestBuilder head(URI uri);
  public HttpRequestBuilder post(URI uri);
  public HttpRequestBuilder put(URI uri);
  public HttpRequestBuilder delete(URI uri);
  
}
