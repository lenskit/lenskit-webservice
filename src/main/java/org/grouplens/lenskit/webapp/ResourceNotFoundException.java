package org.grouplens.lenskit.webapp;

public class ResourceNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ResourceNotFoundException() {
		super();
	}
	
	public ResourceNotFoundException(String message) {
		super(message);
	}
	
	public ResourceNotFoundException(Throwable cause) {
		super(cause);
	}
	
	public ResourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
