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

/**
 * Representation of the submitted task.
 * It provides the access to the task status and result.
 *  
 * @author Mateusz Warowny
 *
 */
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
		
		static ExecutionStatus fromName(String name) {
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
		
		/**
		 * Gets the current job status.
		 * @return Job status.
		 */
		public ExecutionStatus getStatus() {
			return status;
		}
		
		/**
		 * Indicated whether the job finished running.
		 * Ready job does not mean it finished successfully.
		 * For more information on job status use {@link #getStatus()}.
		 * 
		 * @return true if the job has terminated, false otherwise.
		 */
		public boolean isReady() {
			return ready;
		}
		
		/**
		 * Gets the URL where the job result can be retrieved.
		 * @return URL for job result retrieval.
		 */
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
	
	/**
	 * Gets job identifier assigned on form submission.
	 * @return Job identifier.
	 */
	public String getTaskID() {
		return id;
	}
	
	/**
	 * Gets URL where the job status can be checked.
	 * @return Job status check URL.
	 */
	public URI getStatusURL() {
		return statusURL;
	}
	
	/**
	 * Gets URL where the job result can be retrieved.
	 * @return Job result retrieval URL.
	 */
	public URI getResultURL() {
		return resultURL;
	}
	
	/**
	 * Gets the current job status.
	 * Asks the server to return the current status of the submitted job.
	 * 
	 * @return Current job status.
	 * @throws IOException If an error occurs during the connection to the server.
	 * @throws HttpException If the server responds with and error status code.
	 * @throws ServerError If the server response is invalid.
	 */
	public TaskStatus getStatus() throws IOException, ServerError, HttpException {
		HttpGet request = new HttpGet(statusURL);
		CloseableHttpResponse response = client.execute(request);
		int statusCode = response.getStatusLine().getStatusCode();
		try {
		if (statusCode == 200) {
			try {
				JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
				return new TaskStatus(
						ExecutionStatus.fromName(json.getString("execution")),
						json.getBoolean("ready")
						);
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
	
	/**
	 * Gets all currently available results of the job.
	 * Fetches all the result files associated with the job and wraps them
	 * into the file handlers.
	 * 
	 * @return Result files produced by this job.
	 * @throws IOException If an error occurs during the connection to the server.
	 * @throws HttpException If the server responds with and error status code.
	 * @throws ServerError If the server response is invalid.
	 */
	public List<FileHandler> getResult() throws IOException, HttpException, ServerError {
		HttpGet request = new HttpGet(resultURL);
		CloseableHttpResponse response = client.execute(request);
		int statusCode = response.getStatusLine().getStatusCode();
		try {
		if (statusCode == 200) {
			List<FileHandler> files = new ArrayList<>();
			try {
				JSONObject json = new JSONObject(
						EntityUtils.toString(response.getEntity()));
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
			return files;
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
		return String.format("Task %s", id);
	}
}
