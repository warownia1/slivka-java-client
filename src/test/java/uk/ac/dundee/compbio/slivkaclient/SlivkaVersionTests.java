package uk.ac.dundee.compbio.slivkaclient;

import javajs.http.HttpClientFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class SlivkaVersionTests {

  private SlivkaClient client = null;
  private final HttpClientStub httpClient = new HttpClientStub();
  private static final URI slivkaUrl = URI.create("http://example.org/slivka/");
  private HttpClientStub.HttpRequestMock req = null;
  private HttpClientStub.HttpResponseMock rep = null;

  @BeforeClass
  public void setUpClass() {
    HttpClientFactory.setDefaultClassName(HttpClientStub.class.getName());
    client = new SlivkaClient(slivkaUrl);
    client.setHttpClient(httpClient);
  }

  @BeforeMethod
  public void setUp() {
    req = new HttpClientStub.HttpRequestMock();
    rep = new HttpClientStub.HttpResponseMock();
    rep.setStatusCode(200);
    rep.setContentLocation("/response/version-body.json");
    rep.setHeader("Content-Type", "application/json");
    req.setResponse(rep);
    httpClient.setRequests(List.of(req));
  }

  @AfterMethod
  public void tearDown() {
    try {
      rep.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  @Test
  public void testVersionRequest() {
    try {
      client.getVersion();
      assertEquals(req.getUri(), slivkaUrl.resolve("api/version"));
      assertEquals(req.getMethod(), "GET");
    } catch (IOException ioe) {
      fail("connection error", ioe);
    }
  }

  @Test
  public void testVersionNumber() {
    try {
      var version = client.getVersion();
      assertEquals(version.slivka, "0.6b3");
      assertEquals(version.api, "1.0");
    } catch (IOException ioe) {
      fail("connection error", ioe);
    }
  }
}
