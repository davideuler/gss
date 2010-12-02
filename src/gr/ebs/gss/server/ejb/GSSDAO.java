/*
 * Copyright 2007, 2008, 2009, 2010 Electronic Business Systems Ltd.
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

import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.server.domain.AccountingInfo;
import gr.ebs.gss.server.domain.FileBody;
import gr.ebs.gss.server.domain.FileHeader;
import gr.ebs.gss.server.domain.FileUploadStatus;
import gr.ebs.gss.server.domain.Folder;
import gr.ebs.gss.server.domain.Group;
import gr.ebs.gss.server.domain.Invitation;
import gr.ebs.gss.server.domain.Nonce;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.domain.UserClass;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;

/**
 * This class serves as a facade in front of the persistence library so client
 * classes can be independent of the persistence implementation.
 *
 * @author past
 */
@Local
public interface GSSDAO {

	/**
	 * Creates the given object in the persistent storage.
	 *
	 * @param obj The object to be saved or updated
	 */
	public void create(Object obj);

	/**
	 * Updates by re-attaching the given object to the persistence context.
	 *
	 * @param obj The object to be updated
	 */
	public void update(Object obj);

	/**
	 * Refreshes an object by re-attaching it to the persistence context.
	 *
	 * @param obj the supplied object
	 */
	public void refresh(Object obj);

	/**
	 * Deletes the specified entity from the persistent storage.
	 *
	 * @param entity the object to be deleted
	 */
	public void delete(Object entity);

	/**
	 * Returns an Entity of the specified class with the specified id
	 *
	 * @param <T> The type of the entity
	 * @param _class the Class of the entity
	 * @param _id the id of the entity
	 * @return the Object found
	 * @throws ObjectNotFoundException if the Object was not found
	 */
	public <T> T getEntityById(Class<T> _class, Object _id) throws ObjectNotFoundException;

	/**
	 * Returns the list of Groups that belong to a particular User.
	 *
	 * @param userId the ID of the specified User
	 * @return a List of Group objects
	 * @throws ObjectNotFoundException
	 */
	public List<Group> getGroups(Long userId) throws ObjectNotFoundException;

	/**
	 * Retrieves the root folder for the specified user. The caller must ensure
	 * that the userId exists.
	 *
	 * @param userId
	 * @return Folder
	 * @throws ObjectNotFoundException if no Folder was found
	 */
	public Folder getRootFolder(Long userId) throws gr.ebs.gss.client.exceptions.ObjectNotFoundException;

	/**
	 * Retrieves the user for the requested username.
	 *
	 * @param username the username specified
	 * @return the user object
	 * @throws ObjectNotFoundException if no user was found
	 */
	public User getUser(final String username) throws ObjectNotFoundException;

	/**
	 * Returns a list of files contained in the folder specified by its id, CAUTION: it does not return files marked as deleted
	 *
	 * @param folderId
	 * @param userId
	 * @param ignoreDeleted
	 * @return List<FileHeader>
	 * @throws ObjectNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public List<FileHeader> getFiles(Long folderId, Long userId, boolean ignoreDeleted) throws ObjectNotFoundException;

	/**
	 * Returns a list of deleted files of user specified by userId
	 *
	 * @param userId
	 * @return List<FileHeader>
	 * @throws ObjectNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public List<FileHeader> getDeletedFiles(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of deleted root folders of user specified by userId
	 *
	 * @param userId
	 * @return List<Folder>
	 * @throws ObjectNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public List<Folder> getDeletedRootFolders(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of users for the specified group
	 *
	 * @param groupId
	 * @return List<User>
	 * @throws ObjectNotFoundException
	 */
	public List<User> getUsers(Long groupId) throws ObjectNotFoundException;

	/**
	 * Checks if a folder or file with the specified name exists under the
	 * specified parent.
	 *
	 * @param parentId
	 * @param name
	 * @return boolean
	 * @throws ObjectNotFoundException
	 * @throws ObjectNotFoundException
	 */
	public boolean existsFolderOrFile(Long parentId, String name) throws ObjectNotFoundException;

	/**
	 * Checks if a folder with the specified name exists for the specified user.
	 *
	 * @param userId the owner of the group
	 * @param name the name of the group
	 * @return true if a group with the same name exists
	 * @throws ObjectNotFoundException
	 */
	public boolean existsGroup(Long userId, String name) throws ObjectNotFoundException;

