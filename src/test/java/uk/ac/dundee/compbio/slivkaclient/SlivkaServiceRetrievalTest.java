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

public class SlivkaServiceRetrievalTest {

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
    rep.setHeader("Content-Type", "application/json");
    rep.setContentLocation("/response/services-list-body.json");
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
  public void testServiceListRequest() {
    try {
      client.getServices();
      assertEquals(req.getUri(), slivkaUrl.resolve("api/services"));
      assertEquals(req.getMethod(), "GET");
    } catch (IOException ioe) {
      fail("connection error", ioe);
    }
  }

  @Test
  public void testGetServicesList() {
    try {
      var services = client.getServices();
      var actual = services.stream()
          .map(SlivkaService::getName)
          .toArray();
      var expected = new String[]{"service-1", "service-2"};
      assertEquals(actual, expected);
    } catch (IOException ioe) {
      fail("connection error", ioe);
    }
  }

  @Test
  public void testServiceLabel() {
    try {
      var service = client.getService("service-1");
      assertEquals(service.getLabel(), "Service One");
    } catch (IOException ioe) {
      fail("connection error", ioe);
    }
  }

  @Test
  public void testServiceClassifiers() {
    try {
      var service = client.getService("service-1");
      assertEquals(service.getClassifiers(), List.of("Type=test"));
    } catch (IOException ioe) {
      fail("connection error", ioe);
    }
  }

  @Test
  public void testServiceUrl() {
    try {
      var service = client.getService("service-1");
      assertEquals(service.getURL(), slivkaUrl.resolve("api/services/service-1"));
    } catch (IOException ioe) {
      fail("connection error", ioe);
    }
  }

}
