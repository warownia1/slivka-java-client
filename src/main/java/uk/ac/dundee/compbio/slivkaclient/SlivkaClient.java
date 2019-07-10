package uk.ac.dundee.compbio.slivkaclient;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class SlivkaClient {

  private URI slivkaURL;
  CloseableHttpClient httpClient = HttpClients.createDefault();

  public SlivkaClient(String host, int port) throws URISyntaxException {
    slivkaURL = new URIBuilder().setScheme("http").setHost(host).setPort(port).build();
  }

  public URI getUrl() {
    return slivkaURL;
  }

  public URI buildURL(String path) {
    try {
      return new URIBuilder(slivkaURL).setPath(path).build();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Service> getServices() throws IOException {
    CloseableHttpResponse response = httpClient.execute(new HttpGet(buildURL("services")));
    int statusCode = response.getStatusLine().getStatusCode();
    try {
      if (statusCode == 200) {
        List<Service> services = new ArrayList<>();
        JSONObject jsonData = new JSONObject(EntityUtils.toString(response.getEntity()));
        JSONArray servicesJson = jsonData.getJSONArray("services");
        for (int i = 0; i < servicesJson.length(); ++i) {
          services.add(new Service(
              this,
              servicesJson.getJSONObject(i).getString("name"),
              servicesJson.getJSONObject(i).getString("URI")
          ));
        }
        return services;
      } else {
        throw new HttpResponseException(statusCode, "Invalid status code");
      }
    } finally {
      response.close();
    }
  }

  public Service getService(String name) throws IOException {
    for (Service service : getServices()) {
      if (name.equals(service.getName())) {
        return service;
      }
    }
    return null;
  }

  public RemoteFile uploadFile(File input, ContentType mimeType) throws IOException {
    return uploadFile(input, input.getName(), mimeType);
  }

  public RemoteFile uploadFile(File input, String title, ContentType mimeType) throws IOException {
    HttpEntity entity = MultipartEntityBuilder.create()
        .addBinaryBody("file", input, mimeType, title)
        .build();
    return postFileEntity(new BufferedHttpEntity(entity));
  }

  public RemoteFile uploadFile(InputStream input, String title, ContentType mimeType) throws IOException {
    HttpEntity entity = MultipartEntityBuilder.create()
        .addBinaryBody("file", input, mimeType, title)
        .build();
    return postFileEntity(new BufferedHttpEntity(entity));
  }

  private RemoteFile postFileEntity(HttpEntity entity) throws IOException {
    HttpPost request = new HttpPost(buildURL("files"));
    request.setEntity(entity);
    CloseableHttpResponse response = httpClient.execute(request);
    int statusCode = response.getStatusLine().getStatusCode();
    try {
      if (statusCode == 201) {
        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
        return new RemoteFile(
            this,
            json.getString("uuid"),
            json.getString("title"),
            json.getString("mimetype"),
            json.getString("contentURI")
        );
      } else {
        throw new HttpResponseException(statusCode, "Invalid status code");
      }
    } finally {
      response.close();
    }
  }
}
