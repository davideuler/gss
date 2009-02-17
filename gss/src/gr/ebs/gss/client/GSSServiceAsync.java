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

import gr.ebs.gss.client.domain.PermissionDTO;

import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The GSS service RPC interface for clients that will be calling
 * asynchronously.
 *
 * @author past
 */
public interface GSSServiceAsync {

	/**
	 * Returns the groups defined by the specified user.
	 *
	 * @param userId
	 * @param callback
	 */
	void getGroups(Long userId, AsyncCallback callback);

	/**
	 * Returns the users that are members of the specified group.
	 *
	 * @param userId the ID of the current user
	 * @param groupId the ID of the requested group
	 * @param callback the callback that will be used for responding
	 */
	void getUsers(Long userId, Long groupId, AsyncCallback callback);

	/**
	 * Returns the root folder of the specified user
	 *
	 * @param userId
	 * @param callback
	 */
	void getRootFolder(Long userId, AsyncCallback callback);

	/**
	 * Returns the specified folder of the supplied user.
	 *
	 * @param userId the ID of the user
	 * @param folderId the ID of the requested folder
	 * @param callback the callback that will be used for responding
	 */
	void getFolder(Long userId, Long folderId, AsyncCallback callback);

	/**
	 * Returns the files of the specified folder.
	 *
	 * @param userId the ID of the user
	 * @param folderId the ID of the folder containing the requested files
	 * @param callback the callback that will be used for responding
	 */
	void getFiles(Long userId, Long folderId, AsyncCallback callback);

	/**
	 * Creates a folder in the user's namespace.
	 *
	 * @param userId the ID of the user
	 * @param parentId the ID of the parent folder
	 * @param name the name of the new folder
	 * @param callback the callback that will be used for responding
	 */
	void createFolder(Long userId, Long parentId, String name, AsyncCallback callback);

	/**
	 * Removes the specified folder from the user's namespace.
	 *
	 * @param userId the ID of the user
	 * @param folderId the ID of the folder to remove
	 * @param callback the callback that will be used for responding
	 */
	void deleteFolder(Long userId, Long folderId, AsyncCallback callback);

	/**
	 * Retrieve the subfolders of the specified folder.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the specified folder
	 * @param callback the callback that will be used for responding
	 */
	void getSubfolders(Long userId, Long folderId, AsyncCallback callback);

	/**
	 * Modify the specified folder.
	 *
	 * @param userId the ID of the current user
	 * @param folderId the ID of the specified folder
	 * @param folderName the new folder name
	 * @param callback the callback that will be used for responding
	 */
	void modifyFolder(Long userId, Long folderId, String folderName, AsyncCallback callback);

	/**
	 * Creates a group in the user's namespace.
	 *
	 * @param userId the ID of the user
	 * @param groupName the name of the new group
	 * @param callback the callback that will be used for responding
	 */
	void createGroup(Long userId, String groupName, AsyncCallback callback);

	/**
	 * Removes the specified group from the user's namespace.
	 *
	 * @param userId the ID of the user
	 * @param groupId the ID of the group to remove
	 * @param callback the callback that will be used for responding
	 */
	void deleteGroup(Long userId, Long groupId, AsyncCallback callback);

	/**
	 * Removes the specified file from the user's namespace.
	 *
	 * @param userId the ID of the user
	 * @param fileId the ID of the file to remove
	 * @param callback the callback that will be used for responding
	 */
	void deleteFile(Long userId, Long fileId, AsyncCallback callback);

	/**
	 * Returns all tags defined by the specified user
	 *
	 * @param userId
	 * @param callback the callback that will be used for responding
	 */
	void getUserTags(Long userId, AsyncCallback callback);

	/**
	 * Updates file name and tags for the specified file
	 *
	 * @param userId
	 * @param fileId
	 * @param name
	 * @param tagSet a String that contains tags separated by comma
	 * @param callback the callback that will be used for responding
	 */
	void updateFile(Long userId, Long fileId, String name, String tagSet, AsyncCallback callback);

