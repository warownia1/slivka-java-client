package uk.ac.dundee.compbio.slivkaclient;

import org.json.JSONArray;
import org.json.JSONObject;

import uk.ac.dundee.compbio.slivkaclient.http.HttpClientWrapper;
import uk.ac.dundee.compbio.slivkaclient.http.HttpResponse;
import uk.ac.dundee.compbio.slivkaclient.http.impl.ApacheHttpClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.String.format;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SlivkaClient
{

  private URI slivkaURL;

  private HttpClientWrapper httpClient = null;
  
  HttpClientWrapper getHttpClient() {
    if (httpClient == null) {
      httpClient = ApacheHttpClient.create();
    }
    return httpClient;
  }

  public SlivkaClient(URI address)
  {
    slivkaURL = address;
  }

  public SlivkaClient(String address)
  {
    this(URI.create(address));
  }

  public SlivkaClient(String host, int port, String path)
          throws URISyntaxException
  {
    this(new URI("http", null, host, port, path, null, null));
  }

  public SlivkaClient(String host, int port) throws URISyntaxException
  {
    this(host, port, null);
  }

  public URI getUrl()
  {
    return slivkaURL;
  }

  public URI buildURL(String path)
  {
    return slivkaURL.resolve(path);
  }
  
  public SlivkaVersion getVersion() throws IOException {
    HttpResponse response = getHttpClient().get(getUrl().resolve("api/version")).execute();
    try (response)
    {
      int statusCode = response.getStatusCode();
      if (statusCode == 200) {
        JSONObject json = new JSONObject(response.getText());
        return new SlivkaVersion(json.getString("slivka"), json.getString("api"));
      }
      else {
        throw new IOException(format("Unexpected status code: %d", statusCode));
      }
    }
  }

  public List<SlivkaService> getServices() throws IOException
  {
    HttpResponse response = getHttpClient().get(buildURL("api/services")).execute();
    try(response)
    {
      int statusCode = response.getStatusCode();
      if (statusCode == 200)
      {
        ArrayList<SlivkaService> services = new ArrayList<>();
        JSONObject jsonData = new JSONObject(response.getText());
        JSONArray servicesJson = jsonData.getJSONArray("services");
        for (int i = 0; i < servicesJson.length(); ++i)
        {
          JSONObject section = servicesJson.getJSONObject(i);
          JSONArray classifiersJsonArray = section
                  .getJSONArray("classifiers");
          ArrayList<String> classifiers = new ArrayList<>(
                  classifiersJsonArray.length());
          for (Object obj : classifiersJsonArray)
          {
            classifiers.add((String) obj);
          }
          services.add(new SlivkaService(this, section.getString("name"),
                  section.getString("label"), section.getString("URI"),
                  classifiers));
        }
        return services;
      }
      else
      {
        throw new IOException(format("Unexpected status code: %d", statusCode));
      }
    }
  }

  public SlivkaService getService(String name) throws IOException
  {
    for (SlivkaService service : getServices())
    {
      if (name.equals(service.getName()))
      {
        return service;
      }
    }
    return null;
  }
  
  public RemoteFile uploadFile(File file) throws IOException {
    return uploadFile(new FileInputStream(file));
  }
  
  public RemoteFile uploadFile(InputStream stream) throws IOException {
    HttpResponse response = getHttpClient()
        .post(getUrl().resolve("api/files"))
        .addFile("file", stream)
        .execute();
    try(response)
    {
      int statusCode = response.getStatusCode();
      if (statusCode == 201)
      {
        JSONObject json = new JSONObject(response.getText());
        return new RemoteFile(this, json.getString("uuid"),
                json.getString("title"), json.getString("label"),
                json.optString("mimetype"), json.getString("contentURI"));
      }
      else
      {
        throw new IOException(format("Unexpected status code: %d", statusCode));
      }
    }
  }

  @Deprecated
  public RemoteFile uploadFile(File input, String mimeType)
          throws IOException
  {
    return uploadFile(input);
  }

  @Deprecated
  public RemoteFile uploadFile(File input, String title, String mimeType)
          throws IOException
  {
    return uploadFile(input);
  }

  @Deprecated
  public RemoteFile uploadFile(InputStream input, String title, String mimeType)
          throws IOException
  {
    return uploadFile(input);
  }

  public JobState getJobState(String uuid) throws IOException
  {
    URI url = getUrl().resolve(format("api/tasks/%s", uuid));
    HttpResponse response = getHttpClient().get(url).execute();
    try
    {
      int statusCode = response.getStatusCode();
      if (statusCode == 200)
      {
        JSONObject json = new JSONObject(response.getText());
        return JobState.valueOf(json.getString("status").toUpperCase());
      }
      else
      {
        throw new IOException(format("Unexpected status code: %s", statusCode));
      }
    } finally
    {
      response.close();
    }
  }

  public List<RemoteFile> getJobResults(String uuid) throws IOException
  {
    URI url = buildURL(format("api/tasks/%s/files", uuid));
    HttpResponse response = getHttpClient().get(url).execute();
    try
    {
      int statusCode = response.getStatusCode();
      if (statusCode == 200)
      {
        List<RemoteFile> files = new ArrayList<>();
        JSONObject json = new JSONObject(response.getText());
        for (Object obj : json.getJSONArray("files"))
        {
          JSONObject fileJSON = (JSONObject) obj;
          files.add(new RemoteFile(this, fileJSON.getString("uuid"),
                  fileJSON.getString("title"), fileJSON.getString("label"),
                  fileJSON.optString("mimetype"),
                  fileJSON.getString("contentURI")));
        }
        return files;
      }
      else if (statusCode == 404)
      {
        return Collections.emptyList();
      }
      else
      {
        throw new IOException(format("Unexpected status code: %d", statusCode));
      }
    } finally
    {
      response.close();
    }
  }
  
  @Override
  public String toString() {
    return format("SlivkaClient(%s)", slivkaURL.toString());
  }
}
