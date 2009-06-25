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
package gr.ebs.gss.server.ejb;

import gr.ebs.gss.client.exceptions.DuplicateNameException;
import gr.ebs.gss.client.exceptions.GSSIOException;
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.QuotaExceededException;
import gr.ebs.gss.server.domain.FileUploadStatus;
import gr.ebs.gss.server.domain.Nonce;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.domain.dto.FileBodyDTO;
import gr.ebs.gss.server.domain.dto.FileHeaderDTO;
import gr.ebs.gss.server.domain.dto.FolderDTO;
import gr.ebs.gss.server.domain.dto.GroupDTO;
import gr.ebs.gss.server.domain.dto.PermissionDTO;
import gr.ebs.gss.server.domain.dto.StatsDTO;
import gr.ebs.gss.server.domain.dto.UserDTO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * The External API for GSS clients.
 *
 * @author past
 */
@Local
public interface ExternalAPI {

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
	 * @throws InsufficientPermissionsException if ther user does not have read permissions for folder
	 */
	public FolderDTO getFolder(Long userId, Long folderId) throws ObjectNotFoundException,
			InsufficientPermissionsException;

	/**
	 * Returns the user with the specified ID.
	 *
	 * @param userId The ID of the User to be found
	 * @return The User object
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public User getUser(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns the user with the specified ID.
	 *
	 * @param userId The ID of the User to be found
	 * @return The User object
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public UserDTO getUserDTO(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns the group with the specified ID.
	 *
	 * @param groupId The ID of the Group to be found
	 * @return The Group object
	 * @throws ObjectNotFoundException if the group cannot be found
	 */
	public GroupDTO getGroup(Long groupId) throws ObjectNotFoundException;

	/**
	 * Returns the group with the specified name that belongs to the
	 * specified user.
	 *
	 * @param userId the ID of the user
	 * @param name the name of the group
	 * @return The Group object
	 * @throws ObjectNotFoundException if the group cannot be found
	 */
	public GroupDTO getGroup(Long userId, String name) throws ObjectNotFoundException;

