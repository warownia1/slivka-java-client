package uk.ac.dundee.compbio.slivkaclient.http;

import java.io.Closeable;
import java.io.InputStream;
import java.util.Map;

public interface HttpResponse extends Closeable {
  public int getStatusCode();
  public Map<String, String> getHeaders();
  public InputStream getContent();
  public String getText();
  @Override
  public default void close() {}
}
