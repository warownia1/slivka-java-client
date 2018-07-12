package uk.ac.dundee.compbio.slivkaclient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A client managing the connections to the Slivka server.
 * @author Mateusz Warowny
 *
 */
public class SlivkaClient {
	
	private HttpClient httpClient;
	private List<Service> services = null;
	
	/**
	 * Constructs a new client communicating with the specified host on the given port number.
	 * Host and port parameters are used to build a base URL for all requests
	 * using http schema.
	 *  
	 * @param host Host name.
	 * @param port Port number
	 * @throws URISyntaxException If the string cannot be parsed as an URI
	 */
	public SlivkaClient(String host, int port) 
			throws URISyntaxException {
		URI baseURI = new URIBuilder()
				.setScheme("http").setHost(host).setPort(port).build();
		httpClient = new HttpClient(baseURI);
	}
	
	/**
	 * Constructs a new client communicating with the specified host.
	 * Host parameter is used to build a base URL for all requests using http schema
	 * 
	 * @param host Host name
	 * @throws URISyntaxException If the string cannot be parsed as an URI
	 * @see URIBuilder
	 */
	public SlivkaClient(String host)
			throws URISyntaxException {
		URI baseURI = new URIBuilder().setScheme("http").setHost(host).build();
		httpClient = new HttpClient(baseURI);
	}
	
	/**
	 * Gets the list of services.
	 * On the first call, the services list is fetched from the server and cached.
	 * All subsequent calls use the cached list of services.
	 * 
	 * @return Unmodifiable list of services.
	 * @throws IOException If error communicating with the server occurred.
	 * @throws ServerError If server response is not valid.
	 * @throws HttpException If server responded with error status code.
	 */
	public List<Service> getServices()
			throws IOException, ServerError, HttpException {
		if (services == null) {
			services = Collections.unmodifiableList(fetchServices());
		}
		return services;
	}
	
	private List<Service> fetchServices() 
			throws IOException, ServerError, HttpException {
		HttpGet request = new HttpGet(httpClient.getURL("services"));
		CloseableHttpResponse response = httpClient.execute(request);
		int statusCode = response.getStatusLine().getStatusCode();
		try {
		if (statusCode == 200) {
			try {
				List<Service> services = new ArrayList<>();
				JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
				JSONArray servicesArray = json.getJSONArray("services");
				for (int i = 0; i < servicesArray.length(); ++i) {
					services.add(new Service(
							this.httpClient,
							servicesArray.getJSONObject(i).getString("name"),
							servicesArray.getJSONObject(i).getString("submitURI")
							));
				 }
				 return services;
			}
			catch (JSONException e) {
				throw new ServerError("Invalid JSON response", e);
			}
		}
		else if (statusCode >= 400) {
			throw httpClient.getHttpException(response);
		}
		else {
			throw new ServerError("Invalid status code");
		}
		}
		finally {
			response.close();
		}
	}
	
	/**
	 * Uploads the file to the server.
	 * Sends a multipart form post request to the server containing the file and the mime-type.
	 * The file is uploaded using a default binary content type {@link ContentType#DEFAULT_BINARY}
	 * with the title provided in the title parameter.
	 * 
	 * @param input Input file to be submitted.
	 * @param mimeType Mime-type of the file content.
	 * @param title File title.
	 * @return Handler to the file on the server.
	 * @throws IOException If server communication error occurred.
	 * @throws HttpException If server responds with an error status code.
	 * @throws ServerError If server response id not valid.
	 */
	public FileHandler uploadFile(File input, String mimeType, String title)
			throws IOException, HttpException, ServerError {
		if (title == null)
			title = input.getName();
		HttpEntity entity = MultipartEntityBuilder
				.create()
				.addBinaryBody("file", input, ContentType.DEFAULT_BINARY, title)
				.addTextBody("mimetype", mimeType, ContentType.DEFAULT_TEXT)
				.build();
		return postFileEntity(new BufferedHttpEntity(entity));
	}
	
	/**
	 * Uploads the data provided in the stream as a file.
	 * Uploads the data from the stream as a file and its mime-type using a multipart form.
	 * The uploaded data has a default binary content type set {@link ContentType#DEFAULT_BINARY}
	 * File title is set to the value provided in the parameter. 
	 * 
	 * @param input Input stream providing file data.
	 * @param mimeType Mime-type of the stream content.
	 * @param title Title of the file.
	 * @return Handler to the file on the server.
	 * @throws IOException If server communication error occurred.
	 * @throws HttpException If server responds with an error status code.
	 * @throws ServerError If server response id not valid.
	 */
	public FileHandler uploadFile(InputStream input, String mimeType, String title)
			throws IOException, HttpException, ServerError {
		HttpEntity entity = MultipartEntityBuilder
				.create()
				.addBinaryBody("file", input, ContentType.DEFAULT_BINARY, title)
				.addTextBody("mimetype", mimeType, ContentType.DEFAULT_TEXT)
				.build();
		return postFileEntity(new BufferedHttpEntity(entity));
	}

	private FileHandler postFileEntity(HttpEntity entity)
			throws IOException, HttpException, ServerError {
		HttpPost request = new HttpPost(httpClient.getURL("file"));
		request.setEntity(entity);
		CloseableHttpResponse response = httpClient.execute(request);
		int statusCode = response.getStatusLine().getStatusCode();
		try {
		if (statusCode == 201) {
			try {
				JSONObject json = new JSONObject(
						EntityUtils.toString(response.getEntity()));
				return new FileHandler(
						httpClient, 
						json.getString("id"), 
						json.getString("title"), 
						json.getString("mimetype"),
						json.getString("downloadURI"));
			}
			catch (JSONException e) {
				throw new ServerError("Invalid JSON response", e);
			}
		}
		else if (statusCode >= 400) {
			throw httpClient.getHttpException(response);
		}
		else {
			throw new ServerError("Invalid status code");
		}
		}
		finally {
			response.close();
		}
	}
	
	/**
	 * Recreates the file handler from the file id.
	 * 
	 * @param fileID File id provided by the server on file submission.
	 * @return Handler to the file on the server.
	 * @throws IOException If server communication error occurred.
	 * @throws HttpException If server responds with an error status code.
	 * @throws ServerError If server response id not valid.
	 */
	public FileHandler getFileHandler(String fileID)
			throws IOException, HttpException, ServerError {
		FileHandler handler = new FileHandler(httpClient, fileID);
		return handler;
	}
	
	
	/**
	 * Manages URL building and sending requests to the server.
	 * Each HttpClient is bound to the single SlivkaClient and is used by all
	 * components to create URLs and send requests to the server.
	 * This class is not supposed to be instantiated and used outside of slivka client components.
	 * 
	 * @author Mateusz Warowny
	 */
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
		
		public HttpException getHttpException(CloseableHttpResponse response)
				throws IOException, ServerError {
			return new HttpException(response);
		}
	}
}
 