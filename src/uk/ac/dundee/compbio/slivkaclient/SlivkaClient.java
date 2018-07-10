package uk.ac.dundee.compbio.slivkaclient;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SlivkaClient {
	
	private HttpClient httpClient;
	private List<Service> services = null;
	
	public SlivkaClient(String host, int port) throws URISyntaxException {
		URI baseURI = new URIBuilder()
				.setScheme("http").setHost(host).setPort(port).build();
		httpClient = new HttpClient(baseURI);
	}
	
	public SlivkaClient(String host) throws URISyntaxException {
		URI baseURI = new URIBuilder().setScheme("http").setHost(host).build();
		httpClient = new HttpClient(baseURI);
	}
	
	public List<Service> getServices() throws IOException, ServerError {
		if (services == null) {
			services = fetchServices();
		}
		return services;
	}
	
	private List<Service> fetchServices() throws IOException, ServerError {
		HttpGet request = new HttpGet(httpClient.getURL("services"));
		CloseableHttpResponse response = httpClient.execute(request);
		List<Service> services = new ArrayList<>();
		try {
			 JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
			 JSONArray servicesArray = json.getJSONArray("services");
			 for (int i = 0; i < servicesArray.length(); ++i) {
				 services.add(new Service(
						 this.httpClient,
						 servicesArray.getJSONObject(i).getString("name"),
						 servicesArray.getJSONObject(i).getString("submitURI")
						 ));
			 }
		}
		catch (JSONException e) {
			throw new ServerError("Invalid JSON response", e);
		}
		finally {
			response.close();
		}
		return services;
	}
	
	public FileHandler uploadFile(Reader reader) {
		return null;
	}
	
	public FileHandler getFile(String fileID) {
		return null;
	}
	
	
	public class HttpClient {
		private final URI baseURI;
		private CloseableHttpClient httpClient = HttpClients.createDefault();
		
		private HttpClient(URI baseURI) {
			this.baseURI = baseURI;
		}	
		
		public URI getURL(String path) {
			try {
				return new URIBuilder(baseURI).setPath(path).build();
			} catch (URISyntaxException e) {
				throw new Error(String.format("path %s is incorrect", path), e);
			}
		}

		public CloseableHttpResponse execute(HttpUriRequest request) throws IOException {
			return httpClient.execute(request);
		}
	}
}
 