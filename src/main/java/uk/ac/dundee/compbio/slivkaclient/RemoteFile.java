package uk.ac.dundee.compbio.slivkaclient;

import javajs.http.HttpClient;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class RemoteFile {
  private final SlivkaClient client;
  private final String uuid;
  private final String title;
  private final String label;
  private final String mimeType;
  private final String path;

  RemoteFile(
      SlivkaClient client, String uuid, String title, String label,
      String mimeType, String path
  ) {
    this.client = client;
    this.uuid = uuid;
    this.title = title;
    this.label = label;
    this.mimeType = mimeType;
    this.path = path;
  }

  public String getUUID() {
    return uuid;
  }

  public String getTitle() {
    return title;
  }

  public String getLabel() {
    return label;
  }

  public String getMimeType() {
    return mimeType;
  }

  public URI getURL() {
    return client.buildURL(path);
  }

  @Deprecated
  public void dump(OutputStream out) throws IOException {
    writeTo(out);
  }

  public void writeTo(OutputStream out) throws IOException {
    try (HttpClient.HttpResponse response = client.getHttpClient()
        .get(getURL()).execute()) {
      int statusCode = response.getStatusCode();
      if (statusCode == 200) {
        response.getContent().transferTo(out);
      }
      else {
        throw new IOException(format("Unexpected status code: %d", statusCode));
      }
    }
  }

  /**
   * Retrieve the content of the file as an InputStream. The stream needs to be
   * closed when reading is finished. Multiple calls to this function will create
   * a new stream from the server.
   * 
   * @return
   * @throws IOException
   */
  public InputStream getContent() throws IOException {
    HttpClient.HttpResponse response = client.getHttpClient().get(getURL()).execute();
    int statusCode = response.getStatusCode();
    if (statusCode == 200) {
      return response.getContent();
    }
    else {
      throw new IOException(format("Unexpected status code: %d", statusCode));
    }
  }

  @Override
  public String toString() {
    return uuid;
  }
}
