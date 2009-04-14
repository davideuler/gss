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

import gr.ebs.gss.client.exceptions.DuplicateNameException;
import gr.ebs.gss.client.exceptions.GSSIOException;
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.QuotaExceededException;
import gr.ebs.gss.server.domain.FileHeader;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.domain.dto.FileHeaderDTO;
import gr.ebs.gss.server.domain.dto.FolderDTO;
import gr.ebs.gss.server.domain.dto.GroupDTO;
import gr.ebs.gss.server.domain.dto.PermissionDTO;
import gr.ebs.gss.server.domain.dto.UserDTO;
import gr.ebs.gss.server.ejb.ExternalAPI;
import gr.ebs.gss.server.ejb.GSSDAO;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;


/**
 * @author kman
 *
 */
@Stateless
@WebService(name="GSSWSAPI")
public class WSAPIBean implements WSAPIRemote{
	@EJB
	ExternalAPI api;
	@EJB
	GSSDAO dao;

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#copyFile(java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String)
	 */
	@WebMethod(operationName="copyFile")
	@Override
	public void copyFile(@WebParam(name="userId") Long userId, @WebParam(name="fileId") Long fileId, @WebParam(name="destId") Long destId, @WebParam(name="destName") String destName) throws ObjectNotFoundException, DuplicateNameException, GSSIOException, InsufficientPermissionsException, QuotaExceededException {
		api.copyFile(userId, fileId, destId, destName);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#copyFolder(java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String)
	 */
	@WebMethod(operationName="copyFolder")
	@Override
	public void copyFolder(@WebParam(name="userId") Long userId, @WebParam(name="folderId") Long folderId, @WebParam(name="destId") Long destId, @WebParam(name="destName") String destName) throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, GSSIOException, QuotaExceededException {
		api.copyFolderStructure(userId, folderId, destId, destName);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#createFile(java.lang.Long, java.lang.Long, java.lang.String, java.lang.String, java.io.InputStream)
	 */
	@WebMethod(operationName="createFile")
	@XmlMimeType(value = "application/octet-stream")
	@Override
	public void createFile(@WebParam(name="userId")Long userId, @WebParam(name="folderId")Long folderId, @WebParam(name="name")String name, @WebParam(name="mimeType")String mimeType, @WebParam(name="stream")DataHandler stream) throws DuplicateNameException, ObjectNotFoundException, GSSIOException, InsufficientPermissionsException, QuotaExceededException {
		try {
			FileHeaderDTO file = api.createFile(userId, folderId, name, mimeType, stream.getInputStream());
			User user = api.getUser(userId);
			api.updateAccounting(user, new Date(), file.getFileSize());
		} catch (IOException e) {
			throw new GSSIOException(e);
		}

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#createFolder(java.lang.Long, java.lang.Long, java.lang.String)
	 */
	@WebMethod(operationName="createFolder")
	@Override
	public void createFolder(@WebParam(name="userId")Long userId, @WebParam(name="parentId")Long parentId, @WebParam(name="name")String name) throws DuplicateNameException, ObjectNotFoundException, InsufficientPermissionsException {
		api.createFolder(userId, parentId, name);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#createGroup(java.lang.Long, java.lang.String)
	 */
	@WebMethod(operationName="createGroup")
	@Override
	public void createGroup(@WebParam(name="userId") Long userId, @WebParam(name="name") String name) throws ObjectNotFoundException, DuplicateNameException {
		api.createGroup(userId, name);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#createTag(java.lang.Long, java.lang.Long, java.lang.String)
	 */
	@WebMethod(operationName="createTag")
	@Override
	public void createTag(@WebParam(name="userId") Long userId, @WebParam(name="fileHeaderId") Long fileHeaderId, @WebParam(name="tag") String tag) throws ObjectNotFoundException {
		api.createTag(userId, fileHeaderId, tag);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#deleteFile(java.lang.Long, java.lang.Long)
	 */
	@WebMethod(operationName="deleteFile")
	@Override
	public void deleteFile(@WebParam(name="userId") Long userId, @WebParam(name="fileId") Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException {
		api.deleteFile(userId, fileId);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#deleteFolder(java.lang.Long, java.lang.Long)
	 */
	@WebMethod(operationName="deleteFolder")
	@Override
	public void deleteFolder(@WebParam(name="userId") Long userId, @WebParam(name="folderId") Long folderId) throws InsufficientPermissionsException, ObjectNotFoundException {
		api.deleteFolder(userId, folderId);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#deleteGroup(java.lang.Long, java.lang.Long)
	 */
	@WebMethod(operationName="deleteGroup")
	@Override
	public void deleteGroup(@WebParam(name="userId") Long userId, @WebParam(name="groupId") Long groupId) throws ObjectNotFoundException, InsufficientPermissionsException {
		api.deleteGroup(userId, groupId);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#emptyTrash(java.lang.Long)
	 */
	@WebMethod(operationName="emptyTrash")
	@Override
	public void emptyTrash( @WebParam(name="userId") Long userId) throws ObjectNotFoundException, InsufficientPermissionsException {
		api.emptyTrash(userId);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getDeletedFiles(java.lang.Long)
	 */
	@WebMethod(operationName="getDeletedFiles")
	@Override
	public FileHeaderDTO[] getDeletedFiles( @WebParam(name="userId") Long userId) throws ObjectNotFoundException {
		List<FileHeaderDTO> res = api.getDeletedFiles(userId);
		return res.toArray(new FileHeaderDTO[res.size()]);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getDeletedRootFolders(java.lang.Long)
	 */
	@WebMethod(operationName="getDeletedRootFolders")
	@Override
	public FolderDTO[] getDeletedRootFolders(@WebParam(name="userId") Long userId) throws ObjectNotFoundException {
		List<FolderDTO> res = api.getDeletedRootFolders(userId);
		return res.toArray(new FolderDTO[res.size()]);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getFile(java.lang.Long, java.lang.Long)
	 */
	@WebMethod(operationName="getFile")
	@Override
	public FileHeaderDTO getFile(@WebParam(name="userId") Long userId, @WebParam(name="fileId") Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException {
		return api.getFile(userId, fileId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getFileContents(java.lang.Long, java.lang.Long)
	 */
	@WebMethod(operationName="getFileContents")
	@Override
	public @XmlMimeType(value = "application/octet-stream")DataHandler getFileContents(@WebParam(name="userId") Long userId, @WebParam(name="fileId") Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");

		final FileHeader header = dao.getEntityById(FileHeader.class, fileId);
		final User user = dao.getEntityById(User.class, userId);
		if (!header.hasReadPermission(user))
			throw new InsufficientPermissionsException("You don't have the necessary permissions");

		File f = new File(header.getCurrentBody().getStoredFilePath());
		return new DataHandler(new FileDataSource(f));
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getFiles(java.lang.Long, java.lang.Long)
	 */
	@WebMethod(operationName="getFiles")
	@Override
	public FileHeaderDTO[] getFiles(@WebParam(name="userId") Long userId, @WebParam(name="folderId") Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException {
		List<FileHeaderDTO> res = api.getFiles(userId, folderId, true);
		return res.toArray(new FileHeaderDTO[res.size()]);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getFolder(java.lang.Long, java.lang.Long)
	 */
	@WebMethod(operationName="getFolder")
	@Override
	public FolderDTO getFolder(@WebParam(name="userId") Long userId, @WebParam(name="folderId") Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException {
		return api.getFolder(userId, folderId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getGroup(java.lang.Long)
	 */
	@WebMethod(operationName="getGroup")
	@Override
	public GroupDTO getGroup(@WebParam(name="groupId") Long groupId) throws ObjectNotFoundException {
		return api.getGroup(groupId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getGroups(java.lang.Long)
	 */
	@WebMethod(operationName="getGroups")
	@Override
	public GroupDTO[] getGroups(@WebParam(name="userId") Long userId) throws ObjectNotFoundException {
		List<GroupDTO> res = api.getGroups(userId);
		return res.toArray(new GroupDTO[res.size()]);
	}


	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getRootFolder(java.lang.Long)
	 */
	@WebMethod(operationName="getRootFolder")
	@Override
	public FolderDTO getRootFolder(@WebParam(name="userId") Long userId) throws ObjectNotFoundException {
		return api.getRootFolder(userId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getSubfolders(java.lang.Long, java.lang.Long)
	 */
	@WebMethod(operationName="getSubFolders")
	@Override
	public FolderDTO[] getSubfolders(@WebParam(name="userId") Long userId, @WebParam(name="folderId") Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException {
		List<FolderDTO> res = api.getSubfolders(userId, folderId);
		return res.toArray(new FolderDTO[res.size()]);

	}


	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getUserTags(java.lang.Long)
	 */
	@WebMethod(operationName="getUserTags")
	@Override
	public String[] getUserTags(@WebParam(name="userId") Long userId) throws ObjectNotFoundException {
		Set<String> res = api.getUserTags(userId);
		return res.toArray(new String[res.size()]);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getUsers(java.lang.Long, java.lang.Long)
	 */
	@WebMethod(operationName="getUsers")
	@Override
	public UserDTO[] getUsers(@WebParam(name="userId") Long userId, @WebParam(name="groupId") Long groupId) throws ObjectNotFoundException {
		List<UserDTO> res = api.getUsers(userId, groupId);
		return res.toArray(new UserDTO[res.size()]);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#modifyFolder(java.lang.Long, java.lang.Long, java.lang.String)
	 */
	@WebMethod(operationName="modifyFolder")
	@Override
	public void modifyFolder(@WebParam(name="userId") Long userId, @WebParam(name="folderId") Long folderId, @WebParam(name="folderName") String folderName) throws InsufficientPermissionsException, ObjectNotFoundException, DuplicateNameException {
		api.modifyFolder(userId, folderId, folderName);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#moveFile(java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String)
	 */
	@WebMethod(operationName="moveFile")
	@Override
	public void moveFile(@WebParam(name="userId") Long userId, @WebParam(name="fileId") Long fileId, @WebParam(name="destId") Long destId, @WebParam(name="destName") String destName) throws InsufficientPermissionsException, ObjectNotFoundException, DuplicateNameException, GSSIOException, QuotaExceededException {
		api.moveFile(userId, fileId, destId, destName);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#moveFileToTrash(java.lang.Long, java.lang.Long)
	 */
	@WebMethod(operationName="moveFileToTrash")
	@Override
	public void moveFileToTrash(@WebParam(name="userId") Long userId, @WebParam(name="fileId") Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException {
		api.moveFileToTrash(userId, fileId);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#moveFolder(java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String)
	 */
	@WebMethod(operationName="moveFolder")
	@Override
	public void moveFolder(@WebParam(name="userId") Long userId, @WebParam(name="folderId") Long folderId, @WebParam(name="destId") Long destId, @WebParam(name="destName") String destName) throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, GSSIOException, QuotaExceededException {
		api.moveFolder(userId, folderId, destId, destName);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#moveFolderToTrash(java.lang.Long, java.lang.Long)
	 */
	@WebMethod(operationName="moveFolderToTrash")
	@Override
	public void moveFolderToTrash(@WebParam(name="userId") Long userId, @WebParam(name="folderId") Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException {
		api.moveFolderToTrash(userId, folderId);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#removeFileFromTrash(java.lang.Long, java.lang.Long)
	 */
	@WebMethod(operationName="restoreFileFromTrash")
	@Override
	public void restoreFileFromTrash(@WebParam(name="userId") Long userId, @WebParam(name="fileId") Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException {
		api.removeFileFromTrash(userId, fileId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#removeFolderFromTrash(java.lang.Long, java.lang.Long)
	 */
	@WebMethod(operationName="restoreFolderFromTrash")
	@Override
	public void restoreFolderFromTrash(@WebParam(name="userId") Long userId, @WebParam(name="folderId") Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException {
		api.removeFolderFromTrash(userId, folderId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#updateFile(java.lang.Long, java.lang.Long, java.lang.String, java.lang.String)
	 */
	@WebMethod(operationName="updateFile")
	@Override
	public void updateFile(@WebParam(name="userId") Long userId, @WebParam(name="fileId") Long fileId, @WebParam(name="name") String name, @WebParam(name="tagSet") String tagSet) throws ObjectNotFoundException, InsufficientPermissionsException {
		api.updateFile(userId, fileId, name, tagSet);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#updateFileContents(java.lang.Long, java.lang.Long, java.lang.String, java.io.InputStream)
	 */
	@WebMethod(operationName="updateFileContents")
	@XmlMimeType(value = "application/octet-stream")
	@Override
	public void updateFileContents(@WebParam(name="userId") Long userId, @WebParam(name="fileId") Long fileId, @WebParam(name="mimeType") String mimeType, @WebParam(name="resourceStream") DataHandler resourceInputStream) throws ObjectNotFoundException, GSSIOException, InsufficientPermissionsException, QuotaExceededException {
		try {
			FileHeaderDTO file = api.updateFileContents(userId, fileId, mimeType, resourceInputStream.getInputStream());
			User user = api.getUser(userId);
			api.updateAccounting(user, new Date(), file.getFileSize());
		} catch (IOException e) {
			throw new GSSIOException(e);
		}
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#addUserToGroup(java.lang.Long, java.lang.Long, java.lang.Long)
	 */
	@Override
	@WebMethod
	public void addUserToGroup(Long userId, Long groupId, Long userToAddId) throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException {
		api.addUserToGroup(userId, groupId, userToAddId);

	}


	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#findUser(java.lang.String)
	 */
	@Override
	@WebMethod
	public UserDTO getUser(String username) {
		User user = api.findUser(username);
		if(user == null)
			return null;
		return user.getDTO();
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getFilePermissions(java.lang.Long, java.lang.Long)
	 */
	@Override
	@WebMethod
	public Set<PermissionDTO> getFilePermissions(Long userId, Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException {
		return api.getFilePermissions(userId, fileId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getFolderPermissions(java.lang.Long, java.lang.Long)
	 */
	@Override
	@WebMethod
	public Set<PermissionDTO> getFolderPermissions(Long userId, Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException {
		return api.getFolderPermissions(userId, folderId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getSharedFiles(java.lang.Long)
	 */
	@Override
	@WebMethod
	public List<FileHeaderDTO> getSharedFiles(Long userId) throws ObjectNotFoundException {
		return api.getSharedFilesNotInSharedFolders(userId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getSharedFiles(java.lang.Long, java.lang.Long)
	 */
	@Override
	@WebMethod
	public List<FileHeaderDTO> getSharedFilesOfUser(Long ownerId, Long callingUserId) throws ObjectNotFoundException {
		return api.getSharedFiles(ownerId, callingUserId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getSharedRootFolders(java.lang.Long)
	 */
	@Override
	@WebMethod
	public List<FolderDTO> getSharedRootFolders(Long userId) throws ObjectNotFoundException {
		return api.getSharedRootFolders(userId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getSharedRootFolders(java.lang.Long, java.lang.Long)
	 */
	@Override
	@WebMethod
	public List<FolderDTO> getSharedRootFoldersOfUser(Long ownerId, Long callingUserId) throws ObjectNotFoundException {
		return api.getSharedRootFolders(ownerId, callingUserId);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getSharedSubfolders(java.lang.Long, java.lang.Long)
	 */
	@Override
	@WebMethod
	public List<FolderDTO> getSharedSubfolders(Long userId, Long folderId) throws ObjectNotFoundException {
		return api.getSharedSubfolders(userId, folderId);
	}



	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#getUsersSharingFoldersForUser(java.lang.Long)
	 */
	@Override
	@WebMethod
	public List<UserDTO> getUsersSharingFoldersForUser(Long userId) throws ObjectNotFoundException {
		return api.getUsersSharingFoldersForUser(userId);
	}


	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#removeMemberFromGroup(java.lang.Long, java.lang.Long, java.lang.Long)
	 */
	@Override
	@WebMethod
	public void removeMemberFromGroup(Long userId, Long groupId, Long memberId) throws ObjectNotFoundException, InsufficientPermissionsException {
		api.removeMemberFromGroup(userId, groupId, memberId);

	}


	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#setFilePermissions(java.lang.Long, java.lang.Long, java.util.Set)
	 */
	@Override
	@WebMethod
	public void setFilePermissions(Long userId, Long fileId, boolean readForAll, Set<PermissionDTO> permissions) throws ObjectNotFoundException, InsufficientPermissionsException {
		api.setFilePermissions(userId, fileId, readForAll, permissions);

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.soap.WSAPIRemote#setFolderPermissions(java.lang.Long, java.lang.Long, java.util.Set)
	 */
	@Override
	@WebMethod
	public void setFolderPermissions(Long userId, Long folderId, Set<PermissionDTO> permissions) throws ObjectNotFoundException, InsufficientPermissionsException {
		api.setFolderPermissions(userId, folderId, permissions);

	}




}
