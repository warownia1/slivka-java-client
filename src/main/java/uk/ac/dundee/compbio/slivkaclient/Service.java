package uk.ac.dundee.compbio.slivkaclient;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;

public class Service {
  private final SlivkaClient client;
  private final String name;
  private final String path;
  private Form form = null;

  Service(SlivkaClient client, String name, String path) {
    this.client = client;
    this.name = name;
    this.path = path;
  }

  public String getName() {
    return name;
  }

  public URI getURL() {
    return client.buildURL(path);
  }

  public Form getForm() throws IOException {
    if (form == null) {
      CloseableHttpResponse response = client.httpClient.execute(new HttpGet(getURL()));
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200) {
        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
        form = JSONFormFactory.getForm(client, json);
      } else {
        throw new HttpResponseException(statusCode, "Invalid status code");
      }
    }
    return new Form(form);
  }
}
