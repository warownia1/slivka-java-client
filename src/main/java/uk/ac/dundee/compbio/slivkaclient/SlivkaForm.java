package uk.ac.dundee.compbio.slivkaclient;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javajs.http.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class SlivkaForm {
  private final SlivkaClient client;
  private final String name;
  private final Map<String, FormField> fields;
  private final String path;
  private final HashMap<String, List<Object>> values;

  SlivkaForm(SlivkaClient client, String name, Map<String, FormField> fields,
      String path) {
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

  public void add(String name, Object value) {
    values.get(name).add(value);
  }

  public void addAll(String name, Collection<Object> vals) {
    values.get(name).addAll(vals);
  }

  public void clear() {
    for (String key : values.keySet()) {
      clear(key);
    }
  }

  public void clear(String name) {
    values.get(name).clear();
  }

  @Deprecated
  public void insert(String name, Object value) {
    values.get(name).add(value);
  }

  public String submit() throws FormValidationException, IOException {
    HttpClient.HttpRequest request = client.getHttpClient()
        .post(client.buildURL(path));
    for (var entry : values.entrySet()) {
      for (var value : entry.getValue()) {
        if (value instanceof File) {
          request = request.addFile(entry.getKey(), (File) value);
        } else if (value instanceof InputStream) {
          request = request.addFile(entry.getKey(), (InputStream) value);
        } else {
          request = request.addParameter(entry.getKey(), value.toString());
        }
      }
    }
    try (HttpClient.HttpResponse response = request.execute()) {
      int statusCode = response.getStatusCode();
      if (statusCode == 202) {
        JSONObject json = new JSONObject(response.getText());
        return json.getString("uuid");
      } else if (statusCode == 420) {
        JSONObject json = new JSONObject(response.getText());
        List<ValidationException> errors = new ArrayList<>();
        JSONArray errorsJSON = json.getJSONArray("errors");
        for (int i = 0; i < errorsJSON.length(); ++i) {
          JSONObject error = errorsJSON.getJSONObject(i);
          errors.add(new ValidationException(getField(error.getString("field")),
              error.getString("errorCode"), error.getString("message")));
        }
        throw new FormValidationException(errors);
      } else {
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
    return new SlivkaForm(client, json.getString("name"), fields,
        json.getString("URI"));
  }

  private static FormField getField(JSONObject json) {
    FieldType type = FieldType.valueOf(json.getString("type").toUpperCase());
    switch (type) {
    case INTEGER:
      return IntegerField.newFromJson(json);
    case DECIMAL:
      return DecimalField.newFromJson(json);
    case BOOLEAN:
      return BooleanField.newFromJson(json);
    case TEXT:
      return TextField.newFromJson(json);
    case FILE:
      return FileField.newFromJson(json);
    case CHOICE:
      return ChoiceField.newFromJson(json);
    default:
      return null;
    }
  }
}