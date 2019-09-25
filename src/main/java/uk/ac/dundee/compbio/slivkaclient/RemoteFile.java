package uk.ac.dundee.compbio.slivkaclient;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class RemoteFile {
  private final SlivkaClient client;
  private final String uuid;
  private final String title;
  private final String mimeType;
  private final String path;

  RemoteFile(SlivkaClient client, String uuid, String title, String mimeType, String path) {
    this.client = client;
    this.uuid = uuid;
    this.title = title;
    this.mimeType = mimeType;
    this.path = path;
  }

  public String getUUID() {
    return uuid;
  }

  public String getTitle() {
    return title;
  }

  public String getMimeType() {
    return mimeType;
  }

  public URI getURL() {
    return client.buildURL(path);
  }

  public void dump(OutputStream out) throws IOException {
    CloseableHttpResponse response = client.httpClient.execute(new HttpGet(getURL()));
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode == 200) {
      response.getEntity().writeTo(out);
    } else {
      throw new HttpResponseException(statusCode, "Invalid server response");
    }
  }
  
  public InputStream getContent() throws IOException {
    CloseableHttpResponse response = client.httpClient.execute(new HttpGet(getURL()));
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode == 200) {
      return response.getEntity().getContent();
    }
    else {
      throw new HttpResponseException(statusCode, "Invalid server response");
    }
  }
}
