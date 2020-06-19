package uk.ac.dundee.compbio.slivkaclient.http.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Consumer;

import uk.ac.dundee.compbio.slivkaclient.http.HttpClientWrapper;
import uk.ac.dundee.compbio.slivkaclient.http.HttpRequestBuilder;
import uk.ac.dundee.compbio.slivkaclient.http.HttpResponse;

/**
 * JavaScript implementation of the HttpClient.
 * For more details on HTTP methods see: https://www.w3schools.com/tags/ref_httpmethods.asp
 */
public class JSHttpClient implements HttpClientWrapper {

  public JSHttpClient() {
    // TODO Auto-generated constructor stub
  }

  /**
   * Initialises the GET request builder.
   * Usually they have no request body and parameters are passed in the URL query.
   */
  @Override
  public HttpRequestBuilder get(URI uri) {
    // TODO Auto-generated method stub
    return null;
  }
  
  /**
   * Initialises the GET request builder.
   * They have no request body and parameters are passed in the URL query.
   * They are identical to GET requests, the only difference is that the returned
   * response contains headers only.
   */
  @Override
  public HttpRequestBuilder head(URI uri) {
    // TODO Auto-generated method stub
    return null;
  }  

  /**
   * Initialises the POST request builder.
   * Usually they contain data in the request body either as a urlencoded form, 
   * a multipart form or raw bytes.
   * Currently, we only care about the multipart form. 
   */
  @Override
  public HttpRequestBuilder post(URI uri) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Initialises the PUT request builder which construct the same way as POST.
   * The only difference is the request method.
   */
  @Override
  public HttpRequestBuilder put(URI uri) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Initialises the DELETE request builder. The DELETE requests have no body
   * and parameters are passed in the URL query, just like GET.
   */
  @Override
  public HttpRequestBuilder delete(URI uri) {
    // TODO Auto-generated method stub
    return null;
  }
  
  private interface JSHttpRequestBuilder extends HttpRequestBuilder {
    /**
     * These callbacks work the same regardless of the builder
     * so it might be useful to move it up the class hierarchy
     */
    @Override
    public default HttpRequestBuilder doneCallback(
        Consumer<? super HttpResponse> consumer) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public default HttpRequestBuilder failCallback(
        Consumer<? super HttpResponse> consumer) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public default HttpRequestBuilder alwaysCallback(
        Consumer<? super HttpResponse> consumer) {
      // TODO Auto-generated method stub
      return null;
    }
  }

  /**
   * The class for building HTTP requests that contains no body.
   * Should be used to construct GET, HEAD and DELETE requests.
   */
  class HttpRequestWithoutBody implements JSHttpRequestBuilder {

    @Override
    public String getMethod() {
      // TODO Auto-generated method stub
      return null;
    }

    /**
     * Add a header to the request i.e. add a key-value to a Map which will be
     * passed as "headers" parameter to jQuery.ajax.
     */
    @Override
    public HttpRequestBuilder addHeader(String name, String value) {
      // TODO Auto-generated method stub
      return null;
    }

    /**
     * Add parameter to the request i.e. add a key-value pair to the map
     * which will be passed as "data" parameter to jQuery.ajax.
     * Parameters should be appended to the URL which ajax does automatically
     * for GET requests.  
     */
    @Override
    public HttpRequestBuilder addParameter(String name, String value) {
      // TODO Auto-generated method stub
      return null;
    }

    /**
     * Adding files to the request without body is not supported.
     * Either raise UnsupportedOperationException or create and return
     * a new HttpRequestWithBody here
     */
    @Override
    public HttpRequestBuilder addFile(String name, File file) {
      // TODO Auto-generated method stub
      return null;
    }

    /**
     * ditto
     */
    @Override
    public HttpRequestBuilder addFile(String name, InputStream stream) {
      // TODO Auto-generated method stub
      return null;
    }

    /**
     * Send the request to the server and return the response.
     */
    @Override
    public HttpResponse execute() throws IOException {
      /*
       * e.g.:
       * 
       * jQuery.ajax(url, {
       *    method: this.method,
       *    data: this.formData
       *    })
       *    .done(function() {
       *        foreach (consumer in doneConsumers) consumer.accept(response);
       *    })
       *    .fail(function() {
       *        foreach (consumer in failConsumers) consumer.accept(response);
       *    })
       *    .always(function() {
       *        foreach (consumer in alwaysConsumers) consumer.accept(response);
       *    });
       */
      return null;
    } 
  }

  /**
   * The class for building HTTP requests containing body content.
   * Should be used for POST and PUT requests.
   */
  class HttpRequestWithBody implements JSHttpRequestBuilder {

    /**
     * ditto
     */
    @Override
    public String getMethod() {
      // TODO Auto-generated method stub
      return null;
    }

    /**
     * ditto
     */
    @Override
    public HttpRequestBuilder addHeader(String name, String value) {
      // TODO Auto-generated method stub
      return null;
    }

    /**
     * ditto
     */
    @Override
    public HttpRequestBuilder addParameter(String name, String value) {
      // TODO Auto-generated method stub
      return null;
    }

    /**
     * Add file/stream to the request parameters.
     */
    @Override
    public HttpRequestBuilder addFile(String name, File file) {
      // TODO Auto-generated method stub
      return null;
    }

    /**
     * ditto
     */
    @Override
    public HttpRequestBuilder addFile(String name, InputStream stream) {
      // TODO Auto-generated method stub
      return null;
    }

    /**
     * ditto
     */
    @Override
    public HttpResponse execute() throws IOException {
      // TODO Auto-generated method stub
      return null;
    }
  }
}
