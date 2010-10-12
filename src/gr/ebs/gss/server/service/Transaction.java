/*
 * Copyright 2010 Electronic Business Systems Ltd.
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
package gr.ebs.gss.server.service;

import gr.ebs.gss.server.domain.AccountingInfo;
import gr.ebs.gss.server.domain.FileHeader;
import gr.ebs.gss.server.domain.FileUploadStatus;
import gr.ebs.gss.server.domain.Folder;
import gr.ebs.gss.server.domain.Group;
import gr.ebs.gss.server.domain.Nonce;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.domain.UserClass;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.code.morphia.AbstractEntityInterceptor;
import com.google.code.morphia.mapping.Mapper;
import com.google.inject.Inject;
import com.mongodb.DBObject;

/**
 * A simple transaction handler implementation for simplifying datastore
 * persistence.
 *
 * @author  past
 */
public class Transaction extends AbstractEntityInterceptor {
	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(Transaction.class);

	/**
	 * An ArrayList that holds references to the objects that must be
	 * persisted at the end of this thread's transaction.
	 */
	private static final ThreadLocal<ArrayList> dirtyList = new ThreadLocal<ArrayList>() {
		@Override
		protected ArrayList initialValue() {
			return new ArrayList();
		}
	};

	/**
	 * An ArrayList that holds references to the objects that have been loaded
	 * from the datastore since this transaction was started.
	 */
	private static final ThreadLocal<HashMap> sessionCache = new ThreadLocal<HashMap>() {
		@Override
		protected HashMap initialValue() {
			return new HashMap();
		}
	};

	@Inject
	public Transaction(UserDAO aUserDao, UserClassDAO aUserClassDao,
			FolderDAO aFolderDao, FileDAO aFileDao,
			AccountingDAO anAccountingDao, FileUploadDAO aFileUploadDao,
			GroupDAO aGroupDao, NonceDAO aNonceDao) {
		super();
		userDao = aUserDao;
		userClassDao = aUserClassDao;
		folderDao = aFolderDao;
		fileDao = aFileDao;
		accountingDao = anAccountingDao;
		fileUploadDao = aFileUploadDao;
		groupDao = aGroupDao;
		nonceDao = aNonceDao;
	}

	/**
	 * Injected reference to the UserDAO.
	 */
	private UserDAO userDao;

	/**
	 * Injected reference to the UserClassDAO.
	 */
	private UserClassDAO userClassDao;

	/**
	 * Injected reference to the FolderDAO.
	 */
	private FolderDAO folderDao;

	/**
	 * Injected reference to the FileDAO.
	 */
	private FileDAO fileDao;

	/**
	 * Injected reference to the AccountingDAO.
	 */
	private AccountingDAO accountingDao;

	/**
	 * Injected reference to the FileUploadDAO.
	 */
	private FileUploadDAO fileUploadDao;

	/**
	 * Injected reference to the GroupDAO.
	 */
	private GroupDAO groupDao;

	/**
	 * Injected reference to the NonceDAO.
	 */
	private NonceDAO nonceDao;

	/**
	 * Stores the provided object to the dirty list, in order to be persisted
	 * on commit. The insertion order is preserved, but duplicates are
	 * eliminated, so each duplicate simply replaces the previous entry in the
	 * list.
	 */
	public void save(Object newObject) {
		ArrayList d = dirtyList.get();
		if (!d.contains(newObject))
			d.add(newObject);
		else
			d.set(d.indexOf(newObject), newObject);
		dirtyList.set(d);
	}

	/**
	 * Persists the changed objects in the dirty list.
	 */
	public void commit() {
		ArrayList d = dirtyList.get();
		if (logger.isDebugEnabled())
			logger.debug("Commiting dirty list: " + d);
		try {
			for (Object o: d)
				if (o instanceof FileHeader) {
					FileHeader file = (FileHeader) o;
					fileDao.save(file);
				} else if (o instanceof Folder) {
					Folder folder = (Folder) o;
					folderDao.save(folder);
				} else if (o instanceof User) {
					User user = (User) o;
					userDao.save(user);
				} else if (o instanceof Group) {
					Group group = (Group) o;
					groupDao.save(group);
				} else if (o instanceof UserClass) {
					UserClass userClass = (UserClass) o;
					userClassDao.save(userClass);
				} else if (o instanceof AccountingInfo) {
					AccountingInfo accounting = (AccountingInfo) o;
					accountingDao.save(accounting);
				} else if (o instanceof FileUploadStatus) {
					FileUploadStatus fileUpload = (FileUploadStatus) o;
					fileUploadDao.save(fileUpload);
				} else if (o instanceof Nonce) {
					Nonce nonce = (Nonce) o;
					nonceDao.save(nonce);
				}
		} catch (RuntimeException e) {
//			rollback();
		} finally {
			dirtyList.remove();
			sessionCache.remove();
		}
	}

