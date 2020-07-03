package uk.ac.dundee.compbio.slivkaclient;

import static org.testng.Assert.assertEquals;

import java.net.URI;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javajs.http.HttpClientFactory;

@Test
public class SlivkaClientUrlTests {

  private SlivkaClient client = null;
  private final HttpClientStub httpClient = new HttpClientStub();
  private static final URI baseUri = URI.create("http://example.org/slivka/");

  @BeforeClass
  public void setUpClass() {
    HttpClientFactory.setDefaultClassName(HttpClientStub.class.getName());
    client = new SlivkaClient(baseUri);
    client.setHttpClient(httpClient);
  }

  @Test
  public void testBaseUri() {
    assertEquals(client.getUrl(), baseUri);
  }

  @Test
  public void testRelativeUriBuilder() {
    assertEquals(client.buildURL("api/services"),
        baseUri.resolve("api/services"));
  }

  @Test
  public void testAbsoluteUriBuilder() {
    assertEquals(client.buildURL("/slivka/files"), baseUri.resolve("files"));
  }
}
