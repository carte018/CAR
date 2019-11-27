/*
 * Copyright 2015 - 2019 Duke University
 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License Version 2 as published by
    the Free Software Foundation.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License Version 2
    along with this program.  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt>.

 */
package edu.internet2.consent.informed.model;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.informed.util.OMSingleton;

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
		// ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
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
