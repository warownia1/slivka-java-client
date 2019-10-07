package uk.ac.dundee.compbio.slivkaclient;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class IntegerFieldTest {

  @DataProvider(name = "defaultField")
  Object[] createDefaultField() {
    return new Object[] {
        new IntegerField(
            "name", "label", "description",
            true, null, null, null
        )
    };
  }

  @Test(dataProvider = "defaultField")
  public void testValueOf_integer(IntegerField field) {
    assertEquals((int) field.valueOf("1"), 1);
  }

  @Test(expectedExceptions = Exception.class, dataProvider = "defaultField")
  public void testValueOf_float(IntegerField field) {
    field.valueOf("0.4");
  }

  @Test(dataProvider = "defaultField")
  public void testValueOf_null(IntegerField field) {
    assertNull(field.valueOf(null));
  }

  @Test(expectedExceptions = Exception.class, dataProvider = "defaultField")
  public void testValueOf_text(IntegerField field) {
    field.valueOf("text");
  }

  @Test(dataProvider = "defaultField")
  public void testValidate(IntegerField field) throws ValidationException {
    assertEquals(field.validate(10), "10");
  }

  @Test(expectedExceptions = ValidationException.class, dataProvider = "defaultField")
  public void testValidate_nullValue(IntegerField field) throws ValidationException {
    field.validate(null);
  }

  @Test(expectedExceptions = ValidationException.class, dataProvider = "defaultField")
  public void testValidate_invalidType(IntegerField field) throws ValidationException {
    field.validate("Hello");
  }

  @DataProvider(name = "fieldWithMinMax")
  Object[] createField_withMinMax() {
    return new Object[] {
        new IntegerField(
            "name", "label", "description",
            true, null, 4, 12
        )
    };
  }

  @Test(dataProvider = "fieldWithMinMax")
  public void testBounds_valueInBound(IntegerField field) throws ValidationException {
    assertEquals(field.validate(5), "5");
  }

  @Test(expectedExceptions = ValidationException.class, dataProvider = "fieldWithMinMax")
  public void testMaxBound_valueOutBound(IntegerField field) throws ValidationException {
    field.validate(14);
  }

  @Test(dataProvider = "fieldWithMinMax")
  public void textMaxBound_valueEqualToBound(IntegerField field) throws ValidationException {
    assertEquals(field.validate(12), "12");
  }

  @Test(expectedExceptions = ValidationException.class, dataProvider = "fieldWithMinMax")
  public void testMinBound_valueOutBound(IntegerField field) throws ValidationException {
    field.validate(3);
  }

  @Test(dataProvider = "fieldWithMinMax")
  public void testMinBound_valueEqualToBound(IntegerField field) throws ValidationException {
    assertEquals(field.validate(4), "4");
  }

  @Test
  public void testDefault() throws ValidationException {
    IntegerField field = new IntegerField(
        "name", "label", "description",
        true, 9, 0, 15
    );
    assertEquals(field.validate(null), "9");
  }

  @Test
  public void testNotRequired() throws ValidationException {
    IntegerField field = new IntegerField(
        "name", "label", "description",
        false, null, null, null
    );
    assertNull(field.validate(null));
  }
}
