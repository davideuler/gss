/*
 * Copyright 2008, 2009 Electronic Business Systems Ltd.
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
package gr.ebs.gss.client.rest.resource;

import java.io.Serializable;


/**
 * @author kman
 *
 */
public class PermissionHolder implements Serializable{

	private String user;
	private String group;
	private boolean read;
	private boolean write;
	private boolean modifyACL;

	/**
	 * Retrieve the user.
	 *
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Modify the user.
	 *
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Retrieve the group.
	 *
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Modify the group.
	 *
	 * @param group the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Retrieve the read.
	 *
	 * @return the read
	 */
	public boolean isRead() {
		return read;
	}

	/**
	 * Modify the read.
	 *
	 * @param read the read to set
	 */
	public void setRead(boolean read) {
		this.read = read;
	}

	/**
	 * Retrieve the write.
	 *
	 * @return the write
	 */
	public boolean isWrite() {
		return write;
	}

	/**
	 * Modify the write.
	 *
	 * @param write the write to set
	 */
	public void setWrite(boolean write) {
		this.write = write;
	}

	/**
	 * Retrieve the modifyACL.
	 *
	 * @return the modifyACL
	 */
	public boolean isModifyACL() {
		return modifyACL;
	}

	/**
	 * Modify the modifyACL.
	 *
	 * @param modifyACL the modifyACL to set
	 */
	public void setModifyACL(boolean modifyACL) {
		this.modifyACL = modifyACL;
	}



}
