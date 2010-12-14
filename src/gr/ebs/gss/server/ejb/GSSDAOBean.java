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

import static gr.ebs.gss.server.configuration.GSSConfigurationFactory.getConfiguration;
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
import gr.ebs.gss.server.domain.UserLogin;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

/**
 * The implementation of the GSSDAO interface.
 */
@Stateless
public class GSSDAOBean implements GSSDAO {

	private static final int BANDWIDTH_TIME_PERIOD_FIELD = Calendar.MONTH;
	private static final int BANDWIDTH_TIME_PERIOD_AMOUNT = 1;

	/**
	 * The entity manager for the persistence unit
	 */
	@PersistenceContext(unitName = "gss")
	private EntityManager manager;

	@Override
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

	@Override
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

	@Override
	public void create(final Object obj) {
		if (obj == null)
			throw new IllegalArgumentException("No object speficied");
		manager.persist(obj);
	}

	@Override
	public void refresh(final Object obj) {
		if (obj == null)
			throw new IllegalArgumentException("No object speficied");
		manager.refresh(obj);
	}

	@Override
	public void update(final Object obj) {
		if (obj == null)
			throw new IllegalArgumentException("No object speficied");
		manager.merge(obj);
	}

	@Override
	public void delete(final Object entity) {
		if (entity == null)
			throw new IllegalArgumentException("No object speficied");
		manager.remove(entity);
	}

