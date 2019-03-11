package edu.internet2.consent.arpsi.model;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.annotation.*;

public class ErrorModel {
	private int code;
	private String message;
	
	public ErrorModel(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	@JsonProperty("code")
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	
	@JsonProperty("message")
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public boolean equals(Object o) {
		// Short circuit on instance duplicates
		if (this == o) {
			return true;
		}
		// Short circuit on clear fails
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		// Compare and return otherwise
		ErrorModel x = (ErrorModel) o;
		return (x.getCode() == ((ErrorModel) (o)).getCode() && x.getMessage().equals(((ErrorModel) (o)).getMessage()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(code, message);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ErrorModel {\n");
		sb.append("  code: ").append(code).append("\n");
		sb.append("  message: ").append(message).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
	
	public Response toResponse() {
		try {
			return Response.status(Status.fromStatusCode(this.getCode())).entity(this.toJSON()).header("Access-Control-Allow-Methods",  "GET, POST, PUT, DELETE, PATCH").header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept").type("application/json").build();
		} catch (JsonProcessingException j) {
			return Response.status(Status.fromStatusCode(this.getCode())).entity("{\"code\":\""+this.getCode()+"\",\"message\":\"Error\"}").header("Access-Control-Allow-Methods","GET,POST,PUT,DELETE,PATCH").header("Access-Control-Allow-Credentials","true").header("Access-Control-Allow-Headers","Origin,X-Requested-With,Content-Type,Accept").type("application/json").build();
		}
	}
	
}
