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
package gr.ebs.gss.client;

import gr.ebs.gss.client.domain.FileBodyDTO;
import gr.ebs.gss.client.domain.FileHeaderDTO;
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.domain.GroupDTO;
import gr.ebs.gss.client.domain.PermissionDTO;
import gr.ebs.gss.client.domain.StatsDTO;
import gr.ebs.gss.client.domain.UploadStatusDTO;
import gr.ebs.gss.client.domain.UserDTO;
import gr.ebs.gss.client.exceptions.DuplicateNameException;
import gr.ebs.gss.client.exceptions.GSSIOException;
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.QuotaExceededException;
import gr.ebs.gss.client.exceptions.RpcException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * The GSS service RPC interface.
 *
 * @author past
 */
public interface GSSService extends RemoteService {

	/**
	 * Maximum number of files that are visible in the file list
	 */
	static final int VISIBLE_FILE_COUNT = 100;

	/**
	 * Maximum number of groups visible in the group list
	 */
	static final int VISIBLE_GROUP_COUNT = 3;

	/**
	 * Maximum number of users visible in the user list
	 */
	static final int VISIBLE_USERS_COUNT = 4;

	/**
	 * Returns the groups defined by the specified user
	 *
	 * @param userId
	 * @return GroupDTO[]
	 * @throws ObjectNotFoundException
	 * @throws RpcException
	 */
	List<GroupDTO> getGroups(Long userId) throws ObjectNotFoundException, RpcException;

	/**
	 * Returns the users that are members of the specified group.
	 *
	 * @param userId the ID of the current user
	 * @param groupId the ID of the requested group
	 * @return an array of user objects
	 * @throws RpcException in case a communication error occurs
	 * @throws ObjectNotFoundException if the user or group was not found, with
	 *             the exception message mentioning the precise problem
	 */
	List<UserDTO> getUsers(Long userId, Long groupId) throws RpcException, ObjectNotFoundException;

	/**
	 * Returns the root folder of the specified user
	 *
	 * @param userId
	 * @return FolderDTO
	 * @throws RpcException
	 * @throws ObjectNotFoundException
	 */
	FolderDTO getRootFolder(Long userId) throws RpcException, ObjectNotFoundException;

