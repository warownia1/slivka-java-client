package uk.ac.dundee.compbio.slivkaclient;

import javajs.http.ClientProtocolException;
import javajs.http.HttpClient;
import javajs.http.HttpClientFactory;
import javajs.http.HttpResponseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

public class SlivkaClient {

  public static class Version {
    public final String server, API;

    private Version(String server, String API) {
      this.server = server;
      this.API = API;
    }

    @Override
    public String toString() {
      return String.format("Version(server=%s, API=%s)", server, API);
    }
  }

  private URI url;
  private HttpClient httpClient;
  private List<SlivkaService> services;

  public SlivkaClient(HttpClient httpClient, URI url) {
    this.httpClient = httpClient;
    this.url = url;
  }

  public SlivkaClient(URI address) {
    this(HttpClientFactory.getClient(null), address);
  }

  public SlivkaClient(String address) {
    this(URI.create(address));
  }

  public SlivkaClient(String host, int port, String path)
      throws URISyntaxException {
    this(new URI("http", null, host, port, path, null, null));
  }

  public SlivkaClient(String host, int port) throws URISyntaxException {
    this(host, port, "/");
  }

  public HttpClient getHttpClient() { return httpClient; }

  public void setHttpClient(final HttpClient httpClient) { this.httpClient = httpClient; }

  public URI getUrl() { return url; }

  public URI urlFor(String path) {
    return url.resolve(path);
  }

  public Version getVersion() throws IOException {
    HttpClient.HttpResponse response = getHttpClient()
        .get(getUrl().resolve("api/version")).execute();
    try (response) {
      int statusCode = response.getStatusCode();
      if (statusCode == 200) {
        try {
          JSONObject json = new JSONObject(response.getText());
          return new Version(
              json.getString("slivkaVersion"),
              json.getString("APIVersion"));
        }
        catch (JSONException e) {
          throw new ClientProtocolException("Unprocessable server response.", e);
        }
      }
      else {
        throw new HttpResponseException(
            response.getStatusCode(), response.getReasonPhrase());
      }
    }
  }

  public List<SlivkaService> getServices() throws IOException {
    if (this.services != null) {
      return this.services;
    }
    HttpClient.HttpResponse response = getHttpClient()
        .get(urlFor("api/services")).execute();
    try (response) {
      if (response.getStatusCode() != 200) {
        throw new HttpResponseException(
            response.getStatusCode(), response.getReasonPhrase());
      }
      JSONObject jsonData = new JSONObject(response.getText());
      ArrayList<SlivkaService> services = new ArrayList<>();
      JSONArray servicesArray = jsonData.getJSONArray("services");
      for (int i = 0; i < servicesArray.length(); ++i) {
        JSONObject srvc = servicesArray.getJSONObject(i);
        ArrayList<String> classifiers = new ArrayList<>();
        for (Object obj : srvc.getJSONArray("classifiers")) {
          classifiers.add(String.valueOf(obj));
        }
        ArrayList<Parameter> parameters = new ArrayList<>();
        JSONArray prmArray = srvc.getJSONArray("parameters");
        for (int j = 0; j < prmArray.length(); ++j) {
          parameters.add(Parameter.fromJSON(prmArray.getJSONObject(j)));
        }
        ArrayList<SlivkaService.Preset> presets = new ArrayList<>();
        JSONArray presetsArray = srvc.getJSONArray("presets");
        for (int j = 0; j < presetsArray.length(); ++j) {
          JSONObject obj = presetsArray.getJSONObject(j);
          presets.add(new SlivkaService.Preset(
              obj.getString("id"), obj.getString("name"),
              obj.getString("description"), obj.getJSONObject("values").toMap()));
        }
        var status = new SlivkaService.Status(
            srvc.getJSONObject("status").getString("status"),
            srvc.getJSONObject("status").getString("errorMessage"),
            srvc.getJSONObject("status").getString("timestamp"));
        services.add(new SlivkaService(
            this,
            urlFor(srvc.getString("@url")),
            srvc.getString("id"),
            srvc.getString("name"),
            srvc.getString("description"),
            srvc.getString("author"),
            srvc.getString("version"),
            srvc.getString("license"),
            classifiers,
            parameters,
            presets,
            status));
      }
      this.services = services;
    }
    catch (JSONException e) {
      throw new ClientProtocolException("Unprocessable server response.", e);
    }
    return this.services;
  }

  public SlivkaService getService(String id) throws Exception {
    for (SlivkaService service : getServices()) {
      if (id.equals(service.getId())) {
        return service;
      }
    }
    return null;
  }

  public RemoteFile uploadFile(File file) throws IOException {
    return uploadFile(new FileInputStream(file));
  }

  public RemoteFile uploadFile(InputStream stream) throws IOException {
    HttpClient.HttpResponse response = getHttpClient()
        .post(getUrl().resolve("api/files")).addFilePart("file", stream).execute();
    try (response) {
      int statusCode = response.getStatusCode();
      if (statusCode == 201) {
        JSONObject obj = new JSONObject(response.getText());
        return RemoteFile.fromJSON(this, obj);
      }
      else {
        throw new HttpResponseException(
            response.getStatusCode(), response.getReasonPhrase());
      }
    }
    catch (JSONException e) {
      throw new ClientProtocolException("Unprocessable server response.", e);
    }
  }
  
  public Job getJob(String id) throws IOException {
    URI url = getUrl().resolve(format("api/jobs/%s", id));
    HttpClient.HttpResponse response = getHttpClient().get(url).execute();
    try (response) {
      if (response.getStatusCode() == 200) {
        JSONObject obj = new JSONObject(response.getText());
        return Job.fromJSON(this, obj);
      }
      else {
        throw new HttpResponseException(
            response.getStatusCode(), response.getReasonPhrase());
      }
    }
    catch (JSONException e) {
      throw new ClientProtocolException("Unprocessable server response.", e);
    }
  }

  @Override
  public String toString() {
    return format("SlivkaClient(%s)", url.toString());
  }
}
