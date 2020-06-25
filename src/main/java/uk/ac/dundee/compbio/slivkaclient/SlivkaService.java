package uk.ac.dundee.compbio.slivkaclient;

import static java.lang.String.format;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import javajs.http.HttpClient;
import org.json.JSONObject;

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
      HttpClient.HttpResponse response = client.getHttpClient().get(getURL()).execute();
      try (response) {
        int statusCode = response.getStatusCode();
        if (statusCode == 200) {
          JSONObject json = new JSONObject(response.getText());
          form = JSONFormFactory.getForm(client, json);
        } 
        else {
          throw new IOException(format("Unexpected status code: %d", statusCode));
        }
      }
    }
    return new SlivkaForm(form);
  }
}