	/**
	 * Returns the specified folder of the supplied user.
	 *
	 * @param userId
	 * @param folderId
	 * @return FolderDTO
	 * @throws RpcException
	 * @throws ObjectNotFoundException
	 * @throws InsufficientPermissionsException
	 */
	FolderDTO getFolder(Long userId, Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Returns the files of the specified folder.
	 *
	 * @param userId the ID of the user
	 * @param folderId the ID of the folder containing the requested files
	 * @return the files found in the folder
	 * @throws RpcException in case a communication error occurs
	 * @throws ObjectNotFoundException if the user or folder was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException
	 */
	List<FileHeaderDTO> getFiles(Long userId, Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Creates a folder in the user's namespace.
	 *
	 * @param userId the ID of the user
	 * @param parentId the ID of the parent folder
	 * @param name the name of the new folder
	 * @throws RpcException in case a communication error occurs
	 * @throws DuplicateNameException in case a folder with the same name
	 *             already exists inside the parent folder
	 * @throws ObjectNotFoundException if the user or folder was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException
	 */
	void createFolder(Long userId, Long parentId, String name) throws RpcException, DuplicateNameException, ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Removes the specified folder from the user's namespace.
	 *
	 * @param userId the ID of the user
	 * @param folderId the ID of the folder to remove
	 * @throws RpcException in case a communication error occurs
	 * @throws InsufficientPermissionsException in case the user does not have
	 *             the necessary privileges to perform the operation
	 * @throws ObjectNotFoundException if the user or folder was not found, with
	 *             the exception message mentioning the precise problem
	 */
	void deleteFolder(Long userId, Long folderId) throws RpcException, InsufficientPermissionsException, ObjectNotFoundException;

	/**
	 * Retrieve the subfolders of the specified folder.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the specified folder
	 * @return a List with the sublfolders
	 * @throws RpcException in case a communication error occurs
	 * @throws ObjectNotFoundException in case the specified folder was not
	 *             found
	 * @throws InsufficientPermissionsException
	 */
	List<FolderDTO> getSubfolders(Long userId, Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Retrieve the subfolders of the specified folder that are shared by user.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the specified folder
	 * @return a List with the sublfolders
	 * @throws RpcException in case a communication error occurs
	 * @throws ObjectNotFoundException in case the specified folder was not
	 *             found
	 */
	List<FolderDTO> getSharedSubfolders(Long userId, Long folderId) throws RpcException, ObjectNotFoundException;

	/**
	 * Retrieve the subfolders of the specified folder that are shared by user.
	 *
	 * @param userId the ID of the current user
	 * @param callingUserId the ID of the user requesting operation
	 * @param folderId the ID of the specified folder
	 * @return a List with the sublfolders
	 * @throws RpcException in case a communication error occurs
	 * @throws ObjectNotFoundException in case the specified folder was not
	 *             found
	 */
	List<FolderDTO> getSharedSubfolders(Long userId, Long callingUserId, Long folderId) throws RpcException, ObjectNotFoundException;

	/**
	 * Modify the specified folder.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the specified folder
	 * @param folderName the new folder name
	 * @throws RpcException in case a communication error occurs
	 * @throws InsufficientPermissionsException in case the user does not have
	 *             the necessary privileges to perform the operation
	 * @throws ObjectNotFoundException if the user or folder was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws DuplicateNameException if the modified folder name already exists
	 *             in the parent folder
	 */
	void modifyFolder(Long userId, Long folderId, String folderName) throws RpcException, InsufficientPermissionsException, ObjectNotFoundException, DuplicateNameException;

	/**
	 * Creates a group in the user's namespace.
	 *
	 * @param userId the ID of the user
	 * @param name the name of the new group
	 * @throws RpcException in case a communication error occurs
	 * @throws DuplicateNameException in case a folder with the same name
	 *             already exists inside the parent folder
	 * @throws ObjectNotFoundException if the user or group was not found, with
	 *             the exception message mentioning the precise problem
	 */
	void createGroup(Long userId, String name) throws RpcException, DuplicateNameException, ObjectNotFoundException;

	/**
	 * Removes the specified group from the user's namespace.
	 *
	 * @param userId the ID of the user
	 * @param groupId the ID of the group to remove
	 * @throws RpcException in case a communication error occurs
	 * @throws ObjectNotFoundException if the user or group was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException
	 */
	void deleteGroup(Long userId, Long groupId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Removes the specified file from the user's namespace.
	 *
	 * @param userId the ID of the user
	 * @param fileId the ID of the file to remove
	 * @throws RpcException in case a communication error occurs
	 * @throws ObjectNotFoundException if the user or file was not found, with
	 *             the exception message mentioning the precise problem
	 * @throws InsufficientPermissionsException if the user does not have the
	 *             appropriate privileges
	 */
	void deleteFile(Long userId, Long fileId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Returns all tags defined by the specified user
	 *
	 * @param userId
	 * @return Set
	 * @throws RpcException in case a communication error occurs
	 * @throws ObjectNotFoundException if the user was null
	 */
	Set<String> getUserTags(Long userId) throws RpcException, ObjectNotFoundException;

	/**
	 * Updates file name and tags for the specified file
	 *
	 * @param userId
	 * @param fileId
	 * @param name
	 * @param tagSet a String that contains tags separated by comma
	 * @throws RpcException in case a communication error occurs
	 * @throws ObjectNotFoundException if the user or file was null or not found
	 * @throws InsufficientPermissionsException
	 */
	void updateFile(Long userId, Long fileId, String name, String tagSet) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	void copyFile(Long userId, Long fileId, String dest) throws RpcException, ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, QuotaExceededException, GSSIOException;

	void copyFile(Long userId, Long fileId, Long destId, String destName) throws RpcException, ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, QuotaExceededException, GSSIOException;

	void copyFolder(Long userId, Long folderId, String dest) throws RpcException, ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException;

	void copyFolder(Long userId, Long folderId, Long destId, String destName) throws RpcException, ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException;

	void copyFolderStructure(Long userId, Long folderId, Long destId, String destName) throws RpcException, ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, QuotaExceededException, GSSIOException;

	void moveFileToTrash(Long userId, Long fileId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	void removeFileFromTrash(Long userId, Long fileId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	UserDTO getUser(String username) throws RpcException, ObjectNotFoundException;

	void moveFolder(Long userId, Long folderId, Long destId, String destName) throws RpcException, InsufficientPermissionsException, ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, QuotaExceededException, GSSIOException;

	void moveFile(Long userId, Long fileId, Long destId, String destName) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException, DuplicateNameException, QuotaExceededException, GSSIOException;

	void moveFolderToTrash(Long userId, Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	void removeFolderFromTrash(Long userId, Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Returns the deleted files of the specified user.
	 *
	 * @param userId the ID of the user
	 * 	 * @return the files found in the folder
	 * @throws RpcException in case a communication error occurs
	 * @throws ObjectNotFoundException if the user was not found, with
	 *             the exception message mentioning the precise problem
	 */
	List<FileHeaderDTO> getDeletedFiles(Long userId) throws RpcException, ObjectNotFoundException;

	/**
	 * Returns the deleted root folders of the specified user.
	 *
	 * @param userId the ID of the user
	 * 	 * @return the deleted root folders found in the folder
	 * @throws RpcException in case a communication error occurs
	 * @throws ObjectNotFoundException if the user was not found, with
	 *             the exception message mentioning the precise problem
	 */
	List<FolderDTO> getDeletedRootFolders(Long userId) throws RpcException, ObjectNotFoundException;

	/**
	 * Empty Trash by deleting all marked as deleted files and folders
	 * @param userId
	 * @throws RpcException in case a communication error occurs
	 * @throws ObjectNotFoundException if something goes wrong in the delete process
	 * @throws InsufficientPermissionsException  if something goes wrong in the delete process
	 */
	void emptyTrash(Long userId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Restores All Trashed Items by undeleting all marked as deleted files and folders
	 * @param userId
	 * @throws RpcException in case a communication error occurs
	 * @throws ObjectNotFoundException if something goes wrong in the delete process
	 * @throws InsufficientPermissionsException
	 *
	 */
	void restoreTrash(Long userId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	Set<PermissionDTO> getFolderPermissions(Long userId, Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	void setFolderPermissions(Long userId, Long folderId, Set<PermissionDTO> permissions) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	Set<PermissionDTO> getFilePermissions(Long userId, Long fileId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	void setFilePermissions(Long userId, Long fileId, boolean readForAll, Set<PermissionDTO> permissions) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;


	List<UserDTO> getUsersByUserNameLike(String username) throws RpcException;

	void addUserToGroup(Long userId, Long groupId, Long userToAddId) throws RpcException, ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException;

	List<FolderDTO> getSharedRootFolders(Long userId) throws RpcException, ObjectNotFoundException;

	List<FileHeaderDTO> getSharedFiles(Long userId) throws RpcException, ObjectNotFoundException;

	void removeMemberFromGroup(Long userId, Long groupId, Long memberId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	List<UserDTO> getUsersSharingFoldersForUser(Long userId) throws RpcException, ObjectNotFoundException;

	List<FolderDTO> getSharedRootFolders(Long userId, Long callingUserId) throws RpcException, ObjectNotFoundException;

	List<FileHeaderDTO> getSharedFiles(Long userId, Long callingUserId) throws RpcException, ObjectNotFoundException;

	List<FileHeaderDTO> searchFiles(Long userId, String query) throws ObjectNotFoundException, RpcException;

	void copyFiles(Long userId, List<Long> fileIds, Long destId) throws ObjectNotFoundException, DuplicateNameException,  InsufficientPermissionsException, RpcException, QuotaExceededException, GSSIOException;

	void moveFiles(Long userId, List<Long> fileIds, Long destId) throws ObjectNotFoundException, DuplicateNameException,  InsufficientPermissionsException, RpcException, QuotaExceededException, GSSIOException;

	void moveFilesToTrash(Long userId, List<Long> fileIds) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	void deleteFiles(Long userId, List<Long> fileIds) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	void removeFilesFromTrash(Long userId, List<Long> fileIds) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	StatsDTO getUserStatistics(Long userId) throws RpcException, ObjectNotFoundException;



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
	List<FileBodyDTO> getVersions(Long userId, Long fileId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * Restore file version identified by bodyId
	 *
	 * @param userId the ID of the user
	 * @param fileId the ID of the file
	 *
	 * @throws ObjectNotFoundException
	 * @throws InsufficientPermissionsException
	 * @throws QuotaExceededException
	 * @throws GSSIOException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws FileNotFoundException
	 *
	 */
	void restoreVersion(Long userId, Long fileId, Long bodyId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException, QuotaExceededException,  GSSIOException;

	/**
	 * Remove file version identified by bodyId
	 *
	 * @param userId the ID of the user
	 * @param fileId the ID of the file
	 *
	 * @throws ObjectNotFoundException
	 * @throws InsufficientPermissionsException
	 *
	 */
	void removeVersion(Long userId, Long fileId, Long bodyId) throws RpcException,ObjectNotFoundException, InsufficientPermissionsException;

	void removeOldVersions(Long userId, Long fileId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	void toggleFileVersioning(Long userId, Long fileId, boolean versioned) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	UploadStatusDTO getUploadStatus(Long userId, String fileName) throws RpcException, ObjectNotFoundException;

	FolderDTO getFolderWithSubfolders(Long userId, Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	FolderDTO getFolderWithSubfolders(Long userId, Long callingUserId, Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;
}
