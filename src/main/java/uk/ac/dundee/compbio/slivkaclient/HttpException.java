package uk.ac.dundee.compbio.slivkaclient;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Exception thrown when the server responds with an error code.
 * 
 * @author Mateusz Warowny
 *
 */
@SuppressWarnings("serial")
public class HttpException extends Exception {

	private int statusCode;
	private String reason;
	private JSONObject content;
	
	HttpException(CloseableHttpResponse response) throws IOException, ServerError {
		statusCode = response.getStatusLine().getStatusCode();
		reason = response.getStatusLine().getReasonPhrase();
		try {
			content = new JSONObject(EntityUtils.toString(response.getEntity()));
		}
		catch (JSONException e) {
			throw new ServerError("Invalid JSON reponse", e);
		}
	}
	
	/**
	 * Gets http status code of the error.
	 * @return Http status code
	 */
	public int getStatusCode() {
		return statusCode;
	}
	
	/**
	 * Gets http reason phrase
	 * @return
	 */
	public String getReason() {
		return reason;
	}
	
	/***
	 * Gets JSON object built from the response content.
	 * @return response content as JSON
	 */
	public JSONObject getJSONContent() {
		return content;
	}
	
	public String toString() {
		return String.format("%d %s", getStatusCode(), getReason());
	}
}
