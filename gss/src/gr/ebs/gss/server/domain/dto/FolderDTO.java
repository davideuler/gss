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
package gr.ebs.gss.server.domain.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chstath
 */
public class FolderDTO implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The persistence ID of the object.
	 */
	private Long id;

	/**
	 * The folder name.
	 */
	private String name;

	/**
	 * The folder path.
	 */
	private String path;

	/**
	 * The subfolders in this folder. A List so we can keep order.
	 *
	 *
	 */
	private List<FolderDTO> subfolders = new ArrayList<FolderDTO>();

	/**
	 * The owner of this folder.
	 */
	private UserDTO owner;

	/**
	 * The parent folder that contains this folder.
	 */
	private FolderDTO parent;

	/**
	 * The audit information for this object.
	 */
	private AuditInfoDTO auditInfo;
	/**
	 * Is this folder temporarily deleted?
	 */
	private boolean deleted=false;
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param newId the id to set
	 */
	public void setId(final Long newId) {
		id = newId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param newName the name to set
	 */
	public void setName(final String newName) {
		name = newName;
	}

	/**
	 * @return the subfolders
	 */
	public List<FolderDTO> getSubfolders() {
		return subfolders;
	}

	/**
	 * @param newSubfolders the subfolders to set
	 */
	public void setSubfolders(final List<FolderDTO> newSubfolders) {
		subfolders = newSubfolders;
	}

	/**
	 * @return the owner
	 */
	public UserDTO getOwner() {
		return owner;
	}

	/**
	 * @param newOwner the owner to set
	 */
	public void setOwner(final UserDTO newOwner) {
		owner = newOwner;
	}

	/**
	 * Retrieve the parent.
	 *
	 * @return the parent
	 */
	public FolderDTO getParent() {
		return parent;
	}

	/**
	 * Modify the parent.
	 *
	 * @param newParent the parent to set
	 */
	public void setParent(final FolderDTO newParent) {
		parent = newParent;
	}

	/**
	 * Retrieve the auditInfo.
	 *
	 * @return the auditInfo
	 */
	public AuditInfoDTO getAuditInfo() {
		return auditInfo;
	}

	/**
	 * Modify the auditInfo.
	 *
	 * @param newAuditInfo the auditInfo to set
	 */
	public void setAuditInfo(final AuditInfoDTO newAuditInfo) {
		auditInfo = newAuditInfo;
	}


	/**
	 * Retrieve the deleted.
	 *
	 * @return the deleted
	 */
	public boolean isDeleted() {
		return deleted;
	}


	/**
	 * Modify the deleted.
	 *
	 * @param newDeleted the deleted to set
	 */
	public void setDeleted(boolean newDeleted) {
		deleted = newDeleted;
	}


	/**
	 * Retrieve the path.
	 *
	 * @return the path
	 */
	public String getPath() {
		return path;
	}


	/**
	 * Modify the path.
	 *
	 * @param aPath the path to set
	 */
	public void setPath(String aPath) {
		path = aPath;
	}

	/**
	 * Retrieve the URI for this resource. This unique identifier can
	 * be used to refer to the resource from various GSS clients.
	 *
	 * @return the URI
	 */
	public String getURI() {
		return FileHeaderDTO.PATH_GSS + owner.getUsername() + FileHeaderDTO.PATH_FILES + getPath();
	}

}
