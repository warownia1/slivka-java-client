package uk.ac.dundee.compbio.slivkaclient;

import java.util.Map;

public class FileField extends FormField {

  private String mediaType;
  private Map<String, String> mediaTypeParameters;
  
  FileField(String name, String label, String description, boolean required, boolean multiple, String mediaType, Map<String, String> mediaTypeParameters) {
    super(FieldType.FILE, name, label, description, required, multiple);
    this.mediaType = mediaType;
    this.mediaTypeParameters = mediaTypeParameters;
  }

  @Override
  public String validate(Object value) throws ValidationException {
    if (value == null)
      if (required)
        throw fail("required", "Field is required");
      else
        return null;
    if (!(value instanceof RemoteFile))
      throw fail("type", "Invalid value type");
    RemoteFile wrapper = (RemoteFile) value;
    return wrapper.getUUID();
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

  public Object valueOf(String value) {
    throw new RuntimeException("Cannot create file field value form string");
  }
}