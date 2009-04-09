/*
 * Copyright 2009 Electronic Business Systems Ltd.
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

import java.util.Date;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

/**
 * @author kman
 */
public class UserResource extends RestResource {

	public UserResource(String aUri) {
		super(aUri);
	}

	private String name;

	private String username;

	private String email;

	private Date creationDate;

	private Date modificationDate;

	private String filesPath;

	private String trashPath;

	private String sharedPath;

	private String othersPath;

	private String tagsPath;

	private String groupsPath;

	private QuotaHolder quota;

	/**
	 * Retrieve the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Modify the name.
	 *
	 * @param aName the name to set
	 */
	public void setName(String aName) {
		name = aName;
	}

	/**
	 * Retrieve the username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Modify the username.
	 *
	 * @param aUsername the username to set
	 */
	public void setUsername(String aUsername) {
		username = aUsername;
	}

	/**
	 * Retrieve the email.
	 *
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Modify the email.
	 *
	 * @param anEmail the email to set
	 */
	public void setEmail(String anEmail) {
		email = anEmail;
	}

	/**
	 * Retrieve the creationDate.
	 *
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * Modify the creationDate.
	 *
	 * @param aCreationDate the creationDate to set
	 */
	public void setCreationDate(Date aCreationDate) {
		creationDate = aCreationDate;
	}

	/**
	 * Retrieve the modificationDate.
	 *
	 * @return the modificationDate
	 */
	public Date getModificationDate() {
		return modificationDate;
	}

	/**
	 * Modify the modificationDate.
	 *
	 * @param aModificationDate the modificationDate to set
	 */
	public void setModificationDate(Date aModificationDate) {
		modificationDate = aModificationDate;
	}

	/**
	 * Retrieve the filesPath.
	 *
	 * @return the filesPath
	 */
	public String getFilesPath() {
		return filesPath;
	}

	/**
	 * Modify the filesPath.
	 *
	 * @param aFilesPath the filesPath to set
	 */
	public void setFilesPath(String aFilesPath) {
		filesPath = aFilesPath;
	}

	/**
	 * Retrieve the trashPath.
	 *
	 * @return the trashPath
	 */
	public String getTrashPath() {
		return trashPath;
	}

	/**
	 * Modify the trashPath.
	 *
	 * @param aTrashPath the trashPath to set
	 */
	public void setTrashPath(String aTrashPath) {
		trashPath = aTrashPath;
	}

	/**
	 * Retrieve the sharedPath.
	 *
	 * @return the sharedPath
	 */
	public String getSharedPath() {
		return sharedPath;
	}

	/**
	 * Modify the sharedPath.
	 *
	 * @param aSharedPath the sharedPath to set
	 */
	public void setSharedPath(String aSharedPath) {
		sharedPath = aSharedPath;
	}

	/**
	 * Retrieve the othersPath.
	 *
	 * @return the othersPath
	 */
	public String getOthersPath() {
		return othersPath;
	}

	/**
	 * Modify the othersPath.
	 *
	 * @param anOthersPath the othersPath to set
	 */
	public void setOthersPath(String anOthersPath) {
		othersPath = anOthersPath;
	}

	/**
	 * Retrieve the tagsPath.
	 *
	 * @return the tagsPath
	 */
	public String getTagsPath() {
		return tagsPath;
	}

	/**
	 * Modify the tagsPath.
	 *
	 * @param aTagsPath the tagsPath to set
	 */
	public void setTagsPath(String aTagsPath) {
		tagsPath = aTagsPath;
	}

	/**
	 * Retrieve the groupsPath.
	 *
	 * @return the groupsPath
	 */
	public String getGroupsPath() {
		return groupsPath;
	}

	/**
	 * Modify the groupsPath.
	 *
	 * @param aGroupsPath the groupsPath to set
	 */
	public void setGroupsPath(String aGroupsPath) {
		groupsPath = aGroupsPath;
	}

	/**
	 * Retrieve the quota.
	 *
	 * @return the quota
	 */
	public QuotaHolder getQuota() {
		return quota;
	}

	/**
	 * Modify the quota.
	 *
	 * @param aQuota the quota to set
	 */
	public void setQuota(QuotaHolder aQuota) {
		quota = aQuota;
	}

	@Override
	public void createFromJSON(String text) {
		JSONObject json = (JSONObject) JSONParser.parse(text);
		email = unmarshallString(json, "email");
		name = unmarshallString(json, "name");
		username = unmarshallString(json, "username");
		filesPath = unmarshallString(json, "files");
		groupsPath = unmarshallString(json, "groups");
		othersPath = unmarshallString(json, "others");
		sharedPath = unmarshallString(json, "shared");
		tagsPath = unmarshallString(json, "tags");
		trashPath = unmarshallString(json, "trash");
		if (json.get("creationDate") != null)
			creationDate = new Date(new Long(json.get("creationDate").toString()));
		if (json.get("modificationDate") != null)
			modificationDate = new Date(new Long(json.get("modificationDate").toString()));
		if (json.get("quota") != null) {
			JSONObject qj = (JSONObject) json.get("quota");
			if (qj != null) {
				quota = new QuotaHolder();
				if(qj.get("totalFiles") != null)
					quota.setFileCount(new Long(qj.get("totalFiles").toString()));
				if(qj.get("totalBytes") != null)
					quota.setFileSize(new Long(qj.get("totalBytes").toString()));
				if(qj.get("bytesRemaining") != null)
					quota.setQuotaLeftSize(new Long(qj.get("bytesRemaining").toString()));
			}
		}
	}

	@Override
	public String toString() {
		String res = email + "\n" + name + "\n" + username + "\n" + filesPath + "\n" + groupsPath;
		return res;
	}

}
