package uk.ac.dundee.compbio.slivkaclient.form;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.dundee.compbio.slivkaclient.HttpException;
import uk.ac.dundee.compbio.slivkaclient.ServerError;
import uk.ac.dundee.compbio.slivkaclient.SlivkaClient;
import uk.ac.dundee.compbio.slivkaclient.TaskHandler;


public class Form {

	private SlivkaClient.HttpClient client;
	public final String name;
	private final URI submitURI;
	private final HashMap<String, FormField> fields;
	private final boolean template;
	private HashMap<String, Object> values = new HashMap<>();
	
	Form(SlivkaClient.HttpClient httpClient, String name, 
			List<FormField> fields, String submitPath) {
		this.client = httpClient;
		this.name = name;
		this.submitURI = httpClient.getURL(submitPath);
		this.fields = new HashMap<>(fields.size());
		for (FormField field : fields) {
			this.fields.put(field.getName(), field);
		}
		this.template = true;
	}
	
	private Form(Form base) {
		client = base.client;
		name = base.name;
		submitURI = base.submitURI;
		fields = base.fields;
		template = false;
	}
	
	/**
	 * Creates a fillable form from the template. 
	 * 
	 * This method allows to create the fillable form instance from the template form manually.
	 * The fillable form is also created when {@link #insert(String, Object)} or {@link #insert(Map)} 
	 * is called for the first time on the template.
	 * 
	 * @return Empty form suitable for data insertion
	 */
	public Form create() {
		return new Form(this);
	}
	
	/**
	 * Gets the list of form fields.
	 * @return List of form fields.
	 */
	public List<FormField> getFields() {
		return new ArrayList<>(fields.values());
	}
	
	/**
	 * Gets the set of field names.
	 * @return Set of field names.
	 */
	public Set<String> getFieldNames() {
		return new HashSet<>(fields.keySet());
	}
	
	/**
	 * Gets the field having the specified name.
	 * 
	 * @param name Field name
	 * @return Field
	 * @throws IllegalArgumentException If the name doesn't match any field.
	 */
	public FormField getField(String name) {
		if (fields.containsKey(name)) {
			return fields.get(name);
		}
		else {
			throw new IllegalArgumentException("Invalid field \"" + name + "\"");
		}
	}

	/**
	 * Inserts a single value to the form.
	 * 
	 * This method is used to populate the form with values. If the form is a template
	 * a new fillable form instance is created before inserting the data to it.
	 * Returns the form instance allowing chaining.
	 * 
	 * @param name Field name
	 * @param value Inserted value
	 * @return Fillable form instance
	 * @throws IllegalArgumentException If the name doesn't match any field.
	 * @see #create()
	 */
	public Form insert(String name, Object value) {
		if (!getFieldNames().contains(name))
			throw new IllegalArgumentException("Invalid field \"" + name + "\"");
		Form form = template ? new Form(this) : this;
		form.values.put(name, value);
		return form;
	}
	
	/**
	 * Inserts multiple values to the form.
	 * 
	 * This method allows to populate multiple form fields at once. Each key in the map
	 * corresponds to a form field name and maps it to the value inserted to that field.
	 * If the form is a template a new fillable form instance is created before inserting data into it.
	 * Return the fillable form instance allowing chaining.
	 * 
	 * @param values A mapping of the field names to values
	 * @return Fillable form instance
	 * @throws IllegalArgumentException If the name doesn't match any field.
	 * @see #insert(String, Object)
	 * @see #create()
	 */
	public Form insert(Map<String, Object> values) {
		Set<String> fieldNames = getFieldNames();
		for (String name : values.keySet()) {
			if (!fieldNames.contains(name)) {
				throw new IllegalArgumentException("Invalid field \"" + name + "\"");
			}
		}
		Form form = template ? new Form(this) : this;
		form.values.putAll(values);
		return form;
	}

	private HashMap<String, String> validate() throws FormValidationException {
		HashMap<String, String> cleanedValues = new HashMap<>();
		ArrayList<ValidationException> errors = new ArrayList<>();
		for (FormField field : getFields()) {
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
	
	/**
	 * Validates and submits the form.
	 * 
	 * Validates each value against the corresponding field and collects ValidationException
	 * raised during each field validation. If the validation passes successfully, submits
	 * the job request to the server and returns a handler to the created task.
	 * Otherwise, throws a FormValidationException enclosing all errors in the fields.
	 * 
	 * @return Task handler for the submitted job.
	 * @throws FormValidationException If any of the form fields is not valid.
	 * @throws IOException If an error occurs during the connection to the server.
	 * @throws HttpException If the server responds with and error status code.
	 * @throws ServerError If the server response is invalid.
	 * @see FormField#validate(Object)
	 * @see TaskHandler
	 */
	public TaskHandler submit()
			throws FormValidationException, IOException, HttpException, ServerError {
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
		int statusCode = response.getStatusLine().getStatusCode();
		try {
		if (statusCode == 202) {
			try {
				JSONObject json = new JSONObject(
						EntityUtils.toString(response.getEntity()));
				return new TaskHandler(client, json.getString("taskId"), 
						json.getString("checkStatusURI"));
			}
			catch (JSONException e) {
				throw new ServerError("Invalid JSON response", e);
			}
		}
		else if (statusCode == 420) {
			try {
				JSONObject json = new JSONObject(
						EntityUtils.toString(response.getEntity()));
				List<ValidationException> errors = new ArrayList<>();
				JSONArray errorsArray = json.getJSONArray("errors");
				for (int i = 0; i < errorsArray.length(); ++i) {
					JSONObject errorObj = errorsArray.getJSONObject(i);
					errors.add(new ValidationException(
							getField(errorObj.getString("field")),
							errorObj.getString("errorCode"),
							errorObj.getString("message")
							));
				}
				throw new FormValidationException(errors);
			}
			catch (JSONException e) {
				throw new ServerError("Invalid JSON response", e);
			}
		}
		else if (statusCode >= 400) {
			throw client.getHttpException(response);
		}
		else {
			throw new ServerError("Invalid status code");
		}
		}
		finally {
			response.close();
		}
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder(name + " [");
		for (FormField field : getFields())
			builder.append(field.toString() + " ");
		builder.deleteCharAt(builder.length() - 1 ).append("]");
		return builder.toString();
	}
}
