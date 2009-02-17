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

import gr.ebs.gss.client.domain.FileHeaderDTO;
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.domain.UserDTO;

import java.io.Serializable;
import java.util.List;


/**
 * @author kman
 *
 */
public class ClipboardItem implements Serializable{
	private int operation;
	private FileHeaderDTO file;
	private FolderDTO folder;
	private UserDTO user;
	private List<FileHeaderDTO> files;

	public ClipboardItem(){}

	public ClipboardItem(int operation, List<FileHeaderDTO> files){
		this.operation=operation;
		this.files = files;
	}

	public ClipboardItem(int operation, FileHeaderDTO file){
		this.operation=operation;
		this.file = file;
	}

	public ClipboardItem(int operation, FolderDTO folder){
		this.operation=operation;
		this.folder = folder;
	}
	public ClipboardItem(int operation, UserDTO user){
		this.operation=operation;
		this.user = user;
	}

	public ClipboardItem(UserDTO user){
		operation=Clipboard.COPY;
		this.user = user;
	}

	public ClipboardItem(List<FileHeaderDTO> files){
		operation=Clipboard.COPY;
		this.files = files;
	}

	public ClipboardItem(FileHeaderDTO file){
		operation=Clipboard.COPY;
		this.file = file;
	}

	public ClipboardItem(FolderDTO folder){
		operation=Clipboard.COPY;
		this.folder = folder;
	}




	/**
	 * Retrieve the user.
	 *
	 * @return the user
	 */
	public UserDTO getUser() {
		return user;
	}


	/**
	 * Modify the user.
	 *
	 * @param user the user to set
	 */
	public void setUser(UserDTO user) {
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
	public FileHeaderDTO getFile() {
		return file;
	}

	/**
	 * Modify the file.
	 *
	 * @param file the file to set
	 */
	public void setFile(FileHeaderDTO file) {
		this.file = file;
	}

	/**
	 * Retrieve the folder.
	 *
	 * @return the folder
	 */
	public FolderDTO getFolder() {
		return folder;
	}

	/**
	 * Modify the folder.
	 *
	 * @param folder the folder to set
	 */
	public void setFolder(FolderDTO folder) {
		this.folder = folder;
	}


	/**
	 * Retrieve the files.
	 *
	 * @return the files
	 */
	public List<FileHeaderDTO> getFiles() {
		return files;
	}

	/**
	 * checks whether the clipboard item is a file or folder
	 * @return
	 */
	public boolean isFileOrFolder(){
		if( folder!=null || file !=null || files != null)
			return true;
		return false;
	}


	public boolean isUser(){
		if( user!=null  )
			return true;
		return false;
	}

}