	void copyFile(Long userId, Long fileId, String dest, AsyncCallback callback);

	void copyFile(Long userId, Long fileId, Long destId, String destName, AsyncCallback callback);

	void copyFolder(Long userId, Long folderId, String dest, AsyncCallback callback);

	void copyFolder(Long userId, Long folderId, Long destId, String destName, AsyncCallback callback);

	void copyFolderStructure(Long userId, Long folderId, Long destId, String destName, AsyncCallback callback);

	void moveFileToTrash(Long userId, Long fileId, AsyncCallback callback);

	void removeFileFromTrash(Long userId, Long fileId, AsyncCallback callback);

	void moveFolderToTrash(Long userId, Long folderId, AsyncCallback callback);

	void removeFolderFromTrash(Long userId, Long folderId, AsyncCallback callback);

	void getUser(String username, AsyncCallback callback);

	void moveFolder(Long userId, Long folderId, Long destId, String destName, AsyncCallback callback);

	void moveFile(Long userId, Long fileId, Long destId, String destName, AsyncCallback callback);

	void getDeletedFiles(Long userId, AsyncCallback callback);

	void getDeletedRootFolders(Long userId, AsyncCallback callback);

	void emptyTrash(Long userId, AsyncCallback callback);

	void restoreTrash(Long userId, AsyncCallback callback);

	void getFolderPermissions(Long userId, Long folderId, AsyncCallback callback);

	void setFolderPermissions(Long userId, Long folderId, Set<PermissionDTO> permissions, AsyncCallback callback);

	void getFilePermissions(Long userId, Long fileId, AsyncCallback callback);

	void setFilePermissions(Long userId, Long fileId, boolean readForAll, Set<PermissionDTO> permissions, AsyncCallback callback);

	void getUsersByUserNameLike(String username, AsyncCallback callback);

	void addUserToGroup(Long userId, Long groupId, Long userToAddId, AsyncCallback callback);

	void getSharedRootFolders(Long userId, AsyncCallback callback);

	void removeMemberFromGroup(Long userId, Long groupId, Long memberId, AsyncCallback callback);

	void getUsersSharingFoldersForUser(Long userId, AsyncCallback callback);

	void getSharedFiles(Long userId, AsyncCallback callback);

	void getSharedRootFolders(Long userId, Long callingUserId, AsyncCallback callback);

	void getSharedFiles(Long userId, Long callingUserId, AsyncCallback callback);

	void getSharedSubfolders(Long userId, Long folderId, AsyncCallback callback);

	void getSharedSubfolders(Long userId, Long callingUserId, Long folderId, AsyncCallback callback);

	void searchFiles(Long userId, String query, AsyncCallback callback);

	void copyFiles(Long userId, List<Long> fileIds, Long destId, AsyncCallback callback);

	void moveFiles(Long userId, List<Long> fileIds, Long destId, AsyncCallback callback);

	void moveFilesToTrash(Long userId, List<Long> fileIds, AsyncCallback callback);

	void deleteFiles(Long userId, List<Long> fileIds, AsyncCallback callback);

	void removeFilesFromTrash(Long userId, List<Long> fileIds, AsyncCallback callback);

	void getUserStatistics(Long userId, AsyncCallback callback);

	void getVersions(Long userId, Long fileId, AsyncCallback callback);

	void restoreVersion(Long userId, Long fileId, Long bodyId, AsyncCallback callback);

	void removeVersion(Long userId, Long fileId, Long bodyId, AsyncCallback callback);

	void removeOldVersions(Long userId, Long fileId, AsyncCallback callback);

	void toggleFileVersioning(Long userId, Long fileId, boolean versioned, AsyncCallback callback);

	void getUploadStatus(Long userId, String fileName, AsyncCallback callback);

	void getFolderWithSubfolders(Long userId, Long folderId, AsyncCallback callback);

	void getFolderWithSubfolders(Long userId, Long callingUserId, Long folderId, AsyncCallback callback);
}
