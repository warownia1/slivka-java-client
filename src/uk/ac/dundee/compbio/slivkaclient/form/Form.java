package uk.ac.dundee.compbio.slivkaclient.form;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.dundee.compbio.slivkaclient.ServerError;
import uk.ac.dundee.compbio.slivkaclient.SlivkaClient;
import uk.ac.dundee.compbio.slivkaclient.TaskHandler;

public class Form {

	private SlivkaClient.HttpClient client;
	public final String name;
	private final URI submitURI;
	private final List<FormField> fields;
	private final HashSet<String> fieldNames;
	private final boolean template;
	private HashMap<String, Object> values = new HashMap<>();
	
	Form(SlivkaClient.HttpClient httpClient, String name, List<FormField> fields, String submitPath) {
		this.client = httpClient;
		this.name = name;
		this.submitURI = httpClient.getURL(submitPath);
		this.fields = Collections.unmodifiableList(fields);
		this.fieldNames = new HashSet<String>(this.fields.size());
		for (FormField field : this.fields)
			fieldNames.add(field.getName());
		this.template = true;
	}
	
	private Form(Form base) {
		client = base.client;
		name = base.name;
		submitURI = base.submitURI;
		fields = base.fields;
		fieldNames = base.fieldNames;
		template = false;
	}
	
	public List<FormField> getFields() {
		return fields;
	}
	
	public Form insert(String name, Object value) {
		if (!fieldNames.contains(name))
			throw new IllegalArgumentException("Invalid field \"" + name + "\"");
		Form form = template ? new Form(this) : this;
		form.values.put(name, value);
		return form;
	}
	
	public Form insert(Map<String, Object> many) {
		for (String name : many.keySet())
			if (!fieldNames.contains(name))
				throw new IllegalArgumentException("Invalid field \"" + name + "\"");
		Form form = template ? new Form(this) : this;
		form.values.putAll(many);
		return form;
	}

	private HashMap<String, String> validate() throws FormValidationException {
		HashMap<String, String> cleanedValues = new HashMap<>();
		ArrayList<ValidationException> errors = new ArrayList<>();
		for (FormField field : fields) {
			Object value = values.get(field.getName());
			try {
				String result = field.validate(value);
				cleanedValues.put(field.getName(), result);
			} catch (ValidationException e) {
				errors.add(e);
			}
		}
		if (!errors.isEmpty())
			throw new FormValidationException(errors);
		return cleanedValues;
	}
	
	public TaskHandler submit() throws FormValidationException, IOException {
		Map<String, String> cleanedValues = validate();
		List<NameValuePair> formParams = new ArrayList<>();
		for (String name : cleanedValues.keySet()) {
			if (cleanedValues.get(name) != null) {
				formParams.add(new BasicNameValuePair(name, cleanedValues.get(name)));
			}
		}
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
		HttpPost request = new HttpPost(submitURI);
		request.setEntity(entity);
		CloseableHttpResponse response = client.execute(request);
		try {
			JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
			return new TaskHandler(client, json.getString("taskId"), json.getString("checkStatusURI"));
		} 
		catch (JSONException e) {
			throw new ServerError("Invalid json response", e);
		}
		finally {
			response.close();
		}
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder(name + " [");
		for (FormField field : fields)
			builder.append(field.toString() + " ");
		builder.deleteCharAt(builder.length() - 1 ).append("]");
		return builder.toString();
	}
}
