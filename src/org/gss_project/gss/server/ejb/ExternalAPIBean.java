/*
 * Copyright 2007, 2008, 2009, 2010  Electronic Business Systems Ltd.
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
package org.gss_project.gss.server.ejb;

import static org.gss_project.gss.server.configuration.GSSConfigurationFactory.getConfiguration;

import org.gss_project.gss.common.exceptions.DuplicateNameException;
import org.gss_project.gss.common.exceptions.GSSIOException;
import org.gss_project.gss.common.exceptions.InsufficientPermissionsException;
import org.gss_project.gss.common.exceptions.InvitationUsedException;
import org.gss_project.gss.common.exceptions.ObjectNotFoundException;
import org.gss_project.gss.common.exceptions.QuotaExceededException;
import org.gss_project.gss.server.domain.AuditInfo;
import org.gss_project.gss.server.domain.FileBody;
import org.gss_project.gss.server.domain.FileHeader;
import org.gss_project.gss.server.domain.FileTag;
import org.gss_project.gss.server.domain.FileUploadStatus;
import org.gss_project.gss.server.domain.Folder;
import org.gss_project.gss.server.domain.Group;
import org.gss_project.gss.server.domain.FileLock;
import org.gss_project.gss.server.domain.Invitation;
import org.gss_project.gss.server.domain.Nonce;
import org.gss_project.gss.server.domain.Permission;
import org.gss_project.gss.server.domain.SearchResult;
import org.gss_project.gss.server.domain.User;
import org.gss_project.gss.server.domain.UserClass;
import org.gss_project.gss.server.domain.UserLogin;
import org.gss_project.gss.server.domain.WebDavNonce;
import org.gss_project.gss.common.dto.StatsDTO;
import org.gss_project.gss.common.dto.UserDTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.PersistenceException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.hibernate.exception.ConstraintViolationException;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;

/**
 * The concrete implementation of the ExternalAPI interface.
 *
 * @author past
 */
@Stateless
public class ExternalAPIBean implements ExternalAPI, ExternalAPIRemote {
	/**
	 * The default MIME type for files without an explicit one.
	 */
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	/**
	 * The size of the buffer that is used to temporarily store chunks of
	 * uploaded files, while storing them to the file repository.
	 */
	private static final int UPLOAD_BUFFER_SIZE = 1024 * 4;

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(ExternalAPIBean.class);

	/**
	 * Injected reference to the GSSDAO data access facade.
	 */
	@EJB
	private GSSDAO dao;


	/**
	 * A cached random number generator for creating unique filenames.
	 */
	private static Random random = new Random();

	/**
	 * Mark the folder and all of its parent folders as modified from the specified user.
	 */
	private void touchParentFolders(Folder folder, User user, Date date) {
		Folder f = folder;
		while (f != null) {
			AuditInfo ai = f.getAuditInfo();
			ai.setModifiedBy(user);
			ai.setModificationDate(date);
			f.setAuditInfo(ai);
			f = f.getParent();
		}
	}

	private Long getRootFolderId(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		return dao.getRootFolderId(userId);
	}
	
	@Override
	public Folder getRootFolder(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		Folder folder = dao.getRootFolder(userId);
		return folder;
	}

	@Override
	public Folder getFolder(final Long userId, final Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		final User user = dao.getEntityById(User.class, userId);
		final Folder folder = dao.getEntityById(Folder.class, folderId);
		// Check permissions
		if (!folder.hasReadPermission(user))
			throw new InsufficientPermissionsException("You don't have the permissions to read this folder");
		return expandFolder(folder);
	}

	@Override
	public User getUser(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		return dao.getEntityById(User.class, userId);
	}

	@Override
	public UserDTO getUserDTO(final Long userId) throws ObjectNotFoundException {
		return getUser(userId).getDTO();
	}

	@Override
	public Group getGroup(final Long groupId) throws ObjectNotFoundException {
		if (groupId == null)
			throw new ObjectNotFoundException("No group specified");
		final Group group = dao.getEntityById(Group.class, groupId);
		return group;
	}

