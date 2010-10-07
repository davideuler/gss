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

import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.server.domain.Folder;
import gr.ebs.gss.server.domain.Group;
import gr.ebs.gss.server.domain.Permission;
import gr.ebs.gss.server.domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.code.morphia.DAO;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;


/**
 * The Data Access Object for Folder objects.
 *
 * @author past
 */
public class FolderDAO extends DAO<Folder, Long> {
	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(FolderDAO.class);

	/**
	 * The Morphia datastore instance.
	 */
	private final Datastore ds;

	private final UserDAO userDao;

	private final GroupDAO groupDao;

	/**
	 * Construct a FolderDAO object with the provided datastore.
	 */
	@Inject
	public FolderDAO(Datastore aStore, UserDAO aUserDao, GroupDAO aGroupDao) {
		super(aStore);
		ds = aStore;
		userDao = aUserDao;
		groupDao = aGroupDao;
	}

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
	public Folder getFolder(Long parentId, String name) throws ObjectNotFoundException {
		if (parentId == null)
			throw new ObjectNotFoundException("No parent folder specified");
		if (StringUtils.isEmpty(name))
			throw new IllegalArgumentException("No folder name specified");

		List<Folder> tempList = ds.find(Folder.class, "name", name).asList();
		// Fixup reference.
		for (Folder f: tempList) {
			// XXX: this is a workaround for a Morphia/Mongo driver bug and
			// should be removed after it is fixed upstream.
			if (f.getName() == null) {
				logger.debug("Bogus folder returned from Morphia with id " +
						f.getId());
				continue;
			}
			Folder parent = get(f.getParent().getId());
			if (parent.getId().equals(parentId)) {
				f.setParent(parent);
				return f;
			}
		}
		throw new ObjectNotFoundException("Folder not found");
	}

	/**
	 * Retrieves the root folder for the specified user. The caller must ensure
	 * that the userId exists.
	 *
	 * @param user the user
	 * @return the root folder
	 * @throws ObjectNotFoundException if no Folder was found
	 */
	public Folder getRootFolder(User user) throws ObjectNotFoundException {
		if (user == null)
			throw new ObjectNotFoundException("No user specified");

		Folder result = ds.find(Folder.class, "owner", user).
				filter("parent", null).get();
		// TODO: the following will be cleaner with Morphia 0.97, but it
		// produces bogus warnings currently (and not in debug like the above).
		// Folder result = ds.find(Folder.class, "owner", user).field("parent")
		//		.doesNotExist().get();
		if (result == null)
			throw new ObjectNotFoundException("Folder not found");
		return result;
	}

	/**
	 * Returns a list of deleted root folders of the specified user.
	 */
	public List<Folder> getDeletedRootFolders(User user) throws ObjectNotFoundException {
		if (user == null)
			throw new ObjectNotFoundException("No User specified");
		List<Folder> tempList = ds.find(Folder.class, "owner", user).filter("deleted", true).asList();
		// Fixup references.
		// TODO: verify that this works and is necessary
		for (Folder f: tempList)
			if (f.getParent() != null) {
				Folder parent = get(f.getParent().getId());
				f.setParent(parent);
			}
		List<Folder> results = new ArrayList<Folder>();
		for (Folder f: tempList)
			if (!f.getParent().isDeleted())
				results.add(f);
		return results;
	}

	/**
	 * Returns all the shared root folders of a user.
	 * TODO: implement this
	 * @param user the user
	 * @return the list of shared root folders
	 */
	public List<Folder> getSharedRootFolders(User user) {
/*		BasicDBObject groupq = new BasicDBObject("permissions.group", null);
		BasicDBObject userq = new BasicDBObject("permissions.user", "owner");
		List<BasicDBObject> groupOrUser = new ArrayList<BasicDBObject>(2);
		groupOrUser.add(groupq);
		groupOrUser.add(userq);
		BasicDBObject publicq = new BasicDBObject("readForAll", true);
		Query<Folder> query = ds.find(Folder.class, "owner", user).
				filter("deleted", false).
				filter("$or", groupOrUser).
				filter("$or", publicq);
		List<Folder> folders = query.asList();
// TODO:	select distinct f from Folder f " +
//			"LEFT JOIN f.permissions p where f.owner.id=:userId and f.deleted=false " +
//			"and (p.group.id != null or p.user.id != f.owner.id) or f.readForAll=true "
		List<Folder> result = new ArrayList<Folder>();
		for (Folder f: folders)
			if (!folders.contains(f.getParent()))
				result.add(f);
		return result;*/
		return new ArrayList<Folder>();
	}

	/**
	 * Checks if a folder with the specified name exists under the
	 * specified parent.
	 */
	public boolean exists(Long parentId, String name) {
		List<Folder> tempList = ds.find(Folder.class, "name", name).asList();
		for (Folder f: tempList) {
			Folder parent = get(f.getParent().getId());
			if (parent.getId().equals(parentId))
				return true;
		}
		return false;
	}

	/**
	 * Returns the user's folders with permissions for the specified group.
	 */
	public List<Folder> getFoldersPermittedForGroup(User user, Group group) {
		List<Folder> results = new ArrayList<Folder>();
		List<Folder> tempList = ds.find(Folder.class, "deleted", false).asList();
		List<Folder> tempList2 = new ArrayList<Folder>();
		// Compare owners.
		for (Folder f: tempList) {
			User owner = userDao.get(f.getOwner().getId());
			if (owner.equals(user))
				tempList2.add(f);
		}
		// Compare group permissions.
		for (Folder f: tempList) {
			Set<Permission> perms = f.getPermissions();
			for (Permission p: perms) {
				if (p.getGroup() == null) continue;
				Group g = groupDao.get(p.getGroup().getId());
				if (g.equals(group)) {
					results.add(f);
					break;
				}
			}
		}
		return results;
	}
}
