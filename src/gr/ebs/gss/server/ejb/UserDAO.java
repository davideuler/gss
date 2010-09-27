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

import gr.ebs.gss.server.domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.code.morphia.DAO;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;


/**
 * The Data Access Object for User objects.
 *
 * @author past
 */
public class UserDAO extends DAO<User, Long> {
	/**
	 * The Morphia datastore instance.
	 */
	private final Datastore ds;

	/**
	 * Construct a UserDAO object with the provided datastore.
	 */
	@Inject
	public UserDAO(Datastore aStore) {
		super(aStore);
		ds = aStore;
	}

	/**
	 * Search the system for a user with the specified username.
	 * If no such user is found, the method returns null.
	 *
	 * @param username the username to search for
	 * @return the User object with the specified username
	 */
	public User findUser(String username) {
		return ds.find(User.class, "username", username).get();
	}

	/**
	 * Returns a list of users matching specified username.
	 */
	public List<User> getUsersByUserName(String username) {
		Pattern pattern = Pattern.compile("^" + username);
		return createQuery().filter("username", pattern).asList();
	}

	/**
	 * Returns a list of users sharing folders to the specified user.
	 * TODO: implement this
	 * @param user the User
	 * @return the list of users sharing files to selected user
	 */
	public List<User> getUsersSharingFoldersForUser(User user) {
//		return manager.createQuery("select distinct f.owner from Folder f " +
//					"LEFT JOIN f.permissions p where f.owner.id != :userId and f.deleted=false " +
//					"and (p.user.id=:userId or p.group.id in (select distinct gg.id " +
//					"from Group gg join gg.members memb where memb.id=:userId))) ").
//					setParameter("userId", userId).getResultList();
		return new ArrayList<User>();
	}

	/**
	 * Returns a list of users sharing files to specified user
	 * TODO: implement this
	 * @param user the User
	 * @return the list of users sharing files to selected user
	 */
	public List<User> getUsersSharingFilesForUser(User user) {
//		List<User> users = manager.createQuery("select distinct f.owner from FileHeader f " +
//					"LEFT JOIN f.permissions p where f.owner.id != :userId and f.deleted=false " +
//					"and (p.user.id=:userId or p.group.id in (select distinct gg.id from Group gg " +
//					"join gg.members memb where memb.id=:userId)))").
//					setParameter("userId", userId).getResultList();
//		return users;
		return new ArrayList<User>();
	}
}
