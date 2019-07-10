package uk.ac.dundee.compbio.slivkaclient;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.*;

public class Form {
  private final SlivkaClient client;
  private final String name;
  private final Map<String, FormField> fields;
  private final String path;
  private final HashMap<String, Object> values;

  Form(SlivkaClient client, String name, Map<String, FormField> fields, String path) {
    this.client = client;
    this.name = name;
    this.fields = Collections.unmodifiableMap(fields);
    this.path = path;
    this.values = new HashMap<>();
  }

  @SuppressWarnings("CopyConstructorMissesField")
  Form(Form copyFrom) {
    this(copyFrom.client, copyFrom.name, copyFrom.fields, copyFrom.path);
  }

  public String getName() {
    return name;
  }

  public URI getURL() {
    return client.buildURL(path);
  }

  public Collection<FormField> getFields() {
    return fields.values();
  }

  public FormField getField(String name) {
    return fields.get(name);
  }

  public void insert(String name, Object value) {
    values.put(name, value);
  }

  public Map<String, String> validate() throws FormValidationException {
    Map<String, String> cleanedValues = new HashMap<>();
    ArrayList<ValidationException> errors = new ArrayList<>();
    for (FormField field : getFields()) {
      Object value = values.get(field.getName());
      try {
        String cleaned = field.validate(value);
        cleanedValues.put(field.getName(), cleaned);
      } catch (ValidationException e) {
        errors.add(e);
      }
    }
    if (!errors.isEmpty()) throw new FormValidationException(errors);
    return cleanedValues;
  }

  public Task submit() throws FormValidationException, IOException {
    Map<String, String> cleaned = validate();
    List<NameValuePair> formParams = new ArrayList<>();
    for (String name : cleaned.keySet()) {
      if (cleaned.get(name) != null) {
        formParams.add(new BasicNameValuePair(name, cleaned.get(name)));
      }
    }
    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
    HttpPost request = new HttpPost(client.buildURL(path));
    request.setEntity(entity);
    CloseableHttpResponse response = client.httpClient.execute(request);
    int statusCode = response.getStatusLine().getStatusCode();
    try {
      if (statusCode == 202) {
        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
        return new Task(client, json.getString("uuid"), json.getString("URI"));
      } else if (statusCode == 420) {
        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
        List<ValidationException> errors = new ArrayList<>();
        JSONArray errorsJSON = json.getJSONArray("errors");
        for (int i = 0; i < errorsJSON.length(); ++i) {
          JSONObject error = errorsJSON.getJSONObject(i);
          errors.add(new ValidationException(
              getField(error.getString("field")),
              error.getString("errorCode"),
              error.getString("message")
          ));
        }
        throw new FormValidationException(errors);
      } else {
        throw new HttpResponseException(statusCode, "Invalid server response");
      }
    } finally {
      response.close();
    }
  }
}


class JSONFormFactory {
  static Form getForm(SlivkaClient client, JSONObject json) {
    HashMap<String, FormField> fields = new HashMap<>();
    for (Object obj : json.getJSONArray("fields")) {
      JSONObject fieldJSON = (JSONObject) obj;
      fields.put(fieldJSON.getString("name"), getField(fieldJSON));
    }
    return new Form(client, json.getString("name"), fields, json.getString("URI"));
  }

  private static FormField getField(JSONObject json) {
    FieldType type = FieldType.forName(json.getString("type"));
    switch (type) {
      case INTEGER:
        return new IntegerField(
            json.getString("name"),
            json.getString("label"),
            json.getString("description"),
            json.getBoolean("required"),
            json.getInt("default"),
            json.getInt("min"),
            json.getInt("max")
        );
      case DECIMAL:
        return new DecimalField(
            json.getString("name"),
            json.getString("label"),
            json.getString("description"),
            json.getBoolean("required"),
            json.getDouble("default"),
            json.getDouble("min"),
            json.getDouble("max"),
            json.getBoolean("minExclusive"),
            json.getBoolean("maxExclusive")
        );
      case BOOLEAN:
        return new BooleanField(
            json.getString("name"),
            json.getString("label"),
            json.getString("description"),
            json.getBoolean("required"),
            json.getBoolean("default")
        );
      case TEXT:
        return new TextField(
            json.getString("name"),
            json.getString("label"),
            json.getString("description"),
            json.getBoolean("required"),
            json.getString("default"),
            json.getInt("minLength"),
            json.getInt("maxLength")
        );
      case FILE:
        return new FileField(
            json.getString("name"),
            json.getString("label"),
            json.getString("description"),
            json.getBoolean("required")
        );
      case CHOICE:
        JSONArray choicesArray = json.getJSONArray("choices");
        String[] choices = new String[choicesArray.length()];
        for (int i = 0; i < choicesArray.length(); ++i) {
          choices[i] = choicesArray.getString(i);
        }
        return new ChoiceField(
            json.getString("name"),
            json.getString("label"),
            json.getString("description"),
            json.getBoolean("required"),
            json.getString("default"),
            Arrays.asList(choices)
        );
    }
    return null;
  }
}