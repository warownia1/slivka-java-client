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
import static java.lang.String.format;

public class SlivkaJobManagementTests {

  private SlivkaClient client = null;
  private final HttpClientStub httpClient = new HttpClientStub();
  private static final URI slivkaUrl = URI
      .create("http://example.org/slivka/");
  private static final String jobId = "frdeQI2PTa6hh4fBsptnXg";
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
    rep.setHeader("Content-Type", "application/json");
    req.setResponse(rep);
    httpClient.setRequests(List.of(req));
  }

  @AfterMethod
  public void tearDown() {
    try {
      rep.close();
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  @Test
  public void testJobStateRequest() {
    rep.setStatusCode(200);
    rep.setContentLocation("/response/job-completed-body.json");
    try {
      client.getJobState(jobId);
    }
    catch (IOException e) {
      fail("connection error", e);
    }
    assertEquals(req.getMethod(), "GET");
    assertEquals(
        req.getUri(), slivkaUrl.resolve(format("api/tasks/%s", jobId))
    );
  }

  @Test
  public void testCompletedState() {
    rep.setStatusCode(200);
    rep.setContentLocation("/response/job-completed-body.json");
    try {
      var state = client.getJobState(jobId);
      assertEquals(state, JobState.COMPLETED);
    }
    catch (IOException e) {
      fail("connection error", e);
    }
  }

  @Test
  public void testFailedState() {
    rep.setStatusCode(200);
    rep.setContentLocation("/response/job-failed-body.json");
    try {
      var state = client.getJobState(jobId);
      assertEquals(state, JobState.FAILED);
    }
    catch (IOException e) {
      fail("connection error", e);
    }
  }

  @Test
  public void testJobCancellation() {
    rep.setStatusCode(200);
    try {
      client.cancelJob(jobId);
    }
    catch (IOException e) {
      fail("connection error", e);
    }
    assertEquals(
        req.getUri(), slivkaUrl.resolve(format("api/tasks/%s", jobId))
    );
    assertEquals(req.getMethod(), "DELETE");
  }
}
