package uk.ac.dundee.compbio.slivkaclient.http.impl;

import javajs.http.HttpClient;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ApacheHttpClient implements HttpClient {

  private final CloseableHttpClient httpClient = HttpClients.createDefault();

  public ApacheHttpClient() {
  }

  @Override
  public HttpRequest get(URI uri) {
    return new BaseRequestBuilder(new HttpGet(uri));
  }

  @Override
  public HttpRequest head(URI uri) {
    return new BaseRequestBuilder(new HttpHead(uri));
  }

  @Override
  public HttpRequest post(URI uri) {
    return new FormRequestBuilder(new HttpPost(uri));
  }

  @Override
  public HttpRequest put(URI uri) {
    return new FormRequestBuilder(new HttpPut(uri));
  }

  @Override
  public HttpRequest delete(URI uri) {
    return new BaseRequestBuilder(new HttpDelete(uri));
  }

  private class BaseRequestBuilder implements HttpRequest {
    protected HttpRequestBase request;

    protected ArrayList<NameValuePair> queryParameters = new ArrayList<>();

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
    public BaseRequestBuilder addQueryParameter(String name, String value) {
      queryParameters.add(new BasicNameValuePair(name, value));
      return this;
    }

    @Override
    public BaseRequestBuilder clearQueryParameters(String name) {
      for (var it = queryParameters.iterator(); it.hasNext();) {
        NameValuePair pair = it.next();
        if (pair.getName().equals(name)) {
          it.remove();
        }
      }
      return this;
    }

    @Override
    public BaseRequestBuilder addFormPart(String name, String value) {
      throw new UnsupportedOperationException(
          "BaseRequestBuilder does not support form parts."
      );
    }

    @Override
    public BaseRequestBuilder addFilePart(
        String name, File file, String contentType, String fileName
    ) {
      throw new UnsupportedOperationException(
          "BaseRequestBuilder does not support file parts."
      );
    }

    @Override
    public BaseRequestBuilder addFilePart(
        String name, InputStream stream, String contentType, String fileName
    ) {
      throw new UnsupportedOperationException(
          "BaseRequestBuilder does not support parameters."
      );
    }

    @Override
    public BaseRequestBuilder clearFormParts(String name) {
      return this;
    }

    @Override
    public BaseRequestBuilder addHeader(String name, String value) {
      request.addHeader(name, value);
      return this;
    }

    @Override
    public HttpResponse execute() throws IOException {
      try {
        var uriWithQuery = new URIBuilder(getUri())
            .setParameters(queryParameters).build();
        request.setURI(uriWithQuery);
        return new HttpResponseWrapper(httpClient.execute(request));
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException(e);
      }
    }

    @Override
    public void executeAsync(
        Consumer<HttpResponse> done,
        BiConsumer<HttpResponse, ? super IOException> fail,
        BiConsumer<HttpResponse, ? super IOException> always
    ) {
      new Thread(() -> {
        try {
          try (HttpResponse response = execute()) {
            if (done != null)
              done.accept(response);
            if (always != null)
              always.accept(response, null);
            if (fail != null && response.getStatusCode() >= 400)
              fail.accept(response, null);
          }
        } catch (IOException e) {
          if (fail != null)
            fail.accept(null, e);
          if (always != null)
            always.accept(null, e);
        }
      }).start();
    }
  }

  private class FormRequestBuilder extends BaseRequestBuilder {

    protected HttpEntityEnclosingRequestBase request;

    private final ArrayList<NameValuePair> formParams = new ArrayList<>();

    List<NameValuePair> getParameters() {
      return formParams;
    }

    FormRequestBuilder(HttpEntityEnclosingRequestBase request) {
      super(request);
      this.request = request;
    }

    @Override
    public BaseRequestBuilder addFormPart(String name, String value) {
      formParams.add(new BasicNameValuePair(name, value));
      return this;
    }

    @Override
    public BaseRequestBuilder addFilePart(String name, InputStream stream) {
      MultipartRequestBuilder builder = new MultipartRequestBuilder(this);
      return builder.addFilePart(name, stream);
    }

    @Override
    public BaseRequestBuilder addFilePart(
        String name, InputStream stream, String contentType, String fileName
    ) {
      MultipartRequestBuilder builder = new MultipartRequestBuilder(this);
      return builder.addFilePart(name, stream, contentType, fileName);
    }

    @Override
    public BaseRequestBuilder addFilePart(String name, File file) {
      MultipartRequestBuilder builder = new MultipartRequestBuilder(this);
      return builder.addFilePart(name, file);
    }

    @Override
    public BaseRequestBuilder addFilePart(
        String name, File file, String contentType, String fileName
    ) {
      MultipartRequestBuilder builder = new MultipartRequestBuilder(this);
      return builder.addFilePart(name, file, contentType, fileName);
    }

    @Override
    public HttpResponse execute() throws IOException {
      UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
          formParams, Consts.UTF_8
      );
      request.setEntity(entity);
      return super.execute();
    }
  }

  private class MultipartRequestBuilder extends BaseRequestBuilder {

    protected HttpEntityEnclosingRequestBase request;

    private final MultipartEntityBuilder entityBuilder = MultipartEntityBuilder
        .create();

    MultipartRequestBuilder(FormRequestBuilder builder) {
      super(builder.request);
      this.request = builder.request;
      for (var param : builder.getParameters()) {
        entityBuilder.addTextBody(param.getName(), param.getValue());
      }
    }

    @Override
    public BaseRequestBuilder addFormPart(String name, String value) {
      entityBuilder.addTextBody(name, value);
      return this;
    }

    @Override
    public BaseRequestBuilder addFilePart(String name, InputStream stream) {
      // filename must be set, otherwise it won't be posted as a file
      entityBuilder
          .addBinaryBody(name, stream, ContentType.DEFAULT_BINARY, "file");
      return this;
    }

    @Override
    public BaseRequestBuilder addFilePart(
        String name, InputStream stream, String contentType, String fileName
    ) {
      entityBuilder.addBinaryBody(
          name, stream, ContentType.create(contentType), fileName
      );
      return this;
    }

    @Override
    public BaseRequestBuilder addFilePart(String name, File file) {
      entityBuilder.addBinaryBody(name, file);
      return this;
    }

    @Override
    public BaseRequestBuilder addFilePart(
        String name, File file, String contentType, String fileName
    ) {
      entityBuilder.addBinaryBody(
          name, file, ContentType.create(contentType), fileName
      );
      return this;
    }

    @Override
    public HttpResponse execute() throws IOException {
      request.setEntity(entityBuilder.build());
      return super.execute();
    }
  }

  private static class HttpResponseWrapper implements HttpResponse {

    CloseableHttpResponse response;

    HttpResponseWrapper(CloseableHttpResponse response) {
      this.response = response;
    }

    @Override
    public int getStatusCode() {
      return response.getStatusLine().getStatusCode();
    }
    
    @Override
    public String getReasonPhrase() {
      return response.getStatusLine().getReasonPhrase();
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
      return EntityUtils.toString(response.getEntity(), "UTF-8");
    }

    @Override
    public void close() throws IOException {
      response.close();
    }

  }

}
