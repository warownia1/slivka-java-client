package uk.ac.dundee.compbio.slivkaclient.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import uk.ac.dundee.compbio.slivkaclient.ServerError;
import uk.ac.dundee.compbio.slivkaclient.SlivkaClient;

public class FormFactory {
	
	public static Form buildForm(SlivkaClient.HttpClient client, JSONObject object) {
		List<FormField> fields = new ArrayList<>();
		JSONArray fieldsArray = object.getJSONArray("fields");
		for (int i = 0; i < fieldsArray.length(); ++i) {
			fields.add(buildField(fieldsArray.getJSONObject(i)));
		}
		return new Form(client, object.getString("service"), 
				fields, object.getString("submitURI"));
	}
	
	private static FormField buildField(JSONObject object) {
		FieldType type = FieldType.fromName(object.getString("type"));
		if (type == null) {
			throw new ServerError("Invalid field type " + object.getString("type"));
		}
		FormField field = null;
		switch (type) {
		case INTEGER:
			field = buildIntegerField(object);
			break;
		case DECIMAL:
			field = buildDecimalField(object);
			break;
		case TEXT:
			field = buildTextField(object);
			break;
		case BOOLEAN:
			field = buildBooleanField(object);
			break;
		case CHOICE:
			field = buildChoiceField(object);
			break;
		case FILE:
			field = new FileField(
					object.getString("name"),
					object.getString("label"),
					object.optString("description", ""),
					object.optBoolean("required", false)
					);
			break;
		}
		return field;
	}
	
	private static FormField buildIntegerField(JSONObject object) {
		Integer min = null;
		Integer max = null;
		JSONArray constraints = object.getJSONArray("constraints");
		for (int i = 0; i < constraints.length(); ++i) {
			String name = constraints.getJSONObject(i).getString("name");
			if (name.equals("max"))
				max = constraints.getJSONObject(i).getInt("value");
			else if (name.equals("min"))
				min = constraints.getJSONObject(i).getInt("value");
		}
		return new IntegerField(
				object.getString("name"),
				object.getString("label"),
				object.optString("description", ""),
				object.optBoolean("required", false),
				object.isNull("default") ? null : object.getInt("default"),
				min, max
				);
	}
	
	private static FormField buildDecimalField(JSONObject object) {
		Double min = null;
		Double max = null;
		boolean minExclusive = false;
		boolean maxExclusive = false;
		JSONArray constraints = object.getJSONArray("constraints");
		for (int i = 0; i < constraints.length(); ++i) {
			String name = constraints.getJSONObject(i).getString("name");
			if (name.equals("max"))
				max = constraints.getJSONObject(i).getDouble("value");
			else if (name.equals("min"))
				min = constraints.getJSONObject(i).getDouble("value");
			else if (name.equals("minExclusive"))
				minExclusive = constraints.getJSONObject(i).getBoolean("value");
			else if (name.equals("maxExclusive"))
				maxExclusive = constraints.getJSONObject(i).getBoolean("value");
		}
		return new DecimalField(
				object.getString("name"),
				object.getString("label"),
				object.optString("description", ""),
				object.optBoolean("required", false),
				object.isNull("default") ? null : object.getDouble("default"),
				min, max, minExclusive, maxExclusive
				);
	}
	
	private static FormField buildTextField(JSONObject object) {
		Integer minLength = null;
		Integer maxLength = null;
		JSONArray constraints = object.getJSONArray("constraints");
		for (int i = 0; i < constraints.length(); ++i) {
			String name = constraints.getJSONObject(i).getString("name");
			if (name.equals("minLength"))
				minLength = constraints.getJSONObject(i).getInt("value");
			else if (name.equals("maxLength"))
				maxLength = constraints.getJSONObject(i).getInt("value");
		}
		return new TextField(
				object.getString("name"),
				object.getString("label"),
				object.optString("description", ""),
				object.optBoolean("required", false),
				object.isNull("default") ? null : object.getString("default"),
				minLength, maxLength
				);
	}
	
	private static FormField buildBooleanField(JSONObject object) {
		return new BooleanField(
				object.getString("name"),
				object.getString("label"),
				object.optString("description", ""),
				object.optBoolean("required", false),
				object.isNull("default") ? null : object.getBoolean("default")
				);
	}
	
	private static FormField buildChoiceField(JSONObject object) {
		JSONArray choicesArray = null;
		JSONArray constraints = object.getJSONArray("constraints");
		for (int i = 0; i < constraints.length(); ++i) {
			String name = constraints.getJSONObject(i).getString("name");
			if (name.equals("choices"))
				choicesArray = constraints.getJSONObject(i).getJSONArray("value");
		}
		Collection <String> choices = new ArrayList<>(choicesArray.length());
		for (int i = 0; i < choicesArray.length(); ++i) {
			choices.add(choicesArray.getString(i));
		}
		return new ChoiceField(
				object.getString("name"),
				object.getString("label"),
				object.optString("description", ""),
				object.optBoolean("required", false),
				object.optString("default", null),
				choices
				);
	}
}