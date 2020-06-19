package uk.ac.dundee.compbio.slivkaclient.http.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import uk.ac.dundee.compbio.slivkaclient.http.HttpClientWrapper;
import uk.ac.dundee.compbio.slivkaclient.http.HttpRequestBuilder;
import uk.ac.dundee.compbio.slivkaclient.http.HttpResponse;

public class ApacheHttpClient implements HttpClientWrapper {

  ApacheHttpClient() {
    // TODO Auto-generated constructor stub
  }
  
  public static HttpClientWrapper create() {
    return new ApacheHttpClient();
  }

  @Override
  public HttpRequestBuilder get(URI uri) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HttpRequestBuilder post(URI uri) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HttpRequestBuilder put(URI uri) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HttpRequestBuilder delete(URI uri) {
    // TODO Auto-generated method stub
    return null;
  }
 

  abstract class BaseRequestBuilder implements HttpRequestBuilder {
    HttpRequestBase request = null;
    
    protected BaseRequestBuilder(HttpRequestBase request) {
      this.request = request;
    }
        
    @Override
    public String getMethod() { return request.getMethod(); }

    @Override
    public HttpRequestBuilder addHeader(String name, String value) {
      request.addHeader(name, value);
      return this;
    }

    @Override
    public HttpRequestBuilder doneCallback(
        Consumer<? super HttpResponse> consumer) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public HttpRequestBuilder failCallback(Consumer<? super HttpResponse> consumer) {
      // TODO Auto-generated method stub
      return this;
    }

    @Override
    public HttpRequestBuilder alwaysCallback(
        Consumer<? super HttpResponse> consumer) {
      // TODO Auto-generated method stub
      return this;
    }

    @Override
    public HttpResponse execute() throws IOException {
      // TODO Auto-generated method stub
      return null;
    }
  }

  
  class GetRequestBuilder extends BaseRequestBuilder {
    GetRequestBuilder(String method) {
      super(new HttpGet());
    }

    @Override
    public HttpRequestBuilder addParameter(String name, String value) {
      throw new UnsupportedOperationException("GET does not support parameters.");
    }

    @Override
    public HttpRequestBuilder addFile(String name, File file) {
      throw new UnsupportedOperationException("GET does not support parameters.");
    }

    @Override
    public HttpRequestBuilder addFile(String name, InputStream stream) {
      throw new UnsupportedOperationException("GET does not support parameters.");
    }
  }
  
  class HttpResponseWrapper implements HttpResponse {
    CloseableHttpResponse response;
    
    HttpResponseWrapper(CloseableHttpResponse response) {
      this.response = response;
    }

    @Override
    public int getStatusCode() {
      return response.getStatusLine().getStatusCode();
    }

    @Override
    public Map<String, String> getHeaders() {
      LinkedHashMap<String, String> headers = new LinkedHashMap<>();
      for (var header : response.getAllHeaders()) {
        var name = header.getName();
        var value = header.getValue();
        if (headers.containsKey(name)) {
          // join multiple headers with comma preserving order: RFC 2616
          value = headers.get(name) + "," + value;
        }
        headers.put(name, value);
      }
      return headers;
    }

    @Override
    public InputStream getContent() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getText() {
      // TODO Auto-generated method stub
      return null;
    }
    
  }
  
}
