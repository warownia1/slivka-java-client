package uk.ac.dundee.compbio.slivkaclient;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FileField extends FormField {

  private final String mediaType;
  private final Map<String, String> mediaTypeParameters;
  
  private FileField(
      String name, String label, String description, boolean required,
      boolean multiple, String mediaType, Map<String, String> mediaTypeParameters
  ) {
    super(FieldType.FILE, name, label, description, required, multiple);
    this.mediaType = mediaType;
    this.mediaTypeParameters = mediaTypeParameters;
  }

  public static FileField newFromJson(JSONObject json) {
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
  }
  
  public String getMediaType() {
    return mediaType;
  }
  
  public Map<String, String> getMediaTypeParameters() {
    return mediaTypeParameters;
  }

  @Override
  public Object getDefault() {
    return null;
  }
}