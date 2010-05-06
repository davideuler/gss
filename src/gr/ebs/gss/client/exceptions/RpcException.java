/*
 * Copyright 2007, 2008, 2009 Electronic Business Systems Ltd.
 *
 * This file is part of GSS.
 *
 * GSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GSS.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.ebs.gss.client.exceptions;

import java.io.Serializable;

/**
 * An exception thrown when there is an error while communicating with the
 * backend service.
 *
 * @author past
 */
public class RpcException extends Exception implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The stored message that provides details about the problem.
	 */
	private String message;

	/**
	 * Default constructor
	 */
	public RpcException() {
		super();
	}

	/**
	 * Constructor from error message.
	 *
	 * @param newMessage The error message
	 */
	public RpcException(final String newMessage) {
		super(newMessage);
		message = newMessage;
	}

	/**
	 * Constructor from Throwable.
	 *
	 * @param cause The throwable that caused the exception
	 */
	public RpcException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor from error message and Throwable.
	 *
	 * @param newMessage The error message
	 * @param cause The throwable that caused the exception
	 */
	public RpcException(final String newMessage, final Throwable cause) {
		super(newMessage, cause);
		message = newMessage;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Throwable#getMessage()
	 */
	public String getMessage() {
		return message;
	}

}
