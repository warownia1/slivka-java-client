package uk.ac.dundee.compbio.slivkaclient;


import org.testng.Assert;
import org.testng.annotations.Test;


class FormFieldStub extends FormField {
  FormFieldStub(boolean required) {
    super(FieldType.TEXT, "myname", "mylabel", "mydescription", required);
  }

  @Override
  public Object getDefault() {
    return null;
  }

  @Override
  public String validate(Object value) {
    return value.toString();
  }

  @Override
  public Object valueOf(String value) {
    return value;
  }
}


public class FormFieldTest {

  private FormField field = new FormFieldStub(true);

  @Test
  public void testType() {
    Assert.assertEquals(this.field.getType(), FieldType.TEXT);
  }

  @Test
  public void testName() {
    Assert.assertEquals(field.getName(), "myname");
  }

  @Test
  public void testRequired_idTrue() {
    Assert.assertTrue(field.isRequired());
  }

  public void testRequired_isFalse() {
    FormField field = new FormFieldStub(false);
    Assert.assertFalse(field.isRequired());
  }

  @Test
  public void testLabel() {
    Assert.assertEquals(field.getLabel(), "mylabel");
  }

  @Test
  public void testDescription() {
    Assert.assertEquals(field.getDescription(), "mydescription");
  }

}