	@Override
	public Group getGroup(Long userId, String name) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (name == null)
			throw new ObjectNotFoundException("No group specified");
		User user = dao.getEntityById(User.class, userId);
		List<Group> groups = user.getGroupsSpecified();
		for (Group group: groups)
			if (group.getName().equals(name))
				return group;
		throw new ObjectNotFoundException("Group " + name + " not found");
	}

	@Override
	public List<Group> getGroups(final Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		final List<Group> groups = dao.getGroups(userId);
		return groups;
	}

	@Override
	public List<FileHeader> getFiles(Long userId, Long folderId, boolean ignoreDeleted)
			throws ObjectNotFoundException, InsufficientPermissionsException {
		// Validate.
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		User user = dao.getEntityById(User.class, userId);
		Folder folder = dao.getEntityById(Folder.class, folderId);
		if (!folder.hasReadPermission(user))
			throw new InsufficientPermissionsException("You don't have the permissions to read this folder");
		List<FileHeader> files = dao.getFiles(folderId, userId, ignoreDeleted);
		return files;
	}

	@Override
	public List<User> getUsers(final Long userId, final Long groupId) throws ObjectNotFoundException {
		// Validate.
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (groupId == null)
			throw new ObjectNotFoundException("No group specified");

		// Do the actual work.
		final List<User> users = dao.getUsers(groupId);
		return users;
	}

	@Override
	public Folder createFolder(Long userId, Long parentId, String name)
			throws DuplicateNameException, ObjectNotFoundException, InsufficientPermissionsException {
		// Validate.
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (StringUtils.isEmpty(name))
			throw new ObjectNotFoundException("New folder name is empty");
		if (parentId == null)
			throw new ObjectNotFoundException("No parent specified");
		if (dao.existsFolderOrFile(parentId, name))
			throw new DuplicateNameException("A folder or file with the name '" +
						name + "' already exists at this level");

		User creator = dao.getEntityById(User.class, userId);

		Folder parent = null;
		try {
			parent = dao.getEntityById(Folder.class, parentId);
		} catch (ObjectNotFoundException onfe) {
			// Supply a more accurate problem description.
			throw new ObjectNotFoundException("Parent folder not found");
		}
		if (!parent.hasWritePermission(creator))
			throw new InsufficientPermissionsException("You don't have the permissions" +
					" to write to this folder");

		// Do the actual work.
		return createFolder(name, parent, creator);
	}

	/**
	 * Create a new folder with the provided name, parent and owner.
	 *
	 * @param name
	 * @param parent
	 * @param creator
	 * @return the new folder
	 */
	private Folder createFolder(String name, Folder parent, User creator) {
		Folder folder = new Folder();
		folder.setName(name);
		if (parent != null) {
			parent.addSubfolder(folder);
			folder.setOwner(parent.getOwner());
		} else
			folder.setOwner(creator);

		Date now = new Date();
		AuditInfo auditInfo = new AuditInfo();
		auditInfo.setCreatedBy(creator);
		auditInfo.setCreationDate(now);
		auditInfo.setModifiedBy(creator);
		auditInfo.setModificationDate(now);
		folder.setAuditInfo(auditInfo);
		touchParentFolders(folder, auditInfo.getModifiedBy(), auditInfo.getModificationDate());

		if (parent != null)
			for (Permission p : parent.getPermissions()) {
				Permission permission = new Permission();
				permission.setGroup(p.getGroup());
				permission.setUser(p.getUser());
				permission.setRead(p.getRead());
				permission.setWrite(p.getWrite());
				permission.setModifyACL(p.getModifyACL());
				folder.addPermission(permission);
			}
		else {
			Permission permission = new Permission();
			permission.setUser(creator);
			permission.setRead(true);
			permission.setWrite(true);
			permission.setModifyACL(true);
			folder.addPermission(permission);
		}

		if(parent != null)
			folder.setReadForAll(parent.isReadForAll());

		dao.create(folder);
		return folder;
	}

	@Override
	public void deleteFolder(final Long userId, final Long folderId) throws InsufficientPermissionsException, ObjectNotFoundException {
		// Validate.
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");

		// Do the actual work.
		final Folder folder = dao.getEntityById(Folder.class, folderId);
		final Folder parent = folder.getParent();
		if (parent == null)
			throw new ObjectNotFoundException("Deleting the root folder is not allowed");
		final User user = dao.getEntityById(User.class, userId);
		if (!folder.hasDeletePermission(user)) {
			logger.info("User " + user.getId() + " cannot delete folder " + folder.getName() + "(" + folder.getId() + ")");
			throw new InsufficientPermissionsException("User " + user.getId() + " cannot delete folder " + folder.getName() + "(" + folder.getId() + ")");
		}
		removeSubfolderFiles(folder);
		parent.removeSubfolder(folder);
		dao.delete(folder);
		touchParentFolders(parent, user, new Date());
	}

	/**
	 * Traverses the folder and deletes all actual files (file system)
	 * regardless of permissions
	 *
	 * @param folder
	 */
	private void removeSubfolderFiles(Folder folder) {
		//remove files for all subfolders
		for (Folder subfolder:folder.getSubfolders())
			removeSubfolderFiles(subfolder);
		//remove this folder's file bodies (actual files)
		for (FileHeader file:folder.getFiles()) {
			for (FileBody body:file.getBodies())
				deleteActualFile(body.getStoredFilePath());
			indexFile(file.getId(), true);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Folder> getSubfolders(Long userId, Long folderId)
			throws ObjectNotFoundException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		User user = dao.getEntityById(User.class, userId);
		Folder folder = dao.getEntityById(Folder.class, folderId);
		if (!folder.hasReadPermission(user))
			throw new InsufficientPermissionsException("You don't have the permissions to read this folder");
		List<Folder> result = new ArrayList<Folder>();
		if (folder.hasReadPermission(user))
			for (Folder f : folder.getSubfolders())
				if (f.hasReadPermission(user) && !f.isDeleted())
					result.add(f);
		return result;
	}

	@Override
	public Folder updateFolder(Long userId, Long folderId, String folderName,
				Boolean readForAll,
				Set<Permission> permissions)
			throws InsufficientPermissionsException, ObjectNotFoundException,
			DuplicateNameException {

		// Validate.
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");

		Folder folder = dao.getEntityById(Folder.class, folderId);
		User user = dao.getEntityById(User.class, userId);
		if (folderName != null && !folder.hasWritePermission(user))
			throw new InsufficientPermissionsException("You don't have the necessary permissions");
		if(permissions != null && !permissions.isEmpty() && !folder.hasModifyACLPermission(user))
			throw new InsufficientPermissionsException("You don't have the necessary permissions");
		// Check permissions for making file public.
		if (readForAll != null && !user.equals(folder.getOwner()))
				throw new InsufficientPermissionsException("Only the owner can make a folder public or not public");

		Folder parent = folder.getParent();
		if (folderName != null) {
			if (parent != null)
				if (!folder.getName().equals(folderName) && dao.existsFolderOrFile(parent.getId(), folderName))
					throw new DuplicateNameException("A folder or file with the name '" + folderName + "' already exists at this level");

			// Do the actual modification.
			folder.setName(folderName);
		}
		if (permissions != null)
			setFolderPermissions(user, folder, permissions);
		if (readForAll != null)
			setFolderReadForAll(user, folder, readForAll);
		folder.getAuditInfo().setModificationDate(new Date());
		folder.getAuditInfo().setModifiedBy(user);
		dao.update(folder);
		touchParentFolders(folder, user, new Date());
		// Re-index the folder contents if it was modified.
		if ((permissions != null && !permissions.isEmpty()) || readForAll != null) {
            indexFolder(folder);
        }

		return folder;
	}

    private void indexFolder(Folder folder) {
        for (FileHeader fh : folder.getFiles())
            indexFile(fh.getId(), false);
        for (Folder f : folder.getSubfolders())
            indexFolder(f);
    }

	@Override
	public void createGroup(final Long userId, final String name) throws ObjectNotFoundException, DuplicateNameException {
		// Validate.
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (StringUtils.isEmpty(name))
			throw new ObjectNotFoundException("New group name is empty");
		if (name.indexOf('/')>=0)
			throw new IllegalArgumentException("Character '/' is not allowed in group name");
		if (dao.existsGroup(userId, name))
			throw new DuplicateNameException("A group with the name '" + name + "' already exists");

		// TODO: Check permissions

		final User owner = dao.getEntityById(User.class, userId);

		// Do the actual work.
		owner.createGroup(name);
	}

	@Override
	public void deleteGroup(final Long userId, final Long groupId) throws ObjectNotFoundException, InsufficientPermissionsException {
		// Validate.
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (groupId == null)
			throw new ObjectNotFoundException("No group specified");

		// Do the actual work.
		final User owner = dao.getEntityById(User.class, userId);
		final Group group = dao.getEntityById(Group.class, groupId);
		final Date now = new Date();
		// Only delete the group if actually owned by the user.
		if (group.getOwner().equals(owner)) {
			List<Folder> folders = dao.getFoldersPermittedForGroup(userId, groupId);
			for (Folder f : folders){
				f.getPermissions().removeAll(group.getPermissions());
				for(FileHeader file : f.getFiles()){
					file.getPermissions().removeAll(group.getPermissions());
				}
			}
			List<FileHeader> files = dao.getFilesPermittedForGroup(userId, groupId);
			for(FileHeader h : files){
				h.getPermissions().removeAll(group.getPermissions());
			}
			owner.removeSpecifiedGroup(group);
			dao.delete(group);
		}
		else throw new InsufficientPermissionsException("You are not the owner of this group");
	}

	@Override
	public FileHeader createFile(Long userId, Long folderId, String name, String mimeType, InputStream stream)
			throws DuplicateNameException, ObjectNotFoundException, GSSIOException,
			InsufficientPermissionsException, QuotaExceededException {
		File file = null;
		try {
			file = uploadFile(stream, userId);
		} catch ( IOException ioe) {
			// Supply a more accurate problem description.
			throw new GSSIOException("Problem creating file",ioe);
		}
		finally{
			try {
				stream.close();
			} catch (IOException e) {
				logger.error("Unable to close InputStream on FileUpload:",e);
			}
		}
		return createFile(userId, folderId, name, mimeType, file.length(), file.getAbsolutePath());
	}

	private void indexFile(Long fileId, boolean delete) {
		Connection qConn = null;
		Session session = null;
		MessageProducer sender = null;
		try {
			Context jndiCtx = new InitialContext();
			ConnectionFactory factory = (QueueConnectionFactory) jndiCtx.lookup("java:/JmsXA");
			Queue queue = (Queue) jndiCtx.lookup("queue/gss-indexingQueue");
			qConn = factory.createConnection();
			session = qConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			sender = session.createProducer(queue);

			MapMessage map = session.createMapMessage();
			map.setObject("id", fileId);
			map.setBoolean("delete", delete);
			sender.send(map);
		}
		catch (NamingException e) {
			logger.error("Index was not updated: ", e);
		}
		catch (JMSException e) {
			logger.error("Index was not updated: ", e);
		}
		finally {
			try {
				if (sender != null)
					sender.close();
				if (session != null)
					session.close();
				if (qConn != null)
					qConn.close();
			}
			catch (JMSException e) {
				logger.warn(e);
			}
		}
	}



	/**
	 * A helper method that generates a unique file path for a stored file. The
	 * files are stored using random hash names that are distributed evenly in
	 * a 2-level tree of subdirectories named after the first two hex characters
	 * in the name. For example, file ab1234cd5769f will be stored in the path
	 * /file-repository-root/a/b/ab1234cd5769f. The directories will be created
	 * if they don't already exist.
	 *
	 * @return a unique new file path
	 */
	private String generateRepositoryFilePath() {
		String filename = Long.toHexString(random.nextLong());
		String fileRepositoryPath = getConfiguration().getString("fileRepositoryPath","/tmp");
		File root = new File(fileRepositoryPath);
		if (!root.exists())
			root.mkdirs();
		File firstFolder = new File(root + File.separator + filename.substring(0, 1));
		if (!firstFolder.exists())
			firstFolder.mkdir();
		File secondFolder = new File(firstFolder + File.separator + filename.substring(1, 2));
		if (!secondFolder.exists())
			secondFolder.mkdir();
		return secondFolder + File.separator + filename;
	}

	@Override
	public void deleteFile(final Long userId, final Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException {
		// Validate.
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");

		// Do the actual work.
		final FileHeader file = dao.getEntityById(FileHeader.class, fileId);
		final Folder parent = file.getFolder();
		if (parent == null)
			throw new ObjectNotFoundException("The specified file has no parent folder");
		final User user = dao.getEntityById(User.class, userId);
		if (!file.hasDeletePermission(user))
			throw new InsufficientPermissionsException("User " + user.getId() + " cannot delete file " + file.getName() + "(" + file.getId() + ")");
		for (final FileBody body : file.getBodies())
			deleteActualFile(body.getStoredFilePath());
		dao.delete(file);
		touchParentFolders(parent, user, new Date());
		indexFile(fileId, true);
	}

	@Override
	public void deleteActualFile(String path) {
		if (path == null)
			return;
		File file = new File(path);
		if (!file.delete())
			logger.error("Could not delete file " + path + " "+file.exists());
	}

	@Override
	public void createTag(final Long userId, final Long fileHeaderId, final String tag) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileHeaderId == null)
			throw new ObjectNotFoundException("No file specified");
		if (StringUtils.isEmpty(tag))
			throw new ObjectNotFoundException("Tag is empty");

		final User user = dao.getEntityById(User.class, userId);
		final FileHeader fh = dao.getEntityById(FileHeader.class, fileHeaderId);
		final Folder parent = fh.getFolder();
		if (parent == null)
			throw new ObjectNotFoundException("The specified file has no parent folder");
		user.addTag(fh, tag);
		touchParentFolders(parent, user, new Date());
	}

	@Override
	public Set<String> getUserTags(final Long userId) throws ObjectNotFoundException {
		return dao.getUserTags(userId);
	}

	@Override
	public void updateFile(Long userId, Long fileId, String name,
				String tagSet, Date modificationDate, Boolean versioned,
				Boolean readForAll,	Set<Permission> permissions)
			throws DuplicateNameException, ObjectNotFoundException,	InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");
		FileHeader file = dao.getEntityById(FileHeader.class, fileId);
		final Folder parent = file.getFolder();
		if (parent == null)
			throw new ObjectNotFoundException("The specified file has no parent folder");

		User user = dao.getEntityById(User.class, userId);
		// Check permissions for modifying the file metadata.
		if ((name != null || tagSet != null || modificationDate != null || versioned != null) && !file.hasWritePermission(user))
			throw new InsufficientPermissionsException("User " + user.getId() +	" cannot update file " + file.getName() + "(" +	file.getId() + ")");
		// Check permissions for making file public.
		if (readForAll != null && !user.equals(file.getOwner()))
				throw new InsufficientPermissionsException("Only the owner can make a file public or not public");
		// Check permissions for modifying the ACL.
		if(permissions != null && !permissions.isEmpty() &&	!file.hasModifyACLPermission(user))
			throw new InsufficientPermissionsException("User " + user.getId() +	" cannot update the permissions on file " +	file.getName() + "(" + file.getId() + ")");

		if (name != null) {
			// Do plain check for file already exists.
			// Extreme concurrency case should be caught by constraint violation later.
			if (dao.existsFolderOrFile(parent.getId(), name)) throw new DuplicateNameException("A file or folder with the name '" + name + "' already exists");
			file.setName(name);
		}

		if (modificationDate != null)
			file.getAuditInfo().setModificationDate(modificationDate);
		else
			file.getAuditInfo().setModificationDate(new Date());
		file.getAuditInfo().setModifiedBy(user);

		List<FileTag> tags = file.getFileTags();
		if (tagSet != null) {
			Iterator<FileTag> i = tags.iterator();
			while (i.hasNext()) {
				FileTag tag = i.next();
				i.remove();
				tag.setFile(null);
				user.removeTag(tag);
				dao.delete(tag);
			}
			dao.flush();
			StringTokenizer st = new StringTokenizer(tagSet, ",");
			while (st.hasMoreTokens())
				new FileTag(user, file, st.nextToken().trim());
		}
		if (versioned != null && !file.isVersioned() == versioned) {
			if (file.isVersioned())
				removeOldVersions(userId, fileId);
			file.setVersioned(versioned);
		}
		if (readForAll != null && user.equals(file.getOwner()))
			file.setReadForAll(readForAll);
		if (permissions != null && !permissions.isEmpty())
			setFilePermissions(file, permissions);

		/*
		 * Force constraint violation to manifest itself here.
		 * This should cover extreme concurrency cases that the simple check
		 * above hasn't caught.
		 */
		try {
			dao.flush();
		}
		catch (EJBTransactionRolledbackException e) {
			Throwable cause = e.getCause();
			if (cause instanceof PersistenceException && cause.getCause() instanceof ConstraintViolationException)
				throw new DuplicateNameException("A file or folder with the name '" + name + "' already exists");
			throw e;
		}

		touchParentFolders(parent, user, new Date());

		// Re-index the file if it was modified.
		if (name != null || tagSet != null || (permissions != null && !permissions.isEmpty()) || readForAll != null)
			indexFile(fileId, false);
	}

	@Override
	public InputStream getFileContents(Long userId, Long fileId)
			throws ObjectNotFoundException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");

		FileHeader header = dao.getEntityById(FileHeader.class, fileId);
		User user = dao.getEntityById(User.class, userId);
		if (!header.hasReadPermission(user)) {
			logger.info("User " + user.getId() + " cannot read file " + header.getName() + "(" + fileId + ")");
			throw new InsufficientPermissionsException("You don't have the necessary permissions");
		}

		File f = new File(header.getCurrentBody().getStoredFilePath());
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			logger.error("Could not locate the contents of file " + f.getAbsolutePath());
			throw new ObjectNotFoundException("The file contents could not be located");
		}
	}

	/* (non-Javadoc)
	 * @see org.gss_project.gss.server.ejb.ExternalAPI#getFileContents(java.lang.Long, java.lang.Long, java.lang.Long)
	 */
	@Override
	public InputStream getFileContents(Long userId, Long fileId, Long bodyId) throws ObjectNotFoundException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");
		if (bodyId == null)
			throw new ObjectNotFoundException("No file specified");

		final FileHeader header = dao.getEntityById(FileHeader.class, fileId);
		final FileBody body = dao.getEntityById(FileBody.class, bodyId);
		final User user = dao.getEntityById(User.class, userId);
		if (!header.hasReadPermission(user)) {
			logger.info("User " + user.getId() + " cannot read file " + header.getName() + "(" + fileId + ")");
			throw new InsufficientPermissionsException("You don't have the necessary permissions");
		}

		File f = new File(body.getStoredFilePath());
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			logger.error("Could not locate the contents of file " + f.getAbsolutePath());
			throw new ObjectNotFoundException("The file contents could not be located");
		}
	}

	@Override
	public FileHeader getFile(Long userId, Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");
		final User user = dao.getEntityById(User.class, userId);
		final FileHeader file = dao.getEntityById(FileHeader.class, fileId);
		if (!file.hasReadPermission(user) && !file.getFolder().hasReadPermission(user))
			throw new InsufficientPermissionsException("You don't have the necessary permissions");
		return file;
	}

	@Override
	public FileBody getFileBody(Long userId, Long fileId, Long bodyId) throws ObjectNotFoundException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");
		User user = dao.getEntityById(User.class, userId);
		FileHeader file = dao.getEntityById(FileHeader.class, fileId);
		if (!file.hasReadPermission(user) && !file.getFolder().hasReadPermission(user))
			throw new InsufficientPermissionsException("You don't have the necessary permissions");
		FileBody body = dao.getEntityById(FileBody.class, bodyId);
		return body;
	}

	@Override
	public Object getResourceAtPath(Long ownerId, String path, boolean ignoreDeleted)
			throws ObjectNotFoundException {
		if (ownerId == null)
			throw new ObjectNotFoundException("No user specified");
		if (StringUtils.isEmpty(path))
			throw new ObjectNotFoundException("No path specified");

		User owner = dao.getEntityById(User.class, ownerId);
		List<String> pathElements = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(path, "/");
		while (st.hasMoreTokens())
			pathElements.add(st.nextToken());
		if (pathElements.size() < 1)
			return getRootFolder(owner.getId());
		// Store the last element, since it requires special handling.
		String lastElement = pathElements.remove(pathElements.size() - 1);
		
		Folder cursor = null;
		Long rootFolderId = getRootFolderId(owner.getId());
		// Traverse and verify the specified folder path.
		for (String pathElement : pathElements) {
			cursor = getFolder(cursor==null ? rootFolderId : cursor.getId(), pathElement);
			if (cursor.isDeleted())
				throw new ObjectNotFoundException("Folder " + cursor.getPath() + " not found");
		}

		// Use the lastElement to retrieve the actual resource.
		Object resource = null;
		try {
			FileHeader file = getFile(cursor==null ? rootFolderId : cursor.getId(), lastElement);
			if (ignoreDeleted && file.isDeleted())
				throw new ObjectNotFoundException("Resource not found");
			resource = file;
		} catch (ObjectNotFoundException e) {
			// Perhaps the requested resource is not a file, so
			// check for folders as well.
			Folder folder = getFolder(cursor==null ? rootFolderId : cursor.getId(), lastElement);
			if (ignoreDeleted && folder.isDeleted())
				throw new ObjectNotFoundException("Resource not found");
			resource = folder;
		}
		return resource;
	}
	
	@Override
	public Object getResourceAtPathOthersShared(Long ownerId, String path, boolean ignoreDeleted, Long userId)
			throws ObjectNotFoundException {
		if (ownerId == null)
			throw new ObjectNotFoundException("No user specified");
		if (StringUtils.isEmpty(path))
			throw new ObjectNotFoundException("No path specified");
		User owner = dao.getEntityById(User.class, ownerId);
		List<String> pathElements = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(path, "/");
		while (st.hasMoreTokens())
			pathElements.add(st.nextToken());
		if (pathElements.size() < 1)
			return null;
		// Store the last element, since it requires special handling.
		String lastElement = pathElements.remove(pathElements.size() - 1);
		List<Folder> folders = getSharedRootFolders(ownerId, userId);
		List<FileHeader> fileHeaders = getSharedFiles(ownerId, userId);
		Folder cursor = null;
		String pathToCheck; 
		if(pathElements.size()==0)
			pathToCheck=lastElement;
		else
			pathToCheck=pathElements.get(0);
		for(Folder f : folders){
			if(f.getName().equals(pathToCheck)){
				cursor=f;
			}
			
		}
		if(cursor==null){
			for(FileHeader f : fileHeaders){
				if(f.getName().equals(pathToCheck)){
					return f;
				}
			}
			return null;
		}
		else if(pathElements.size()==0)
			return cursor;
		Long rootFolderId=-1l;
		// Traverse and verify the specified folder path.
		for(int i=1;i<pathElements.size();i++){
			cursor = getFolder(cursor==null ? rootFolderId : cursor.getId(), pathElements.get(i));
			if (cursor.isDeleted())
				throw new ObjectNotFoundException("Folder " + cursor.getPath() + " not found");
		}

		// Use the lastElement to retrieve the actual resource.
		Object resource = null;
		try {
			FileHeader file = getFile(cursor==null ? rootFolderId : cursor.getId(), lastElement);
			if (ignoreDeleted && file.isDeleted())
				throw new ObjectNotFoundException("Resource not found");
			resource = file;
		} catch (ObjectNotFoundException e) {
			// Perhaps the requested resource is not a file, so
			// check for folders as well.
			Folder folder = getFolder(cursor==null ? rootFolderId : cursor.getId(), lastElement);
			if (ignoreDeleted && folder.isDeleted())
				throw new ObjectNotFoundException("Resource not found");
			resource = folder;
		}
		return resource;
	}

	/**
	 * Retrieve a file for the specified user that has the specified name and
	 * its parent folder has id equal to folderId.
	 *
	 * @param folderId the ID of the parent folder
	 * @param name the name of the requested file
	 * @return the file found
	 * @throws ObjectNotFoundException if the specified folder or file was not
	 *             found, with the exception message mentioning the precise
	 *             problem
	 */
	private FileHeader getFile(Long folderId, String name) throws ObjectNotFoundException {
		if (folderId == null)
			throw new ObjectNotFoundException("No parent folder specified");
		if (StringUtils.isEmpty(name))
			throw new ObjectNotFoundException("No file specified");

		FileHeader file = dao.getFile(folderId, name);
		return file;
	}

	/**
	 * Retrieve a folder for the specified user that has the specified name and
	 * its parent folder has id equal to parentId.
	 *
	 * @param parentId the ID of the parent folder
	 * @param name the name of the requested folder
	 * @return the folder found
	 * @throws ObjectNotFoundException if the specified folder or parent was not
	 *             found, with the exception message mentioning the precise
	 *             problem
	 */
	private Folder getFolder(Long parentId, String name) throws ObjectNotFoundException {
		if (parentId == null)
			throw new ObjectNotFoundException("No parent folder specified");
		if (StringUtils.isEmpty(name))
			throw new ObjectNotFoundException("No folder specified");

		Folder folder = dao.getFolder(parentId, name);
		return folder;
	}

	private FileHeader updateFileContents(Long userId, Long fileId, String mimeType, InputStream resourceInputStream) throws ObjectNotFoundException, GSSIOException, InsufficientPermissionsException, QuotaExceededException {
		File file = null;
		try {
			file = uploadFile(resourceInputStream, userId);
		} catch ( IOException ioe) {
			// Supply a more accurate problem description.
			throw new GSSIOException("Problem creating file",ioe);
		}
		return updateFileContents(userId, fileId, mimeType, file.length(), file.getAbsolutePath());
	}

	@Override
	public void copyFile(Long userId, Long fileId, String dest) throws ObjectNotFoundException, DuplicateNameException, GSSIOException, InsufficientPermissionsException, QuotaExceededException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");
		if (StringUtils.isEmpty(dest))
			throw new ObjectNotFoundException("No destination specified");

		Object destination = getResourceAtPath(userId, getParentPath(dest), true);
		if (!(destination instanceof Folder))
			throw new ObjectNotFoundException("Destination parent folder not found");
		Folder parent = (Folder) destination;
		copyFile(userId, fileId, parent.getId(), getLastElement(dest));
	}

	@Override
	public void copyFileToPath(Long userId, Long ownerId, Long fileId, String dest) throws ObjectNotFoundException, DuplicateNameException, GSSIOException, InsufficientPermissionsException, QuotaExceededException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (ownerId == null)
			throw new ObjectNotFoundException("No owner specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");
		if (StringUtils.isEmpty(dest))
			throw new ObjectNotFoundException("No destination specified");

		Object destination = getResourceAtPath(ownerId, getParentPath(dest), true);
		if (!(destination instanceof Folder))
			throw new ObjectNotFoundException("Destination parent folder not found");
		Folder parent = (Folder) destination;
		copyFile(userId, fileId, parent.getId(), getLastElement(dest));
	}

	@Override
	public void copyFile(Long userId, Long fileId, Long destId, String destName) throws ObjectNotFoundException, DuplicateNameException, GSSIOException, InsufficientPermissionsException, QuotaExceededException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");
		if (destId == null)
			throw new ObjectNotFoundException("No destination specified");
		if (StringUtils.isEmpty(destName))
			throw new ObjectNotFoundException("No destination file name specified");

		FileHeader file = dao.getEntityById(FileHeader.class, fileId);
		Folder destination = dao.getEntityById(Folder.class, destId);
		User user = dao.getEntityById(User.class, userId);
		if (!file.hasReadPermission(user) || !destination.hasWritePermission(user))
			throw new InsufficientPermissionsException("You don't have the necessary permissions");
		boolean versioned = file.isVersioned();
		int versionsNumber = file.getBodies().size();
		FileBody oldestBody = file.getBodies().get(0);
		assert oldestBody != null;
		File contents = new File(oldestBody.getStoredFilePath());
		try {
			createFile(user.getId(), destination.getId(), destName, oldestBody.getMimeType(), new FileInputStream(contents));
			FileHeader copiedFile = dao.getFile(destination.getId(), destName);
			copiedFile.setVersioned(versioned);
			dao.flush();
			if (versionsNumber > 1)
				for (int i = 1; i < versionsNumber; i++) {
					FileBody body = file.getBodies().get(i);
					assert body != null;
					contents = new File(body.getStoredFilePath());
					updateFileContents(user.getId(), copiedFile.getId(), body.getMimeType(), new FileInputStream(contents));
				}
			List<FileTag> tags = file.getFileTags();
			for (FileTag tag : tags)
				createTag(userId, copiedFile.getId(), tag.getTag());

		} catch (FileNotFoundException e) {
			throw new ObjectNotFoundException("File contents not found for file " + contents.getAbsolutePath());
		}

	}

	@Override
	public void copyFolder(Long userId, Long folderId, String dest) throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		if (StringUtils.isEmpty(dest))
			throw new ObjectNotFoundException("No destination specified");

		Object destination = getResourceAtPath(userId, getParentPath(dest), true);
		if (!(destination instanceof Folder))
			throw new ObjectNotFoundException("Destination folder not found");
		Folder parent = (Folder) destination;
		copyFolder(userId, folderId, parent.getId(), getLastElement(dest));
	}

	@Override
	public void copyFolder(Long userId, Long folderId, Long destId, String destName) throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		if (destId == null)
			throw new ObjectNotFoundException("No destination specified");
		if (StringUtils.isEmpty(destName))
			throw new ObjectNotFoundException("No destination folder name specified");
		Folder folder = dao.getEntityById(Folder.class, folderId);
		Folder destination = dao.getEntityById(Folder.class, destId);
		User user = dao.getEntityById(User.class, userId);
		if (!destination.hasWritePermission(user) || !folder.hasReadPermission(user))
			throw new InsufficientPermissionsException("You don't have the necessary permissions");
		createFolder(user.getId(), destination.getId(), destName);
	}

	@Override
	public void copyFolderStructureToPath(Long userId, Long ownerId, Long folderId, String dest) throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, GSSIOException, QuotaExceededException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (ownerId == null)
			throw new ObjectNotFoundException("No owner specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		if (StringUtils.isEmpty(dest))
			throw new ObjectNotFoundException("No destination specified");

		Object destination = getResourceAtPath(ownerId, getParentPath(dest), true);
		if (!(destination instanceof Folder))
			throw new ObjectNotFoundException("Destination folder not found");
		Folder parent = (Folder) destination;
		copyFolderStructure(userId, folderId, parent.getId(), getLastElement(dest));
	}

	@Override
	public void copyFolderStructure(Long userId, Long folderId, Long destId, String destName) throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException, GSSIOException, QuotaExceededException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		if (destId == null)
			throw new ObjectNotFoundException("No destination specified");
		if (StringUtils.isEmpty(destName))
			throw new ObjectNotFoundException("No destination folder name specified");

		Folder folder = dao.getEntityById(Folder.class, folderId);
		Folder destination = dao.getEntityById(Folder.class, destId);
		final User user = dao.getEntityById(User.class, userId);
		// XXX: quick fix need to copy only visible items to user (Source
		// for bugs)
		if (!folder.getOwner().getId().equals(userId) && !folder.hasReadPermission(user))
			return;
		if(folder.isDeleted())//do not copy trashed folder and contents
			return;
		if (!destination.hasWritePermission(user) || !folder.hasReadPermission(user))
			throw new InsufficientPermissionsException("You don't have the necessary permissions");
		createFolder(user.getId(), destination.getId(), destName);
		Folder createdFolder = dao.getFolder(destination.getId(), destName);
		List<FileHeader> files = folder.getFiles();
		if (files != null)
			for (FileHeader file : files)
				if(!file.isDeleted())
					copyFile(userId, file.getId(), createdFolder.getId(), file.getName());
		List<Folder> subFolders = folder.getSubfolders();
		if (subFolders != null)
			for (Folder sub : subFolders)
				if(!sub.getId().equals(createdFolder.getId()))
					copyFolderStructure(userId, sub.getId(), createdFolder.getId(), sub.getName());

	}

	/**
	 * For a provided path, remove the last element and return the rest, that is
	 * the path of the parent folder.
	 *
	 * @param path the specified path
	 * @return the path of the parent folder
	 * @throws ObjectNotFoundException if the provided string contains no path
	 *             delimiters
	 */
	private String getParentPath(String path) throws ObjectNotFoundException {
		int lastDelimiter = path.lastIndexOf('/');
		if (lastDelimiter == 0)
			return "/";
		if (lastDelimiter == -1)
			// No path found.
			throw new ObjectNotFoundException("There is no parent in the path: " + path);
		else if (lastDelimiter < path.length() - 1)
			// Return the part before the delimiter.
			return path.substring(0, lastDelimiter);
		else {
			// Remove the trailing delimiter and then recurse.
			String strippedTrail = path.substring(0, lastDelimiter);
			return getParentPath(strippedTrail);
		}
	}

	/**
	 * Get the last element in a path that denotes the file or folder name.
	 *
	 * @param path the provided path
	 * @return the last element in the path
	 */
	private String getLastElement(String path) {
		int lastDelimiter = path.lastIndexOf('/');
		if (lastDelimiter == -1)
			// No path found.
			return path;
		else if (lastDelimiter < path.length() - 1)
			// Return the part after the delimiter.
			return path.substring(lastDelimiter + 1);
		else {
			// Remove the trailing delimiter and then recurse.
			String strippedTrail = path.substring(0, lastDelimiter);
			return getLastElement(strippedTrail);
		}
	}

	@Override
	public void moveFileToTrash(Long userId, Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");

		// Do the actual work.
		FileHeader file = dao.getEntityById(FileHeader.class, fileId);
		Folder parent = file.getFolder();
		if (parent == null)
			throw new ObjectNotFoundException("The specified file has no parent folder");
		User user = dao.getEntityById(User.class, userId);
        trashFile(user, file);
        touchParentFolders(parent, user, new Date());
	}

    private void trashFile(User user, FileHeader file) throws InsufficientPermissionsException {
        if (!file.hasDeletePermission(user))
            throw new InsufficientPermissionsException("User " + user.getId() + " cannot delete file " + file.getName() + "(" + file.getId() + ")");

        file.setDeleted(true);
    }

	@Override
	public void moveFileToPath(Long userId, Long ownerId, Long fileId, String dest) throws ObjectNotFoundException, InsufficientPermissionsException, QuotaExceededException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (ownerId == null)
			throw new ObjectNotFoundException("No owner specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");
		if (StringUtils.isEmpty(dest))
			throw new ObjectNotFoundException("No destination specified");

		Object destination = getResourceAtPath(ownerId, getParentPath(dest), true);
		if (!(destination instanceof Folder))
			throw new ObjectNotFoundException("Destination parent folder not found");
		Folder parent = (Folder) destination;
		moveFile(userId, fileId, parent.getId(), getLastElement(dest));
	}

	@Override
	public void moveFile(Long userId, Long fileId, Long destId, String destName) throws InsufficientPermissionsException, ObjectNotFoundException, QuotaExceededException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");
		if (destId == null)
			throw new ObjectNotFoundException("No destination specified");
		if (StringUtils.isEmpty(destName))
			throw new ObjectNotFoundException("No destination file name specified");

		FileHeader file = dao.getEntityById(FileHeader.class, fileId);
		Folder source = file.getFolder();
		Folder destination = dao.getEntityById(Folder.class, destId);

		User owner = dao.getEntityById(User.class, userId);
		if (!file.hasDeletePermission(owner) || !destination.hasWritePermission(owner))
			throw new InsufficientPermissionsException("User " + owner.getId() + " cannot move file " + file.getName() + "(" + file.getId() + ")");

		// if the destination folder belongs to another user:
		if (!file.getOwner().equals(destination.getOwner())) {
			// (a) check if the destination quota allows the move
			if(getQuotaLeft(destination.getOwner().getId()) < file.getTotalSize())
				throw new QuotaExceededException("Not enough free space available");
			User newOwner = destination.getOwner();
			// (b) if quota OK, change the owner of the file
			file.setOwner(newOwner);
			// if the file has no permission for the new owner, add it
			Permission ownerPermission = null;
			for (final Permission p : file.getPermissions())
				if (p.getUser() != null)
					if (p.getUser().equals(newOwner)) {
						ownerPermission = p;
						break;
					}
			if (ownerPermission == null) {
				ownerPermission = new Permission();
				ownerPermission.setUser(newOwner);
				file.addPermission(ownerPermission);
			}
			ownerPermission.setRead(true);
			ownerPermission.setWrite(true);
			ownerPermission.setModifyACL(true);
		}
		// move the file to the destination folder
		file.setFolder(destination);
		touchParentFolders(source, owner, new Date());
		touchParentFolders(destination, owner, new Date());
	}

	@Override
	public void moveFolderToPath(Long userId, Long ownerId, Long folderId, String dest) throws ObjectNotFoundException, InsufficientPermissionsException, QuotaExceededException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (ownerId == null)
			throw new ObjectNotFoundException("No owner specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		if (StringUtils.isEmpty(dest))
			throw new ObjectNotFoundException("No destination specified");

		Object destination = getResourceAtPath(ownerId, getParentPath(dest), true);
		if (!(destination instanceof Folder))
			throw new ObjectNotFoundException("Destination parent folder not found");
		Folder parent = (Folder) destination;
		moveFolder(userId, folderId, parent.getId(), getLastElement(dest));
	}

	@Override
	public void moveFolder(Long userId, Long folderId, Long destId, String destName)
			throws ObjectNotFoundException, InsufficientPermissionsException,
			QuotaExceededException {
		Folder source = dao.getEntityById(Folder.class, folderId);
		Folder destination = dao.getEntityById(Folder.class, destId);
		User user = dao.getEntityById(User.class, userId);
		User sourceOwner = source.getOwner();
		User destinationOwner = destination.getOwner();
		// Do not move trashed folders and contents.
		if (source.isDeleted())
			return;
		// Check permissions.
		if (!destination.hasWritePermission(user)
				|| !source.hasReadPermission(user)
				|| !source.hasWritePermission(user))
			throw new InsufficientPermissionsException("You don't have the " +
					"necessary permissions");
		// Use the same timestamp for all subsequent modifications to make
		// changes appear simultaneous.
		Date now = new Date();
		// If source and destination are not in the same user's namespace,
		// change owners and check quota.
		if (!sourceOwner.equals(destinationOwner)) {
			changeOwner(source, destinationOwner, user, now);
			if (getQuotaLeft(destinationOwner.getId()) < 0)
				throw new QuotaExceededException("Not enough free space " +
						"available in destination folder");
		}
		// Perform the move.
		Folder oldParent = source.getParent();
		oldParent.removeSubfolder(source);
		destination.addSubfolder(source);
		// Mark the former parent and destination trees upwards as modified.
		touchParentFolders(oldParent, user, now);
		touchParentFolders(source, user, now);
	}

	/**
	 * Recursively change the owner of the specified folder and all of its
	 * contents to the specified owner. Also mark them all as modified with the
	 * specified modifier and modificationDate.
	 */
	private void changeOwner(Folder folder, User owner, User modifier, Date modificationDate) {
		for (FileHeader file: folder.getFiles()) {
			file.setOwner(owner);
			file.getAuditInfo().setModificationDate(modificationDate);
			file.getAuditInfo().setModifiedBy(modifier);
		}
		for (Folder sub: folder.getSubfolders())
			changeOwner(sub, owner, modifier, modificationDate);
		folder.setOwner(owner);
		folder.getAuditInfo().setModificationDate(modificationDate);
		folder.getAuditInfo().setModifiedBy(modifier);
	}

	@Override
	public List<FileHeader> getDeletedFiles(Long userId) throws ObjectNotFoundException {
		// Validate.
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");

		// Do the actual work.
		final List<FileHeader> files = dao.getDeletedFiles(userId);
		return files;
	}

	@Override
	public void removeFileFromTrash(Long userId, Long fileId)
			throws ObjectNotFoundException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");

		// Do the actual work.
		FileHeader file = dao.getEntityById(FileHeader.class, fileId);
		Folder parent = file.getFolder();
		if (parent == null)
			throw new ObjectNotFoundException("The specified file has no parent folder");
		User user = dao.getEntityById(User.class, userId);
        untrashFile(user, file);
		touchParentFolders(parent, user, new Date());
	}

    private void untrashFile(User user, FileHeader file) throws InsufficientPermissionsException {
        if (!file.hasDeletePermission(user))
            throw new InsufficientPermissionsException("User " + user.getUsername() +
                        " cannot restore file " + file.getName());

        file.setDeleted(false);
    }

	@Override
	public void moveFolderToTrash(Long userId, Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException {
        if (userId == null)
            throw new ObjectNotFoundException("No user specified");
        if (folderId == null)
            throw new ObjectNotFoundException("No folder specified");
        Folder folder = dao.getEntityById(Folder.class, folderId);
        User user = dao.getEntityById(User.class, userId);
        trashFolder(user, folder);
        touchParentFolders(folder, user, new Date());
	}

    private void trashFolder(User user, Folder folder) throws ObjectNotFoundException, InsufficientPermissionsException {
        if (!folder.hasDeletePermission(user))
            throw new InsufficientPermissionsException("You don't have the necessary permissions");
        folder.setDeleted(true);
        for (FileHeader file : folder.getFiles())
            trashFile(user, file);
        for (Folder subFolder : folder.getSubfolders())
            trashFolder(user, subFolder);
    }

	@Override
	public void removeFolderFromTrash(Long userId, Long folderId)
			throws ObjectNotFoundException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		Folder folder = dao.getEntityById(Folder.class, folderId);
		User user = dao.getEntityById(User.class, userId);
        untrashFolder(user, folder);
		touchParentFolders(folder, user, new Date());
	}

    private void untrashFolder(User user, Folder folder) throws ObjectNotFoundException, InsufficientPermissionsException {
        if (!folder.hasDeletePermission(user))
            throw new InsufficientPermissionsException("User " + user.getUsername() +
                        " cannot restore folder " + folder.getName());
        folder.setDeleted(false);
        for (FileHeader file : folder.getFiles())
            untrashFile(user, file);
        for (Folder subFolder : folder.getSubfolders())
            untrashFolder(user, subFolder);
    }

	@Override
	public List<Folder> getDeletedRootFolders(Long userId) throws ObjectNotFoundException {
		List<Folder> folders = dao.getDeletedRootFolders(userId);
		return folders;
	}

	@Override
	public void emptyTrash(Long userId) throws ObjectNotFoundException, InsufficientPermissionsException {
		List<Folder> deletedRootFolders = getDeletedRootFolders(userId);
		for (Folder folder : deletedRootFolders)
			deleteFolder(userId, folder.getId());
		List<FileHeader> deletedFiles = getDeletedFiles(userId);
		for (FileHeader file : deletedFiles)
			deleteFile(userId, file.getId());
	}

	@Override
	public void restoreTrash(Long userId) throws ObjectNotFoundException, InsufficientPermissionsException {
		List<Folder> deletedRootFolders = getDeletedRootFolders(userId);
		for (Folder folder : deletedRootFolders)
			removeFolderFromTrash(userId, folder.getId());
		List<FileHeader> deletedFiles = getDeletedFiles(userId);
		for (FileHeader file : deletedFiles)
			removeFileFromTrash(userId, file.getId());
	}

	@Override
	public User createUser(String username, String name, String mail,
				String idp, String idpid, String homeOrg) throws ObjectNotFoundException {
		if (username == null)
			throw new ObjectNotFoundException("No username specified");
		if (name == null)
			throw new ObjectNotFoundException("No name specified");

		User user = new User();
		user.setUsername(username);
		user.setName(name);
		user.setEmail(mail);
		user.setIdentityProvider(idp);
		user.setIdentityProviderId(idpid);
		Date now = new Date();
		AuditInfo auditInfo = new AuditInfo();
		auditInfo.setCreationDate(now);
		auditInfo.setModificationDate(now);
		user.setAuditInfo(auditInfo);
		user.setActive(true);
		user.generateAuthToken();
		user.generateWebDAVPassword();
		user.setUserClass(getDefaultUserClass());
        user.setHomeOrganization(homeOrg);
		dao.create(user);
		// Make sure we get an ID in the user object.
		dao.flush();
		// Create the root folder for the user.
		createFolder(user.getName(), null, user);
		return user;
	}

	/**
	 * Get the default user class, which is the one with the lowest quota.
	 */
	private UserClass getDefaultUserClass() {
		return getUserClasses().get(0);
	}

	@Override
	public List<UserClass> getUserClasses() {
		List<UserClass> classes = dao.getUserClasses();
		// Create a default user class for first-time use. Afterwards, the
		// admin should modify or add to the userclass table.
		if (classes.size() == 0) {
			UserClass defaultClass = new UserClass();
			defaultClass.setName("default");
			Long defaultQuota = getConfiguration().getLong("quota", new Long(52428800L));
			defaultClass.setQuota(defaultQuota);
			dao.create(defaultClass);
			classes.add(defaultClass);
		}
		return classes;
	}

	@Override
	public User findUserByEmail(String email) {
		return dao.findUserByEmail(email);
	}

	@Override
	public void updateUser(User user) {
		dao.update(user);
	}

	@Override
	public User findUser(String username) {
		if (username == null)
			return null;
		return dao.findUser(username);
	}

	@Override
	public User updateUserToken(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		User user = dao.getEntityById(User.class, userId);
		user.generateAuthToken();
		return user;
	}

	@Override
	public Set<Permission> getFolderPermissions(Long userId, Long folderId) throws ObjectNotFoundException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		User user = dao.getEntityById(User.class, userId);
		Folder folder = dao.getEntityById(Folder.class, folderId);
		if(!folder.hasReadPermission(user))
			throw new InsufficientPermissionsException("You don't have the necessary permissions");
		Set<Permission> perms = folder.getPermissions();
		Set<Permission> result = new LinkedHashSet<Permission>();
		for (Permission perm : perms)
			if (perm.getUser() != null && perm.getUser().getId().equals(folder.getOwner().getId()))
				result.add(perm);
		for (Permission perm : perms)
			if (perm.getUser() != null && perm.getUser().getId().equals(folder.getOwner().getId())) {
			} else
				result.add(perm);
		return result;

	}

	/**
	 * Set the provided permissions as the new permissions of the specified
	 * folder.
	 *
	 * @param user
	 * @param folder
	 * @param permissions
	 * @throws ObjectNotFoundException
	 * @throws InsufficientPermissionsException
	 */
	private void setFolderPermissions(User user, Folder folder, Set<Permission> permissions) throws ObjectNotFoundException, InsufficientPermissionsException {
		if (permissions != null && !permissions.isEmpty()) {
			User owner = folder.getOwner();
			Permission ownerPerm = null;
			for (Permission perm : permissions)
				if (perm.getUser() != null && perm.getUser().getId().equals(owner.getId())) {
					ownerPerm = perm;
					break;
				}
			if (ownerPerm == null || !ownerPerm.hasRead() || !ownerPerm.hasWrite() || !ownerPerm.hasModifyACL())
				throw new InsufficientPermissionsException("Can't remove permissions from owner");
			// Delete previous entries
			for (Permission perm: folder.getPermissions())
				dao.delete(perm);
			folder.getPermissions().clear();
			for (Permission p : permissions) {
				// Skip 'empty' permission entries.
				if (!p.getRead() && !p.getWrite() && !p.getModifyACL()) continue;
				folder.addPermission(getPermission(p));
			}
			dao.update(folder);
			for (FileHeader file : folder.getFiles()) {
				setFilePermissions(file, permissions);
				Date now = new Date();
				file.getAuditInfo().setModificationDate(now);
				file.getAuditInfo().setModifiedBy(user);
			}
			for (Folder sub : folder.getSubfolders())
				setFolderPermissions(user, sub, permissions);
		}
	}

	private Permission getPermission(Permission perm) throws ObjectNotFoundException {
		Permission res = new Permission();
		if (perm.getGroup() != null)
			res.setGroup(dao.getEntityById(Group.class, perm.getGroup().getId()));
		else if (perm.getUser() != null)
			if (perm.getUser().getId() == null)
				res.setUser(dao.getUser(perm.getUser().getUsername()));
			else
				res.setUser(dao.getEntityById(User.class, perm.getUser().getId()));
		res.setRead(perm.hasRead());
		res.setWrite(perm.hasWrite());
		res.setModifyACL(perm.hasModifyACL());
		return res;
	}

	/* (non-Javadoc)
	 * @see org.gss_project.gss.server.ejb.ExternalAPI#getUsersByUserNameLike(java.lang.String)
	 */
	@Override
	public List<User> getUsersByUserNameLike(String username) {
		List<User> users = dao.getUsersByUserNameLike(username);
		return users;

	}

	@Override
	public void addUserToGroup(Long userId, Long groupId, Long userToAddId) throws ObjectNotFoundException, DuplicateNameException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (groupId == null)
			throw new ObjectNotFoundException("No group specified");
		if (userToAddId == null)
			throw new ObjectNotFoundException("No user to add specified");
		User user = dao.getEntityById(User.class, userId);
		Group group = dao.getEntityById(Group.class, groupId);
		if (!group.getOwner().equals(user))
			throw new InsufficientPermissionsException();
		User userToAdd = dao.getEntityById(User.class, userToAddId);
		if (group.contains(userToAdd))
			throw new DuplicateNameException("User already exists in group");
		group.getMembers().add(userToAdd);
		dao.update(group);

	}

	@Override
	public void invalidateUserToken(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		User user = dao.getEntityById(User.class, userId);
		user.invalidateAuthToken();
		return;
	}

	@Override
	public List<Folder> getSharedRootFolders(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		List<Folder> folders = dao.getSharedRootFolders(userId);
		List<Folder> result = new ArrayList<Folder>();
		for (Folder f : folders) {
			Folder lf = f;
			lf.setSubfolders(getSharedSubfolders(userId, f.getId()));
			result.add(lf);
		}
		return result;
	}

	@Override
	public void removeMemberFromGroup(Long userId, Long groupId, Long memberId) throws ObjectNotFoundException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (groupId == null)
			throw new ObjectNotFoundException("No group specified");
		if (memberId == null)
			throw new ObjectNotFoundException("No member specified");
		User owner = dao.getEntityById(User.class, userId);
		Group group = dao.getEntityById(Group.class, groupId);
		User member = dao.getEntityById(User.class, memberId);
		if (!group.getOwner().equals(owner))
			throw new InsufficientPermissionsException("User is not the owner of the group");
		group.removeMemberFromGroup(member);
		dao.update(group);

	}

	@Override
	public List<User> getUsersSharingFoldersForUser(Long userId) throws ObjectNotFoundException {
		List<User> users = dao.getUsersSharingFoldersForUser(userId);
		List<User> usersFiles = dao.getUsersSharingFilesForUser(userId);
		List<User> result = new ArrayList<User>();
		for (User u : users)
			result.add(u);
		for(User fu : usersFiles)
			if(!users.contains(fu))
				result.add(fu);
		return result;
	}

	@Override
	public Set<Permission> getFilePermissions(Long userId, Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No folder specified");
		User user = dao.getEntityById(User.class, userId);
		FileHeader folder = dao.getEntityById(FileHeader.class, fileId);
		if(!folder.hasReadPermission(user))
			throw new InsufficientPermissionsException("You don't have the necessary permissions");
		Set<Permission> perms = folder.getPermissions();
		Set<Permission> result = new LinkedHashSet<Permission>();
		for (Permission perm : perms)
			if (perm.getUser() != null && perm.getUser().getId().equals(folder.getOwner().getId()))
				result.add(perm);
		for (Permission perm : perms)
			if (perm.getUser() != null && perm.getUser().getId().equals(folder.getOwner().getId())) {
			} else
				result.add(perm);
		return result;
	}

	/**
	 * Set the provided permissions as the new permissions of the specified
	 * file. This method sets the modification date/user attributes to the
	 * current values as a side effect.
	 *
	 * @param file
	 * @param permissions
	 * @throws ObjectNotFoundException
	 * @throws InsufficientPermissionsException
	 */
	private void setFilePermissions(FileHeader file,
				Set<Permission> permissions)
			throws ObjectNotFoundException, InsufficientPermissionsException {
		if (permissions != null && !permissions.isEmpty()) {
			Permission ownerPerm = null;
			for (Permission perm : permissions)
				if (perm.getUser() != null && perm.getUser().getId().equals(file.getOwner().getId())) {
					ownerPerm = perm;
					break;
				}
			if (ownerPerm == null || !ownerPerm.hasRead() || !ownerPerm.hasWrite() || !ownerPerm.hasModifyACL())
				throw new InsufficientPermissionsException("Can't remove permissions from owner");
			// Delete previous entries.
			for (Permission perm: file.getPermissions())
				dao.delete(perm);
			file.getPermissions().clear();
			for (Permission perm : permissions) {
				// Skip 'empty' permission entries.
				if (!perm.getRead() && !perm.getWrite() && !perm.getModifyACL()) continue;
				file.addPermission(getPermission(perm));
			}
			dao.flush();
		}
	}

	@Override
	public List<FileHeader> getSharedFilesNotInSharedFolders(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		List<FileHeader> files = dao.getSharedFilesNotInSharedFolders(userId);
		return files;
	}

	@Override
	public List<FileHeader> getSharedFiles(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		List<FileHeader> files = dao.getSharedFiles(userId);
		return files;
	}

	@Override
	public List<Folder> getSharedFolders(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		List<Folder> folders = dao.getSharedFolders(userId);
		return folders;
	}

	@Override
	public List<FileHeader> getSharedFiles(Long ownerId, Long callingUserId) throws ObjectNotFoundException {
		if (ownerId == null)
			throw new ObjectNotFoundException("No owner specified");
		if (callingUserId == null)
			throw new ObjectNotFoundException("No calling user specified");
		List<FileHeader> folders = dao.getSharedFiles(ownerId, callingUserId);
		return folders;
	}

	@Override
	public List<Folder> getSharedRootFolders(Long ownerId, Long callingUserId) throws ObjectNotFoundException {
		if (ownerId == null)
			throw new ObjectNotFoundException("No owner specified");
		if (callingUserId == null)
			throw new ObjectNotFoundException("No calling user specified");
		List<Folder> folders = dao.getSharedRootFolders(ownerId, callingUserId);
		List<Folder> result = new ArrayList<Folder>();
		for (Folder f : folders) {
			Folder lf = f;
			lf.setSubfolders(getSharedSubfolders(ownerId, callingUserId, f.getId()));
			result.add(lf);
		}
		return result;

	}

	@Override
	public List<Folder> getSharedSubfolders(Long userId, Long folderId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		User user = dao.getEntityById(User.class, userId);
		Folder folder = dao.getEntityById(Folder.class, folderId);
		List<Folder> result = new ArrayList<Folder>();
		if (folder.isShared(user) || folder.isReadForAll())
			for (Folder f : folder.getSubfolders())
				if ((f.isShared(user) || f.isReadForAll()) && !f.isDeleted())
					result.add(f);
		return result;
	}

	@Override
	public List<Folder> getSharedSubfolders(Long userId, Long callingUserId, Long folderId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (callingUserId == null)
			throw new ObjectNotFoundException("No user specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		User user = dao.getEntityById(User.class, callingUserId);
		Folder folder = dao.getEntityById(Folder.class, folderId);
		List<Folder> result = new ArrayList<Folder>();
		if (folder.isSharedForOtherUser(user))
			for (Folder f : folder.getSubfolders())
				if (f.isSharedForOtherUser(user) && !f.isDeleted()){
					Folder lf = f;
					lf.setSubfolders(getSharedSubfolders(userId, callingUserId, lf.getId()));
					result.add(lf);
				}
		return result;

	}

	/**
	 * Performs the search on the solr server and returns the results
	 *
	 *
     * @param userId
     * @param query
     * @param luceneQuery
     * @return a List of FileHeader objects
	 */
	public SearchResult search(Long userId, String query, int start, boolean luceneQuery) throws ObjectNotFoundException {
        if (userId == null)
            throw new ObjectNotFoundException("No user specified");
        if (query == null)
            throw new ObjectNotFoundException("No query specified");

        final int maxRows = getConfiguration().getInt("searchResultsPerPage", 25);
		List<FileHeader> result = new ArrayList<FileHeader>();
		try {
			CommonsHttpSolrServer solr = new CommonsHttpSolrServer(getConfiguration().getString("solr.url"));
            List<Group> groups = dao.getGroupsContainingUser(userId);
            String escapedQuery = luceneQuery ? normalizeSearchQuery(query) : escapeCharacters(normalizeSearchQuery(query));
            String constructedQuery =  escapedQuery + " AND (public: true OR ureaders: " + userId;
            if (!groups.isEmpty()) {
                constructedQuery += " OR (";
                for (int i=0; i<groups.size(); i++) {
                    Group g = groups.get(i);
                    constructedQuery += "greaders :" + g.getId();
                    if (i < groups.size() - 1)
                        constructedQuery += " OR ";
                }
                constructedQuery += ")";
            }
            constructedQuery += ")";
			SolrQuery solrQuery = new SolrQuery(constructedQuery);
            solrQuery.setRows(maxRows);
            if(start > 0)
            	solrQuery.setStart(start);
			QueryResponse response = solr.query(solrQuery);
			SolrDocumentList results = response.getResults();
            if (results.getNumFound() > maxRows && start < 0) {
                solrQuery.setRows(Integer.valueOf((int) results.getNumFound()));
                response = solr.query(solrQuery);
                results = response.getResults();
            }
			for (SolrDocument d : results) {
				Long id = Long.valueOf((String) d.getFieldValue("id"));
				try {
					FileHeader f = dao.getEntityById(FileHeader.class, id);
					result.add(f);
				} catch (ObjectNotFoundException e) {
					logger.warn("Search result id " + id + " cannot be found", e);
				}
			}
            return new SearchResult(results.getNumFound(), result);
		} catch (MalformedURLException e) {
			logger.error(e);
			throw new EJBException(e);
		} catch (SolrServerException e) {
			logger.error(e);
			throw new EJBException(e);
		}
	}

	@Override
	public void copyFiles(Long userId, List<Long> fileIds, Long destId) throws ObjectNotFoundException, DuplicateNameException, GSSIOException, InsufficientPermissionsException, QuotaExceededException {
		for(Long l : fileIds){
			FileHeader file = dao.getEntityById(FileHeader.class, l);
			copyFile(userId, l, destId, file.getName());
		}


	}

	@Override
	public void moveFiles(Long userId, List<Long> fileIds, Long destId) throws InsufficientPermissionsException, ObjectNotFoundException, QuotaExceededException {
		for(Long l : fileIds){
			FileHeader file = dao.getEntityById(FileHeader.class, l);
			moveFile(userId, l, destId, file.getName());
		}

	}

	@Override
	public Nonce createNonce(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		User user = dao.getEntityById(User.class, userId);
		Nonce nonce = Nonce.createNonce(user.getId());
		dao.create(nonce);
		return nonce;
	}

	@Override
	public Nonce getNonce(String nonce, Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (nonce == null)
			throw new ObjectNotFoundException("No nonce specified");
		return dao.getNonce(nonce, userId);
	}

	@Override
	public void removeNonce(Long id) throws ObjectNotFoundException {
		if (id == null)
			throw new ObjectNotFoundException("No nonce specified");
		Nonce nonce = dao.getEntityById(Nonce.class, id);
		dao.delete(nonce);
	}

	@Override
	public void activateUserNonce(Long userId, String nonce, Date nonceExpiryDate) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		User user = dao.getEntityById(User.class, userId);
		user.setNonce(nonce);
		user.setNonceExpiryDate(nonceExpiryDate);
	}

	@Override
	public StatsDTO getUserStatistics(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		StatsDTO stats = new StatsDTO();
		stats.setFileCount(dao.getFileCount(userId));
		Long fileSize = dao.getFileSize(userId);
		stats.setFileSize(fileSize);
		Long quota = getQuota(userId);
		Long quotaLeft = quota - fileSize;
		stats.setQuotaLeftSize(quotaLeft);
		return stats;
	}

	@Override
	public void restoreVersion(Long userId, Long fileId, int version) throws ObjectNotFoundException, InsufficientPermissionsException,  GSSIOException, QuotaExceededException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");
		User user = dao.getEntityById(User.class, userId);
		FileHeader header = dao.getEntityById(FileHeader.class, fileId);
		if(!header.hasWritePermission(user))
			throw new InsufficientPermissionsException("You don't have the necessary permissions");
		FileBody body = dao.getFileVersion(fileId, version);
		final File fileContents = new File(body.getStoredFilePath());

		try {
			updateFileContents(userId, fileId, body.getMimeType(), new FileInputStream(fileContents) );
		} catch (FileNotFoundException e) {
			throw new GSSIOException(e);
		}

	}

	/* (non-Javadoc)
	 * @see org.gss_project.gss.server.ejb.ExternalAPI#removeOldVersions(java.lang.Long, java.lang.Long)
	 */
	@Override
	public void removeOldVersions(Long userId, Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");
		User user = dao.getEntityById(User.class, userId);
		FileHeader header = dao.getEntityById(FileHeader.class, fileId);
		if(!header.hasWritePermission(user))
			throw new InsufficientPermissionsException("You don't have the necessary permissions");
		Iterator<FileBody> it = header.getBodies().iterator();
		while(it.hasNext()){
			FileBody body = it.next();
			if(!body.equals(header.getCurrentBody())){
				deleteActualFile(body.getStoredFilePath());
				it.remove();
				dao.delete(body);
			}
		}
		header.getCurrentBody().setVersion(1);

		Folder parent = header.getFolder();
		touchParentFolders(parent, user, new Date());
	}

	/**
	 * Gets the quota left for specified user ID.
	 */
	private Long getQuotaLeft(Long userId) throws ObjectNotFoundException{
		Long fileSize = dao.getFileSize(userId);
		Long quota = getQuota(userId);
		return quota - fileSize;
	}

	/**
	 * Gets the quota for specified user ID.
	 */
	private Long getQuota(Long userId) throws ObjectNotFoundException{
		UserClass uc = getUser(userId).getUserClass();
		if (uc == null)
			uc = getDefaultUserClass();
		return uc.getQuota();
	}

	@Override
    @TransactionAttribute(TransactionAttributeType.NEVER)
	public String rebuildSolrIndex() {
		try {
            CommonsHttpSolrServer solr = new CommonsHttpSolrServer(getConfiguration().getString("solr.url"));
			solr.deleteByQuery("*:*");
			solr.commit();
            logger.info("Deleted everything in solr");

			List<Long> fileIds = dao.getAllFileIds();
            logger.info("Total of " + fileIds.size() + " will be indexed");
            int i = 0;
			for (Long id : fileIds) {
                try {
                    postFileToSolr(solr, id);
                }
                catch (ObjectNotFoundException e) {
                    logger.error("Indexing of file id " + id + " failed.", e);
                }
                i++;
                if (i % 10 == 0) {
                    solr.commit();
                    logger.info("Sent commit to solr at file " + i);
                }
			}
			solr.optimize();
			solr.commit();
            logger.info("Finished indexing of " + i + " files");
            return "Finished indexing of " + i + " files";
		} catch (IOException e) {
			throw new EJBException(e);
		} catch (SolrServerException e) {
			throw new EJBException(e);
		}
	}

	@Override
    @TransactionAttribute(TransactionAttributeType.NEVER)
	public String refreshSolrIndex() {
		try {
			CommonsHttpSolrServer solr = new CommonsHttpSolrServer(getConfiguration().getString("solr.url"));
			
			List<Long> fileIds = dao.getAllFileIds();
            logger.info("Total of " + fileIds.size() + " will be checked");
            int i = 0;
			for (Long id : fileIds) {
                if (!fileIsInSolr(solr, id)) {
                    try {
                        postFileToSolr(solr, id);
                    }
                    catch (ObjectNotFoundException e) {
                     	logger.error("Indexing of file id " + id + " failed.", e);
                    }
                }
                i++;
                if (i % 10 == 0) {
                    solr.commit();
                    logger.info("Sent commit to solr at file " + i);
                }
			}
			solr.optimize();
			solr.commit();
            logger.info("Finished indexing of " + i + " files");
            return "Finished indexing of " + i + " files";
		} catch (IOException e) {
			throw new EJBException(e);
		} catch (SolrServerException e) {
			throw new EJBException(e);
		}
	}

    private boolean fileIsInSolr(CommonsHttpSolrServer solr, Long id) {
        try {
            SolrQuery query = new SolrQuery("id:" + id);
            QueryResponse response = solr.query(query);
            return !(response.getResults().size() == 0);
        }
        catch (SolrServerException e) {
            logger.warn("Exception while checking file " + id, e);
            return false;
        }
    }

    @Override
	public FileHeader createFile(Long userId, Long folderId, String name, String mimeType, long fileSize, String filePath)
			throws DuplicateNameException, ObjectNotFoundException, GSSIOException,
			InsufficientPermissionsException, QuotaExceededException {
		// Validate.
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		String contentType = mimeType;
		if (StringUtils.isEmpty(mimeType))
			contentType = DEFAULT_MIME_TYPE;
		if (StringUtils.isEmpty(name))
			throw new ObjectNotFoundException("No file name specified");
		if (dao.existsFolderOrFile(folderId, name))
			throw new DuplicateNameException("A folder or file with the name '" + name +
						"' already exists at this level");

		// Do the actual work.
		Folder parent = null;
		try {
			parent = dao.getEntityById(Folder.class, folderId);
		} catch (final ObjectNotFoundException onfe) {
			// Supply a more accurate problem description.
			throw new ObjectNotFoundException("Parent folder not found");
		}
		final User owner = dao.getEntityById(User.class, userId);
		if (!parent.hasWritePermission(owner))
			throw new InsufficientPermissionsException("You don't have the permissions to write to this folder");
		final FileHeader file = new FileHeader();
		file.setName(name);
		parent.addFile(file);
		// set file owner to folder owner
		file.setOwner(parent.getOwner());
		//set file's readForAll value according to parent folder readForAll value
		file.setReadForAll(parent.isReadForAll());

		final Date now = new Date();
		final AuditInfo auditInfo = new AuditInfo();
		auditInfo.setCreatedBy(owner);
		auditInfo.setCreationDate(now);
		auditInfo.setModifiedBy(owner);
		auditInfo.setModificationDate(now);
		file.setAuditInfo(auditInfo);
		// TODO set the proper versioning flag on creation
		file.setVersioned(false);

		for (final Permission p : parent.getPermissions()) {
			final Permission permission = new Permission();
			permission.setGroup(p.getGroup());
			permission.setUser(p.getUser());
			permission.setRead(p.getRead());
			permission.setWrite(p.getWrite());
			permission.setModifyACL(p.getModifyACL());
			file.addPermission(permission);
		}

		// Create the file body.
		try {
			createFileBody(name, contentType, fileSize, filePath, file, auditInfo);
		} catch (FileNotFoundException e) {
			throw new GSSIOException(e);
		}
		touchParentFolders(parent, owner, new Date());
		dao.flush();
		indexFile(file.getId(), false);

		return file;
	}

	@Override
	public FileHeader updateFileContents(Long userId, Long fileId, String mimeType, long fileSize, String filePath) throws ObjectNotFoundException, GSSIOException, InsufficientPermissionsException, QuotaExceededException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");
		String contentType = mimeType;

		FileHeader file = dao.getEntityById(FileHeader.class, fileId);

		// if no mime type or the generic mime type is defined by the client, then try to identify it from the filename extension
		if (StringUtils.isEmpty(mimeType) || "application/octet-stream".equals(mimeType)
					|| "application/download".equals(mimeType) || "application/force-download".equals(mimeType)
					|| "octet/stream".equals(mimeType) || "application/unknown".equals(mimeType))
			contentType = identifyMimeType(file.getName());

		final User owner = dao.getEntityById(User.class, userId);
		if (!file.hasWritePermission(owner))
			throw new InsufficientPermissionsException("You don't have the necessary permissions");
		final Date now = new Date();
		final AuditInfo auditInfo = new AuditInfo();
		auditInfo.setCreatedBy(owner);
		auditInfo.setCreationDate(now);
		auditInfo.setModifiedBy(owner);
		auditInfo.setModificationDate(now);
		try {
			createFileBody(file.getName(), contentType, fileSize, filePath, file, auditInfo);
		} catch (FileNotFoundException e) {
			throw new GSSIOException(e);
		}
		Folder parent = file.getFolder();
		touchParentFolders(parent, owner, new Date());

		indexFile(fileId, false);
		return file;
	}

	/**
	 * Helper method for identifying mime type by examining the filename extension
	 *
	 * @param filename
	 * @return the mime type
	 */
	private String identifyMimeType(String filename) {
		if (filename.indexOf('.') != -1) {
			String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ENGLISH);
			if (".doc".equals(extension))
				return "application/msword";
			else if (".xls".equals(extension))
				return "application/vnd.ms-excel";
			else if (".ppt".equals(extension))
				return "application/vnd.ms-powerpoint";
			else if (".pdf".equals(extension))
				return "application/pdf";
			else if (".gif".equals(extension))
				return "image/gif";
			else if (".jpg".equals(extension) || ".jpeg".equals(extension) || ".jpe".equals(extension))
				return "image/jpeg";
			else if (".tiff".equals(extension) || ".tif".equals(extension))
				return "image/tiff";
			else if (".png".equals(extension))
				return "image/png";
			else if (".bmp".equals(extension))
				return "image/bmp";
		}
		// when all else fails assign the default mime type
		return DEFAULT_MIME_TYPE;
	}

	/**
	 * Helper method to create a new file body and attach it as the current body
	 * of the provided file header.
	 *
	 * @param name the original file name
	 * @param mimeType the content type
	 * @param fileSize the uploaded file size
	 * @param filePath the uploaded file full path
	 * @param header the file header that will be associated with the new body
	 * @param auditInfo the audit info
	 * @throws FileNotFoundException
	 * @throws QuotaExceededException
	 * @throws ObjectNotFoundException if the owner was not found
	 */
	private void createFileBody(String name, String mimeType, long fileSize, String filePath,
				FileHeader header, AuditInfo auditInfo)
			throws FileNotFoundException, QuotaExceededException, ObjectNotFoundException {

		long currentTotalSize = 0;
		if (!header.isVersioned() && header.getCurrentBody() != null && header.getBodies() != null)
			currentTotalSize = header.getTotalSize();
		Long quotaLeft = getQuotaLeft(header.getOwner().getId());
		if(quotaLeft < fileSize-currentTotalSize) {
			// quota exceeded -> delete the file
			deleteActualFile(filePath);
			throw new QuotaExceededException("Not enough free space available");
		}

		FileBody body = new FileBody();

		// if no mime type or the generic mime type is defined by the client, then try to identify it from the filename extension
		if (StringUtils.isEmpty(mimeType) || "application/octet-stream".equals(mimeType)
					|| "application/download".equals(mimeType) || "application/force-download".equals(mimeType)
					|| "octet/stream".equals(mimeType) || "application/unknown".equals(mimeType))
			body.setMimeType(identifyMimeType(name));
		else
			body.setMimeType(mimeType);
		body.setAuditInfo(auditInfo);
		body.setFileSize(fileSize);
		body.setOriginalFilename(name);
		body.setStoredFilePath(filePath);
		//CLEAR OLD VERSION IF FILE IS NOT VERSIONED AND GETS UPDATED
		if(!header.isVersioned() && header.getCurrentBody() != null){
			header.setCurrentBody(null);
			if (header.getBodies() != null) {
				Iterator<FileBody> it = header.getBodies().iterator();
				while(it.hasNext()){
					FileBody bo = it.next();
					deleteActualFile(bo.getStoredFilePath());
					it.remove();
					dao.delete(bo);
				}
			}
		}

		dao.flush();
		header.addBody(body);
		header.setAuditInfo(auditInfo);

		dao.create(body);
	}


	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public File uploadFile(InputStream stream, Long userId) throws IOException, ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		User owner = dao.getEntityById(User.class, userId);
		if(owner == null)
			throw new ObjectNotFoundException("No user specified");
		long start = 0, end = 0;
		if (logger.isDebugEnabled())
			start = System.currentTimeMillis();
		File result = new File(generateRepositoryFilePath());
		try {
			final FileOutputStream output = new FileOutputStream(result);
			final byte[] buffer = new byte[UPLOAD_BUFFER_SIZE];
			int n = 0;

			while (-1 != (n = stream.read(buffer)))
				output.write(buffer, 0, n);
			output.close();
			stream.close();
		} catch (IOException e) {
			if (!result.delete())
				logger.warn("Could not delete " + result.getPath());
			throw e;
		}
		if (logger.isDebugEnabled()) {
			end = System.currentTimeMillis();
			logger.debug("Time to upload: " + (end - start) + " (msec)");
		}
		return result;
	}


	@Override
	public void createFileUploadProgress(Long userId, String filename, Long bytesTransfered, Long fileSize) throws ObjectNotFoundException{

		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		User user = dao.getEntityById(User.class, userId);
		FileUploadStatus status = dao.getFileUploadStatus(userId, filename);
		if(status == null){
			status = new FileUploadStatus();
			status.setOwner(user);
			status.setFilename(filename);
			status.setBytesUploaded(bytesTransfered);
			status.setFileSize(fileSize);
			dao.create(status);
		}
		else{
			status.setBytesUploaded(bytesTransfered);
			status.setFileSize(fileSize);
			dao.update(status);
		}

	}

	@Override
	public void removeFileUploadProgress(Long userId, String filename) throws ObjectNotFoundException{
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		FileUploadStatus status = dao.getFileUploadStatus(userId, filename);
		if(status != null)
			dao.delete(status);
	}

	@Override
	public FileUploadStatus getFileUploadStatus(Long userId, String fileName) {
		return dao.getFileUploadStatus(userId, fileName);
	}

	@Override
	public FileBody getFileVersion(Long userId, Long fileId, int version)
			throws ObjectNotFoundException, InsufficientPermissionsException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (fileId == null)
			throw new ObjectNotFoundException("No file specified");
		if (version < 1)
			throw new ObjectNotFoundException("No valid version specified");
		User user = dao.getEntityById(User.class, userId);
		FileHeader file = dao.getEntityById(FileHeader.class, fileId);
		if (!file.hasReadPermission(user) && !file.getFolder().hasReadPermission(user))
			throw new InsufficientPermissionsException("You don't have the necessary permissions");
		FileBody body = dao.getFileVersion(fileId, version);
		return body;
	}

	@Override
	public User updateUserPolicyAcceptance(Long userId, boolean isAccepted) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		User user = dao.getEntityById(User.class, userId);
		user.setAcceptedPolicy(isAccepted);
		return user;
	}

	@Override
	public void updateAccounting(User user, Date date, long bandwidthDiff) {
		dao.updateAccounting(user, date, bandwidthDiff);
	}

	@Override
	public boolean canReadFolder(Long userId, Long folderId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		User user = dao.getEntityById(User.class, userId);
		Folder folder = dao.getEntityById(Folder.class, folderId);
		// Check permissions
		if (!folder.hasReadPermission(user))
			return false;
		return true;
	}

	@Override
	public String resetWebDAVPassword(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		User user = dao.getEntityById(User.class, userId);
		user.generateWebDAVPassword();
		return user.getWebDAVPassword();
	}

	@Override
	public Invitation findInvite(String code) {
		if (code == null)
			return null;
		return dao.findInvite(code);
	}

	@Override
	public void createLdapUser(String username, String firstname, String lastname, String email, String password) {
		LDAPConnection lc = new LDAPConnection();
        LDAPAttributeSet attributeSet = new LDAPAttributeSet();
        attributeSet.add(new LDAPAttribute("objectClass", getConfiguration().getStringArray("objectClass")));
        attributeSet.add(new LDAPAttribute("uid", username));
        attributeSet.add(new LDAPAttribute("cn", new String[]{firstname + " " + lastname}));
        attributeSet.add(new LDAPAttribute("sn", lastname));
        attributeSet.add(new LDAPAttribute("givenName", firstname));
        attributeSet.add(new LDAPAttribute("mail", email));
        attributeSet.add(new LDAPAttribute("userPassword", password));
        String dn = "uid=" + username + "," + getConfiguration().getString("baseDn");
        LDAPEntry newEntry = new LDAPEntry(dn, attributeSet);
        try {
        	lc.connect(getConfiguration().getString("ldapHost"), LDAPConnection.DEFAULT_PORT);
        	lc.bind(LDAPConnection.LDAP_V3, getConfiguration().getString("bindDn"),
        			getConfiguration().getString("bindPassword").getBytes("UTF8"));
        	lc.add(newEntry);
        	logger.info("Successfully added LDAP account: " + dn);
        	lc.disconnect();
        } catch(LDAPException e) {
        	throw new RuntimeException(e);
        } catch(UnsupportedEncodingException e) {
        	throw new RuntimeException(e);
        }

	}

	@Override
	public UserClass upgradeUserClass(String username, String code) throws ObjectNotFoundException, InvitationUsedException {
		User user = findUser(username);
		if (user == null)
			throw new ObjectNotFoundException("The user was not found");
		Invitation invite = findInvite(code);
		if (invite.getUser() != null)
			throw new InvitationUsedException("This code has already been used");
		invite.setUser(user);
		UserClass couponClass = getCouponUserClass();
		user.setUserClass(couponClass);
		return couponClass;
	}

	@Override
	public UserClass getCouponUserClass() {
		return dao.findCouponUserClass();
	}

	/**
	 * Set the provided readForAll as the new readforAll value of the specified
	 * folder and sub-folders.
	 *
	 * @param user
	 * @param folder
	 * @param readForAll
	 * @throws ObjectNotFoundException
	 *
	 */
	private void setFolderReadForAll(User user, Folder folder, Boolean readForAll){
		if (readForAll != null && user.equals(folder.getOwner())){
			folder.setReadForAll(readForAll);
			dao.update(folder);
			for (FileHeader file : folder.getFiles())
				file.setReadForAll(readForAll);
			if(readForAll)
				//only update subfolders when readforall is true. otherwise all sub-folders stay untouched
				for (Folder sub : folder.getSubfolders())
					setFolderReadForAll(user, sub, readForAll);

		}

	}
		
	/**
	 * Update the userLogin with the values from the supplied object.
	 */
	
	public void addUserLogin(UserLogin userLogin) {
		dao.update(userLogin);		

	}
		
	/**
	 * Retrieves the current session user login and the user's last login
	 * 
	 * @param userId
	 * @return a list of last two user logins
	 * @throws ObjectNotFoundException 
	 */
	
	public List<UserLogin> getLastUserLogins(Long userId) throws ObjectNotFoundException{
		List<UserLogin> userLoginResults = new ArrayList<UserLogin>();		
		userLoginResults = dao.getLoginsForUser(userId);	
		if(userLoginResults.size() == 0)
			throw new ObjectNotFoundException("No userlogin found for the user");
		//if the user logins for the first time lastLoginDate = currentLoginDate
		if(userLoginResults.size()==1)
			userLoginResults.add(userLoginResults.get(0));
		return userLoginResults;
	}
	

    public void postFileToSolr(Long id) throws IOException, SolrServerException, ObjectNotFoundException {
        CommonsHttpSolrServer solr = new CommonsHttpSolrServer(getConfiguration().getString("solr.url"));
        postFileToSolr(solr, id);
        solr.commit();
    }

	private void postFileToSolr(CommonsHttpSolrServer solr, Long id) throws ObjectNotFoundException {
		try {
			FileHeader file = dao.getFileForIndexing(id);
			FileBody body = file.getCurrentBody();
			String mime = body.getMimeType();
			boolean multipart = true;
			if (!mime.equals("application/pdf") 
						&& !mime.equals("text/plain")
						&& !mime.equals("text/html")
						&& !mime.endsWith("msword")
						&& !mime.endsWith("ms-excel")
						&& !mime.endsWith("powerpoint")
						|| (body.getFileSize() > getConfiguration().getLong("solrDocumentUploadLimitInKB") * 1024))
				multipart = false;

			if (!multipart)
				sendMetaDataOnly(solr, file);
			else {
                ContentStreamUpdateRequest solrRequest = new ContentStreamUpdateRequest(getConfiguration().getString("solr.rich.update.path"));
				solrRequest.setParam("literal.id", file.getId().toString());
				solrRequest.setParam("literal.name", file.getName());
				for (FileTag t : file.getFileTags()) {
					solrRequest.getParams().add("literal.tag", t.getTag());
				}
                for (Permission p : file.getPermissions()) {
                    if (p.getRead()) {
                        if (p.getUser() != null)
                            solrRequest.getParams().add("literal.ureaders", p.getUser().getId().toString());
                        else if (p.getGroup() != null)
                            solrRequest.getParams().add("literal.greaders", p.getGroup().getId().toString());
                    }
                }
                solrRequest.setParam("literal.owner", file.getOwner().getId().toString());
                solrRequest.setParam("literal.public", String.valueOf(file.isReadForAll()));
                File fsFile = new File(body.getStoredFilePath());
				solrRequest.addFile(fsFile);
				try {
					solr.request(solrRequest);
				}
				catch (SolrException e) {
					logger.warn("File " + id + " failed with SolrException: " + e.getLocalizedMessage() + ". Retrying without the file");
					//Let 's try without the file
					sendMetaDataOnly(solr, file);
				}
				catch (NullPointerException e) {
					logger.warn("File " + id + " failed with NullPointerException: " + e.getLocalizedMessage() + ". Retrying without the file");
					//Let 's try without the file
					sendMetaDataOnly(solr, file);
				}
				catch (SolrServerException e) {
					logger.warn("File " + id + " failed with SolrServerException: " + e.getLocalizedMessage() + ". Retrying without the file");
					//Let 's try without the file
					sendMetaDataOnly(solr, file);
				}
			}
		} catch (MalformedURLException e) {
			throw new EJBException(e);
		} catch (SolrServerException e) {
			throw new EJBException(e);
		} catch (IOException e) {
			throw new EJBException(e);
		}
	}

	private void sendMetaDataOnly(CommonsHttpSolrServer solr, FileHeader file) throws SolrServerException, IOException {
		SolrInputDocument solrDoc = new SolrInputDocument();
		solrDoc.addField("id", file.getId().toString());
		solrDoc.addField("name", file.getName());
		for (FileTag t : file.getFileTags()) {
			solrDoc.addField("tag", t.getTag());
		}
        for (Permission p : file.getPermissions()) {
            if (p.getRead()) {
                if (p.getUser() != null)
                    solrDoc.addField("ureaders", p.getUser().getId());
                else if (p.getGroup() != null)
                    solrDoc.addField("greaders", p.getGroup().getId());
            }
        }
        solrDoc.addField("owner", file.getOwner().getId());
        solrDoc.addField("public", file.isReadForAll());
		solr.add(solrDoc);
	}

	private String tokenizeFilename(String filename){
		StringBuffer result = new StringBuffer();
		StringTokenizer tokenizer = new StringTokenizer(filename,"._");
		while(tokenizer.hasMoreTokens()){
			result.append(tokenizer.nextToken());
			result.append(" ");
		}
		result.append(filename);
		return result.toString();
	}

	private String normalizeSearchQuery(String query) {
		if (query.contains("*"))
			return query.toLowerCase().replace('ά', 'α').replace('έ', 'ε').replace('ί', 'ι').replace('ή', 'η').replace('ύ', 'υ')
					.replace('ό', 'ο').replace('ς', 'σ').replace('ώ', 'ω').replace('ϊ', 'ι').replace('ϋ', 'υ');
		else
			return query;
	}
	
	private String escapeCharacters(String text) {
		return text.replaceAll(":", "\\\\:");
	}
	
	/*** NEW METHODS IN ORDER TO AVOID LAZY loading exception in json render 
	 ****/
	@Override
	public Folder expandFolder(Folder folder) throws ObjectNotFoundException{
		Folder result = dao.getEntityById(Folder.class, folder.getId());
		result.getSubfolders().size();
		result.getFiles().size();
		result.getPermissions().size();
		return result;
}

	@Override
	public FileHeader expandFile(FileHeader folder) throws ObjectNotFoundException{
		FileHeader result = dao.getEntityById(FileHeader.class, folder.getId());
		result.getFolder();
		result.getPermissions().size();
		result.getFileTags().size();
		return result;
	}
	
	@Override
	public Group expandGroup(Group folder) throws ObjectNotFoundException{
		Group result = dao.getEntityById(Group.class, folder.getId());
		result.getMembers().size();
		return result;
	}

	/* (non-Javadoc)
	 * @see org.gss_project.gss.server.ejb.ExternalAPI#getUsersByUserNameLike(java.lang.String)
	 */
	@Override
	public User getUserByUserName(String username) {
		User result = dao.getUserByUserName(username);
		return result;
	}
	
	/*WEBDAV CREATE EMPTY FILE*/
	@Override
	public FileHeader createEmptyFile(Long userId, Long folderId, String name)
			throws DuplicateNameException, ObjectNotFoundException, GSSIOException,
			InsufficientPermissionsException, QuotaExceededException {
		// Validate.
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		String contentType = DEFAULT_MIME_TYPE;
		if (StringUtils.isEmpty(name))
			throw new ObjectNotFoundException("No file name specified");
		if (dao.existsFolderOrFile(folderId, name))
			throw new DuplicateNameException("A folder or file with the name '" + name +
						"' already exists at this level");

		// Do the actual work.
		Folder parent = null;
		try {
			parent = dao.getEntityById(Folder.class, folderId);
		} catch (final ObjectNotFoundException onfe) {
			// Supply a more accurate problem description.
			throw new ObjectNotFoundException("Parent folder not found");
		}
		final User owner = dao.getEntityById(User.class, userId);
		if (!parent.hasWritePermission(owner))
			throw new InsufficientPermissionsException("You don't have the permissions to write to this folder");
		final FileHeader file = new FileHeader();
		file.setName(name);
		parent.addFile(file);
		// set file owner to folder owner
		file.setOwner(parent.getOwner());
		//set file's readForAll value according to parent folder readForAll value
		file.setReadForAll(parent.isReadForAll());

		final Date now = new Date();
		final AuditInfo auditInfo = new AuditInfo();
		auditInfo.setCreatedBy(owner);
		auditInfo.setCreationDate(now);
		auditInfo.setModifiedBy(owner);
		auditInfo.setModificationDate(now);
		file.setAuditInfo(auditInfo);
		// TODO set the proper versioning flag on creation
		file.setVersioned(false);

		for (final Permission p : parent.getPermissions()) {
			final Permission permission = new Permission();
			permission.setGroup(p.getGroup());
			permission.setUser(p.getUser());
			permission.setRead(p.getRead());
			permission.setWrite(p.getWrite());
			permission.setModifyACL(p.getModifyACL());
			file.addPermission(permission);
		}
		// Create the file body.
		try {
			createEmptyFileBody(name, contentType, 0,  file, auditInfo);
		} catch (FileNotFoundException e) {
			throw new GSSIOException(e);
		}
		touchParentFolders(parent, owner, new Date());
		dao.flush();
		return file;
	}
	
	private void createEmptyFileBody(String name, String mimeType, long fileSize, 
				FileHeader header, AuditInfo auditInfo)
			throws FileNotFoundException, QuotaExceededException, ObjectNotFoundException {

		long currentTotalSize = 0;
		if (!header.isVersioned() && header.getCurrentBody() != null && header.getBodies() != null)
			currentTotalSize = header.getTotalSize();
		Long quotaLeft = getQuotaLeft(header.getOwner().getId());
		

		FileBody body = new FileBody();

		// if no mime type or the generic mime type is defined by the client, then try to identify it from the filename extension
		if (StringUtils.isEmpty(mimeType) || "application/octet-stream".equals(mimeType)
					|| "application/download".equals(mimeType) || "application/force-download".equals(mimeType)
					|| "octet/stream".equals(mimeType) || "application/unknown".equals(mimeType))
			body.setMimeType(identifyMimeType(name));
		else
			body.setMimeType(mimeType);
		body.setAuditInfo(auditInfo);
		body.setFileSize(fileSize);
		body.setOriginalFilename(name);
		String filePath = generateRepositoryFilePath();
		try{
			File f = new File(filePath);
			if(!f.exists()){
				f.createNewFile();
			}
		}
		catch(IOException ex){/*swallow*/}
		body.setStoredFilePath(filePath);
		//CLEAR OLD VERSION IF FILE IS NOT VERSIONED AND GETS UPDATED
		if(!header.isVersioned() && header.getCurrentBody() != null){
			header.setCurrentBody(null);
			if (header.getBodies() != null) {
				Iterator<FileBody> it = header.getBodies().iterator();
				while(it.hasNext()){
					FileBody bo = it.next();
					deleteActualFile(bo.getStoredFilePath());
					it.remove();
					dao.delete(bo);
				}
			}
		}

		dao.flush();
		header.addBody(body);
		header.setAuditInfo(auditInfo);

		dao.create(body);
	}
	/*** WEBDAV LOCK **/
	@Override
	public FileLock getLockById(String id) {
		return dao.getLockById(id);
	}

	@Override
	public FileLock getLockByToken(String tokenId) {
		return dao.getLockByToken(tokenId);
	}

	@Override
	public void removeLock(FileLock lock) {
		dao.removeLock(lock);		
	}

	@Override
	public FileLock saveOrUpdateLock(FileLock lock) {
		return dao.saveOrUpdateLock(lock);
	}
	
	@Override
	public WebDavNonce getWebDavNonce(String tokenId) {
		return dao.getWebDavNonce(tokenId);
	}

	@Override
	public void removeWebDavNonce(WebDavNonce nonce) {
		dao.removeWebDavNonce(nonce);		
	}

	@Override
	public WebDavNonce saveOrUpdateWebDavNonce(WebDavNonce nonce) {
		return dao.saveOrUpdateWebDavNonce(nonce);
	}
	
	
	
}
