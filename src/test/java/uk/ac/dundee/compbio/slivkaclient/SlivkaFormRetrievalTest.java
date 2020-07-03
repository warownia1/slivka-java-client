package uk.ac.dundee.compbio.slivkaclient;

import javajs.http.HttpClientFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class SlivkaFormRetrievalTest {
  private SlivkaForm form = null;
  private final HttpClientStub httpClient = new HttpClientStub();
  private static final URI slivkaUrl = URI.create("http://example.org/slivka/");

  @BeforeClass
  public void setUpClass() {
    HttpClientFactory.setDefaultClassName(HttpClientStub.class.getName());
    SlivkaClient client = new SlivkaClient(slivkaUrl);
    client.setHttpClient(httpClient);
    SlivkaService service = new SlivkaService(
        client, "mock-service", "Mock service",
        "api/services/mock-service", List.of()
    );
    var req = new HttpClientStub.HttpRequestMock();
    var rep = new HttpClientStub.HttpResponseMock();
    rep.setStatusCode(200);
    rep.setHeader("Content-Type", "application/json");
    rep.setContentLocation("/response/form-body.json");
    req.setResponse(rep);
    httpClient.setRequests(List.of(req));
    try (rep) {
      form = service.getForm();
    } catch (IOException e) {
      e.printStackTrace();
      throw new IOError(e);
    }
  }

  @Test
  public void testFormUrl() {
    assertEquals(form.getURL(), slivkaUrl.resolve("api/services/mock-service"));
  }

  @Test
  public void testFieldsList() {
    assertTrue(form.getField("file-field") instanceof FileField);
    assertTrue(form.getField("bool-field") instanceof BooleanField);
    assertTrue(form.getField("int-field") instanceof IntegerField);
    assertTrue(form.getField("float-field") instanceof DecimalField);
    assertTrue(form.getField("text-field") instanceof TextField);
    assertTrue(form.getField("choice-field") instanceof ChoiceField);
  }

  @Test
  public void testFieldNotRequired() {
    var field = form.getField("not-required-field");
    assertFalse(field.isRequired());
  }

  @Test
  public void testFieldRequired() {
    var field = form.getField("required-field");
    assertTrue(field.isRequired());
  }

  @Test
  public void testFieldMultivalue() {
    var field = form.getField("multivalue-field");
    assertTrue(field.isMultivalued());
  }

  @Test
  public void testFileFieldMediaType() {
    var field = (FileField) form.getField("file-field");
    assertEquals(field.getMediaType(), "application/json");
  }

  @Test
  public void testFileFieldMediaTypeParameters() {
    var field = (FileField) form.getField("file-field");
    assertEquals(
        field.getMediaTypeParameters(),
        Map.of("type", "array", "maxItems", "5")
    );
  }

  @Test
  public void testBoolFieldDefault() {
    var field = (BooleanField) form.getField("bool-field");
    assertEquals(field.getDefault(), Boolean.TRUE);
  }

  @Test
  public void testIntFieldDefault() {
    var field = (IntegerField) form.getField("int-field");
    assertEquals(field.getDefault(), Integer.valueOf(1));
  }

  @Test
  public void testIntFieldBounds() {
    var field = (IntegerField) form.getField("int-field");
    assertEquals(field.getMin(), Integer.valueOf(1));
    assertEquals(field.getMax(), Integer.valueOf(10));
  }

  @Test
  public void testDecFieldDefault() {
    var field = (DecimalField) form.getField("float-field");
    assertEquals(field.getDefault(), Double.valueOf(0.5));
  }

  @Test
  public void testDecFieldBounds() {
    var field = (DecimalField) form.getField("float-field");
    assertEquals(field.getMin(), Double.valueOf(0.0));
    assertEquals(field.getMax(), Double.valueOf(1.0));
    assertTrue(field.isMinExclusive());
    assertFalse(field.isMaxExclusive());
  }

  @Test
  public void testTextFieldDefault() {
    var field = (TextField) form.getField("text-field");
    assertEquals(field.getDefault(), "def-string");
  }

  @Test
  public void testChoiceFieldDefault() {
    var field = (ChoiceField) form.getField("choice-field");
    assertEquals(field.getDefault(), "select 2");
  }

  @Test
  public void testChoiceFieldChoices() {
    var field = (ChoiceField) form.getField("choice-field");
    assertEquals(
        field.getChoices(),
        List.of("select 1", "select 2", "select 3")
    );
  }
}