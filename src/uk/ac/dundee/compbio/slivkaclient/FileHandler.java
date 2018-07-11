package uk.ac.dundee.compbio.slivkaclient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class FileHandler {
	
	private final SlivkaClient.HttpClient client;
	private final String fileID;
	private String title;
	private String mimeType;
	private URI downloadURL;
	private final URI dataURL;
	private boolean initialized;
	
	public FileHandler(SlivkaClient.HttpClient client, String fileID) {
		this.client = client;
		this.fileID = fileID;
		dataURL = client.getURL("file/" + fileID);
		initialized = false;
	}
	
	FileHandler(SlivkaClient.HttpClient client, String fileID, 
			String title, String mimeType, String downloadPath) {
		this.client = client;
		this.fileID = fileID;
		this.title = title;
		this.mimeType = mimeType;
		this.downloadURL = client.getURL(downloadPath);
		this.dataURL = client.getURL("file/" + fileID);
		initialized = true;
	}
	
	public String getID() {
		return fileID;
	}
	
	public boolean isPopulated() {
		return initialized;
	}
	
	public String getMimeType() {
		if (!initialized)
			throw new IllegalStateException();
		return mimeType;
	}
	
	public String getTitle() {
		if (!initialized)
			throw new IllegalStateException();
		return title;
	}

	public URI getDownloadURL() {
		if (!initialized)
			throw new IllegalStateException();
		return downloadURL;
	}
	
	public void updateData() throws IOException, HttpException {
		HttpGet request = new HttpGet(dataURL);
		CloseableHttpResponse response = client.execute(request);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 200) {
			try {
				JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
				title = json.getString("title");
				mimeType = json.getString("mimetype");
				downloadURL = client.getURL(json.getString("downloadURI"));
				initialized = true;
			}
			catch (JSONException e) {
				throw new ServerError("Invalid JSON response", e);
			}
			finally {
				response.close();
			}
		}
		else if (statusCode >= 400) {
			throw client.getHttpException(response);
		}
		else {
			throw new ServerError("Invalid status code");
		}
	}
	
	public void writeTo(OutputStream out) throws IOException {
		HttpGet request = new HttpGet(downloadURL);
		CloseableHttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() == 200)
			response.getEntity().writeTo(out);
	}
}
