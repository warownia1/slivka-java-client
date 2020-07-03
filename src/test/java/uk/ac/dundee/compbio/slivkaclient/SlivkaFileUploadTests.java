package uk.ac.dundee.compbio.slivkaclient;

import javajs.http.HttpClientFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.testng.Assert.*;
import static java.lang.String.format;

public class SlivkaFileUploadTests {

  SlivkaClient client = null;
  private final HttpClientStub httpClient = new HttpClientStub();
  private static final URI slivkaUrl = URI.create("http://example.org/slivka/");
  private HttpClientStub.HttpRequestMock req = null;
  private HttpClientStub.HttpResponseMock rep = null;
  private Path filePath = null;

  @BeforeClass
  public void setUpClass() throws IOException {
    HttpClientFactory.setDefaultClassName(HttpClientStub.class.getName());
    client = new SlivkaClient(slivkaUrl);
    client.setHttpClient(httpClient);
    rep = new HttpClientStub.HttpResponseMock();
    rep.setStatusCode(201);
    rep.setHeader("Content-Type", "application/json");
    rep.setContentLocation("/response/file-created-body.json");
    filePath = Files.createTempFile("", ".txt");
  }

  @BeforeMethod
  public void setUp() {
    req = new HttpClientStub.HttpRequestMock();
    req.setResponse(rep);
    httpClient.setRequests(List.of(req));
  }

  @Test
  public void testFileUploadRequest() throws IOException {
    client.uploadFile(filePath.toFile());
    assertEquals(req.getMethod(), "POST");
    var param = req.getFiles().get(0);
    assertEquals(param.getKey(), "file");
    assertTrue(param.getValue() instanceof InputStream
        | param.getValue() instanceof File);
  }

  @Test
  public void testFileUuid() throws IOException {
    var rf = client.uploadFile(filePath.toFile());
    assertEquals(rf.getUUID(), "YaSJJBbMVOcsHRcaDSTvos");
  }

  @Test
  public void testFileTitle() throws IOException {
    var rf = client.uploadFile(filePath.toFile());
    assertEquals(rf.getTitle(), "filename.txt");
  }

  @Test
  public void testFileLabel() throws IOException {
    var rf = client.uploadFile(filePath.toFile());
    assertEquals(rf.getLabel(), "uploaded");
  }

  @Test
  public void testFileMediaType() throws IOException {
    var rf = client.uploadFile(filePath.toFile());
    assertEquals(rf.getMimeType(), "text/plain");
  }

  @Test
  public void testFileUrl() throws IOException {
    var rf = client.uploadFile(filePath.toFile());
    assertEquals(rf.getURL(), slivkaUrl.resolve(format("api/files/%s", rf.getUUID())));
  }

  @Test
  public void testFileDownload() throws IOException {
    var rf = client.uploadFile(filePath.toFile());
    var req = new HttpClientStub.HttpRequestMock();
    var rep = new HttpClientStub.HttpResponseMock();
    rep.setStatusCode(200);
    rep.setContentLocation("/response/downloadable-file.txt");
    req.setResponse(rep);
    httpClient.setRequests(List.of(req));
    var content = new String(rf.getContent().readAllBytes());
    assertEquals(content, "example\n");
  }
}
