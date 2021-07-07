package uk.ac.dundee.compbio.slivkaclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Parameter {
  protected String id;
  protected String name;
  protected String description;
  protected String type;
  protected boolean required;
  protected boolean array;
  protected Object defaultValue;
  protected Map<String, Object> additionalProperties = new HashMap<>();

  public String getId() { return id; }

  public String getName() { return name; }

  public String getDescription() { return description; }

  public String getType() { return type; }

  public boolean isRequired() { return required; }

  public boolean isArray() { return array; }

  public Object getDefaultValue() { return defaultValue; }
  
  public Object getDefault() { return defaultValue; }

  public Map<String, Object> getAdditionalProperties() { return additionalProperties; }

  Parameter() {}

  protected void readJSON(JSONObject obj) {
    id = obj.getString("id");
    name = obj.getString("name");
    description = obj.optString("description", "");
    type = obj.getString("type");
    required = obj.optBoolean("required", true);
    array = obj.optBoolean("array", false);
    defaultValue = obj.isNull("default") ? null : obj.get("default");
    if (defaultValue instanceof JSONArray) {
      defaultValue = ((JSONArray) defaultValue).toList();
    }
  }

  static Parameter fromJSON(JSONObject obj) {
    String type = obj.getString("type");
    Parameter param;
    switch (type) {
    case "integer":
      param = new IntegerParameter();
      break;
    case "decimal":
      param = new DecimalParameter();
      break;
    case "text":
      param = new TextParameter();
      break;
    case "flag":
      param = new FlagParameter();
      break;
    case "choice":
      param = new ChoiceParameter();
      break;
    case "file":
      param = new FileParameter();
      break;
    default:
      param = new UnknownParameter();
    }
    param.readJSON(obj);
    return param;
  }

  public static class UnknownParameter extends Parameter {
    protected void readJSON(JSONObject obj) {
      super.readJSON(obj);
      var keys = obj.keySet();
      keys.removeAll(List.of(
          "id", "name", "description", "type", "required", "array", "default"));
      for (String key : keys) {
        additionalProperties.put(key, obj.get(key));
      }
    }
  }

  public static class IntegerParameter extends Parameter {
    protected Integer min;
    protected Integer max;

    public Integer getMin() { return min; }

    public Integer getMax() { return max; }

    IntegerParameter() {}

    protected void readJSON(JSONObject obj) {
      super.readJSON(obj);
      min = (Integer) obj.opt("min");
      max = (Integer) obj.opt("max");
      additionalProperties.put("min", min);
      additionalProperties.put("max", max);
    }
  }

  public static class DecimalParameter extends Parameter {
    protected Double min;
    protected Double max;
    protected boolean minExclusive;
    protected boolean maxExclusive;

    public Double getMin() { return min; }

    public Double getMax() { return max; }

    public boolean isMinExclusive() { return minExclusive; }

    public boolean isMaxExclusive() { return maxExclusive; }

    DecimalParameter() {}

    protected void readJSON(JSONObject obj) {
      super.readJSON(obj);
      if (defaultValue != null) {
        if (defaultValue instanceof Number)
          defaultValue = ((Number) defaultValue).doubleValue();
        else if (defaultValue instanceof List<?>) {
          var newDefault = new ArrayList<Double>();
          for (Object value : (List<?>) defaultValue) {
            newDefault.add(((Number) value).doubleValue());
          }
          defaultValue = newDefault;
        }
      }
      min = obj.isNull("min") ? null : ((Number) obj.get("min")).doubleValue();
      max = obj.isNull("max") ? null : ((Number) obj.get("max")).doubleValue();
      minExclusive = obj.optBoolean("minExclusive", false);
      maxExclusive = obj.optBoolean("maxExclusive", false);
      additionalProperties.put("min", min);
      additionalProperties.put("max", max);
      additionalProperties.put("minExclusive", minExclusive);
      additionalProperties.put("maxExclusive", maxExclusive);
    }
  }

  public static class TextParameter extends Parameter {
    protected Integer minLength;
    protected Integer maxLength;

    public Integer getMinLength() { return minLength; }

    public Integer getMaxLength() { return maxLength; }

    TextParameter() {}

    protected void readJSON(JSONObject obj) {
      super.readJSON(obj);
      minLength = (Integer) obj.opt("minLength");
      maxLength = (Integer) obj.opt("maxLength");
      additionalProperties.put("minLength", minLength);
      additionalProperties.put("maxLength", maxLength);
    }
  }

  public static class FlagParameter extends Parameter {
    FlagParameter() {}
  }

  public static class ChoiceParameter extends Parameter {
    protected List<String> choices;

    public List<String> getChoices() { return choices; }

    ChoiceParameter() {}

    protected void readJSON(JSONObject obj) {
      super.readJSON(obj);
      choices = new ArrayList<>();
      JSONArray arr = obj.getJSONArray("choices");
      for (Object it : arr) {
        choices.add((String) it);
      }
      choices = Collections.unmodifiableList(choices);
      additionalProperties.put("choices", choices);
    }
  }

  public static class FileParameter extends Parameter {
    protected String mediaType;
    protected Map<String, String> mediaTypeParameters;

    public String getMediaType() { return mediaType; }

    public Map<String, String> getMediaTypeParameters() { return mediaTypeParameters; }

    FileParameter() {}

    protected void readJSON(JSONObject obj) {
      super.readJSON(obj);
      mediaType = obj.optString("mediaType");
      mediaTypeParameters = new HashMap<>();
      JSONObject map = obj.optJSONObject("mediaTypeParameters");
      if (map != null) {
        for (String key : map.keySet()) {
          mediaTypeParameters.put(key, obj.get(key).toString());
        }
      }
      mediaTypeParameters = Collections.unmodifiableMap(mediaTypeParameters);
      additionalProperties.put("mediaType", mediaType);
      additionalProperties.put("mediaTypeParameters", mediaTypeParameters);
    }
  }
}
