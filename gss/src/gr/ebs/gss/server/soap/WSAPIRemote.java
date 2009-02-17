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
package gr.ebs.gss.server.soap;

import gr.ebs.gss.client.domain.FileHeaderDTO;
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.domain.GroupDTO;
import gr.ebs.gss.client.domain.PermissionDTO;
import gr.ebs.gss.client.domain.UserDTO;
import gr.ebs.gss.client.exceptions.DuplicateNameException;
import gr.ebs.gss.client.exceptions.GSSIOException;
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.QuotaExceededException;

import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;
import javax.ejb.Remote;


/**
 * @author kman
 *
 */
@Remote
public interface WSAPIRemote {

	/* USER METHODS */

	/**
	 * Search the system for a user with the specified username.
	 * If no such user is found, the method returns null.
	 *
	 * @param username the username to search for
	 * @return the User object with the specified username
	 */
	public UserDTO getUser(String username);

	/**
	 * Returns the group with the specified ID.
	 *
	 * @param groupId The ID of the Group to be found
	 * @return The Group object
	 * @throws ObjectNotFoundException if the group cannot be found
	 */
	public GroupDTO getGroup(Long groupId) throws ObjectNotFoundException;

	/**
	 * Retrieve the list of groups for a particular user.
	 *
	 * @param userId the ID of the User
	 * @return a List of Groups that belong to the specified User
	 * @throws ObjectNotFoundException if the user was not found
	 */
	public GroupDTO[] getGroups(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of users for the specified group
	 *
	 * @param userId the ID of the User
	 * @param groupId the ID of the requested group
	 * @return List<UserDTO>
	 * @throws ObjectNotFoundException if the user or group was not found, with
	 *             the exception message mentioning the precise problem
	 */
	public UserDTO[] getUsers(Long userId, Long groupId) throws ObjectNotFoundException;

	/**
	 * Creates a new group with the specified owner and name.
	 *
	 * @param userId the ID of the current user
	 * @param name the name of the new group
	 * @throws DuplicateNameException if the new group name already exists
	 * @throws ObjectNotFoundException if the user or group was not found, with
	 *             the exception message mentioning the precise problem
	 */
	public void createGroup(Long userId, String name) throws ObjectNotFoundException, DuplicateNameException;

	/**
	 * Deletes the specified group in the specified user's namespace.
	 *
	 * @param userId the ID of the current user
	 * @param groupId the ID of the group to delete
	 * @throws ObjectNotFoundException if the user or group was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException
	 */
	public void deleteGroup(Long userId, Long groupId) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Adds a user to the specified group
	 *
	 * @param userId the ID of the current user
	 * @param groupId the id of the new group
	 * @param userToAddId the id of the user to add
	 * @throws DuplicateNameException if the user already exists in group
	 * @throws ObjectNotFoundException if the user or group was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException
	 */
	public void addUserToGroup(Long userId, Long groupId, Long userToAddId) throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException;

	/**
	 * Remove a user member from a group
	 *
	 * @param userId the ID of the User owning the group
	 * @param groupId the ID of the requested group
	 * @param memberId the ID of the member to be removed
	 *
	 * @throws ObjectNotFoundException if the user or group was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException
	 */
	public void removeMemberFromGroup(Long userId, Long groupId, Long memberId) throws ObjectNotFoundException, InsufficientPermissionsException;


	/* FOLDER METHODS */
	/**
	 * Retrieves the root folder for the specified user. The caller must ensure
	 * that the userId exists.
	 *
	 * @param userId
	 * @return Folder
	 * @throws ObjectNotFoundException if no Folder or user was found
	 */
	public FolderDTO getRootFolder(Long userId) throws ObjectNotFoundException;

	/**
	 * Retrieve the folder with the specified ID.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the folder to retrieve
	 * @return the folder found
	 * @throws ObjectNotFoundException if the folder or the user was not found
	 * @throws InsufficientPermissionsException
	 */
	public FolderDTO getFolder(Long userId, Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Creates a new folder with the specified owner, parent folder and name.
	 * New folder has the same permissions as its parent
	 *
	 * @param userId
	 * @param parentId
	 * @param name
	 * @throws DuplicateNameException if the specified name already exists in
	 *             the parent folder, as either a folder or file
	 * @throws ObjectNotFoundException if the user or parent folder was not
	 *             found, with the exception message mentioning the precise
	 *             problem
	 * @throws InsufficientPermissionsException
	 */
	public void createFolder(Long userId, Long parentId, String name) throws DuplicateNameException, ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Deletes the specified folder if the specified user has the appropriate
	 * permission
	 *
	 * @param userId
	 * @param folderId
	 * @throws InsufficientPermissionsException if the user does not have the
	 *             appropriate privileges
	 * @throws ObjectNotFoundException if the user or folder was not found, with
	 *             the exception message mentioning the precise problem
	 */
	public void deleteFolder(Long userId, Long folderId) throws InsufficientPermissionsException, ObjectNotFoundException;

	/**
	 * Retrieve the subfolders of the specified folder.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the folder to retrieve
	 * @return the list of subfolders found
	 * @throws ObjectNotFoundException if the folder or user was not found
	 * @throws InsufficientPermissionsException
	 */
	public FolderDTO[] getSubfolders(Long userId, Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Modifies the specified folder if the specified user has the appropriate
	 * permission.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the folder to retrieve
	 * @param folderName
	 * @throws InsufficientPermissionsException if the user does not have the
	 *             appropriate privileges
	 * @throws ObjectNotFoundException if the user or folder was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws DuplicateNameException if the specified name already exists in
	 *             the parent folder, as either a folder or file
	 */
	public void modifyFolder(Long userId, Long folderId, String folderName) throws InsufficientPermissionsException, ObjectNotFoundException, DuplicateNameException;

	/**
	 * Copy the provided folder to the specified destination.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the IF of the provided folder
	 * @param destId the ID of the destination folder
	 * @param destName the name of the new folder
	 * @throws ObjectNotFoundException if the user, folder or destination was not
	 * 			found, with	the exception message mentioning the precise problem
	 * @throws DuplicateNameException if the specified name already exists in
	 *          the destination folder, as either a folder or file
	 * @throws InsufficientPermissionsException InsufficientPermissionsException if the user does not have the
	 *          appropriate privileges
	 * @throws GSSIOException
	 * @throws QuotaExceededException
	 */
	public void copyFolder(Long userId, Long folderId, Long destId, String destName) throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, GSSIOException, QuotaExceededException;



	/**
	 * Move the provided folder and all its subfolders and files to the specified destination.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the IF of the provided folder
	 * @param destId the ID of the destination folder
	 * @param destName the name of the new folder
	 * @throws ObjectNotFoundException if the user, folder or destination was not
	 * 			found, with	the exception message mentioning the precise problem
	 * @throws DuplicateNameException if the specified name already exists in
	 *          the destination folder, as either a folder or file
	 * @throws InsufficientPermissionsException InsufficientPermissionsException if the user does not have the
	 *          appropriate privileges
	 * @throws GSSIOException
	 * @throws QuotaExceededException
	 */
	public void moveFolder(Long userId, Long folderId, Long destId, String destName) throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, GSSIOException, QuotaExceededException;


	/**
	 * Retrieve folder user and group permissions
	 *
	 * @param userId the ID of the user whose token should be updated
	 * @param folderId the ID of the folder
	 * @return the Set of permissions from requested folder
	 * @throws ObjectNotFoundException if the user or folder could not be found
	 * @throws InsufficientPermissionsException
	 */
	public Set<PermissionDTO> getFolderPermissions(Long userId, Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException;


	/**
	 * update folder permissions
	 * @param userId
	 * @param folderId
	 * @param permissions
	 * @throws ObjectNotFoundException
	 * @throws InsufficientPermissionsException
	 */
	public void setFolderPermissions(Long userId, Long folderId, Set<PermissionDTO> permissions) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Returns a list of all deleted root folders of a user.
	 *
	 * @param userId the ID of the User
	 * @return the list of deleted file header objects
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public FolderDTO[] getDeletedRootFolders(Long userId) throws ObjectNotFoundException;


	/* FILE METHODS */
	/**
	 * Creates a new file with the specified owner, parent folder and name. The
	 * new file has the same permissions as its parent folder. The file contents
	 * are read from the input stream to a new File object.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the parent folder
	 * @param name the name of the new file
	 * @param mimeType the MIME type of the file
	 * @param stream the input stream with the file contents
	 * @throws DuplicateNameException if the specified name already exists in
	 *             the parent folder, as either a folder or file
	 * @throws ObjectNotFoundException if the user or parent folder was not
	 *             found, with the exception message mentioning the precise
	 *             problem
	 * @throws GSSIOException if there was an error while storing the file contents
	 * @throws InsufficientPermissionsException
	 * @throws QuotaExceededException
	 */
	public void createFile(Long userId, Long folderId, String name, String mimeType, DataHandler stream) throws DuplicateNameException, ObjectNotFoundException, GSSIOException, InsufficientPermissionsException, QuotaExceededException;

	/**
	 * Deletes the specified file in the specified user's namespace.
	 *
	 * @param userId the ID of the current user
	 * @param fileId the ID of the file to delete
	 * @throws ObjectNotFoundException if the user or file was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException if the user does not have the
	 *             appropriate privileges
	 */
	public void deleteFile(Long userId, Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Creates a new tag for the specified user and file.
	 *
	 * @param userId the creator of the tag
	 * @param fileHeaderId the file that is tagged
	 * @param tag the tag
	 * @throws ObjectNotFoundException if the user or the file was not found
	 */
	public void createTag(Long userId, Long fileHeaderId, String tag) throws ObjectNotFoundException;

	/**
	 * Returns all tags defined by the specified user
	 *
	 * @param userId
	 * @return Set<String>
	 * @throws ObjectNotFoundException if the user was null
	 */
	public String[] getUserTags(final Long userId) throws ObjectNotFoundException;

	/**
	 * Updates name and tags for the specified file
	 *
	 * @param userId
	 * @param fileId
	 * @param name
	 * @param tagSet a String that contains tags separated by comma
	 * @throws ObjectNotFoundException
	 * @throws InsufficientPermissionsException
	 */
	public void updateFile(Long userId, Long fileId, String name, String tagSet) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Retrieve the contents of the current body for the file
	 * with the specified FileHeader ID. The file contents
	 * are provided as an InputStream from which the caller can
	 * retrieve the raw bytes.
	 *
	 * @param userId the ID of the current user
	 * @param fileId the ID of the file to retrieve
	 * @return an InputStream from the current file body contents
	 * @throws ObjectNotFoundException if the file or the user was not found
	 * @throws InsufficientPermissionsException  if the user does not have the
	 *             appropriate privileges
	 */
	public DataHandler getFileContents(Long userId, Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Retrieve the file with the specified ID.
	 *
	 * @param userId the ID of the current user
	 * @param fileId the ID of the file to retrieve
	 * @return the file found
	 * @throws ObjectNotFoundException if the file or the user was not found, with
	 * 			the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException
	 */
	public FileHeaderDTO getFile(Long userId, Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException;



	/**
	 * Create a new FileBody with the supplied contents and make it the current body
	 * of the file.
	 *
	 * @param userId the ID of the current user
	 * @param fileId the ID of the file header object
	 * @param mimeType the content type of the file
	 * @param resourceInputStream a stream of the file contents
	 * @throws ObjectNotFoundException if the user or file was not found, with
	 * 			the exception message mentioning the precise problem
	 * @throws GSSIOException if there was an error while storing the file contents
	 * @throws InsufficientPermissionsException
	 * @throws QuotaExceededException
	 */
	public void updateFileContents(Long userId, Long fileId, String mimeType, DataHandler resourceInputStream) throws ObjectNotFoundException, GSSIOException, InsufficientPermissionsException, QuotaExceededException;

	/**
	 * Copy the provided file to the specified destination.
	 *
	 * @param userId the ID of the current user
	 * @param fileId the IF of the provided file
	 * @param destId the ID of the destination folder
	 * @param destName the name of the new file
	 * @throws ObjectNotFoundException if the user, file or destination was not
	 * 			found, with	the exception message mentioning the precise problem
	 * @throws GSSIOException if there was an error while accessing the file contents
	 * @throws DuplicateNameException if the specified name already exists in
	 *          the destination folder, as either a folder or file
	 * @throws InsufficientPermissionsException
	 * @throws QuotaExceededException
	 */
	public void copyFile(Long userId, Long fileId, Long destId, String destName) throws ObjectNotFoundException, DuplicateNameException, GSSIOException, InsufficientPermissionsException, QuotaExceededException;


	/**
	 * move the provided file to the specified destination.
	 *
	 * @param userId the ID of the current user
	 * @param fileId the IF of the provided file
	 * @param destId the ID of the destination folder
	 * @param destName the name of the new file
	 * @throws InsufficientPermissionsException
	 * @throws ObjectNotFoundException if the user, file or destination was not
	 * 			found, with	the exception message mentioning the precise problem
	 * @throws GSSIOException if there was an error while accessing the file contents
	 * @throws DuplicateNameException if the specified name already exists in
	 *          the destination folder, as either a folder or file
	 * @throws QuotaExceededException
	 */
	public void moveFile(Long userId, Long fileId, Long destId, String destName) throws InsufficientPermissionsException, ObjectNotFoundException, DuplicateNameException, GSSIOException, QuotaExceededException;

	/**
	 * Returns a list of all deleted files of a user.
	 *
	 * @param userId the ID of the User
	 * @return the list of deleted file header objects
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public FileHeaderDTO[] getDeletedFiles(Long userId) throws ObjectNotFoundException;




	/**
	 * Retrieve file user and group permissions
	 *
	 * @param userId the ID of the user whose token should be updated
	 * @param fileId the ID of the folder
	 * @return the Set of permissions from requested folder
	 * @throws ObjectNotFoundException if the user or folder could not be found
	 * @throws InsufficientPermissionsException
	 */
	public Set<PermissionDTO> getFilePermissions(Long userId, Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException;


	/**
	 * update file permissions
	 * @param userId
	 * @param fileId
	 * @param permissions
	 * @throws ObjectNotFoundException
	 * @throws InsufficientPermissionsException
	 */
	public void setFilePermissions(Long userId, Long fileId, boolean readForAll, Set<PermissionDTO> permissions) throws ObjectNotFoundException, InsufficientPermissionsException;



	/**
	 * Returns a list of files contained in the folder specified by its id.
	 *
	 * @param userId the ID of the User
	 * @param folderId the ID of the folder containing the files
	 * @return the list of file header objects
	 * @throws ObjectNotFoundException if the user or the folder cannot be found
	 * @throws InsufficientPermissionsException
	 */
	public FileHeaderDTO[] getFiles(Long userId, Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException;

	/* TRASH METHODS */

	/**
	 * Marks  the specified file as deleted in the specified user's namespace.
	 *
	 * @param userId the ID of the current user
	 * @param fileId the ID of the file to delete
	 * @throws ObjectNotFoundException if the user or file was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException if the user does not have the
	 *             appropriate privileges
	 */
	public void moveFileToTrash(Long userId, Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Marks  the specified deleted file as undeleted in the specified user's namespace.
	 *
	 * @param userId the ID of the current user
	 * @param fileId the ID of the file to undelete
	 * @throws ObjectNotFoundException if the user or file was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException
	 *
	 */
	public void restoreFileFromTrash(Long userId, Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Marks  the specified folder as deleted in the specified user's namespace.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the folder to delete
	 * @throws ObjectNotFoundException if the user or file was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException if the user does not have the
	 *             appropriate privileges
	 */
	public void moveFolderToTrash(Long userId, Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Marks  the specified deleted folder as undeleted in the specified user's namespace.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the folder to undelete
	 * @throws ObjectNotFoundException if the user or file was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException
	 *
	 */
	public void restoreFolderFromTrash(Long userId, Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Empty Trash by deleting all marked as deleted files and folders
	 * @param userId
	 * @throws ObjectNotFoundException if something goes wrong in the delete process
	 * @throws InsufficientPermissionsException  if something goes wrong in the delete process
	 */
	public void emptyTrash(Long userId) throws ObjectNotFoundException, InsufficientPermissionsException;

	/*SHARING METHODS */

	/**
	 * Returns a list of All Shared root folders of a user.
	 *
	 * @param userId the ID of the User
	 * 	 * @return the list of shared root folders
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FolderDTO> getSharedRootFolders(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of All Shared  files of a user.
	 *
	 * @param userId the ID of the User
	 * 	 * @return the list of shared files
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FileHeaderDTO> getSharedFiles(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of All Shared root folders of a user that calling user has at least read permissions.
	 *
	 * @param ownerId the ID of the User
	 * @return the list of shared root folders
	 * @param callingUserId
	 *
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FolderDTO> getSharedRootFoldersOfUser(Long ownerId, Long callingUserId) throws ObjectNotFoundException;

	/**
	 * Returns a list of All Shared  files of a user that calling user has at least read permissions..
	 *
	 * @param ownerId the ID of the User
	 * @return the list of shared files
	 * @param callingUserId
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FileHeaderDTO> getSharedFilesOfUser(Long ownerId, Long callingUserId) throws ObjectNotFoundException;



	/**
	 * Retrieves the list of users sharing files to user identified by user id
	 * @param userId
	 * @return the List of users sharing files to user
	 * @throws ObjectNotFoundException
	 */
	public List<UserDTO> getUsersSharingFoldersForUser(Long userId) throws ObjectNotFoundException;


	/**
	 * Retrieve the subfolders of the specified folder that are shared to others.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the folder to retrieve
	 * @return the list of subfolders found
	 * @throws ObjectNotFoundException if the folder or user was not found
	 */
	public List<FolderDTO> getSharedSubfolders(Long userId, Long folderId) throws ObjectNotFoundException;





}