	/**
	 * Retrieves all tags defined by the specified user
	 *
	 * @param userId
	 * @return Set<String> A set of string tags
	 * @throws ObjectNotFoundException if the user was null
	 */
	public Set<String> getUserTags(final Long userId) throws ObjectNotFoundException;

	/**
	 * Flushes the persistence context
	 */
	public void flush();

	/**
	 * Retrieve the file with the supplied name that is contained
	 * in a folder with the specified ID.
	 *
	 * @param folderId the ID of the parent folder
	 * @param name the name of the file
	 * @return the file found
	 * @throws ObjectNotFoundException if the file or parent folder was not found,
	 * 			with the exception message mentioning the precise problem
	 */
	public FileHeader getFile(Long folderId, String name) throws ObjectNotFoundException;

	/**
	 * Retrieve the folder with the supplied name that is contained
	 * in a folder with the specified ID.
	 *
	 * @param parentId the ID of the parent folder
	 * @param name the name of the folder
	 * @return the folder found
	 * @throws ObjectNotFoundException if the folder or parent was not found,
	 * 			with the exception message mentioning the precise problem
	 */
	public Folder getFolder(Long parentId, String name) throws ObjectNotFoundException;

	/**
	 * Search the system for a user with the specified username.
	 * If no such user is found, the method returns null.
	 *
	 * @param username the username to search for
	 * @return the User object with the specified username
	 */
	public User findUser(String username);

	/**
	 * Search the system for a user with the specified email address.
	 * If no such user is found, the method returns null.
	 */
	public User findUserByEmail(String email);

	/**
	 * Returns a list of users matching specified username
	 *
	 * @param username the email of the User
	 * @return List<User>
	 */
	public List<User> getUsersByUserNameLike(String username);

	/**
	 * Returns a list of users matching specified username or email
	 * @param query
	 * @return List<User>
	 */
	public List<User> getUsersByUserNameOrEmailLike(String query);

	/**
	 * Returns a list of All Shared root folders of a user.
	 *
	 * @param userId the ID of the User
	 * @return the list of shared root folders
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<Folder> getSharedRootFolders(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of all shared files of a user, not contained in a shared folder.
	 *
	 * @param userId the ID of the User
	 * @return the list of shared files
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FileHeader> getSharedFilesNotInSharedFolders(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of all shared files of a user.
	 *
	 * @param userId the ID of the User
	 * @return the list of shared files
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FileHeader> getSharedFiles(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of all shared folders of a user.
	 *
	 * @param userId the ID of the User
	 * @return the list of shared folders
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<Folder> getSharedFolders(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of folders of user with permissions for specified group
	 *
	 * @param userId the ID of the User
	 * @return the list of shared root folders
	 * @param groupId
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<Folder> getFoldersPermittedForGroup(Long userId, Long groupId) throws ObjectNotFoundException;

	/**
	 * Returns a list of users sharing files to specified user
	 *
	 * @param userId the ID of the User
	 * @return the list of users sharing files to selected user
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<User> getUsersSharingFoldersForUser(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of users sharing files to specified user
	 *
	 * @param userId the ID of the User
	 * @return the list of users sharing files to selected user
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<User> getUsersSharingFilesForUser(Long userId) throws ObjectNotFoundException;

	/**
	 * Returns a list of All Shared root folders of a user that calling user has permissions to read them at least.
	 *
	 * @param userId the ID of the User
	 * @param callingUserId
	 * @return the list of shared root folders
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<Folder> getSharedRootFolders(Long userId, Long callingUserId) throws ObjectNotFoundException;

	/**
	 * Returns a list of All Shared files of a user not contained in a shared folder that calling user has permissions.
	 *
	 * @param userId the ID of the User
	 * @param callingUserId
	 * @return the list of shared files
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FileHeader> getSharedFiles(Long userId, Long callingUserId) throws ObjectNotFoundException;

	/**
	 * Search Files
	 * @param userId
	 * @param query
	 * @return list of files that match query
	 * @throws ObjectNotFoundException
	 */
	public List<FileHeader> searchFiles(Long userId, String query) throws ObjectNotFoundException;

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
	 * Loads the file for indexing. That means the file is loaded with the lazy fields needed for inedexing, initialized.
	 * For now only the tags need to be initialized
	 *
	 * @param id
	 * @return the {@link FileHeader} with initialized tags
	 * @throws ObjectNotFoundException when a file with the specified id does not exist
	 */
	public FileHeader getFileForIndexing(Long id) throws ObjectNotFoundException;

