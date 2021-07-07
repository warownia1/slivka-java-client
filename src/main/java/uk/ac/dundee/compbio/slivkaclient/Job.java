package uk.ac.dundee.compbio.slivkaclient;

import java.io.IOException;
import java.net.URI;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javajs.http.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javajs.http.HttpResponseException;

public class Job {
  public enum Status {
    PENDING, REJECTED, ACCEPTED, QUEUED, RUNNING, COMPLETED, INTERRUPTED,
    DELETED, FAILED, ERROR, UNKNOWN;
  }

  private static final SimpleDateFormat dateFmt =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  protected final SlivkaClient client;
  protected final URI url;
  protected final String id;
  protected final String service;
  protected final List<Map.Entry<String, String>> parameters;
  protected final Date submissionTime;
  protected Date completionTime;
  protected Status status;

  Job(
      SlivkaClient client, URI url, String id, String service,
      List<Map.Entry<String, String>> parameters, String submissionTime,
      String completionTime, String status) {
    this.client = client;
    this.url = url;
    this.id = id;
    this.service = service;
    this.parameters = parameters;
    this.submissionTime = dateFmt.parse(submissionTime, new ParsePosition(0));
    setCompletionTime(completionTime);
    setStatus(status);
  }

  static Job fromJSON(SlivkaClient client, JSONObject obj) throws JSONException {
    JSONObject paramsObj = obj.getJSONObject("parameters");
    List<Map.Entry<String, String>> parameters = new ArrayList<>();
    for (String key : paramsObj.keySet()) {
      Object val = paramsObj.get(key);
      if (val instanceof String) {
        parameters.add(new AbstractMap.SimpleEntry<>(key, (String) val));
      }
      else if (val instanceof JSONArray) {
        for (Object it : (JSONArray) val) {
          if (!JSONObject.NULL.equals(it)) {
            parameters.add(new AbstractMap.SimpleEntry<>(key, String.valueOf(it)));
          }
        }
      }
      else {
        throw new JSONException("JSONObject[" + JSONObject.quote(key) +
            "] not a string or array of strings.");
      }
    }
    return new Job(
        client,
        client.getUrl().resolve(obj.getString("@url")),
        obj.getString("id"),
        obj.getString("service"),
        parameters,
        obj.getString("submissionTime"),
        obj.optString("completionTime", null),
        obj.getString("status"));
  }

  public URI getUrl() { return url; }

  public String getId() { return id; }

  public String getService() { return service; }

  public List<Map.Entry<String, String>> getParameters() { return parameters; }

  public Date getSubmissionTime() { return submissionTime; }

  public Date getCompletionTime() { return completionTime; }

  void setCompletionTime(Date date) { completionTime = date; }

  void setCompletionTime(String date) {
    if (date != null) {
      setCompletionTime(dateFmt.parse(date, new ParsePosition(0)));
    }
  }

  public Status getStatus() throws IOException {
    var response = client.getHttpClient().get(url).execute();
    try (response) {
      if (response.getStatusCode() == 200) {
        try {
          JSONObject obj = new JSONObject(response.getText());
          setStatus(obj.getString("status"));
        }
        catch (JSONException e) {
          throw new ClientProtocolException("Unprocessable server response.", e);
        }
      }
      else {
        throw new HttpResponseException(response.getStatusCode(), response.getReasonPhrase());
      }
    }
    return status;
  }

  void setStatus(String status) { this.status = Status.valueOf(status.toUpperCase()); }

  public Collection<RemoteFile> getResults() throws IOException {
    URI url = getUrl().resolve(getUrl().getPath() + "/files");
    var response = client.getHttpClient().get(url).execute();
    try (response) {
      if (response.getStatusCode() == 200) {
        Collection<RemoteFile> files = new ArrayList<>();
        try {
          JSONArray arr = new JSONObject(response.getText()).getJSONArray("files");
          for (int i = 0; i < arr.length(); i++) {
            files.add(RemoteFile.fromJSON(client, arr.getJSONObject(i)));
          }
        }
        catch (JSONException e) {
          throw new ClientProtocolException("Unprocessable server response.", e);
        }
        return files;
      }
      else {
        throw new HttpResponseException(response.getStatusCode(), response.getReasonPhrase());
      }
    }

  }
}
