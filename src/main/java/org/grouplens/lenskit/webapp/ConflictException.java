package org.grouplens.lenskit.webapp;

public class ConflictException extends Exception {

	private static final long serialVersionUID = 1L;

	public ConflictException() {
		super();
	}
	
	public ConflictException(String message) {
		super(message);
	}
	
	public ConflictException(Throwable cause) {
		super(cause);
	}
	
	public ConflictException(String message, Throwable cause) {
		super(message, cause);
	}

}