	/**
	 * Calculates total file size of user.
	 *
	 * @param userId the ID of the user
	 * @return the aggregate size of all the user's files
	 */
	public Long getFileSize(Long userId);

	/**
	 * Calculates total file count of user.
	 *
	 * @param userId the ID of the user
	 * @return the total number of files in the user's namespace
	 */
	public Long getFileCount(Long userId);

	/**
	 * This method returns all file ids for rebuilding the search index
	 *
	 * @return a list of Long file ids
	 */
	public List<Long> getAllFileIds();

	public FileUploadStatus getFileUploadStatus(Long userId, String fileName);

	/**
	 * Fetch the file body with the specified version number.
	 *
	 * @param fileId the ID of the file header
	 * @param version the version number
	 * @return the file body
	 * @throws ObjectNotFoundException if the file body was not found
	 */
	public FileBody getFileVersion(Long fileId, int version) throws ObjectNotFoundException;

	/**
	 * Update accounting info for given user.
	 * Adds bandwidth used to appropriate time period bucket.
	 * Bucket is created if needed.
	 *
	 * @param user The user to update
	 * @param date Date of transaction
	 * @param bandwidthDiff Bandwidth used; positive for addition,
	 * negative for subtraction (e.g. to rollback)
	 */
	public void updateAccounting(User user, Date date, long bandwidthDiff);

	/**
	 * Retrieves available user classes.
	 *
	 */
	public List<UserClass> getUserClasses();

	/**
	 * Find the invite for the specified invitation code.
	 *
	 * @param code the invitation code
	 * @return the Invitation or null if not found
	 */
	public Invitation findInvite(String code);

	/**
	 * Retrieve the user class for coupon-bearing users.
	 */
	public UserClass findCouponUserClass();

	/**
	 * Gets the user count.
	 *
	 * @param userClass the user class to use or null to retrieve system statistics
	 * @return the user count
	 */
	public Long getUserCount(UserClass userClass);

	/**
	 * Gets the file count.
	 *
	 * @param userClass the user class to use or null to retrieve system statistics
	 * @return the file count
	 */
	public Long getFileCount(UserClass userClass);

	/**
	 * Gets the file size.
	 *
	 * @param userClass the user class to use or null to retrieve system statistics
	 * @return the file size
	 */
	public Long getFileSize(UserClass userClass);

	public List<User> getUsersByLastLogin(Date lastLoginDate);

	public List<User> getUsersByLastLogin(Date lastLoginDate, int firstResult, int maxResult);

	public Long getCountUsersByLastLogin(Date lastLoginDate);

	public List<User> getInactiveUsers();

	public List<FileHeader> searchFileByFilename(String filename);

	public Long getBandwithUsed(UserClass userClass, Date date);

	public List<AccountingInfo> getAccountingInfo(User user);

	public AccountingInfo getAccountingInfo(User user, Date date);

	/**
	 * Returns a list of files of user with permissions for specified group
	 *
	 * @param userId the ID of the User
	 * @return the list of shared root files
	 * @param groupId
	 * @throws ObjectNotFoundException if the user cannot be found
	 */
	public List<FileHeader> getFilesPermittedForGroup(Long userId, Long groupId) throws ObjectNotFoundException;

	/**
	 * @param userId
	 * @return
	 */
	List<FileHeader> getSharingFilesForUser(Long userId);

	/**
	 * @param userId
	 * @return
	 */
	List<Folder> getSharingFoldersForUser(Long userId);

	/**
	 * @param userId
	 * @return
	 */
	List<Group> getGroupsContainingUser(Long userId);

	/**
	 * @param userId
	 * @return
	 */
	List<FileUploadStatus> getUploadStatus(Long userId);

	/**
	 * @param userId
	 * @return
	 */
	int deletePermissionsNotCorrespondingToFilesAndFolders(Long userId);

	/**
	 * 
	 */
	void fixSharedFlagForAllFoldersAndFiles();

}
