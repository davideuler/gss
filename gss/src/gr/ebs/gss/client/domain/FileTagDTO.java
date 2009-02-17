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
package gr.ebs.gss.client.domain;

import java.io.Serializable;

/**
 * A file tag. This is basically a many-to-many relationship between User and
 * FileHeader, with an extra string which is the tag. For implementation
 * details, see Java Persistence With Hibernate, p. 303.
 *
 * @author droutsis
 */
public class FileTagDTO implements Serializable {

	/**
	 * The serial version UID of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The user who added this tag.
	 */
	private UserDTO user;

	/**
	 * The file to which this tag belongs.
	 */
	private FileHeaderDTO file;

	/**
	 * The name of the tag.
	 */
	private String tag;

	/**
	 * Retrieve the user.
	 *
	 * @return the user DTO object
	 */
	public UserDTO getUser() {
		return user;
	}

	/**
	 * Retrieve the file header.
	 *
	 * @return the file header DTO object
	 */
	public FileHeaderDTO getFile() {
		return file;
	}

	/**
	 * Retrieve the tag.
	 *
	 * @return the tag string
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * Modify the user.
	 *
	 * @param newUser the user to set
	 */
	public void setUser(final UserDTO newUser) {
		user = newUser;
	}

	/**
	 * Modify the file.
	 *
	 * @param newFile the file to set
	 */
	public void setFile(final FileHeaderDTO newFile) {
		file = newFile;
	}

	/**
	 * Modify the tag.
	 *
	 * @param newTag the tag to set
	 */
	public void setTag(final String newTag) {
		tag = newTag;
	}

}
