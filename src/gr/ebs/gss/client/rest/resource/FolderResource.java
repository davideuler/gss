/*
 * Copyright 2009, 2010 Electronic Business Systems Ltd.
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

import gr.ebs.gss.client.DisplayHelper;
import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.rest.MultipleGetCommand;
import gr.ebs.gss.client.rest.MultipleGetCommand.Cached;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * @author kman
 */
public class FolderResource extends RestResource {

	public FolderResource(String aUri) {
		super(aUri);
	}

	String name;

	String owner;

	String createdBy;

	String modifiedBy;

	Date creationDate;

	Date modificationDate;

	List<String> filePaths = new LinkedList<String>();

	List<String> subfolderPaths = new LinkedList<String>();

	Set<PermissionHolder> permissions = new HashSet<PermissionHolder>();

	List<FolderResource> folders = new ArrayList<FolderResource>();

	List<FileResource> files = new ArrayList<FileResource>();

	String parentURI;

	boolean deleted = false;

	boolean needsExpanding = false;

	String parentName;

	private boolean filesExpanded=false;

	/**
	 * Modify the parentName.
	 *
	 * @param aParentName the parentName to set
	 */
	public void setParentName(String aParentName) {
		parentName = aParentName;
	}

	/**
	 * Retrieve the name.
	 *
	 * @return the name
	 */
	@Override
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
	 * Retrieve the owner.
	 *
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Modify the owner.
	 *
	 * @param anOwner the owner to set
	 */
	public void setOwner(String anOwner) {
		owner = anOwner;
	}

	/**
	 * Retrieve the createdBy.
	 *
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * Modify the createdBy.
	 *
	 * @param aCreatedBy the createdBy to set
	 */
	public void setCreatedBy(String aCreatedBy) {
		createdBy = aCreatedBy;
	}

	/**
	 * Retrieve the modifiedBy.
	 *
	 * @return the modifiedBy
	 */
	public String getModifiedBy() {
		return modifiedBy;
	}

