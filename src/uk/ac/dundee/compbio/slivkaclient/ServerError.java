package uk.ac.dundee.compbio.slivkaclient;

@SuppressWarnings("serial")
public class ServerError extends Error {

	public ServerError(String message) {
		super(message);
	}
	
	public ServerError(Throwable cause) {
		super(cause);
	}
	
	public ServerError(String message, Throwable cause) {
		super(message, cause);
	}
}
