package uk.ac.dundee.compbio.slivkaclient;

import javajs.http.HttpClient;
import javajs.http.HttpResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.json.JSONObject;

public class RemoteFile {
  private final SlivkaClient client;
  private final URI url;
  private final URI contentUrl;
  private final String id;
  private final String jobId;
  private final String path;
  private final String label;
  private final String mediaType;

  public SlivkaClient getClient() { return client; }

  public URI getUrl() { return url; }

  public URI getContentUrl() { return contentUrl; }

  public String getId() { return id; }

  public String getJobId() { return jobId; }

  public String getPath() { return path; }

  public String getLabel() { return label; }

  public String getMediaType() { return mediaType; }

  RemoteFile(
      SlivkaClient client, URI url, URI contentUrl, String id, String jobId,
      String path, String label, String mediaType) {
    this.client = client;
    this.url = url;
    this.contentUrl = contentUrl;
    this.id = id;
    this.jobId = jobId;
    this.path = path;
    this.label = label;
    this.mediaType = mediaType;
  }
  
  static RemoteFile fromJSON(SlivkaClient client, JSONObject obj) {
    return new RemoteFile(
        client,
        client.getUrl().resolve(obj.getString("@url")),
        client.getUrl().resolve(obj.getString("@content")),
        obj.getString("id"),
        obj.optString("jobId"),
        obj.optString("path"),
        obj.optString("label"),
        obj.optString("mediaType"));
  }

  /**
   * Retrieve the file content from the server and write it to the
   * provided output stream.
   *
   * @param out stream the data is written to
   */
  public void writeTo(OutputStream out) throws IOException {
    HttpClient.HttpResponse response =
        client.getHttpClient().get(getContentUrl()).execute();
    try (response) {
      if (response.getStatusCode() == 200) {
        response.getContent().transferTo(out);
      }
      else {
        throw new HttpResponseException(
            response.getStatusCode(), response.getReasonPhrase());
      }
    }
  }

  /**
   * Retrieve the content of the file as an InputStream. The stream needs to be
   * closed when reading is finished. Multiple calls to this function will request
   * a new stream from the server.
   */
  public InputStream getContent() throws IOException {
    HttpClient.HttpResponse response =
        client.getHttpClient().get(getContentUrl()).execute();
    if (response.getStatusCode() == 200) {
      return response.getContent();
    }
    else {
      throw new HttpResponseException(
          response.getStatusCode(), response.getReasonPhrase());
    }
  }

  @Override
  public String toString() {
    return id;
  }
}
