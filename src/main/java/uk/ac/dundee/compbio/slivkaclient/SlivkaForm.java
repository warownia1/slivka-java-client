package uk.ac.dundee.compbio.slivkaclient;

import static java.lang.String.format;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import uk.ac.dundee.compbio.slivkaclient.http.HttpRequestBuilder;
import uk.ac.dundee.compbio.slivkaclient.http.HttpResponse;


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
    HttpRequestBuilder requestBuilder = client.getHttpClient().post(client.buildURL(path));
    Map<String, List<String>> cleanedValues = validate();
    for (var entry : cleanedValues.entrySet()) {
      for (var value : entry.getValue()) {
        if (value != null) {
          requestBuilder = requestBuilder.addParameter(entry.getKey(), value);
        }
      }
    }
    try (HttpResponse response = requestBuilder.execute()) {
      int statusCode = response.getStatusCode();
      if (statusCode == 202) {
        JSONObject json = new JSONObject(response.getText());
        return json.getString("uuid");
      }
      else if (statusCode == 420) {
        JSONObject json = new JSONObject(response.getText());
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
      }
      else {
        throw new IOException(format("Unexpected status code: %d", statusCode));
      }
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
        JSONObject jsonParams = json.optJSONObject("mediaTypeParameters");
        HashMap<String, String> mediaTypeParams = new HashMap<>();
        if (jsonParams != null) {
          jsonParams.keys().forEachRemaining((String key) -> {
            mediaTypeParams.put(key, jsonParams.get(key).toString());
          });
        }
        return new FileField(
            json.getString("name"),
            json.getString("label"),
            json.getString("description"),
            json.getBoolean("required"),
            json.optBoolean("multiple", false),
            json.optString("mediaType"),
            mediaTypeParams
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