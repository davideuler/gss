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

import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.server.domain.FileBody;
import gr.ebs.gss.server.domain.FileHeader;
import gr.ebs.gss.server.domain.FileUploadStatus;
import gr.ebs.gss.server.domain.Folder;
import gr.ebs.gss.server.domain.Group;
import gr.ebs.gss.server.domain.Nonce;
import gr.ebs.gss.server.domain.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;

/**
 * The implementation of the GSSDAO interface.
 */
@Stateless
public class GSSDAOBean implements GSSDAO {

	/**
	 * The entity manager for the persistence unit
	 */
	@PersistenceContext(unitName = "gss")
	private EntityManager manager;

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getRootFolder(java.lang.Long)
	 */
	public Folder getRootFolder(final Long userId) throws ObjectNotFoundException {
		try {
			if (userId == null)
				throw new ObjectNotFoundException("No user specified");
			return (Folder) manager	.createQuery("select f from Folder f where f.owner.id=:ownerId and f.parent is null")
									.setParameter("ownerId", userId)
									.getSingleResult();
		} catch (final NoResultException e) {
			throw new ObjectNotFoundException("Root folder not found for user with id=" + userId);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getUser(java.lang.String)
	 */
	public User getUser(final String username) throws ObjectNotFoundException {
		try {
			if (username == null)
				throw new ObjectNotFoundException("No user specified");
			return (User) manager	.createQuery("select f from User f where f.username=:username")
									.setParameter("username", username)
									.getSingleResult();
		} catch (final NoResultException e) {
			throw new ObjectNotFoundException("No User found for username=" + username);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.server.ejb.GSSDAO#create(java.lang.Object)
	 */
	public void create(final Object obj) {
		if (obj == null)
			throw new IllegalArgumentException("No object speficied");
		manager.persist(obj);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.server.ejb.GSSDAO#refresh(java.lang.Object)
	 */
	public void refresh(final Object obj) {
		if (obj == null)
			throw new IllegalArgumentException("No object speficied");
		manager.refresh(obj);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.server.ejb.GSSDAO#update(java.lang.Object)
	 */
	public void update(final Object obj) {
		if (obj == null)
			throw new IllegalArgumentException("No object speficied");
		manager.merge(obj);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.server.ejb.GSSDAO#delete(java.lang.Object)
	 */
	public void delete(final Object entity) {
		if (entity == null)
			throw new IllegalArgumentException("No object speficied");
		manager.remove(entity);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getEntityById(java.lang.Class,
	 *      java.lang.Object)
	 */
	public <T> T getEntityById(final Class<T> _class, final Object _id) throws ObjectNotFoundException {
		if (_id == null)
			throw new ObjectNotFoundException("No " + _class.getSimpleName() + " specified");

		final T entity = manager.find(_class, _id);
		if (entity == null)
			throw new ObjectNotFoundException(_class.getSimpleName() + " with id=" + _id + " was not found");

		return entity;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getGroups(java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	public List<Group> getGroups(final Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");

		return manager.createQuery("select g from Group g where g.owner.id=:userId").setParameter("userId", userId).getResultList();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getFiles(java.lang.Long, java.lang.Boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<FileHeader> getFiles(final Long folderId, boolean ignoreDeleted) throws ObjectNotFoundException {
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		String query;
		if(ignoreDeleted)
			query = "select f from FileHeader f where f.folder.id=:folderId  and f.deleted=false";
		else
			query = "select f from FileHeader f where f.folder.id=:folderId";
		return manager.createQuery(query).setParameter("folderId", folderId).getResultList();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getUsers(java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsers(final Long groupId) throws ObjectNotFoundException {
		if (groupId == null)
			throw new ObjectNotFoundException("No group specified");
		return manager.createQuery("select u from User u join u.groupsMember g where g.id=:groupId").
				setParameter("groupId", groupId).getResultList();
	}

	@Override
	public boolean existsFolderOrFile(Long parentId, String name) throws ObjectNotFoundException {
		if (parentId == null)
			throw new ObjectNotFoundException("No parent folder specified");
		if (StringUtils.isEmpty(name))
			throw new IllegalArgumentException("No folder name specified");

		try {
			manager	.createQuery("select f from Folder f " +
					"where f.parent.id=:parentId and f.name=:name")
					.setParameter("parentId", parentId)
					.setParameter("name", name)
					.getSingleResult();
			return true;
		} catch (NoResultException e) {
			try {
				manager	.createQuery("select f from FileHeader f " +
						"where f.folder.id=:parentId and f.name=:name")
						.setParameter("parentId", parentId)
						.setParameter("name", name)
						.getSingleResult();
				return true;
			} catch (NoResultException e1) {
				return false;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gr.ebs.gss.server.ejb.GSSDAO#existsGroup(java.lang.Long,
	 *      java.lang.String)
	 */
	public boolean existsGroup(final Long userId, final String name) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		if (StringUtils.isEmpty(name))
			throw new ObjectNotFoundException("No group name specified");
		try {
			manager	.createQuery("select g from Group g where g.owner.id=:userId and g.name=:name")
					.setParameter("userId", userId)
					.setParameter("name", name)
					.getSingleResult();
			return true;
		} catch (final NoResultException e) {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getUserTags(java.lang.Long)
	 */
	public Set<String> getUserTags(final Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		return new HashSet(manager.createQuery("select t.tag from FileTag t where t.user.id=:userId order by t.tag")
						.setParameter("userId", userId)
						.getResultList());
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#flush()
	 */
	public void flush() {
		manager.flush();
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getFile(java.lang.Long, java.lang.String)
	 */
	public FileHeader getFile(Long folderId, String name) throws ObjectNotFoundException {
		if (folderId == null)
			throw new ObjectNotFoundException("No parent folder specified");
		if (StringUtils.isEmpty(name))
			throw new IllegalArgumentException("No file name specified");

		try {
			return (FileHeader) manager.createQuery("select f from FileHeader f where f.folder.id=:parentId and f.name=:name")
					.setParameter("parentId", folderId)
					.setParameter("name", name)
					.getSingleResult();
		} catch (final NoResultException e1) {
			throw new ObjectNotFoundException("File not found");
		}
	}

	@Override
	public Folder getFolder(Long parentId, String name) throws ObjectNotFoundException {
		if (parentId == null)
			throw new ObjectNotFoundException("No parent folder specified");
		if (StringUtils.isEmpty(name))
			throw new IllegalArgumentException("No folder name specified");

		try {
			return (Folder) manager.createQuery("select f from Folder f where f.parent.id=:parentId and f.name=:name")
					.setParameter("parentId", parentId)
					.setParameter("name", name)
					.getSingleResult();
		} catch (NoResultException e) {
			throw new ObjectNotFoundException("Folder not found");
		}
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getDeletedFiles(java.lang.Long)
	 */
	public List<FileHeader> getDeletedFiles(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No User specified");

		return manager.createQuery("select f from FileHeader f where f.owner.id=:userId and " +
					"f.deleted=true and f.folder.deleted=false").
					setParameter("userId", userId).getResultList();
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getDeletedRootFolders(java.lang.Long)
	 */
	public List<Folder> getDeletedRootFolders(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No User specified");
		return manager.createQuery("select f from Folder f where f.owner.id=:userId and " +
					"f.deleted=true and f.parent.deleted=false").
					setParameter("userId", userId).getResultList();
	}

	@Override
	public User findUser(String username) {
		if (username == null)
			return null;
		List<User> results = manager.createQuery("select u from User u where u.username=:username").
				setParameter("username", username).getResultList();
		if (results.isEmpty()) return null;
		return results.get(0);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getUsersByUserNameLike(java.lang.String)
	 */
	@Override
	public List<User> getUsersByUserNameLike(String username) {
		return manager.createQuery("select u from User u where u.username like :username").
		setParameter("username", username+"%").getResultList();
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getSharedRootFolders(java.lang.Long)
	 */
	@Override
	public List<Folder> getSharedRootFolders(Long userId) {
		List<Folder> folders = manager.createQuery("select distinct f from Folder f " +
					"LEFT JOIN f.permissions p where f.owner.id=:userId and f.deleted=false " +
					"and (p.group.id != null or p.user.id != f.owner.id) ").
					setParameter("userId", userId).getResultList();
		List<Folder> result = new ArrayList<Folder>();
		for(Folder f : folders)
			if(!folders.contains(f.getParent()))
				result.add(f);
		return result;
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getFoldersPermittedForGroup(java.lang.Long, java.lang.Long)
	 */
	@Override
	public List<Folder> getFoldersPermittedForGroup(Long userId, Long groupId) {
		return manager.createQuery("select distinct f from Folder f LEFT JOIN f.permissions p " +
					"where f.owner.id=:userId and f.deleted = false and p.group.id=:groupId ").
					setParameter("userId", userId).setParameter("groupId", groupId).getResultList();
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getUsersSharingFilesForUser(java.lang.Long)
	 */
	@Override
	public List<User> getUsersSharingFoldersForUser(Long userId) {
		return manager.createQuery("select distinct f.owner from Folder f " +
					"LEFT JOIN f.permissions p where f.owner.id != :userId and f.deleted=false " +
					"and (p.user.id=:userId or p.group.id in (select distinct gg.id " +
					"from Group gg join gg.members memb where memb.id=:userId))) ").
					setParameter("userId", userId).getResultList();
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getUsersSharingFilesForUser(java.lang.Long)
	 */
	@Override
	public List<User> getUsersSharingFilesForUser(Long userId) {
		List<User> users = manager.createQuery("select distinct f.owner from FileHeader f " +
					"LEFT JOIN f.permissions p where f.owner.id != :userId and f.deleted=false " +
					"and (p.user.id=:userId or p.group.id in (select distinct gg.id from Group gg " +
					"join gg.members memb where memb.id=:userId)))").
					setParameter("userId", userId).getResultList();
		return users;

	}

	@Override
	public List<FileHeader> getSharedFilesNotInSharedFolders(Long userId) {
		List<FileHeader> files = manager.createQuery("select distinct f from FileHeader f " +
					"LEFT JOIN f.permissions p where f.owner.id=:userId and f.deleted=false " +
					"and (f.readForAll=true or p.group.id != null or p.user.id != f.owner.id)" +
					" and f.folder.id not in (select distinct fo.id from Folder fo LEFT JOIN " +
					"fo.permissions po where fo.owner.id=:userId and fo.deleted=false and " +
					"(po.group.id != null or po.user.id != fo.owner.id))").
					setParameter("userId", userId).getResultList();
		return files;
	}

	@Override
	public List<FileHeader> getSharedFiles(Long userId) {
		List<FileHeader> files = manager.createQuery("select distinct f from FileHeader f " +
					"LEFT JOIN f.permissions p where f.owner.id=:userId and f.deleted=false " +
					"and (p.group.id != null or p.user.id != f.owner.id)").
					setParameter("userId", userId).getResultList();
		return files;
	}

	@Override
	public List<Folder> getSharedFolders(Long userId) {
		List<Folder> folders = manager.createQuery("select distinct f from Folder f " +
					"LEFT JOIN f.permissions p where f.owner.id=:userId and f.deleted=false " +
					"and (p.group.id != null or p.user.id != f.owner.id)").
					setParameter("userId", userId).getResultList();
		return folders;
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getSharedFiles(java.lang.Long, java.lang.Long)
	 */
	@Override
	public List<FileHeader> getSharedFiles(Long userId, Long callingUserId) {
		List<FileHeader> files = manager.createQuery("select distinct f from FileHeader f " +
					"LEFT JOIN f.permissions p where f.owner.id=:userId and f.deleted=false " +
					"and p.read=true and (p.user.id=:cuserId or p.group.id in " +
					"(select distinct gg.id from Group gg join gg.members memb " +
					"where memb.id=:cuserId)) and f.folder.id not in (select distinct fo.id " +
					"from Folder fo LEFT JOIN fo.permissions po where fo.owner.id = :userId " +
					"and fo.deleted=false and po.read=true and (po.user.id=:cuserId " +
					"or po.group.id in (select distinct ggo.id from Group ggo " +
					"join ggo.members membo where membo.id=:cuserId)))").
					setParameter("userId", userId).setParameter("cuserId", callingUserId).getResultList();
		return files;
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getSharedRootFolders(java.lang.Long, java.lang.Long)
	 */
	@Override
	public List<Folder> getSharedRootFolders(Long userId, Long callingUserId) {
		List<Folder> folders = manager.createQuery("select distinct f from Folder f " +
					"LEFT JOIN f.permissions p where f.owner.id = :userId and f.deleted=false " +
					"and p.read=true and (p.user.id=:cuserId or p.group.id in " +
					"(select distinct gg.id from Group gg join gg.members memb " +
					"where memb.id=:cuserId))) ").
					setParameter("userId", userId).
					setParameter("cuserId", callingUserId).
					getResultList();
		List<Folder> result = new ArrayList<Folder>();
		for(Folder f : folders)
			if(!folders.contains(f.getParent()))
				result.add(f);
		return result;

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#searchFiles(java.lang.Long, java.lang.String)
	 */
	@Override
	public List<FileHeader> searchFiles(Long userId, String query) {
		return manager.createQuery("select f from FileHeader f where f.owner.id=:userId and f.name like :query").
				setParameter("query", "%"+query+"%").setParameter("userId",userId).getResultList();
	}

	@Override
	public Nonce getNonce(String nonce, Long userId) throws ObjectNotFoundException {
		List<Nonce> results = manager.createQuery("select n from Nonce n where n.userId=:userId and n.encodedNonce=:nonce").
				setParameter("userId", userId).setParameter("nonce", nonce).getResultList();
		if (results.isEmpty())
			throw new ObjectNotFoundException("No nonce found");
		return results.get(0);
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getFileForIndexing(java.lang.Long)
	 */
	@Override
	public FileHeader getFileForIndexing(Long id) throws ObjectNotFoundException {
		FileHeader h = getEntityById(FileHeader.class, id);
		h.getFileTags().size();
		return h;
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getFileCount(java.lang.Long)
	 */
	@Override
	public Long getFileCount(Long userId) {
		Long singleResult = (Long) manager.createQuery("select count(f) from FileHeader f where f.owner.id=:ownerId")
		.setParameter("ownerId", userId)
		.getSingleResult();
		return singleResult;
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getFileSize(java.lang.Long)
	 */
	@Override
	public Long getFileSize(Long userId) {
		Long singleResult = (Long) manager.createQuery("select sum(f.fileSize) from FileBody f where f.header.owner.id=:ownerId")
		.setParameter("ownerId", userId)
		.getSingleResult();
		if(singleResult == null)
			singleResult = new Long(0L);
		return singleResult;

	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getAllFileIds()
	 */
	@Override
	public List<Long> getAllFileIds() {
		List<Long> ids = manager.createQuery("select f.id from FileHeader f").getResultList();
		return ids;
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.server.ejb.GSSDAO#getFileUploadStatus(java.lang.Long, java.lang.String)
	 */
	@Override
	public FileUploadStatus getFileUploadStatus(Long userId, String fileName) {
		List<FileUploadStatus> res = manager.createQuery(" select f from FileUploadStatus f where f.owner.id=:userId and f.filename=:filename").setParameter("userId", userId).setParameter("filename", fileName).getResultList();
		if(res.size()>0)
			return res.get(0);
		return null;
	}

	@Override
	public FileBody getFileVersion(Long fileId, int version) throws ObjectNotFoundException {
		try {
			return (FileBody) manager.createQuery("select f from FileBody f " +
			"where f.header.id=:fileId and f.version=:version")
			.setParameter("fileId", fileId).setParameter("version", version)
			.getSingleResult();
		} catch (NoResultException e) {
			throw new ObjectNotFoundException("No version " + version + " found for file #" + fileId);
		}
	}
}
