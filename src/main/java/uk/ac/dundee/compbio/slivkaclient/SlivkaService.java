package uk.ac.dundee.compbio.slivkaclient;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class SlivkaService {
  private final SlivkaClient client;
  public final String name;
  public final String path;
  public final String label;
  public final List<String> classifiers;
  private SlivkaForm form = null;

  SlivkaService(SlivkaClient client, String name, String label, String path, List<String> classifiers) {
    this.client = client;
    this.name = name;
    this.label = label;
    this.path = path;
    this.classifiers = Collections.unmodifiableList(classifiers);
  }

  public String getName() {
    return name;
  }
  
  public String getLabel() {
    return label;
  }

  public URI getURL() {
    return client.buildURL(path);
  }
  
  public List<String> getClassifiers() {
    return classifiers;
  }

  public SlivkaForm getForm() throws IOException {
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
    return new SlivkaForm(form);
  }
}
