package uk.ac.dundee.compbio.slivkaclient;

import javajs.http.HttpClientFactory;
import org.json.JSONObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class SlivkaFormSubmissionTests {
  private SlivkaForm form = null;
  private final HttpClientStub httpClient = new HttpClientStub();
  private static final URI slivkaUrl = URI.create("http://example.org/slivka/");
  private HttpClientStub.HttpRequestMock req = null;
  private HttpClientStub.HttpResponseMock rep = null;

  @BeforeClass
  public void setUpClass() throws IOException {
    HttpClientFactory.setDefaultClassName(HttpClientStub.class.getName());
    SlivkaClient client = new SlivkaClient(slivkaUrl);
    client.setHttpClient(httpClient);
    byte[] bytes = getClass().getResourceAsStream("/response/form-body.json").readAllBytes();
    var json = new JSONObject(new String(bytes, StandardCharsets.UTF_8));
    form = JSONFormFactory.getForm(client, json);
  }

  @BeforeMethod
  public void setUp() {
    form.clear();
    req = new HttpClientStub.HttpRequestMock();
    rep = new HttpClientStub.HttpResponseMock();
    rep.setHeader("Content-Type", "application/json");
    rep.setStatusCode(202);
    rep.setContentLocation("/response/job-created-body.json");
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
  public void testRequest() {
    try {
      form.submit();
      assertEquals(req.getMethod(), "POST");
      assertEquals(req.getUri(), slivkaUrl.resolve("api/services/mock-service"));
    } catch (IOException | FormValidationException e) {
      fail("form submission error", e);
    }
  }

  @Test
  public void testTextParameter() {
    form.add("text-field", "foobar");
    try {
      form.submit();
      var param = req.getParameters().get(0);
      assertEquals(param.getKey(), "text-field");
      assertEquals(param.getValue(), "foobar");
    } catch (FormValidationException | IOException e) {
      fail("form submission error", e);
    }
  }

  @Test
  public void testNumberParameter() {
    form.add("int-field", 10);
    try {
      form.submit();
      var param = req.getParameters().get(0);
      assertEquals(param.getKey(), "int-field");
      assertEquals(param.getValue(), "10");
    } catch (FormValidationException | IOException e) {
      fail("form submission error", e);
    }
  }

  @Test
  public void testFileParameter() {
    var file = new File("/tmp/example");
    form.add("file-field", file);
    try {
      form.submit();
      var param = req.getFiles().get(0);
      assertEquals(param.getKey(), "file-field");
      assertEquals(param.getValue(), file);
    } catch (IOException | FormValidationException e) {
      fail("form submission error", e);
    }
  }

  @Test
  public void testMultipleParameters() {
    form.add("text-field", "foo");
    form.add("text-field", "bar");
    form.add("text-field", "quz");
    try {
      form.submit();
      var actual = req.getParameters().stream()
          .filter(item -> item.getKey().equals("text-field"))
          .map(Map.Entry::getValue)
          .toArray();
      var expected = new String[] {"foo", "bar", "quz"};
      assertEquals(actual, expected);
    } catch (IOException | FormValidationException e) {
      fail("form submission error", e);
    }
  }

  @Test
  public void testReturnedJob() {
    try {
      var jobId = form.submit();
      assertEquals(jobId, "frdeQI2PTa6hh4fBsptnXg");
    } catch (IOException | FormValidationException e) {
      fail("form submission error", e);
    }
  }

}