	@Override
	public <T> T getEntityById(final Class<T> _class, final Object _id) throws ObjectNotFoundException {
		if (_id == null)
			throw new ObjectNotFoundException("No " + _class.getSimpleName() + " specified");

		final T entity = manager.find(_class, _id);
		if (entity == null)
			throw new ObjectNotFoundException(_class.getSimpleName() + " with id=" + _id + " was not found");

		return entity;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Group> getGroups(final Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");

		return manager.createQuery("select g from Group g where g.owner.id=:userId").setParameter("userId", userId).getResultList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<FileHeader> getFiles(final Long folderId, Long userId, boolean ignoreDeleted) throws ObjectNotFoundException {
		if (folderId == null)
			throw new ObjectNotFoundException("No folder specified");
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		User user = getEntityById(User.class, userId);
		String query;
		if(ignoreDeleted)
			query = "select f from FileHeader f where f.folder.id=:folderId  and f.deleted=false";
		else
			query = "select f from FileHeader f where f.folder.id=:folderId";
		List<FileHeader> tempList = manager.createQuery(query).setParameter("folderId", folderId).getResultList();
		List<FileHeader> retv = new ArrayList<FileHeader>();
		for (FileHeader f: tempList)
			if (f.hasReadPermission(user)) retv.add(f);

		return retv;
	}

	@Override
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

	@Override
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

	@Override
	public Set<String> getUserTags(final Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		return new HashSet(manager.createQuery("select t.tag from FileTag t where t.user.id=:userId order by t.tag")
						.setParameter("userId", userId)
						.getResultList());
	}

	@Override
	public void flush() {
		manager.flush();
	}

	@Override
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

	@Override
	public List<FileHeader> getDeletedFiles(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No User specified");
		User user = getEntityById(User.class, userId);

		List<FileHeader> tempList = manager.createQuery("select f from FileHeader f where f.owner.id=:userId and " +
					"f.deleted=true and f.folder.deleted=false").
					setParameter("userId", userId).getResultList();
		List<FileHeader> retv = new ArrayList<FileHeader>();
		for (FileHeader f: tempList)
			if (f.hasReadPermission(user)) retv.add(f);

		return retv;
	}

	@Override
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

	@Override
	public User findUserByEmail(String email) {
		if (email == null)
			return null;
		List<User> results = manager.createQuery("select u from User u where u.email=:email").
				setParameter("email", email).getResultList();
		if (results.isEmpty()) return null;
		return results.get(0);
	}

	@Override
	public List<User> getUsersByUserNameLike(String username) {
		return manager.createQuery("select u from User u where u.username like :username").
		setParameter("username", username+"%").getResultList();
	}

	@Override
	public List<Folder> getSharedRootFolders(Long userId) {
		List<Folder> folders = manager.createQuery("select distinct f from Folder f " +
					"LEFT JOIN f.permissions p where f.owner.id=:userId and f.deleted=false " +
					"and (p.group.id != null or p.user.id != f.owner.id or f.readForAll=true) ").
					setParameter("userId", userId).getResultList();
		List<Folder> result = new ArrayList<Folder>();
		for(Folder f : folders)
			if(!folders.contains(f.getParent()))
				result.add(f);
		return result;
	}

	@Override
	public List<Folder> getFoldersPermittedForGroup(Long userId, Long groupId) {
		return manager.createQuery("select distinct f from Folder f LEFT JOIN f.permissions p " +
					"where f.owner.id=:userId and f.deleted = false and p.group.id=:groupId ").
					setParameter("userId", userId).setParameter("groupId", groupId).getResultList();
	}

	private List<Long> getGroupIdsForUserId(Long userId) {
		List<BigInteger> groups = manager.createNativeQuery("select distinct groupsmember_id " +
		"from GSS_Group_GSS_User where members_id=:userId")
			.setParameter("userId", userId)
			.getResultList();
		List<Long> groupIds = new ArrayList<Long>();
		for (BigInteger id : groups)
			groupIds.add(id.longValue());
		return groupIds;
	}
	
	@Override
	public List<User> getUsersSharingFoldersForUser(Long userId) {
        List<Long> groupIds = getGroupIdsForUserId(userId);
		Query q = manager.createQuery("select distinct f.owner from Folder f " +
					"LEFT JOIN f.permissions p where f.owner.id != :userId and f.deleted=false " +
					"and (p.user.id=:userId "+ (groupIds.isEmpty() ? "" : "or p.group.id in (:groupIds)") +")").
					setParameter("userId", userId);
        if (!groupIds.isEmpty())
            q.setParameter("groupIds", groupIds);
        return q.getResultList();            
	}

	@Override
	public List<User> getUsersSharingFilesForUser(Long userId) {
        List<Long> groupIds = getGroupIdsForUserId(userId);
		Query q = manager.createQuery("select distinct f.owner from FileHeader f " +
					"LEFT JOIN f.permissions p where f.owner.id != :userId and f.deleted=false " +
					"and (p.user.id=:userId " + (groupIds.isEmpty() ? "" : "or p.group.id in (:groupIds)") + ")").
					setParameter("userId", userId);
        if (!groupIds.isEmpty())
            q.setParameter("groupIds", groupIds);
        return q.getResultList();
	}

	@Override
	public List<FileHeader> getSharedFilesNotInSharedFolders(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		User user = getEntityById(User.class, userId);
		List<FileHeader> tempList = manager.createQuery("select distinct f from FileHeader f " +
					"LEFT JOIN f.permissions p where f.owner.id=:userId and f.deleted=false " +
					"and (f.readForAll=true or p.group.id != null or p.user.id != f.owner.id)" +
					" and f.folder.id not in (select distinct fo.id from Folder fo LEFT JOIN " +
					"fo.permissions po where fo.owner.id=:userId and fo.deleted=false and " +
					"(po.group.id != null or po.user.id != fo.owner.id or fo.readForAll = true))").
					setParameter("userId", userId).getResultList();
		List<FileHeader> retv = new ArrayList<FileHeader>();
		for (FileHeader f: tempList)
			if (f.hasReadPermission(user)) retv.add(f);

		return retv;
	}

	@Override
	public List<FileHeader> getSharedFiles(Long userId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		User user = getEntityById(User.class, userId);
		List<FileHeader> tempList = manager.createQuery("select distinct f from FileHeader f " +
					"LEFT JOIN f.permissions p where f.owner.id=:userId and f.deleted=false " +
					"and (p.group.id != null or p.user.id != f.owner.id)").
					setParameter("userId", userId).getResultList();
		List<FileHeader> retv = new ArrayList<FileHeader>();
		for (FileHeader f: tempList)
			if (f.hasReadPermission(user)) retv.add(f);

		return retv;
	}

	@Override
	public List<Folder> getSharedFolders(Long userId) {
		List<Folder> folders = manager.createQuery("select distinct f from Folder f " +
					"LEFT JOIN f.permissions p where f.owner.id=:userId and f.deleted=false " +
					"and (p.group.id != null or p.user.id != f.owner.id)").
					setParameter("userId", userId).getResultList();
		return folders;
	}

	@Override
	public List<FileHeader> getSharedFiles(Long userId, Long callingUserId) throws ObjectNotFoundException {
		if (userId == null)
			throw new ObjectNotFoundException("No user specified");
		User user = getEntityById(User.class, userId);
		List<FileHeader> tempList = manager.createQuery("select distinct f from FileHeader f " +
					"LEFT JOIN f.permissions p where f.owner.id=:userId and f.deleted=false " +
					"and p.read=true and (p.user.id=:cuserId or p.group.id in " +
					"(select distinct gg.id from Group gg join gg.members memb " +
					"where memb.id=:cuserId)) and f.folder.id not in (select distinct fo.id " +
					"from Folder fo LEFT JOIN fo.permissions po where fo.owner.id = :userId " +
					"and fo.deleted=false and po.read=true and (po.user.id=:cuserId " +
					"or po.group.id in (select distinct ggo.id from Group ggo " +
					"join ggo.members membo where membo.id=:cuserId)))").
					setParameter("userId", userId).setParameter("cuserId", callingUserId).getResultList();
		List<FileHeader> retv = new ArrayList<FileHeader>();
		for (FileHeader f: tempList)
			if (f.hasReadPermission(user)) retv.add(f);

		return retv;
	}

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

	@Override
	public Long getFileCount(Long userId) {
		Long singleResult = (Long) manager.createQuery("select count(f) from FileHeader f where f.owner.id=:ownerId")
		.setParameter("ownerId", userId)
		.getSingleResult();
		return singleResult;
	}

	@Override
	public Long getFileSize(Long userId) {
		Long singleResult = (Long) manager.createQuery("select sum(f.fileSize) from FileBody f where f.header.owner.id=:ownerId")
		.setParameter("ownerId", userId)
		.getSingleResult();
		if(singleResult == null)
			singleResult = new Long(0L);
		return singleResult;

	}

	@Override
	public List<Long> getAllFileIds() {
		List<Long> ids = manager.createQuery("select f.id from FileHeader f").getResultList();
		return ids;
	}

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

	@Override
	public void updateAccounting(User user, Date date, long bandwidthDiff) {
		AccountingInfo ai = null;
		try {
			ai = (AccountingInfo) manager.createQuery("select ai from AccountingInfo ai " +
				"where ai.user=:user and ai.dateFrom<=:date and ai.dateTo>:date")
				.setParameter("user", user)
				.setParameter("date", date)
				.getSingleResult();
		}
		catch (NoResultException e) {
			ai = null;
		}

		if (ai==null) {
			// The right entry does not exist; must be created.
			// This is where we set the initial time period.
			// We now start from the user's creation, we can change this to something else.
			Calendar creationDate = new GregorianCalendar();
			creationDate.setTime(user.getAuditInfo().getCreationDate());
			int offset = 0;
			Calendar dateFrom;
			Calendar dateTo;
			long timeInMillis = date.getTime();
			do {
				dateFrom = (Calendar) creationDate.clone();
				dateFrom.add(BANDWIDTH_TIME_PERIOD_FIELD, offset);
				dateTo = (Calendar) dateFrom.clone();
				dateTo.add(BANDWIDTH_TIME_PERIOD_FIELD, 1);
				offset += BANDWIDTH_TIME_PERIOD_AMOUNT;
			}
			while (!(dateFrom.getTimeInMillis()<=timeInMillis && dateTo.getTimeInMillis()>timeInMillis));

			ai = new AccountingInfo(user, dateFrom.getTime(), dateTo.getTime());
			manager.persist(ai);
		}

		// Do the update.
		ai.updateBandwidth(bandwidthDiff);
	}

	@Override
	public List<UserClass> getUserClasses() {
		// Ordering by quota is important here.
		List<UserClass> ids = manager.createQuery("select uc from UserClass uc order by uc.quota").getResultList();
		return ids;
	}

	@Override
	public List<User> getUsersByUserNameOrEmailLike(String query) {
		return manager.createQuery("select u from User u where " +
					" upper(u.username) like :query or upper(u.email) like :query order by u.username").
			setParameter("query", query.toUpperCase()+"%").getResultList();
	}

	@Override
	public Invitation findInvite(String code) {
		if (code == null)
			return null;
		List<Invitation> results = manager.createQuery("select i from Invitation i where i.code=:code").
				setParameter("code", code).getResultList();
		if (results.isEmpty()) return null;
		return results.get(0);
	}

	@Override
	public UserClass findCouponUserClass() {
		List<UserClass> results = manager.createQuery("select uc from UserClass uc where uc.name=:name").
				setParameter("name", getConfiguration().getString("couponQuota")).getResultList();
		if (results.isEmpty()) {
			// Create the coupon user class on first use.
			UserClass couponClass = new UserClass();
			couponClass.setName("coupon");
			Long couponQuota = getConfiguration().getLong("couponQuota", new Long(52428800L));
			couponClass.setQuota(couponQuota);
			create(couponClass);
			flush();
			results.add(couponClass);
		}
		return results.get(0);
	}

	@Override
	public Long getFileCount(UserClass userClass) {
		Long result;
		if(userClass==null)
			result=(Long) manager.createQuery("select count(f) from FileHeader f").getSingleResult();
		else result= (Long) manager.createQuery("select count(f) from FileHeader f where f.owner.userClass.id=:id").setParameter("id", userClass.getId()).getSingleResult();
		if(result==null)
			result =0L;
		return result;
	}

	@Override
	public Long getFileSize(UserClass userClass) {
		Long result;
		if(userClass==null)
			result=(Long) manager.createQuery("select sum(f.currentBody.fileSize) from FileHeader f").getSingleResult();
		else result=(Long) manager.createQuery("select sum(f.currentBody.fileSize) from FileHeader f where f.owner.userClass.id=:id").setParameter("id", userClass.getId()).getSingleResult();
		if(result==null)
			result =0L;
		return result;
	}

	@Override
	public Long getUserCount(UserClass userClass) {
		Long result;
		if(userClass==null)
			result = (Long) manager.createQuery("select count(u) from User u").getSingleResult();
		else result = (Long) manager.createQuery("select count(u) from User u where u.userClass.id=:id").setParameter("id", userClass.getId()).getSingleResult();
		if(result==null)
			result =0L;
		return result;
	}

	@Override
	public Long getCountUsersByLastLogin(Date lastLoginDate) {
		return (Long) manager.createQuery("select count(u) from User u where " +
		" u.lastLogin >= :ldate ").setParameter("ldate", lastLoginDate).getSingleResult();
	}

	@Override
	public List<User> getUsersByLastLogin(Date lastLoginDate) {
		return manager.createQuery("select u from User u where " +
			" u.lastLogin >= :ldate order by u.lastLogin desc").
			setParameter("ldate", lastLoginDate).getResultList();
	}

	@Override
	public List<User> getUsersByLastLogin(Date lastLoginDate, int firstResult, int maxResult) {
		return manager.createQuery("select u from User u where " +
		" u.lastLogin >= :ldate order by u.lastLogin desc").
		setParameter("ldate", lastLoginDate).setFirstResult(firstResult).setMaxResults(maxResult).getResultList();
	}

	@Override
	public List<User> getInactiveUsers() {
		return manager.createQuery("select u from User u where u.active=:active").setParameter("active", false).getResultList();
	}

	@Override
	public List<FileHeader> searchFileByFilename(String filename) {
		return manager.createQuery("select f from FileHeader f where f.name=:name").setParameter("name", filename).getResultList();
	}

	@Override
	public Long getBandwithUsed(UserClass userClass, Date date) {
		Long result;
		if (userClass == null)
			result = (Long) manager.createQuery("select sum(ai.bandwidthUsed)" +
					" from AccountingInfo ai where ai.dateFrom<=:date and " +
					"ai.dateTo>:date").
					setParameter("date", date).
					getSingleResult();
		else
			result = (Long) manager.createQuery("select sum(ai.bandwidthUsed)" +
					" from AccountingInfo ai where ai.user.userClass.id=:id " +
					"and ai.dateFrom<=:date and ai.dateTo>:date").
					setParameter("date", date).
					setParameter("id", userClass.getId()).
					getSingleResult();
		if (result == null)
			result = 0L;
		return result;
	}

	@Override
	public List<AccountingInfo> getAccountingInfo(User user) {
		List<AccountingInfo> ai = new ArrayList<AccountingInfo>();
		try {
			ai =  manager.createQuery("select ai from AccountingInfo ai " +
				"where ai.user=:user")
				.setParameter("user", user)
				.getResultList();
		}
		catch (NoResultException e) {}
		return ai;
	}

	@Override
	public AccountingInfo getAccountingInfo(User user, Date date) {
		AccountingInfo ai = null;
		try {
			ai = (AccountingInfo) manager.createQuery("select ai from AccountingInfo ai " +
				"where ai.user=:user and ai.dateFrom<=:date and ai.dateTo>:date")
				.setParameter("user", user)
				.setParameter("date", date)
				.getSingleResult();
		}
		catch (NoResultException e) {
			// If not found, that means that there is no accounting info noted
			// for given time. So return a 0 as an answer.
			ai = new AccountingInfo(user, date, date);
		}
		return ai;
	}

	@Override
	public List<FileHeader> getFilesPermittedForGroup(Long userId, Long groupId) {
		return manager.createQuery("select distinct f from FileHeader f LEFT JOIN f.permissions p " +
					"where f.owner.id=:userId and f.deleted = false and p.group.id=:groupId ").
					setParameter("userId", userId).setParameter("groupId", groupId).getResultList();
	}
	
	@Override
	public List<Folder> getSharingFoldersForUser(Long userId) {
		return manager.createQuery("select distinct f from Folder f " +
					"LEFT JOIN f.permissions p where  " +
					" (p.user.id=:userId or p.group.id in (select distinct gg.id " +
					"from Group gg join gg.members memb where memb.id=:userId))) ").
					setParameter("userId", userId).getResultList();
	}
	
	@Override
	public List<FileHeader> getSharingFilesForUser(Long userId) {
		List<FileHeader> users = manager.createQuery("select distinct f from FileHeader f " +
					"LEFT JOIN f.permissions p where " +
					" (p.user.id=:userId or p.group.id in (select distinct gg.id from Group gg " +
					"join gg.members memb where memb.id=:userId)))").
					setParameter("userId", userId).getResultList();
		return users;
    }

    @Override
    public FileHeader getFileForIndexing(Long id) throws ObjectNotFoundException {
        FileHeader h = getEntityById(FileHeader.class, id);
        h.getFileTags().size();
        h.getPermissions().size();
        return h;
    }
	
	@Override 
	public List<Group> getGroupsContainingUser(Long userId){
		List<Group> groups = manager.createQuery("select distinct gg " +
					"from Group gg join gg.members memb where memb.id=:userId)").setParameter("userId", userId).getResultList();
		return groups;
	}
	
	@Override
	public List<FileUploadStatus> getUploadStatus(Long userId){
		List<FileUploadStatus> res = manager.createQuery("select f from FileUploadStatus f where f.owner.id=:userId").setParameter("userId", userId).getResultList();
		return res;
	}
	@Override
	public int deletePermissionsNotCorrespondingToFilesAndFolders(Long userId){
		return manager.createNativeQuery("delete from permission where user_id=:userId and id not in(select permissions_id from fileheader_permission) and id not in(select permissions_id from folder_permission)").setParameter("userId", userId).executeUpdate();
	}
	
	public List<UserLogin> getLoginsForUser (Long userId){
		List<UserLogin> res = manager
								.createQuery("select ul from UserLogin ul where ul.user.id=:userId " +
											" order by ul.loginDate desc")											
								.setParameter("userId", userId)
								.setMaxResults(2)								
								.getResultList();
		return res;									
	}

}
