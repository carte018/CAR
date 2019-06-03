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
