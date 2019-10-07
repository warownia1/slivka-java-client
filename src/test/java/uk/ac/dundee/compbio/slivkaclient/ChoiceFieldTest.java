package uk.ac.dundee.compbio.slivkaclient;

import static org.testng.Assert.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class ChoiceFieldTest {
  @DataProvider(name = "defaultField")
  Object[] createDefaultField() {
    return new Object[] {
        new ChoiceField(
            "name", "label", "description",
            true, null, Arrays.asList("FOO", "BAR", "BAZ")
        )
    };
  }

  @Test(dataProvider = "defaultField")
  public void testType(ChoiceField field) {
    assertEquals(field.getType(), FieldType.CHOICE);
  }

  @Test(dataProvider = "defaultField")
  public void testGetChoices(ChoiceField field) {
    List<String> choices = Arrays.asList("BAR", "BAZ", "FOO");
    assertTrue(field.getChoices().containsAll(choices));
  }

  @Test(dataProvider = "defaultField")
  public void testValueOf_valid(ChoiceField field) {
    assertEquals(field.valueOf("BAR"), "BAR");
  }

  @Test(dataProvider = "defaultField")
  public void testValueOf_invalid(ChoiceField field) {
    assertEquals(field.valueOf("QUX"), "QUX");
  }

  @Test(dataProvider = "defaultField")
  public void testValueOf_null(ChoiceField field) {
    assertNull(field.valueOf(null));
  }

  @Test
  public void testNotRequired_withInitial() throws ValidationException {
    ChoiceField field = new ChoiceField(
        "name", "label", "description",
        false, "FOO", Arrays.asList("FOO", "BAR")
    );
    assertEquals(field.validate(null), "FOO");
  }

  @Test
  public void testNotRequired_noInitial() throws ValidationException {
    ChoiceField field = new ChoiceField(
        "name", "label", "description",
        false, null, Arrays.asList("FOO", "BAR")
    );
    assertNull(field.validate(null));
  }
}
