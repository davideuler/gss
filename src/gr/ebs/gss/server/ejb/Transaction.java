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
package gr.ebs.gss.server.ejb;

import gr.ebs.gss.server.domain.AccountingInfo;
import gr.ebs.gss.server.domain.FileHeader;
import gr.ebs.gss.server.domain.FileUploadStatus;
import gr.ebs.gss.server.domain.Folder;
import gr.ebs.gss.server.domain.Group;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.domain.UserClass;

import java.util.ArrayList;

import com.google.inject.Inject;

/**
 * A simple transaction handler implementation for simplifying datastore
 * persistence.
 *
 * @author  past
 */
public class Transaction {
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

	@Inject
	public Transaction(UserDAO aUserDao, UserClassDAO aUserClassDao,
			FolderDAO aFolderDao, FileDAO aFileDao,
			AccountingDAO anAccountingDao, FileUploadDAO aFileUploadDao,
			GroupDAO aGroupDao) {
		super();
		userDao = aUserDao;
		userClassDao = aUserClassDao;
		folderDao = aFolderDao;
		fileDao = aFileDao;
		accountingDao = anAccountingDao;
		fileUploadDao = aFileUploadDao;
		groupDao = aGroupDao;
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
	 * Stores the provided object to the dirty list, in order to be persisted
	 * on commit.
	 */
	public void save(Object o) {
		ArrayList d = dirtyList.get();
		d.add(o);
		dirtyList.set(d);
	}

	/**
	 * Persists the changed objects in the dirty list.
	 */
	public void commit() {
		ArrayList d = dirtyList.get();
		// TODO: eliminate duplicates
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
			}
		dirtyList.remove();
	}
}