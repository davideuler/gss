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

import gr.ebs.gss.server.domain.Group;
import gr.ebs.gss.server.domain.User;

import java.util.List;

import com.google.code.morphia.DAO;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;


/**
 * The Data Access Object for Group objects.
 *
 * @author past
 */
public class GroupDAO extends DAO<Group, Long> {
	/**
	 * The Morphia datastore instance.
	 */
	private final Datastore ds;

	/**
	 * Construct a UserDAO object with the provided datastore.
	 */
	@Inject
	public GroupDAO(Datastore aStore) {
		super(aStore);
		ds = aStore;
	}

	/**
	 * Checks if a group with the specified name was defined by the provided
	 * user.
	 */
	public boolean existsGroup(User user, String name) {
		List<Group> tempList = ds.find(Group.class, "name", name).asList();
		for (Group group: tempList)
			if (user.getGroupsSpecified().contains(group))
				return true;
		return false;
	}

}