	/**
	 * A rollback simply results in clearing the list of pending updates. This
	 * should help avoid memory leaks.
	 */
	public void rollback() {
		logger.error("Rolling back changes in dirty list");
		HashMap sc = sessionCache.get();
		ArrayList d = dirtyList.get();
		for (Object o: d)
			if (o instanceof FileHeader) {
				FileHeader file = (FileHeader) o;
				FileHeader orig = (FileHeader) sc.get(file.getId().toString());
				if (orig == null)
					throw new RuntimeException(file.getClass().getSimpleName() +
							"#" + file.getId() + " in dirty list but not in session cache!");
				fileDao.save(orig);
			} else if (o instanceof Folder) {
				Folder folder = (Folder) o;
				Folder orig = (Folder) sc.get(folder.getId().toString());
				if (orig == null)
					throw new RuntimeException(folder.getClass().getSimpleName() +
								"#" + folder.getId() + " in dirty list but not in session cache!");
				folderDao.save(orig);
			} else if (o instanceof User) {
				User user = (User) o;
				User orig = (User) sc.get(user.getId().toString());
				if (orig == null)
					throw new RuntimeException(user.getClass().getSimpleName() +
								"#" + user.getId() + " in dirty list but not in session cache!");
				userDao.save(orig);
			} else if (o instanceof Group) {
				Group group = (Group) o;
				Group orig = (Group) sc.get(group.getId().toString());
				if (orig == null)
					throw new RuntimeException(group.getClass().getSimpleName() +
								"#" + group.getId() + " in dirty list but not in session cache!");
				groupDao.save(orig);
			} else if (o instanceof UserClass) {
				UserClass userClass = (UserClass) o;
				UserClass orig = (UserClass) sc.get(userClass.getId().toString());
				if (orig == null)
					throw new RuntimeException(userClass.getClass().getSimpleName() +
								"#" + userClass.getId() + " in dirty list but not in session cache!");
				userClassDao.save(orig);
			} else if (o instanceof AccountingInfo) {
				AccountingInfo accounting = (AccountingInfo) o;
				AccountingInfo orig = (AccountingInfo) sc.get(accounting.getId().toString());
				if (orig == null)
					throw new RuntimeException(accounting.getClass().getSimpleName() +
								"#" + accounting.getId() + " in dirty list but not in session cache!");
				accountingDao.save(orig);
			} else if (o instanceof FileUploadStatus) {
				FileUploadStatus fileUpload = (FileUploadStatus) o;
				FileUploadStatus orig = (FileUploadStatus) sc.get(fileUpload.getId().toString());
				if (orig == null)
					throw new RuntimeException(fileUpload.getClass().getSimpleName() +
								"#" + fileUpload.getId() + " in dirty list but not in session cache!");
				fileUploadDao.save(orig);
			} else if (o instanceof Nonce) {
				Nonce nonce = (Nonce) o;
				Nonce orig = (Nonce) sc.get(nonce.getId().toString());
				if (orig == null)
					throw new RuntimeException(nonce.getClass().getSimpleName() +
								"#" + nonce.getId() + " in dirty list but not in session cache!");
				nonceDao.save(orig);
			}
		dirtyList.remove();
		sessionCache.remove();
	}

	@Override
	public void postLoad(Object entity, DBObject dbObj, Mapper mapr) {
		Object key = dbObj.get("_id");
		// Ignore entities without id, they are embedded objects.
		if (key == null) return;
		// Store the provided entity in the session cache, replacing any
		// previous instance.
		HashMap d = sessionCache.get();
		d.remove(key);
		d.put(key, entity);
		sessionCache.set(d);
	}
}