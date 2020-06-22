package uk.ac.dundee.compbio.slivkaclient.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface HttpResponse extends Closeable {
  public int getStatusCode();
  public Map<String, String> getHeaders();
  public InputStream getContent() throws IOException;
  public String getText() throws IOException;
  @Override
  public default void close() throws IOException {}
}
