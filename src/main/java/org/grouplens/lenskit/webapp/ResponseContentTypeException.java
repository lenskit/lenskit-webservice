package org.grouplens.lenskit.webapp;

public class ResponseContentTypeException extends Exception {
	private static final long serialVersionUID = 1L;

	public ResponseContentTypeException() {
		super();
	}
	
	public ResponseContentTypeException(String message) {
		super(message);
	}
	
	public ResponseContentTypeException(Throwable cause) {
		super(cause);
	}
	
	public ResponseContentTypeException(String message, Throwable cause) {
		super(message, cause);
	}
}