package uk.ac.dundee.compbio.slivkaclient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Representation of the file on the slivka server.
 * It provides access to file metadata and content downloading. 
 * @author Mateusz Warowny
 *
 */
public class FileHandler {
	
	private final SlivkaClient.HttpClient client;
	private final String fileID;
	private String title;
	private String mimeType;
	private URI downloadURL;
	private final URI dataURL;
	
	/**
	 * Constructs a basic file handler containing the file id only.
	 * Can be used to reconstruct the file handler having the file id.
	 * Created object is not initialised with data hence {@link #updateData()}
	 * needs to be called before accessing any other method or field.
	 *  
	 * @param client Http client bound to the slivka client instance.
	 * @param fileID Id of the file.
	 * @throws IOException If server communication error occurred.
	 * @throws HttpException If server responds with an error status code.
	 */
	FileHandler(SlivkaClient.HttpClient client, String fileID) throws IOException, HttpException {
		this.client = client;
		this.fileID = fileID;
		dataURL = client.getURL("file/" + fileID);
		updateData();
	}
	
	/**
	 * Constructs a full file handler initialised with all metadata.
	 * 
	 * @param client Http client bound to the slivka client.
	 * @param fileID File identifier provided by the server.
	 * @param title File title.
	 * @param mimeType Mime-type of the file content.
	 * @param downloadPath Server path pointing to file download.
	 */
	FileHandler(SlivkaClient.HttpClient client, String fileID, 
			String title, String mimeType, String downloadPath) {
		this.client = client;
		this.fileID = fileID;
		this.title = title;
		this.mimeType = mimeType;
		this.downloadURL = client.getURL(downloadPath);
		this.dataURL = client.getURL("file/" + fileID);
	}
	
	/**
	 * Gets file ID.
	 * @return File identifier provided by the server.
	 */
	public String getID() {
		return fileID;
	}
	
	/**
	 * Gets the mime-type of the file.
	 * @return Mime-type of the file content.
	 */
	public String getMimeType() {
		return mimeType;
	}
	
	/**
	 * Gets the title of the file.
	 * @return File title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Gets the URL for downloading the file content.
	 * @return File download URL.
	 */
	public URI getDownloadURL() {
		return downloadURL;
	}
	
	/**
	 * Fetches the file data from the server and updates fields.
	 * @throws IOException If server communication error occurred.
	 * @throws HttpException If server responds with an error status code.
	 */
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
	
	/**
	 * Downloads the file from the server and writes its content to the stream.
	 * @param out Stream the data is written to.
	 * @throws IOException If server communication error occurred.
	 */
	public void writeTo(OutputStream out) throws IOException {
		HttpGet request = new HttpGet(getDownloadURL());
		CloseableHttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() == 200) {
			response.getEntity().writeTo(out);
		}
	}
}
