package uk.ac.dundee.compbio.slivkaclient;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

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
	
	public int getStatusCode() {
		return statusCode;
	}
	
	public String getReason() {
		return reason;
	}
	
	public JSONObject getJSONContent() {
		return content;
	}
	
	public String toString() {
		return String.format("%d %s", getStatusCode(), getReason());
	}
}
