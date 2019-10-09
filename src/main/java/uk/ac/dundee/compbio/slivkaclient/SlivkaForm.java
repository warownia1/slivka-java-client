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

public class SlivkaForm {
  private final SlivkaClient client;
  private final String name;
  private final Map<String, FormField> fields;
  private final String path;
  private final HashMap<String, List<Object>> values;

  SlivkaForm(SlivkaClient client, String name, Map<String, FormField> fields, String path) {
    this.client = client;
    this.name = name;
    this.fields = Collections.unmodifiableMap(fields);
    this.path = path;
    this.values = new HashMap<>();
    for (FormField field : getFields()) {
      values.put(field.getName(), new ArrayList<Object>());
    }
  }

  SlivkaForm(SlivkaForm copyFrom) {
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
    values.get(name).add(value);
  }

  public Map<String, List<String>> validate() throws FormValidationException {
    HashMap<String, List<String>> cleanedValues = new HashMap<>();
    ArrayList<ValidationException> errors = new ArrayList<>();
    for (FormField field : getFields()) {
      ArrayList<String> cleanedList = new ArrayList<>();
      List<Object> rawValues = new ArrayList<>(values.get(field.getName()));
      if (rawValues.isEmpty()) {
        rawValues.add(null);
      }
      for (Object val: rawValues) {
        try {
          cleanedList.add(field.validate(val));
        }
        catch (ValidationException e) {
          errors.add(e);
        }
      }
      cleanedValues.put(field.getName(), cleanedList);
    }
    if (!errors.isEmpty()) throw new FormValidationException(errors);
    return cleanedValues;
  }

  public String submit() throws FormValidationException, IOException {
    Map<String, List<String>> cleanedValues = validate();
    List<NameValuePair> formParams = new ArrayList<>();
    for (String name : cleanedValues.keySet()) {
      for (String val : cleanedValues.get(name)) {
        if (val != null) {
          formParams.add(new BasicNameValuePair(name, val));
        }
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
        return json.getString("uuid");
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
  static SlivkaForm getForm(SlivkaClient client, JSONObject json) {
    HashMap<String, FormField> fields = new HashMap<>();
    for (Object obj : json.getJSONArray("fields")) {
      JSONObject fieldJSON = (JSONObject) obj;
      fields.put(fieldJSON.getString("name"), getField(fieldJSON));
    }
    return new SlivkaForm(client, json.getString("name"), fields, json.getString("URI"));
  }

  private static FormField getField(JSONObject json) {
    FieldType type = FieldType.valueOf(json.getString("type").toUpperCase());
    switch (type) {
      case INTEGER:
        return new IntegerField(
            json.getString("name"),
            json.getString("label"),
            json.getString("description"),
            json.getBoolean("required"),
            json.optBoolean("multiple", false),
            json.isNull("default") ? null : json.getInt("default"),
            json.has("min") ? json.getInt("min") : null,
            json.has("max") ? json.getInt("max") : null
        );
      case DECIMAL:
        return new DecimalField(
            json.getString("name"),
            json.getString("label"),
            json.getString("description"),
            json.getBoolean("required"),
            json.optBoolean("multiple", false),
            json.isNull("default") ? null : json.getDouble("default"),
            json.has("min") ? json.getDouble("min") : null,
            json.has("max") ? json.getDouble("max") : null,
            json.optBoolean("minExclusive", false),
            json.optBoolean("maxExclusive", false)
        );
      case BOOLEAN:
        return new BooleanField(
            json.getString("name"),
            json.getString("label"),
            json.getString("description"),
            json.getBoolean("required"),
            json.optBoolean("multiple", false),
            json.isNull("default") ? null : json.getBoolean("default")
        );
      case TEXT:
        return new TextField(
            json.getString("name"),
            json.getString("label"),
            json.getString("description"),
            json.getBoolean("required"),
            json.optBoolean("multiple", false),
            json.isNull("default") ? null : json.getString("default"),
            json.has("minLength") ? json.getInt("minLength") : null,
            json.has("maxLength") ? json.getInt("maxLength") : null
        );
      case FILE:
        return new FileField(
            json.getString("name"),
            json.getString("label"),
            json.getString("description"),
            json.getBoolean("required"),
            json.optBoolean("multiple", false)
        );
      case CHOICE:
        JSONArray choicesArray = json.getJSONArray("choices");
        ArrayList<String> choices = new ArrayList<>(choicesArray.length());
        for (Object obj: choicesArray) {
          choices.add((String) obj);
        }
        return new ChoiceField(
            json.getString("name"),
            json.getString("label"),
            json.getString("description"),
            json.getBoolean("required"),
            json.optBoolean("multiple", false),
            json.isNull("default") ? null : json.getString("default"),
            choices
        );
    }
    return null;
  }
}