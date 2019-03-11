package edu.internet2.consent.exceptions;

public class CopsuInitializationException extends RuntimeException {
	static final long serialVersionUID = 46571830;
	public CopsuInitializationException() {
		super();
	}
	public CopsuInitializationException(String msg) {
		super(msg);
	}
	public CopsuInitializationException(Throwable cause) {
		super(cause);
	}
	public CopsuInitializationException(String msg, Throwable cause) {
		super(msg,cause);
	}
}
