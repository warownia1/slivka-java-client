package uk.ac.dundee.compbio.slivkaclient;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Task {

  @SuppressWarnings("unused")
  public enum StatusCode {
    PENDING("pending"),
    QUEUED("queued"),
    RUNNING("running"),
    COMPLETED("completed"),
    FAILED("failed"),
    ERROR("error"),
    UNKNOWN("unknown");

    public final String name;

    StatusCode(String name) {
      this.name = name;
    }

    static StatusCode forName(String name) {
      for (StatusCode status : values()) {
        if (name.equals(status.name)) {
          return status;
        }
      }
      return UNKNOWN;
    }
  }

  public class Status {
    private StatusCode code = StatusCode.PENDING;
    private boolean ready = false;

    private Status() {
    }

    public StatusCode getCode() {
      return code;
    }

    public boolean isReady() {
      return ready;
    }
  }

  private final SlivkaClient client;
  private final String uuid;
  private final String path;
  private String filesPath;
  private Status status;

  Task(SlivkaClient client, String uuid, String path) {
    this.client = client;
    this.uuid = uuid;
    this.path = path;
    this.filesPath = path + "/files";
    this.status = new Status();
  }

  public String getUUID() {
    return uuid;
  }

  public URI getURL() {
    return client.buildURL(path);
  }

  public Status getStatus() throws IOException {
    HttpGet request = new HttpGet(getURL());
    CloseableHttpResponse response = client.httpClient.execute(request);
    int statusCode = response.getStatusLine().getStatusCode();
    try {
      if (statusCode == 200) {
        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
        status.code = StatusCode.forName(json.getString("status").toLowerCase());
        status.ready = json.getBoolean("ready");
        filesPath = json.getString("filesURI");
      } else {
        throw new HttpResponseException(statusCode, "Invalid server response");
      }
    } finally {
      response.close();
    }
    return status;
  }

  public List<RemoteFile> getFiles() throws IOException {
    CloseableHttpResponse response = client.httpClient.execute(new HttpGet(filesPath));
    int statusCode = response.getStatusLine().getStatusCode();
    try {
      if (statusCode == 200) {
        List<RemoteFile> files = new ArrayList<>();
        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
        for (Object obj : json.getJSONArray("files")) {
          JSONObject fileJSON = (JSONObject) obj;
          files.add(new RemoteFile(
              client,
              fileJSON.getString("uuid"),
              fileJSON.getString("title"),
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
