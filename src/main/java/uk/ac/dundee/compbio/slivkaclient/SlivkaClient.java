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

  public List<SlivkaService> getServices() throws IOException {
    CloseableHttpResponse response = httpClient.execute(new HttpGet(buildURL("services")));
    int statusCode = response.getStatusLine().getStatusCode();
    try {
      if (statusCode == 200) {
        ArrayList<SlivkaService> services = new ArrayList<>();
        JSONObject jsonData = new JSONObject(EntityUtils.toString(response.getEntity()));
        JSONArray servicesJson = jsonData.getJSONArray("services");
        for (int i = 0; i < servicesJson.length(); ++i) {
          JSONObject section = servicesJson.getJSONObject(i);
          JSONArray classifiersJsonArray = section.getJSONArray("classifiers");
          ArrayList<String> classifiers = new ArrayList<>(classifiersJsonArray.length());
          for (Object obj: classifiersJsonArray) {
            classifiers.add((String) obj);
          }
          services.add(new SlivkaService(
              this,
              section.getString("name"),
              section.getString("label"),
              section.getString("URI"),
              classifiers
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

  public SlivkaService getService(String name) throws IOException {
    for (SlivkaService service : getServices()) {
      if (name.equals(service.getName())) {
        return service;
      }
    }
    return null;
  }

  public RemoteFile uploadFile(File input, String mimeType) throws IOException {
    return uploadFile(input, input.getName(), mimeType);
  }

  public RemoteFile uploadFile(File input, String title, String mimeType) throws IOException {
    HttpEntity entity = MultipartEntityBuilder.create()
        .addBinaryBody("file", input, ContentType.create(mimeType), title)
        .build();
    return postFileEntity(new BufferedHttpEntity(entity));
  }

  public RemoteFile uploadFile(InputStream input, String title, String mimeType) throws IOException {
    HttpEntity entity = MultipartEntityBuilder.create()
        .addBinaryBody("file", input, ContentType.create(mimeType), title)
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
            json.getString("label"),
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

  public JobState getJobState(String uuid) throws IOException {
    URI url = buildURL(String.format("tasks/%s", uuid));
    CloseableHttpResponse response = httpClient.execute(new HttpGet(url));
    int statusCode = response.getStatusLine().getStatusCode();
    try {
      if (statusCode == 200) {
        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
        return JobState.valueOf(json.getString("status").toUpperCase());
      } else {
        throw new HttpResponseException(statusCode, "Invalid server response");
      }
    } finally {
      response.close();
    }
  }

  public List<RemoteFile> getJobResults(String uuid) throws IOException {
    URI url = buildURL(String.format("tasks/%s/files", uuid));
    CloseableHttpResponse response = httpClient.execute(new HttpGet(url));
    int statusCode = response.getStatusLine().getStatusCode();
    try {
      if (statusCode == 200) {
        List<RemoteFile> files = new ArrayList<>();
        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
        for (Object obj : json.getJSONArray("files")) {
          JSONObject fileJSON = (JSONObject) obj;
          files.add(new RemoteFile(
              this,
              fileJSON.getString("uuid"),
              fileJSON.getString("title"),
              fileJSON.getString("label"),
              fileJSON.getString("mimetype"),
              fileJSON.getString("contentURI")
          ));
        }
        return files;
      } else {
        throw new HttpResponseException(statusCode, "Invalid server response");
      }
    } finally {
      response.close();
    }
  }
}
