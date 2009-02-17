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
package gr.ebs.gss.server;

import gr.ebs.gss.client.GSSService;
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
import gr.ebs.gss.server.domain.FileUploadStatus;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.ejb.ExternalAPI;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The implementation of the RPC service on the server.
 *
 * @author past
 */
public class GSSServiceImpl extends RemoteServiceServlet implements GSSService {

	/**
	 * The serial version UID of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(GSSServiceImpl.class);

	/**
	 * A helper method that retrieves a reference to the ExternalApi bean and
	 * stores it for future use.
	 *
	 * @return an ExternalAPI instance
	 * @throws RpcException in case an error occurs
	 */
	private ExternalAPI getService() throws RpcException {
		try {
			final Context ctx = new InitialContext();
			final Object ref = ctx.lookup("gss/ExternalAPIBean/local");
			return (ExternalAPI) PortableRemoteObject.narrow(ref, ExternalAPI.class);
		} catch (final NamingException e) {
			logger.error("Unable to retrieve the ExternalAPI EJB", e);
			throw new RpcException("An error occurred while contacting the naming service");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.client.GSSService#getGroups(java.lang.Long)
	 */
	public List<GroupDTO> getGroups(final Long userId) throws RpcException, ObjectNotFoundException {

		return getService().getGroups(userId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.client.GSSService#getUsers(java.lang.Long,
	 *      java.lang.Long)
	 */
	public List<UserDTO> getUsers(final Long userId, final Long groupId) throws RpcException, ObjectNotFoundException {
		final List<UserDTO> users = getService().getUsers(userId, groupId);
		return users;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.client.GSSService#getRootFolder(java.lang.Long)
	 */
	public FolderDTO getRootFolder(final Long userId) throws RpcException, ObjectNotFoundException {
		FolderDTO dto = getService().getRootFolder(userId);

		return dto;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.client.GSSService#getFolder(java.lang.Long,
	 *      java.lang.Long)
	 */
	public FolderDTO getFolder(final Long userId, final Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		return getService().getFolder(userId, folderId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.client.GSSService#getFiles(java.lang.Long,
	 *      java.lang.Long)
	 */
	public List<FileHeaderDTO> getFiles(final Long userId, final Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		final List<FileHeaderDTO> fileHeaders = getService().getFiles(userId, folderId);
		return fileHeaders;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.client.GSSService#createFolder(java.lang.Long,
	 *      java.lang.Long, java.lang.String)
	 */
	public void createFolder(final Long userId, final Long parentId, final String name) throws RpcException, DuplicateNameException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().createFolder(userId, parentId, name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.client.GSSService#deleteFolder(java.lang.Long,
	 *      java.lang.Long)
	 */
	public void deleteFolder(final Long userId, final Long folderId) throws RpcException, InsufficientPermissionsException, ObjectNotFoundException {
		getService().deleteFolder(userId, folderId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.client.GSSService#getSubfolders(java.lang.Long,
	 *      java.lang.Long)
	 */
	public List<FolderDTO> getSubfolders(final Long userId, final Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		List<FolderDTO> res = getService().getSubfolders(userId, folderId);
		return res;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.client.GSSService#modifyFolder(java.lang.Long,
	 *      java.lang.Long, java.lang.String)
	 */
	public void modifyFolder(final Long userId, final Long folderId, final String folderName) throws RpcException, InsufficientPermissionsException, ObjectNotFoundException, DuplicateNameException {
		getService().modifyFolder(userId, folderId, folderName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.client.GSSService#createGroup(java.lang.Long,
	 *      java.lang.String)
	 */
	public void createGroup(final Long userId, final String name) throws RpcException, DuplicateNameException, ObjectNotFoundException {
		getService().createGroup(userId, name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.client.GSSService#deleteGroup(java.lang.Long,
	 *      java.lang.Long)
	 */
	public void deleteGroup(final Long userId, final Long groupId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().deleteGroup(userId, groupId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.client.GSSService#deleteFile(java.lang.Long,
	 *      java.lang.Long)
	 */
	public void deleteFile(final Long userId, final Long fileId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().deleteFile(userId, fileId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getUserTags(java.lang.Long)
	 */
	public Set<String> getUserTags(Long userId) throws RpcException, ObjectNotFoundException {
		return getService().getUserTags(userId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#updateFile(java.lang.Long, java.lang.Long, java.lang.String, java.util.Set)
	 */
	public void updateFile(Long userId, Long fileId, String name, String tagSet) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().updateFile(userId, fileId, name, tagSet);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#copyFile(java.lang.Long, java.lang.Long, java.lang.String)
	 */
	public void copyFile(Long userId, Long fileId, String dest) throws RpcException, ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, QuotaExceededException, GSSIOException {
			getService().copyFile(userId, fileId, dest);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#copyFile(java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String)
	 */
	public void copyFile(Long userId, Long fileId, Long destId, String destName) throws RpcException, ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, QuotaExceededException, GSSIOException {
			getService().copyFile(userId, fileId, destId, destName);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#markDeletedFile(java.lang.Long, java.lang.Long)
	 */
	public void moveFileToTrash(Long userId, Long fileId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().moveFileToTrash(userId, fileId);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#copyFolder(java.lang.Long, java.lang.Long, java.lang.String)
	 */
	public void copyFolder(Long userId, Long folderId, String dest) throws RpcException, ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException {
		getService().copyFolder(userId, folderId, dest);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#copyFolder(java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String)
	 */
	public void copyFolder(Long userId, Long folderId, Long destId, String destName) throws RpcException, ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException {
		getService().copyFolder(userId, folderId, destId, destName);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#copyFolderStructure(java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String)
	 */
	public void copyFolderStructure(Long userId, Long folderId, Long destId, String destName) throws RpcException, ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, QuotaExceededException, GSSIOException {
		getService().copyFolderStructure(userId, folderId, destId, destName);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getUser(java.lang.String)
	 */
	public UserDTO getUser(String username) throws RpcException, ObjectNotFoundException {
		if (username == null)
			throw new ObjectNotFoundException("No username specified");
		String usernameDecoded = null;
		try {
			usernameDecoded = URLDecoder.decode(username, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new RpcException(e.getMessage());
		}
		User user = getService().findUser(usernameDecoded);
		if (user == null)
			throw new ObjectNotFoundException("Username not found");
		return user.getDTO();
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#moveFile(java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String)
	 */
	public void moveFile(Long userId, Long fileId, Long destId, String destName) throws RpcException, InsufficientPermissionsException, ObjectNotFoundException, DuplicateNameException, QuotaExceededException, GSSIOException {
			getService().moveFile(userId, fileId, destId, destName);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#moveFolder(java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String)
	 */
	public void moveFolder(Long userId, Long folderId, Long destId, String destName) throws RpcException, InsufficientPermissionsException, ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, QuotaExceededException, GSSIOException {
			getService().moveFolder(userId, folderId, destId, destName);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getDeletedFiles(java.lang.Long)
	 */
	public List<FileHeaderDTO> getDeletedFiles(Long userId) throws RpcException, ObjectNotFoundException {
		final List<FileHeaderDTO> fileHeaders = getService().getDeletedFiles(userId);
		return fileHeaders;
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#markUnDeletedFile(java.lang.Long, java.lang.Long)
	 */
	public void removeFileFromTrash(Long userId, Long fileId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().removeFileFromTrash(userId, fileId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#moveFolderToTrash(java.lang.Long, java.lang.Long)
	 */
	public void moveFolderToTrash(Long userId, Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().moveFolderToTrash(userId, folderId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#removeFolderFromTrash(java.lang.Long, java.lang.Long)
	 */
	public void removeFolderFromTrash(Long userId, Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().removeFolderFromTrash(userId, folderId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getDeletedRootFolders(java.lang.Long)
	 */
	public List<FolderDTO> getDeletedRootFolders(Long userId) throws RpcException, ObjectNotFoundException {
		return getService().getDeletedRootFolders(userId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#emptyTrash(java.lang.Long)
	 */
	public void emptyTrash(Long userId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().emptyTrash(userId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#restoreTrash(java.lang.Long)
	 */
	public void restoreTrash(Long userId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().restoreTrash(userId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getFolderPermissions(java.lang.Long, java.lang.Long)
	 */
	@Override
	public Set<PermissionDTO> getFolderPermissions(Long userId, Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		return getService().getFolderPermissions(userId, folderId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#setFolderPermissions(java.lang.Long, java.lang.Long, java.util.Set)
	 */
	@Override
	public void setFolderPermissions(Long userId, Long folderId, Set<PermissionDTO> permissions) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().setFolderPermissions(userId, folderId, permissions);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getUsersByUserNameLike(java.lang.String)
	 */
	@Override
	public List<UserDTO> getUsersByUserNameLike(String username) throws RpcException {
		return getService().getUsersByUserNameLike(username);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#addUserToGroup(java.lang.Long, java.lang.Long, java.lang.Long)
	 */
	@Override
	public void addUserToGroup(Long userId, Long groupId, Long userToAddId) throws RpcException, ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException {
		getService().addUserToGroup(userId, groupId, userToAddId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getSharedRootFolders(java.lang.Long)
	 */
	@Override
	public List<FolderDTO> getSharedRootFolders(Long userId) throws RpcException, ObjectNotFoundException {
		List<FolderDTO> res = getService().getSharedRootFolders(userId);
		return res;
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#removeMemberFromGroup(java.lang.Long, java.lang.Long, java.lang.Long)
	 */
	@Override
	public void removeMemberFromGroup(Long userId, Long groupId, Long memberId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().removeMemberFromGroup(userId, groupId, memberId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getUsersSharingFoldersForUser(java.lang.Long)
	 */
	@Override
	public List<UserDTO> getUsersSharingFoldersForUser(Long userId) throws RpcException, ObjectNotFoundException {
		List<UserDTO> res = getService().getUsersSharingFoldersForUser(userId);
		return res;
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getFilePermissions(java.lang.Long, java.lang.Long)
	 */
	@Override
	public Set<PermissionDTO> getFilePermissions(Long userId, Long fileId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		return getService().getFilePermissions(userId, fileId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#setFilePermissions(java.lang.Long, java.lang.Long, java.util.Set)
	 */
	@Override
	public void setFilePermissions(Long userId, Long fileId, boolean readForAll, Set<PermissionDTO> permissions) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().setFilePermissions(userId, fileId, readForAll, permissions);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getSharedFiles(java.lang.Long)
	 */
	@Override
	public List<FileHeaderDTO> getSharedFiles(Long userId) throws RpcException, ObjectNotFoundException {
		return getService().getSharedFilesNotInSharedFolders(userId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getSharedFiles(java.lang.Long, java.lang.Long)
	 */
	@Override
	public List<FileHeaderDTO> getSharedFiles(Long userId, Long callingUserId) throws RpcException, ObjectNotFoundException {
		return getService().getSharedFiles(userId, callingUserId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getSharedRootFolders(java.lang.Long, java.lang.Long)
	 */
	@Override
	public List<FolderDTO> getSharedRootFolders(Long userId, Long callingUserId) throws RpcException, ObjectNotFoundException {
		return getService().getSharedRootFolders(userId, callingUserId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getSharedSubfolders(java.lang.Long, java.lang.Long)
	 */
	@Override
	public List<FolderDTO> getSharedSubfolders(Long userId, Long folderId) throws RpcException, ObjectNotFoundException {
		return getService().getSharedSubfolders(userId, folderId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#searchFiles(java.lang.Long, java.lang.String)
	 */
	@Override
	public List<FileHeaderDTO> searchFiles(Long userId, String query) throws ObjectNotFoundException, RpcException {
		return getService().searchFiles(userId, query);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#copyFiles(java.lang.Long, java.util.List, java.lang.Long)
	 */
	@Override
	public void copyFiles(Long userId, List<Long> fileIds, Long destId) throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, RpcException, QuotaExceededException, GSSIOException {

		getService().copyFiles(userId, fileIds, destId);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#moveFiles(java.lang.Long, java.util.List, java.lang.Long)
	 */
	@Override
	public void moveFiles(Long userId, List<Long> fileIds, Long destId) throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, RpcException, QuotaExceededException, GSSIOException {
		getService().moveFiles(userId, fileIds, destId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#deleteFiles(java.lang.Long, java.util.List)
	 */
	@Override
	public void deleteFiles(Long userId, List<Long> fileIds) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().deleteFiles(userId, fileIds);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#moveFilesToTrash(java.lang.Long, java.util.List)
	 */
	@Override
	public void moveFilesToTrash(Long userId, List<Long> fileIds) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().moveFilesToTrash(userId, fileIds);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#removeFilesFromTrash(java.lang.Long, java.util.List)
	 */
	@Override
	public void removeFilesFromTrash(Long userId, List<Long> fileIds) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().removeFilesFromTrash(userId, fileIds);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getUserStatistics(java.lang.Long)
	 */
	@Override
	public StatsDTO getUserStatistics(Long userId) throws RpcException, ObjectNotFoundException {
		return getService().getUserStatistics(userId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getVersions(java.lang.Long, java.lang.Long)
	 */
	@Override
	public List<FileBodyDTO> getVersions(Long userId, Long fileId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		return getService().getVersions(userId, fileId);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#removeVersion(java.lang.Long, java.lang.Long, java.lang.Long)
	 */
	@Override
	public void removeVersion(Long userId, Long fileId, Long bodyId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().removeVersion(userId, fileId, bodyId);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#restoreVersion(java.lang.Long, java.lang.Long, java.lang.Long)
	 */
	@Override
	public void restoreVersion(Long userId, Long fileId, Long bodyId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException, QuotaExceededException, GSSIOException {
		getService().restoreVersion(userId, fileId, bodyId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#removeOldVersions(java.lang.Long, java.lang.Long)
	 */
	@Override
	public void removeOldVersions(Long userId, Long fileId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().removeOldVersions(userId, fileId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#toggleFileVersioning(java.lang.Long, java.lang.Long, boolean)
	 */
	@Override
	public void toggleFileVersioning(Long userId, Long fileId, boolean versioned) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().toggleFileVersioning(userId, fileId, versioned);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getUploadStatus()
	 */
	@Override
	public UploadStatusDTO getUploadStatus(Long userId, String fileName) throws RpcException, ObjectNotFoundException {
		FileUploadStatus status = getService().getFileUploadStatus(userId, fileName);
		if(status == null)
			return null;
		UploadStatusDTO dto = new UploadStatusDTO();
		dto.setBytesTransferred(status.getBytesUploaded());
		dto.setFileSize(status.getFileSize());
		if( status.getFileSize().equals(status.getBytesUploaded()))
			getService().removeFileUploadProgress(userId, fileName);
		return dto;

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getFolderWithSubfolders(java.lang.Long, java.lang.Long)
	 */
	@Override
	public FolderDTO getFolderWithSubfolders(Long userId, Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		return getService().getFolderWithSubfolders(userId, folderId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getSharedSubfolders(java.lang.Long, java.lang.Long, java.lang.Long)
	 */
	@Override
	public List<FolderDTO> getSharedSubfolders(Long userId, Long callingUserId, Long folderId) throws RpcException, ObjectNotFoundException {
		return getService().getSharedSubfolders(userId, callingUserId, folderId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.GSSService#getFolderWithSubfolders(java.lang.Long, java.lang.Long, java.lang.Long)
	 */
	@Override
	public FolderDTO getFolderWithSubfolders(Long userId, Long callingUserId, Long folderId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		return getService().getFolderWithSubfolders(userId, callingUserId, folderId);
	}

}
