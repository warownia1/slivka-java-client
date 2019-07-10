package uk.ac.dundee.compbio.slivkaclient;

public enum FieldType {
  INTEGER("integer"),
  DECIMAL("decimal"),
  TEXT("text"),
  BOOLEAN("boolean"),
  CHOICE("choice"),
  FILE("file");

  private String name;

  FieldType(String name) {
    this.name = name;
  }

  public static FieldType forName(String name) {
    for (FieldType type : FieldType.values()) {
      if (name.equals(type.name)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid field type \"" + name + "\"");
  }
}
