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

import com.google.code.morphia.DAO;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;


/**
 * @author past
 *
 */
public class UserDAO extends DAO<User, String> {
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
		return ds.find(User.class).filter("username", username).get();
	}
}
