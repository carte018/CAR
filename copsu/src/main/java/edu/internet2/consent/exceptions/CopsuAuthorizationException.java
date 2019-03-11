package edu.internet2.consent.exceptions;

public class CopsuAuthorizationException extends RuntimeException {
	static final long serialVersionUID = 109118713;
	public CopsuAuthorizationException() {
		super();
	}
	public CopsuAuthorizationException(String msg) {
		super(msg);
	}
	public CopsuAuthorizationException(Throwable cause) {
		super (cause);
	}
	public CopsuAuthorizationException(String msg, Throwable cause) {
		super (msg,cause);
	}
}
