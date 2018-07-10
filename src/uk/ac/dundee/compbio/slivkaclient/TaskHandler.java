package uk.ac.dundee.compbio.slivkaclient;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TaskHandler {
	
	public static enum ExecutionStatus {
		PENDING("pending"),
		QUEUED("queued"),
		RUNNING("running"),
		COMPLETED("completed"),
		FAILED("failed"),
		ERROR("error");
		
		private String name;
		
		ExecutionStatus(String name) {
			this.name = name;
		}
		
		public static ExecutionStatus fromName(String name) {
			for (ExecutionStatus status : ExecutionStatus.values()) {
				if (name.equals(status.name)) {
					return status;
				}
			}
			throw new IllegalArgumentException("Invalid status \"" + name + "\"");
		}
	}


	public class TaskStatus {
		
		private final ExecutionStatus status;
		private final boolean ready;

		private TaskStatus(ExecutionStatus status, boolean ready) {
			this.status = status;
			this.ready = ready;
		}
		
		public ExecutionStatus getStatus() {
			return status;
		}
		
		public boolean isReady() {
			return ready;
		}
		
		public URI getResultURL() {
			return resultURL;
		}
	}
	
	
	private final SlivkaClient.HttpClient client;
	private final String id;
	private final URI statusURL;
	private final URI resultURL;
	
	public TaskHandler(SlivkaClient.HttpClient client, String taskID, String statusPath) {
		this.client = client;
		this.id = taskID;
		this.statusURL = client.getURL(statusPath);
		this.resultURL = client.getURL("/task/" + taskID + "/result");
	}
	
	public String getTaskID() {
		return id;
	}
	
	public URI getStatusURL() {
		return statusURL;
	}
	
	public URI getResultURL() {
		return resultURL;
	}
	
	public TaskStatus getStatus() throws IOException {
		HttpGet request = new HttpGet(statusURL);
		CloseableHttpResponse response = client.execute(request);
		try {
			if (response.getStatusLine().getStatusCode() == 200) {
				JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
				return new TaskStatus(
						ExecutionStatus.fromName(json.getString("execution")),
						json.getBoolean("ready")
						);
			}
			else
				return null;
		}
		catch (JSONException e) {
			throw new ServerError("Invalid JSON response", e);
		}
		finally {
			response.close();
		}
	}
	
	public List<FileHandler> getResult() throws IOException {
		List<FileHandler> files = new ArrayList<>();
		HttpGet request = new HttpGet(resultURL);
		CloseableHttpResponse response = client.execute(request);
		try {
			JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
			JSONArray filesArray = json.getJSONArray("files");
			for (int i = 0; i < filesArray.length(); ++i) {
				JSONObject fileObject = filesArray.getJSONObject(i);
				files.add(new FileHandler(
						client,
						fileObject.getString("id"),
						fileObject.getString("title"),
						fileObject.optString("mimetype", ""),
						fileObject.getString("downloadURI")
						));
			}
		}
		catch (JSONException e) {
			throw new ServerError("Invalid JSON response", e);
		}
		finally {
			response.close();
		}
		return files;
	}
	
	public String toString() {
		return String.format("<Task %s>", id);
	}
}
