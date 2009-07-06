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
package gr.ebs.gss.client.clipboard;

import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.GroupUserResource;

import java.io.Serializable;
import java.util.List;


/**
 * @author kman
 *
 */
public class ClipboardItem implements Serializable{
	private int operation;
	private FileResource file;
	private List<FileResource> files;
	private FolderResource folderResource;
	private GroupUserResource user;

	public ClipboardItem(){}

	public ClipboardItem(int operation, List<FileResource> files){
		this.operation=operation;
		this.files = files;
	}

	public ClipboardItem(int operation, FileResource file){
		this.operation=operation;
		this.file = file;
	}

	public ClipboardItem(int operation, FolderResource folder){
		this.operation=operation;
		folderResource = folder;
	}
	public ClipboardItem(int operation, GroupUserResource user){
		this.operation=operation;
		this.user = user;
	}

	public ClipboardItem(GroupUserResource user){
		operation=Clipboard.COPY;
		this.user = user;
	}

	public ClipboardItem(List<FileResource> files){
		operation=Clipboard.COPY;
		this.files = files;
	}

	public ClipboardItem(FileResource file){
		operation=Clipboard.COPY;
		this.file = file;
	}

	public ClipboardItem(FolderResource folder){
		operation=Clipboard.COPY;
		folderResource = folder;
	}




	/**
	 * Retrieve the user.
	 *
	 * @return the user
	 */
	public GroupUserResource getUser() {
		return user;
	}


	/**
	 * Modify the user.
	 *
	 * @param user the user to set
	 */
	public void setUser(GroupUserResource user) {
		this.user = user;
	}

	/**
	 * Retrieve the operation.
	 *
	 * @return the operation
	 */
	public int getOperation() {
		return operation;
	}

	/**
	 * Modify the operation.
	 *
	 * @param operation the operation to set
	 */
	public void setOperation(int operation) {
		this.operation = operation;
	}

	/**
	 * Retrieve the file.
	 *
	 * @return the file
	 */
	public FileResource getFile() {
		return file;
	}

	/**
	 * Modify the file.
	 *
	 * @param file the file to set
	 */
	public void setFile(FileResource file) {
		this.file = file;
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
	 * checks whether the clipboard item is a file or folder
	 */
	public boolean isFileOrFolder(){
		if(file !=null || files != null || folderResource != null)
			return true;
		return false;
	}

	/**
	 * checks whether the clipboard item is a file (or files)
	 */
	public boolean isFile() {
		if(file !=null || files != null)
			return true;
		return false;
	}

	public boolean isUser(){
		if( user!=null  )
			return true;
		return false;
	}


	/**
	 * Retrieve the folderResource.
	 *
	 * @return the folderResource
	 */
	public FolderResource getFolderResource() {
		return folderResource;
	}


	/**
	 * Modify the folderResource.
	 *
	 * @param folderResource the folderResource to set
	 */
	public void setFolderResource(FolderResource folderResource) {
		this.folderResource = folderResource;
	}


}
