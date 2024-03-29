/*
 * Copyright 2010 Electronic Business Systems Ltd.
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
package org.gss_project.gss.common.exceptions;

import java.io.Serializable;

import javax.ejb.ApplicationException;

/**
 * An exception thrown when trying to reuse an already used invitation.
 *
 * @author past
 */
@ApplicationException(rollback=true)
public class InvitationUsedException extends Exception implements Serializable {

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
	public InvitationUsedException() {
		super();
	}

	/**
	 * Constructor from error message.
	 *
	 * @param newMessage The error message
	 */
	public InvitationUsedException(final String newMessage) {
		super(newMessage);
		message = newMessage;
	}

	/**
	 * Constructor from Throwable.
	 *
	 * @param cause The throwable that caused the exception
	 */
	public InvitationUsedException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor from error message and Throwable.
	 *
	 * @param newMessage The error message
	 * @param cause The throwable that caused the exception
	 */
	public InvitationUsedException(final String newMessage, final Throwable cause) {
		super(newMessage, cause);
		message = newMessage;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
