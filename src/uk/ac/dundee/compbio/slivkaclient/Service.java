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
	
	public String getName() {
		return name;
	}
	
	public Form getForm() throws IOException, ServerError {
		if (form == null) {
			form = fetchForm();
		}
		return form;
	}
	
	private Form fetchForm() throws IOException, ServerError {
		HttpGet request = new HttpGet(formURI);
		CloseableHttpResponse response = client.execute(request);
		try {
			JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
			return FormFactory.buildForm(client, json);
		}
		catch (JSONException e) {
			throw new ServerError("Invalid JSON response", e);
		}
		finally {
			response.close();
		}
	}
	
	public String toString() {
		return String.format("<Service %s>", name);
	}
}
