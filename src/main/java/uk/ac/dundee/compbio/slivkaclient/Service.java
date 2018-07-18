package uk.ac.dundee.compbio.slivkaclient;

import java.io.IOException;
import java.net.URI;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.dundee.compbio.slivkaclient.form.Form;
import uk.ac.dundee.compbio.slivkaclient.form.FormFactory;

/**
 * Representation of the service available on the server.
 * Each Service objects gives the access to the job submission form.
 * 
 * @author mmwarowny
 *
 */
public class Service {

	private SlivkaClient.HttpClient client;
	private String name;
	private URI formURI;
	private Form form = null;

	Service(SlivkaClient.HttpClient client, String name, String getFormPath) {
		this.client = client;
		this.name = name;
		this.formURI = client.getURL(getFormPath);
	}
	
	/**
	 * Gets the service name.
	 * @return Service name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the form template associated with the service.
	 * The form contains all customisable options for that service.
	 * The first time the method is called, the form is fetched from the server.
	 * Any subsequent call to this method will use cached version of the form.
	 * 
	 * @return Form template
	 * @throws IOException If server communication error occurred.
	 * @throws HttpException If server responds with an error status code.
	 * @throws ServerError If server response id not valid.
	 */
	public Form getForm() throws IOException, ServerError, HttpException {
		if (form == null) {
			form = fetchForm();
		}
		return form;
	}
	
	private Form fetchForm() throws IOException, ServerError, HttpException {
		HttpGet request = new HttpGet(formURI);
		CloseableHttpResponse response = client.execute(request);
		int statusCode = response.getStatusLine().getStatusCode();
		try {
		if (statusCode == 200) {
			try {
				JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
				return FormFactory.buildForm(client, json);
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
		return String.format("<Service %s>", name);
	}
}