	/**
	 * Modify the modifiedBy.
	 *
	 * @param aModifiedBy the modifiedBy to set
	 */
	public void setModifiedBy(String aModifiedBy) {
		modifiedBy = aModifiedBy;
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
	 * Retrieve the filePaths.
	 *
	 * @return the filePaths
	 */
	public List<String> getFilePaths() {
		return filePaths;
	}

	/**
	 * Modify the filePaths.
	 *
	 * @param newFilePaths the filePaths to set
	 */
	public void setFilePaths(List<String> newFilePaths) {
		filePaths = newFilePaths;
	}

	/**
	 * Retrieve the subfolderPaths.
	 *
	 * @return the subfolderPaths
	 */
	public List<String> getSubfolderPaths() {
		return subfolderPaths;
	}

	/**
	 * Modify the subfolderPaths.
	 *
	 * @param newSubfolderPaths the subfolderPaths to set
	 */
	public void setSubfolderPaths(List<String> newSubfolderPaths) {
		subfolderPaths = newSubfolderPaths;
	}

	/**
	 * Retrieve the permissions.
	 *
	 * @return the permissions
	 */
	public Set<PermissionHolder> getPermissions() {
		return permissions;
	}

	/**
	 * Modify the permissions.
	 *
	 * @param newPermissions the permissions to set
	 */
	public void setPermissions(Set<PermissionHolder> newPermissions) {
		permissions = newPermissions;
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

	public void removeSubfolderPath(String spath) {
		if (subfolderPaths.remove(spath))
			return;
		else if (subfolderPaths.remove(spath + "/"))
			return;
		else
			subfolderPaths.remove(spath.substring(0, spath.length() - 1));
	}

	/**
	 * Retrieve the folders.
	 *
	 * @return the folders
	 */
	public List<FolderResource> getFolders() {
		return folders;
	}

	/**
	 * Modify the folders.
	 *
	 * @param newFolders the folders to set
	 */
	public void setFolders(List<FolderResource> newFolders) {
		folders = newFolders;
	}

	/**
	 * Retrieve the files.
	 *
	 * @return the files
	 */
	public List<FileResource> getFiles() {
		return files;
	}

	/**
	 * Modify the files.
	 *
	 * @param newFiles the files to set
	 */
	public void setFiles(List<FileResource> newFiles) {
		files = newFiles;
	}

	/**
	 * Retrieve the parentURI.
	 *
	 * @return the parentURI
	 */
	public String getParentURI() {
		return parentURI;
	}

	/**
	 * Modify the parentURI.
	 *
	 * @param aParentURI the parentURI to set
	 */
	public void setParentURI(String aParentURI) {
		parentURI = aParentURI;
	}

	@Override
	public void createFromJSON(String text) {
		JSONObject json = (JSONObject) JSONParser.parse(text);
		name = unmarshallString(json, "name");
		owner = unmarshallString(json, "owner");
		deleted = unmarshallBoolean(json, "deleted");
		if (deleted)
			GWT.log("FOUND A DELETED FOLDER:" + name, null);

		if (json.get("parent") != null) {
			JSONObject parent = json.get("parent").isObject();
			parentURI = unmarshallString(parent, "uri");
			parentName = unmarshallString(parent, "name");
		}

		if (json.get("permissions") != null) {
			JSONArray perm = json.get("permissions").isArray();
			if (perm != null)
				for (int i = 0; i < perm.size(); i++) {
					JSONObject obj = perm.get(i).isObject();
					if (obj != null) {
						PermissionHolder permission = new PermissionHolder();
						if (obj.get("user") != null)
							permission.setUser(unmarshallString(obj, "user"));
						if (obj.get("group") != null)
							permission.setGroup(URL.decodeComponent(unmarshallString(obj, "group")));
						permission.setRead(unmarshallBoolean(obj, "read"));
						permission.setWrite(unmarshallBoolean(obj, "write"));
						permission.setModifyACL(unmarshallBoolean(obj, "modifyACL"));
						permissions.add(permission);
					}
				}
		}
		if (json.get("folders") != null) {
			JSONArray subs = json.get("folders").isArray();
			if (subs != null)
				for (int i = 0; i < subs.size(); i++) {
					JSONObject so = subs.get(i).isObject();
					if (so != null) {
						String subUri = unmarshallString(so, "uri");
						String subName = unmarshallString(so, "name");
						if (subUri != null && subName != null) {
							if (!subUri.endsWith("/"))
								subUri = subUri + "/";
							FolderResource sub = new FolderResource(subUri);
							sub.setName(subName);
							sub.setNeedsExpanding(true);
							folders.add(sub);
							subfolderPaths.add(subUri);
						}
					}
				}
		}
		if (json.get("files") != null) {
			JSONArray subs = json.get("files").isArray();
			if (subs != null)
				for (int i = 0; i < subs.size(); i++) {
					JSONObject fo = subs.get(i).isObject();
					if (fo != null) {
						String fname = unmarshallString(fo, "name");
						String fowner = unmarshallString(fo, "owner");
						String fcontent = unmarshallString(fo, "content");
						String fpath = unmarshallString(fo, "path");
						fpath = URL.decodeComponent(fpath);
						Integer fversion = null;
						if (fo.get("version") != null)
							fversion = new Integer(fo.get("version").toString());
						boolean fdeleted = unmarshallBoolean(fo, "deleted");
						Date fcreationDate = null;
						if (fo.get("creationDate") != null)
							fcreationDate = new Date(new Long(fo.get("creationDate").toString()));
						Date fmodificationDate = null;
						if (fo.get("modificationDate") != null)
							fmodificationDate = new Date(new Long(fo.get("modificationDate").toString()));
						String furi = unmarshallString(fo, "uri");
						Long fsize = 0L;
						if (fo.get("size") != null)
							fsize = new Long(fo.get("size").toString());
						filePaths.add(furi);
						FileResource fs = new FileResource(furi);
						fs.setName(fname);
						fs.setOwner(fowner);
						fs.setPath(fpath);
						fs.setVersion(fversion);
						fs.setContentLength(fsize);
						fs.setDeleted(fdeleted);
						fs.setCreationDate(fcreationDate);
						fs.setModificationDate(fmodificationDate);
						fs.setContentType(fcontent);
						files.add(fs);
					}
				}
		}
		if (json.get("creationDate") != null)
			creationDate = new Date(new Long(json.get("creationDate").toString()));
		if (json.get("modificationDate") != null)
			modificationDate = new Date(new Long(json.get("modificationDate").toString()));
	}

	public String getParentName(){
		return parentName;
	}

	/**
	 * Retrieve the needsExpanding.
	 *
	 * @return the needsExpanding
	 */
	public boolean isNeedsExpanding() {
		return needsExpanding;
	}

	/**
	 * Modify the needsExpanding.
	 *
	 * @param newNeedsExpanding the needsExpanding to set
	 */
	public void setNeedsExpanding(boolean newNeedsExpanding) {
		needsExpanding = newNeedsExpanding;
	}

	public boolean isShared(){

		for(PermissionHolder perm : permissions){
			if(perm.getUser() != null && !owner.equals(perm.getUser()))
				return true;
			if(perm.getGroup() != null)
				return true;
		}
		return false;
	}

	@Override
	public String getLastModifiedSince() {
		if(modificationDate != null)
			return getDate(modificationDate.getTime());
		return null;
	}

	public MultipleGetCommand.Cached[] getCache(){
		if(getSubfolderPaths().size() != getFolders().size()){
			GWT.log("MISMATCH IN PATH AND FOLDERS SIZE", null);
			return null;
		}
		MultipleGetCommand.Cached[] result = new MultipleGetCommand.Cached[getSubfolderPaths().size()];
		for(int i=0; i<getFolders().size();i++){
			FolderResource r = getFolders().get(i);
			Cached c = new Cached();
			c.cache=r;
			c.uri=r.uri;
			result[i] = c;
		}
		return result;
	}

	public MultipleGetCommand.Cached[] getFileCache(){
		if(getFilePaths().size() != getFiles().size()){
			GWT.log("MISMATCH IN PATH AND FILES SIZE", null);
			return null;
		}
		if(!filesExpanded)
			return null;
		MultipleGetCommand.Cached[] result = new MultipleGetCommand.Cached[getFilePaths().size()];
		for(int i=0; i<getFiles().size();i++){
			FileResource r = getFiles().get(i);
			Cached c = new Cached();
			c.cache=r;
			c.uri=r.uri;
			result[i] = c;
		}
		return result;
	}

	public void setFilesExpanded(boolean newFilesExpanded) {
		filesExpanded = newFilesExpanded;
	}
	/**
	 * construct the partial path of the selected TreeItem
	 *
	 * @param selectedItem the selectedItem to check
	 */
	private String constructPartialPath(TreeItem selectedItem){
	   String result = DisplayHelper.trim(selectedItem.getText());
	   TreeItem parent = selectedItem.getParentItem();
	   while (!(DisplayHelper.trim(parent.getText()).equals("My Shared") || DisplayHelper.trim(parent.getText()).equals("Other's Shared")||DisplayHelper.trim(parent.getText()).equals("Trash"))){
	      result = DisplayHelper.trim(parent.getText()) + "/" + result;
	      if(result.equals("My Shared")||result.equals("Other's Shared")) return result;
	      parent = parent.getParentItem();
	   }

	   return result;
	}
	/**
	 * examine whether a folder name like "Trash", "My Shared", "Other's Shared" is inside path
	 *
	 * @param selectedItem the selectedTreeItem to check
	 */

	private boolean containsFolder(TreeItem selectedItem, String folderName){
		TreeItem parent = selectedItem.getParentItem();
		while (parent != null){
			String parentItemText = parent.getText();
			String parentItemTextTr = DisplayHelper.trim(parentItemText);
			if(parentItemTextTr.equals(folderName)) return true;
			parent = parent.getParentItem();
			}
		return false;
	}
	@Override
	public String constructUri(TreeItem treeItem, String path){
		String constructedUri = "";
		if(containsFolder(treeItem, "My Shared")){
			//case: selected folders below My Shared folder
			String partialUri = constructPartialPath(treeItem);
			constructedUri = constructedUri + "Files/shared/" + partialUri;
			return constructedUri;
		}else if(containsFolder(treeItem, "Other's Shared")){
			//case: selected folders below Other's Shared folder
			String partialPath = constructPartialPath(treeItem);
			constructedUri = constructedUri + "Files/others/"+ partialPath;
			return constructedUri;
		}
		else if(getParentURI()==null){
			if(containsFolder(treeItem, "Trash")){
				//case: selected folders below Trash folder
				String partialUri = constructPartialPath(treeItem);
				constructedUri = constructedUri + "Files/trash/" + partialUri;
				return constructedUri;
			}
			//case: home folder is selected
			constructedUri = constructedUri + "Files/files/" + getName();
			return constructedUri;
		}
		else if(treeItem.getParentItem() == null){
			//this is the case when the user uses the browser's forward arrow to navigate through other's
			//shared folders and item.getParentItem is null only inside other's shared folder
			String apiPath = GSS.get().getApiPath();
			String newPath = getParentURI().substring(apiPath.lastIndexOf("/"));
			constructedUri = constructedUri + "Files"+ newPath + getName();
			return constructedUri;
		}
		else{
			String finalUri = getParentURI().substring(path.lastIndexOf("/")) + getName();
			constructedUri = constructedUri + "Files"+ finalUri;
			return constructedUri;
		}

	}

}
