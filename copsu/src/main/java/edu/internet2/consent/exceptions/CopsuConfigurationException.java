package edu.internet2.consent.exceptions;

public class CopsuConfigurationException extends RuntimeException {
	static final long serialVersionUID = 81750982;
	public CopsuConfigurationException() {
		super();
	}
	public CopsuConfigurationException(String msg) {
		super(msg);
	}
	public CopsuConfigurationException(Throwable cause) {
		super(cause);
	}
	public CopsuConfigurationException(String msg, Throwable cause) {
		super(msg,cause);
	}
}