	/**
	 * Retrieve the list of groups for a particular user.
	 *
	 * @param userId the ID of the User
	 * @return a List of Groups that belong to the specified User
	 * @throws ObjectNotFoundException if the user was not found
	 */
	public List<GroupDTO> getGroups(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of files contained in the folder specified by its id.
	 *
	 * @param userId the ID of the User
	 * @param folderId the ID of the folder containing the files
	 * @param ignoreDeleted
	 * @return the list of file header objects
	 * @throws ObjectNotFoundException if the user or the folder cannot be found
	 * @throws InsufficientPermissionsException
	 */
	public List<FileHeaderDTO> getFiles(Long userId, Long folderId, boolean ignoreDeleted) throws ObjectNotFoundException,
			InsufficientPermissionsException;

	/**
	 * Returns a list of users for the specified group
	 *
	 * @param userId the ID of the User
	 * @param groupId the ID of the requested group
	 * @return List<UserDTO>
	 * @throws ObjectNotFoundException if the user or group was not found, with
	 *             the exception message mentioning the precise problem
	 */
	public List<UserDTO> getUsers(Long userId, Long groupId) throws ObjectNotFoundException;

	/**
	 * Returns a list of users matching the specified username
	 *
	 * @param username the username of the User
	 * @return List<UserDTO>
	 */
	public List<UserDTO> getUsersByUserNameLike(String username);

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
	public void createFolder(Long userId, Long parentId, String name) throws DuplicateNameException,
			ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Deletes the specified folder, if the specified user has the appropriate
	 * permission.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the folder to delete
	 * @throws InsufficientPermissionsException if the user does not have the
	 *             appropriate privileges
	 * @throws ObjectNotFoundException if the user or folder was not found, with
	 *             the exception message mentioning the precise problem
	 */
	public void deleteFolder(Long userId, Long folderId)
			throws InsufficientPermissionsException, ObjectNotFoundException;

	/**
	 * Retrieve the subfolders of the specified folder.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the folder to retrieve
	 * @return the list of subfolders found
	 * @throws ObjectNotFoundException if the folder or user was not found
	 * @throws InsufficientPermissionsException
	 */
	public List<FolderDTO> getSubfolders(Long userId, Long folderId)
			throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Retrieve the folder with the specified ID with subfolders.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the folder to retrieve
	 * @return the folder found
	 * @throws ObjectNotFoundException if the folder or the user was not found
	 * @throws InsufficientPermissionsException if ther user does not have read permissions for folder
	 */
	public FolderDTO getFolderWithSubfolders(Long userId, Long folderId)
			throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Retrieve the folder with the specified ID with subfolders.
	 *
	 * @param userId the ID of the current user
	 * @param callingUserId the ID of the user requesting this operation
	 * @param folderId the ID of the folder to retrieve
	 * @return the folder found
	 * @throws ObjectNotFoundException if the folder or the user was not found
	 * @throws InsufficientPermissionsException if ther user does not have read permissions for folder
	 */
	public FolderDTO getFolderWithSubfolders(Long userId, Long callingUserId, Long folderId)
			throws ObjectNotFoundException, InsufficientPermissionsException;
	/**
	 * Retrieve the subfolders of the specified folder that are shared to others.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the folder to retrieve
	 * @return the list of subfolders found
	 * @throws ObjectNotFoundException if the folder or user was not found
	 */
	public List<FolderDTO> getSharedSubfolders(Long userId, Long folderId) throws ObjectNotFoundException;

	/**
	 * Retrieve the subfolders of the specified folder that are shared to others.
	 *
	 * @param userId the ID of the current user
	 * @param callingUserId the id of the user requesting this operation
	 * @param folderId the ID of the folder to retrieve
	 * @return the list of subfolders found
	 * @throws ObjectNotFoundException if the folder or user was not found
	 */
	public List<FolderDTO> getSharedSubfolders(Long userId, Long callingUserId, Long folderId) throws ObjectNotFoundException;
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
	public void modifyFolder(Long userId, Long folderId, String folderName)
			throws InsufficientPermissionsException, ObjectNotFoundException, DuplicateNameException;

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
	public void addUserToGroup(Long userId, Long groupId, Long userToAddId)
			throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException;

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
	 * Creates a new file with the specified owner, parent folder and name. The
	 * new file has the same permissions as its parent folder. The file contents
	 * are read from the input stream to a new File object.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the parent folder
	 * @param name the name of the new file
	 * @param mimeType the MIME type of the file
	 * @param stream the input stream with the file contents
	 * @return The FileHeaderDTO created
	 * @throws DuplicateNameException if the specified name already exists in
	 *             the parent folder, as either a folder or file
	 * @throws ObjectNotFoundException if the user or parent folder was not
	 *             found, with the exception message mentioning the precise
	 *             problem
	 * @throws GSSIOException if there was an error while storing the file contents
	 * @throws InsufficientPermissionsException
	 * @throws QuotaExceededException
	 */
	public FileHeaderDTO createFile(Long userId, Long folderId, String name, String mimeType,
				InputStream stream) throws DuplicateNameException, ObjectNotFoundException,
				GSSIOException, InsufficientPermissionsException, QuotaExceededException;

	/**
	 * Deletes the specified file, provided the specified user has
	 * the necessary permissions.
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
	 * Deletes the specified file in the specified user's namespace.
	 *
	 * @param userId the ID of the current user
	 * @param fileIds the IDs of the files to delete
	 * @throws ObjectNotFoundException if the user or file was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException if the user does not have the
	 *             appropriate privileges
	 */
	public void deleteFiles(Long userId, List<Long> fileIds)
			throws ObjectNotFoundException, InsufficientPermissionsException;

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
	public Set<String> getUserTags(final Long userId) throws ObjectNotFoundException;

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
	public void updateFile(Long userId, Long fileId, String name, String tagSet)
			throws ObjectNotFoundException, InsufficientPermissionsException;

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
	public InputStream getFileContents(Long userId, Long fileId)
			throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Retrieve the contents of the  body identified by bodyId for the file
	 * with the specified FileHeader ID. The file contents
	 * are provided as an InputStream from which the caller can
	 * retrieve the raw bytes.
	 *
	 * @param userId the ID of the current user
	 * @param fileId the ID of the file to retrieve
	 * @param bodyId the body ID
	 * @return an InputStream from the current file body contents
	 * @throws ObjectNotFoundException if the file or the user was not found
	 * @throws InsufficientPermissionsException  if the user does not have the
	 *             appropriate privileges
	 */
	public InputStream getFileContents(Long userId, Long fileId, Long bodyId)
			throws ObjectNotFoundException, InsufficientPermissionsException;

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
	public FileHeaderDTO getFile(Long userId, Long fileId) throws ObjectNotFoundException,
			InsufficientPermissionsException;

	/**
	 * Retrieve the filebody with the specified ID.
	 *
	 * @param userId the ID of the current user
	 * @param fileId the ID of the file to retrieve
	 * @param bodyId the ID of the body
	 * @return the file found
	 * @throws ObjectNotFoundException if the file or the user was not found, with
	 * 			the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException
	 */
	public FileBodyDTO getFileBody(Long userId, Long fileId, Long bodyId)
			throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Get the resource (file or folder) at the specified path in
	 * the specified user's namespace. The returned object will be of type
	 * FileHeaderDTO or FolderDTO.<p><strong>Note:</strong> this method does not
	 * receive the current user as a parameter, therefore it is unable to perform
	 * the necessary permission checks and should <strong>NOT</strong> be directly
	 * exposed to remote clients. It is the caller's responsibility to verify that
	 * the calling user has the required privileges for performing any subsequent
	 * action on the resource through one of the other ExternalAPI methods.
	 *
	 * @param ownerId the ID of the user owning the namespace
	 * @param path the absolute path in the user's namespace
	 * @param ignoreDeleted if true, resources that have been moved to the trash
	 * 			will be ignored
	 * @throws ObjectNotFoundException if the user or resource was not found, with
	 * 			the exception message mentioning the precise problem
	 * @return the resource found
	 */
	public Object getResourceAtPath(Long ownerId, String path, boolean ignoreDeleted)
			throws ObjectNotFoundException;

	/**
	 * Copy the provided file to the specified destination.
	 *
	 * @param userId the ID of the current user
	 * @param fileId the IF of the provided file
	 * @param dest the path of the new resource
	 * @throws ObjectNotFoundException if the user, file or destination was not
	 * 			found, with	the exception message mentioning the precise problem
	 * @throws DuplicateNameException if the specified name already exists in
	 *          the destination folder, as either a folder or file
	 * @throws GSSIOException if there was an error while accessing the file contents
	 * @throws InsufficientPermissionsException
	 * @throws QuotaExceededException
	 */
	public void copyFile(Long userId, Long fileId, String dest) throws ObjectNotFoundException,
			DuplicateNameException, GSSIOException, InsufficientPermissionsException, QuotaExceededException;

	/**
	 * Copy the provided file to the specified destination.
	 *
	 * @param userId the ID of the current user
	 * @param ownerId the ID of the owner of the destination namespace
	 * @param fileId the IF of the provided file
	 * @param dest the path of the new resource
	 * @throws ObjectNotFoundException if the user, file or destination was not
	 * 			found, with	the exception message mentioning the precise problem
	 * @throws DuplicateNameException if the specified name already exists in
	 *          the destination folder, as either a folder or file
	 * @throws GSSIOException if there was an error while accessing the file contents
	 * @throws InsufficientPermissionsException
	 * @throws QuotaExceededException
	 */
	public void copyFileToPath(Long userId, Long ownerId, Long fileId, String dest) throws ObjectNotFoundException,
			DuplicateNameException, GSSIOException, InsufficientPermissionsException, QuotaExceededException;

	/**
	 * Copy the provided file to the specified destination.
	 *
	 * @param userId the ID of the current user
	 * @param fileIds the IDs of the provided files
	 * @param destId the ID of the destination folder
	 * @throws ObjectNotFoundException if the user, file or destination was not
	 * 			found, with	the exception message mentioning the precise problem
	 * @throws GSSIOException if there was an error while accessing the file contents
	 * @throws DuplicateNameException if the specified name already exists in
	 *          the destination folder, as either a folder or file
	 * @throws InsufficientPermissionsException
	 * @throws QuotaExceededException
	 */
	public void copyFiles(Long userId, List<Long> fileIds, Long destId)
			throws ObjectNotFoundException, DuplicateNameException, GSSIOException,
			InsufficientPermissionsException, QuotaExceededException;

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
	public void copyFile(Long userId, Long fileId, Long destId, String destName)
			throws ObjectNotFoundException, DuplicateNameException, GSSIOException,
			InsufficientPermissionsException, QuotaExceededException;

	/**
	 * Copy the provided folder to the specified destination.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the IF of the provided folder
	 * @param dest the path of the new folder
	 * @throws ObjectNotFoundException if the user, folder or destination was not
	 * 			found, with	the exception message mentioning the precise problem
	 * @throws DuplicateNameException if the specified name already exists in
	 *          the destination folder, as either a folder or file
	 * @throws InsufficientPermissionsException InsufficientPermissionsException if the user does not have the
	 *          appropriate privileges
	 */
	public void copyFolder(Long userId, Long folderId, String dest) throws ObjectNotFoundException,
			DuplicateNameException, InsufficientPermissionsException;

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
	 * @throws InsufficientPermissionsException InsufficientPermissionsException
	 * 			if the user does not have the appropriate privileges
	 */
	public void copyFolder(Long userId, Long folderId, Long destId, String destName)
			throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException;

	/**
	 * Copy the provided folder and all its subfolders and files to the specified destination.
	 *
	 * @param userId the ID of the current user
	 * @param ownerId the ID of the owner of the destination namespace
	 * @param folderId the IF of the provided folder
	 * @param dest the path of the new folder
	 * @throws ObjectNotFoundException if the user, folder or destination was not
	 * 			found, with	the exception message mentioning the precise problem
	 * @throws DuplicateNameException if the specified name already exists in
	 *          the destination folder, as either a folder or file
	 * @throws InsufficientPermissionsException InsufficientPermissionsException if the user does not have the
	 *          appropriate privileges
	 * @throws QuotaExceededException if the user quota limit would be exceeded
	 * @throws GSSIOException if there was an error while accessing the file contents
	 */
	public void copyFolderStructureToPath(Long userId, Long ownerId, Long folderId, String dest) throws ObjectNotFoundException,
			DuplicateNameException, InsufficientPermissionsException, GSSIOException, QuotaExceededException;

	/**
	 * Copy the provided folder and all its subfolders and files to the specified destination.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the IF of the provided folder
	 * @param destId the ID of the destination folder
	 * @param destName the name of the new folder
	 * @throws ObjectNotFoundException if the user, folder or destination was not
	 * 			found, with	the exception message mentioning the precise problem
	 * @throws DuplicateNameException if the specified name already exists in
	 *          the destination folder, as either a folder or file
	 * @throws InsufficientPermissionsException InsufficientPermissionsException
	 * 			if the user does not have the appropriate privileges
	 * @throws GSSIOException if there was an error while accessing the file contents
	 * @throws QuotaExceededException if the user quota limit would be exceeded
	 */
	public void copyFolderStructure(Long userId, Long folderId, Long destId, String destName)
			throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException,
			GSSIOException, QuotaExceededException;

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
	public void moveFileToTrash(Long userId, Long fileId) throws ObjectNotFoundException,
			InsufficientPermissionsException;

	/**
	 * Marks  the specified deleted file as undeleted in the specified user's namespace.
	 *
	 * @param userId the ID of the current user
	 * @param fileId the ID of the file to undelete
	 * @throws ObjectNotFoundException if the user or file was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException if the user does not have the
	 *             appropriate privileges
	 *
	 */
	public void removeFileFromTrash(Long userId, Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Marks  the specified files as deleted in the specified user's namespace.
	 *
	 * @param userId the ID of the current user
	 * @param fileIds the IDs of the file to delete
	 * @throws ObjectNotFoundException if the user or file was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException if the user does not have the
	 *             appropriate privileges
	 */
	public void moveFilesToTrash(Long userId, List<Long> fileIds)
			throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Marks  the specified deleted files as undeleted in the specified user's namespace.
	 *
	 * @param userId the ID of the current user
	 * @param fileIds the IDs of the file to undelete
	 * @throws ObjectNotFoundException if the user or file was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException if the user does not have the
	 *             appropriate privileges
	 */
	public void removeFilesFromTrash(Long userId, List<Long> fileIds) throws ObjectNotFoundException, InsufficientPermissionsException;

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
	public void moveFolderToTrash(Long userId, Long folderId)
			throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Marks  the specified deleted folder as undeleted in the specified user's namespace.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the folder to undelete
	 * @throws ObjectNotFoundException if the user or file was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException if the user does not have the
	 *          appropriate privileges
	 *
	 */
	public void removeFolderFromTrash(Long userId, Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException;

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
	 * @throws InsufficientPermissionsException if the user does not have the
	 *          appropriate privileges
	 * @throws GSSIOException
	 * @throws QuotaExceededException
	 */
	public void moveFolder(Long userId, Long folderId, Long destId, String destName)
			throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException,
			GSSIOException, QuotaExceededException;

	/**
	 * Move the provided folder and all its subfolders and files to the specified destination.
	 *
	 * @param userId the ID of the current user
	 * @param ownerId the owner of the destination namespace
	 * @param folderId the IF of the provided folder
	 * @param dest the path of the new folder
	 * @throws ObjectNotFoundException if the user, folder or destination was not
	 * 			found, with	the exception message mentioning the precise problem
	 * @throws DuplicateNameException if the specified name already exists in
	 *          the destination folder, as either a folder or file
	 * @throws InsufficientPermissionsException InsufficientPermissionsException if the user does not have the
	 *          appropriate privileges
	 * @throws GSSIOException
	 * @throws QuotaExceededException
	 */
	public void moveFolderToPath(Long userId, Long ownerId, Long folderId, String dest) throws ObjectNotFoundException,
			DuplicateNameException, InsufficientPermissionsException, GSSIOException, QuotaExceededException;

	/**
	 * Move the provided file to the specified destination.
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
	public void moveFile(Long userId, Long fileId, Long destId, String destName)
			throws InsufficientPermissionsException, ObjectNotFoundException,
			DuplicateNameException, GSSIOException, QuotaExceededException;

	/**
	 * Move the provided file to the specified destination.
	 *
	 * @param userId the ID of the current user
	 * @param ownerId the owner of the destination namespace
	 * @param fileId the IF of the provided file
	 * @param dest the path of the new file
	 * @throws InsufficientPermissionsException
	 * @throws ObjectNotFoundException if the user, file or destination was not
	 * 			found, with	the exception message mentioning the precise problem
	 * @throws GSSIOException if there was an error while accessing the file contents
	 * @throws DuplicateNameException if the specified name already exists in
	 *          the destination folder, as either a folder or file
	 * @throws QuotaExceededException
	 */
	public void moveFileToPath(Long userId, Long ownerId, Long fileId, String dest) throws ObjectNotFoundException, InsufficientPermissionsException, DuplicateNameException, GSSIOException, QuotaExceededException;

	/**
	 * move the provided file to the specified destination.
	 *
	 * @param userId the ID of the current user
	 * @param fileIds the IDs of the provided files
	 * @param destId the ID of the destination folder
	 * @throws InsufficientPermissionsException
	 * @throws ObjectNotFoundException if the user, file or destination was not
	 * 			found, with	the exception message mentioning the precise problem
	 * @throws GSSIOException if there was an error while accessing the file contents
	 * @throws DuplicateNameException if the specified name already exists in
	 *          the destination folder, as either a folder or file
	 * @throws QuotaExceededException
	 */
	public void moveFiles(Long userId, List<Long> fileIds, Long destId)
			throws InsufficientPermissionsException, ObjectNotFoundException,
			DuplicateNameException, GSSIOException, QuotaExceededException;

	/**
	 * Returns a list of All deleted files of a user.
	 *
	 * @param userId the ID of the User
	 * 	 * @return the list of deleted file header objects
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FileHeaderDTO> getDeletedFiles(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of All deleted root folders of a user.
	 *
	 * @param userId the ID of the User
	 * 	 * @return the list of deleted file header objects
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FolderDTO> getDeletedRootFolders(Long userId) throws ObjectNotFoundException;

	/**
	 * Empty Trash by deleting all marked as deleted files and folders
	 * @param userId
	 * @throws ObjectNotFoundException if something goes wrong in the delete process
	 * @throws InsufficientPermissionsException if something goes wrong in the delete process
	 */
	public void emptyTrash(Long userId) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Restores All Trashed Items by undeleting all marked as deleted files and folders
	 * @param userId
	 * @throws ObjectNotFoundException if something goes wrong in the delete process
	 * @throws InsufficientPermissionsException
	 */
	public void restoreTrash(Long userId) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Search the system for a user with the specified username.
	 * If no such user is found, the method returns null.
	 *
	 * @param username the username to search for
	 * @return the User object with the specified username
	 */
	public User findUser(String username);

	/**
	 * Create a new user with the specified name, username and e-mail address.
	 *
	 * @param username the username of the new user
	 * @param name the full name of the new user
	 * @param mail the e-mail of the new user
	 * @return the newly-created User object
	 * @throws DuplicateNameException if a user with the same username already exists
	 * @throws ObjectNotFoundException if no username was provided
	 */
	public User createUser(String username, String name, String mail)
			throws DuplicateNameException, ObjectNotFoundException;

	/**
	 * Update the user with the specified username with the provoded name
	 * and e-mail address.
	 *
	 * @param username the username of the user
	 * @param name the modified full name of the user
	 * @param mail the modified e-mail of the user
	 * @return the updated User object
	 * @throws ObjectNotFoundException if no username was provided
	 */
	public User updateUser(String username, String name, String mail) throws ObjectNotFoundException;

	/**
	 * Updates the authentication token for the specified user.
	 *
	 * @param userId the ID of the user whose token should be updated
	 * @return the updated user
	 * @throws ObjectNotFoundException if the user could not be found
	 */
	public User updateUserToken(Long userId) throws ObjectNotFoundException;

	/**
	 * Updates the policy acceptance flag for the specified user.
	 *
	 * @param userId the ID of the user whose flag should be updated
	 * @param isAccepted the new value of the flag
	 * @return the updated user
	 * @throws ObjectNotFoundException if the user could not be found
	 */
	public User updateUserPolicyAcceptance(Long userId, boolean isAccepted) throws ObjectNotFoundException;

	/**
	 * Invalidates the authentication token for the specified user.
	 *
	 * @param userId the ID of the user whose token should be updated
	 * @throws ObjectNotFoundException if the user could not be found
	 */
	public void invalidateUserToken(Long userId) throws ObjectNotFoundException;

	/**
	 * Retrieve folder user and group permissions
	 *
	 * @param userId the ID of the user whose token should be updated
	 * @param folderId the ID of the folder
	 * @return the Set of permissions from requested folder
	 * @throws ObjectNotFoundException if the user or folder could not be found
	 * @throws InsufficientPermissionsException
	 */
	public Set<PermissionDTO> getFolderPermissions(Long userId, Long folderId)
			throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * update folder permissions
	 * @param userId
	 * @param folderId
	 * @param permissions
	 * @throws ObjectNotFoundException
	 * @throws InsufficientPermissionsException
	 */
	public void setFolderPermissions(Long userId, Long folderId, Set<PermissionDTO> permissions)
			throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Retrieve file user and group permissions
	 *
	 * @param userId the ID of the user whose token should be updated
	 * @param fileId the ID of the folder
	 * @return the Set of permissions from requested folder
	 * @throws ObjectNotFoundException if the user or folder could not be found
	 * @throws InsufficientPermissionsException
	 */
	public Set<PermissionDTO> getFilePermissions(Long userId, Long fileId)
			throws ObjectNotFoundException, InsufficientPermissionsException;


	/**
	 * update file permissions
	 * @param userId
	 * @param fileId
	 * @param readForAll
	 * @param permissions
	 * @throws ObjectNotFoundException
	 * @throws InsufficientPermissionsException
	 */
	public void setFilePermissions(Long userId, Long fileId, Boolean readForAll,
				Set<PermissionDTO> permissions)
			throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Returns a list of all shared root folders of a user.
	 *
	 * @param userId the ID of the User
	 * @return the list of shared root folders
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FolderDTO> getSharedRootFolders(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of all shared files of a user that are not
	 * inside other shared folders.
	 *
	 * @param userId the ID of the User
	 * @return the list of shared files
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FileHeaderDTO> getSharedFilesNotInSharedFolders(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of all shared files of a user.
	 *
	 * @param userId the ID of the User
	 * @return the list of shared files
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FileHeaderDTO> getSharedFiles(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of all shared folders of a user.
	 *
	 * @param userId the ID of the User
	 * @return the list of shared folders
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FolderDTO> getSharedFolders(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of all shared root folders of a user that calling
	 * user has at least read permissions.
	 *
	 * @param ownerId the ID of the User
	 * @return the list of shared root folders
	 * @param callingUserId
	 *
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FolderDTO> getSharedRootFolders(Long ownerId, Long callingUserId)
			throws ObjectNotFoundException;

	/**
	 * Returns a list of all shared  files of a user that calling user has
	 * at least read permissions..
	 *
	 * @param ownerId the ID of the User
	 * @return the list of shared files
	 * @param callingUserId
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FileHeaderDTO> getSharedFiles(Long ownerId, Long callingUserId)
			throws ObjectNotFoundException;

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
	public void removeMemberFromGroup(Long userId, Long groupId, Long memberId)
			throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Retrieves the list of users sharing files to user identified by user id
	 * @param userId
	 * @return the List of users sharing files to user
	 * @throws ObjectNotFoundException
	 */
	public List<UserDTO> getUsersSharingFoldersForUser(Long userId) throws ObjectNotFoundException;

	/**
	 * Search Files
	 *
	 * @param userId
	 * @param query
	 * @return list of files that match query
	 * @throws ObjectNotFoundException if no user or query was specified
	 */
	public List<FileHeaderDTO> searchFiles(Long userId, String query) throws ObjectNotFoundException;

	/**
	 * Creates a nonce for the specified user.
	 *
	 * @param userId the ID of the user
	 * @return the new nonce
	 * @throws ObjectNotFoundException if the user could not be found
	 */
	public Nonce createNonce(Long userId) throws ObjectNotFoundException;

	/**
	 * Find the nonce object for the specified encoded nonce, that should be
	 * associated with the specified user.
	 *
	 * @param nonce the issued nonce in Base64 encoding
	 * @param userId the ID of the user for whom this nonce should have been issued
	 * @return the retrieved nonce object
	 * @throws ObjectNotFoundException if the nonce or user were not found
	 */
	public Nonce getNonce(String nonce, Long userId) throws ObjectNotFoundException;

	/**
	 * Remove the specified nonce from the persistent store.
	 *
	 * @param id the ID of the nonce
	 * @throws ObjectNotFoundException if the specified nonce could not be found
	 */
	public void removeNonce(Long id) throws ObjectNotFoundException;

	/**
	 * Activate the supplied nonce for the specified user.
	 *
	 * @param userId the ID of the user
	 * @param nonce the nonce to activate
	 * @param nonceExpiryDate the expiry date of the nonce
	 * @throws ObjectNotFoundException
	 */
	public void activateUserNonce(Long userId, String nonce, Date nonceExpiryDate) throws ObjectNotFoundException;

	/**
	 * Retrieves user statistics.
	 *
	 * @param userId the ID of the user
	 * @return the statistics
	 * @throws ObjectNotFoundException
	 *
	 */
	public StatsDTO getUserStatistics(Long userId) throws ObjectNotFoundException;

	/**
	 * Retrieves file versions
	 *
	 * @param userId the ID of the user
	 * @param fileId the ID of the file
	 * @return the list of filebodies
	 * @throws ObjectNotFoundException
	 * @throws InsufficientPermissionsException
	 *
	 */
	public List<FileBodyDTO> getVersions(Long userId, Long fileId)
			throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Restore the file contents to the specified version.
	 *
	 * @param userId the ID of the user
	 * @param fileId the ID of the file
	 * @param version the version number of the desired file contents
	 * @throws ObjectNotFoundException if the user or file was not
	 * 			found, with	the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException if the user does not have the
	 *          appropriate privileges
	 * @throws QuotaExceededException if the user quota limit would be exceeded
	 * @throws GSSIOException if there was an error while accessing the file contents
	 */
	public void restoreVersion(Long userId, Long fileId, int version)
			throws ObjectNotFoundException, InsufficientPermissionsException,  GSSIOException, QuotaExceededException;

	/**
	 * Remove file version identified by bodyId
	 *
	 * @param userId the ID of the user
	 * @param fileId the ID of the file
	 * @param bodyId the ID of the body
	 *
	 * @throws ObjectNotFoundException
	 * @throws InsufficientPermissionsException
	 *
	 */
	public void removeVersion(Long userId, Long fileId, Long bodyId)
			throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Removes all old file versions for specified file keeping only the current revision
	 *
	 * @param userId the ID of the user
	 * @param fileId the ID of the file
	 *
	 * @throws ObjectNotFoundException
	 * @throws InsufficientPermissionsException
	 *
	 */
	public void removeOldVersions(Long userId, Long fileId)
			throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Toggle between file being versioned or not.
	 *
	 * @param userId the ID of the user
	 * @param fileId the ID of the file
	 * @param versioned the new value of the flag
	 *
	 * @throws ObjectNotFoundException
	 * @throws InsufficientPermissionsException
	 *
	 */
	public void toggleFileVersioning(Long userId, Long fileId, boolean versioned)
			throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * It is used by the Solr mbean to rebuild the index.
	 */
	public void rebuildSolrIndex();

	/**
	 * Creates a new file with the specified owner, parent folder and name. The
	 * new file has the same permissions as its parent folder. The file contents
	 * are already uploaded outside of externalAPI and the uploaded file is used as a parameter.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the parent folder
	 * @param name the name of the new file
	 * @param mimeType the MIME type of the file
	 * @param uploadedFile the uploaded file
	 * @return The FileHeaderDTO created
	 * @throws DuplicateNameException if the specified name already exists in
	 *             the parent folder, as either a folder or file
	 * @throws ObjectNotFoundException if the user or parent folder was not
	 *             found, with the exception message mentioning the precise
	 *             problem
	 * @throws GSSIOException if there was an error while storing the file contents
	 * @throws InsufficientPermissionsException
	 * @throws QuotaExceededException
	 */
	public FileHeaderDTO createFile(Long userId, Long folderId, String name, String mimeType, File uploadedFile)
			throws DuplicateNameException, ObjectNotFoundException, GSSIOException,
			InsufficientPermissionsException, QuotaExceededException;

	/**
	 * Create a new FileBody with the supplied uploaded file as contents and make it the current body
	 * of the file.
	 *
	 * @param userId the ID of the current user
	 * @param fileId the ID of the file header object
	 * @param mimeType the content type of the file
	 * @param uploadedFile the already uploadedFile
	 * @return The FileHeaderDTO updated
	 * @throws ObjectNotFoundException if the user or file was not found, with
	 * 			the exception message mentioning the precise problem
	 * @throws GSSIOException when an IO exception occurs
	 * @throws InsufficientPermissionsException
	 * @throws QuotaExceededException
	 */
	public FileHeaderDTO updateFileContents(Long userId, Long fileId, String mimeType,
				File uploadedFile) throws ObjectNotFoundException, GSSIOException,
				InsufficientPermissionsException, QuotaExceededException;

	/**
	 * Create a file based on inputstream without using transaction.
	 *
	 * @param stream
	 * @param userId
	 * @return the file
	 * @throws IOException
	 * @throws ObjectNotFoundException
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public File uploadFile(InputStream stream, Long userId)
			throws IOException, ObjectNotFoundException;

	public void createFileUploadProgress(Long userId, String filename,
				Long bytesTransfered, Long fileSize) throws ObjectNotFoundException;

	public FileUploadStatus getFileUploadStatus(Long userId, String fileName);

	public void removeFileUploadProgress(Long userId, String filename)
			throws ObjectNotFoundException;

	/**
	 * Fetch the file body with the specified version number.
	 *
	 * @param userId the ID of the current user
	 * @param fileId the ID of the file header
	 * @param version the version number
	 * @return the file body
	 * @throws ObjectNotFoundException if the user file or version
	 * 			were not found
	 * @throws InsufficientPermissionsException if the user does not
	 * 			have enough privileges for reading this file
	 */
	public FileBodyDTO getFileVersion(Long userId, Long fileId, int version) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Search the system for a user with the specified email address.
	 * If no such user is found, the method returns null.
	 */
	public User findUserByEmail(String email);

	/**
	 * Update the user with the values from the supplied object.
	 */
	public void updateUser(User user);

	/**
	 * Update accounting information for given user.
	 *
	 * @param user The user to update
	 * @param date Date of transaction
	 * @param bandwidthDiff Bandwidth used; positive for addition,
	 * negative for subtraction (e.g. to rollback)
	 */
	public void updateAccounting(User user, Date date, long bandwidthDiff);

	/**
	 * Check if the user with the specified ID has permission to read the
	 * folder with the supplied ID.
	 */
	public boolean canReadFolder(Long userId, Long folderId) throws ObjectNotFoundException;

	/**
	 * Reset WebDAV password for given user.
	 *
	 * @param userId
	 * @return the new password
	 * @throws ObjectNotFoundException
	 */
	public String resetWebDAVPassword(Long userId) throws ObjectNotFoundException;

}
