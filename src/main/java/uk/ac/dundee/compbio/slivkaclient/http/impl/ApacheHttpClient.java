package uk.ac.dundee.compbio.slivkaclient.http.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import uk.ac.dundee.compbio.slivkaclient.http.HttpClientWrapper;
import uk.ac.dundee.compbio.slivkaclient.http.HttpRequestBuilder;
import uk.ac.dundee.compbio.slivkaclient.http.HttpResponse;


public class ApacheHttpClient implements HttpClientWrapper {

  private CloseableHttpClient httpClient = HttpClients.createDefault();
  
  private ApacheHttpClient() {}
  
  public static HttpClientWrapper create() {
    return new ApacheHttpClient();
  }

  @Override
  public HttpRequestBuilder get(URI uri) {
    return new BaseRequestBuilder(new HttpGet(uri));
  }
  
  @Override
  public HttpRequestBuilder head(URI uri) {
    return new BaseRequestBuilder(new HttpHead(uri));
  }

  @Override
  public HttpRequestBuilder post(URI uri) {
    return new FormRequestBuilder(new HttpPost(uri));
  }

  @Override
  public HttpRequestBuilder put(URI uri) {
    return new FormRequestBuilder(new HttpPut(uri));
  }

  @Override
  public HttpRequestBuilder delete(URI uri) {
    return new BaseRequestBuilder(new HttpDelete(uri));
  }
 

  private class BaseRequestBuilder implements HttpRequestBuilder {
    protected HttpRequestBase request;
    
    BaseRequestBuilder(HttpRequestBase request) {
      this.request = request;
    }
        
    @Override
    public String getMethod() {
      return request.getMethod(); 
    }
    
    @Override
    public URI getUri() {
      return request.getURI();
    }

    @Override
    public HttpRequestBuilder addParameter(String name, String value) {
      throw new UnsupportedOperationException("BaseRequestBuilder does not support parameters.");
    }

    @Override
    public HttpRequestBuilder addFile(String name, File file) {
      throw new UnsupportedOperationException("BaseRequestBuilder does not support parameters.");
    }

    @Override
    public HttpRequestBuilder addFile(String name, InputStream stream) {
      throw new UnsupportedOperationException("BaseRequestBuilder does not support parameters.");
    }

    @Override
    public HttpRequestBuilder addHeader(String name, String value) {
      request.addHeader(name, value);
      return this;
    }

    @Override
    public HttpResponse execute() throws IOException {
      return new HttpResponseWrapper(httpClient.execute(request));
    }

    @Override
    public void executeAsync(
        Consumer<HttpResponse> done,
        BiConsumer<HttpResponse, ? super IOException> fail,
        BiConsumer<HttpResponse, ? super IOException> always)
    {
      new Thread(() -> {
        try {
          try (HttpResponse response = execute()) {
            if (done != null) done.accept(response);
            if (always != null) always.accept(response, null);
            if (fail != null && response.getStatusCode() >= 400) fail.accept(response, null);
          }
        }
        catch (IOException e) {
          if (fail != null) fail.accept(null, e);
          if (always != null) always.accept(null, e);
        }
      }).start();
    }
  }

  
  private class FormRequestBuilder extends BaseRequestBuilder {
    
    protected HttpEntityEnclosingRequestBase request;
    private ArrayList<NameValuePair> formParams = new ArrayList<>();
    
    List<NameValuePair> getParameters() {
      return formParams;
    }
    
    FormRequestBuilder(HttpEntityEnclosingRequestBase request) {
      super(request);
      this.request = request;
    }
    
    @Override
    public HttpRequestBuilder addParameter(String name, String value) {
      formParams.add(new BasicNameValuePair(name, value));
      return this;
    }
    
    @Override
    public HttpRequestBuilder addFile(String name, InputStream stream) {
      MultipartRequestBuilder builder = new MultipartRequestBuilder(this);
      return builder.addFile(name, stream);
    }
    
    @Override
    public HttpRequestBuilder addFile(String name, File file) {
      MultipartRequestBuilder builder = new MultipartRequestBuilder(this);
      return builder.addFile(name, file);
    }
    
    @Override
    public HttpResponse execute() throws IOException {
      UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
      request.setEntity(entity);
      return super.execute();
    }
  }
  
  
  private class MultipartRequestBuilder extends BaseRequestBuilder {
    
    protected HttpEntityEnclosingRequestBase request;
    private MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
    
    MultipartRequestBuilder(FormRequestBuilder builder) {
      super(builder.request);
      this.request = builder.request;
      for (var param : builder.getParameters()) {
        entityBuilder.addTextBody(param.getName(), param.getValue());
      }
    }
    
    @Override
    public HttpRequestBuilder addParameter(String name, String value) {
      entityBuilder.addTextBody(name, value);
      return this;
    }
    
    @Override
    public HttpRequestBuilder addFile(String name, InputStream stream) {
      // filename must be set, otherwise it won't be posted as a file
      entityBuilder.addBinaryBody(name, stream, ContentType.DEFAULT_BINARY, "file");
      return this;
    }
    
    @Override
    public HttpRequestBuilder addFile(String name, File file) {
      entityBuilder.addBinaryBody(name, file);
      return this;
    }
    
    @Override
    public HttpResponse execute() throws IOException {
      request.setEntity(entityBuilder.build());
      return super.execute();
    }
  }
  
  
  private class HttpResponseWrapper implements HttpResponse {
    
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
      Map<String, String> headers = new LinkedHashMap<>();
      for (var header : response.getAllHeaders()) {
        String name = header.getName();
        String value = header.getValue();
        if (headers.containsKey(name)) {
          // join multiple headers with comma preserving order: RFC 2616
          value = headers.get(name) + "," + value;
        }
        headers.put(name, value);
      }
      return headers;
    }

    @Override
    public InputStream getContent() throws IOException {
      return response.getEntity().getContent();
    }

    @Override
    public String getText() throws IOException {
      return EntityUtils.toString(response.getEntity());
    }
    
    @Override
    public void close() throws IOException {
      response.close();
    }
    
  }
  
}
