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
package gr.ebs.gss.server.domain;

import gr.ebs.gss.server.domain.dto.GroupDTO;

import java.io.Serializable;
import java.util.Random;
import java.util.Set;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;
import com.google.code.morphia.annotations.Version;


/**
 * A group of users of the GSS service.
 *
 * @author past
 */
@Entity
public class Group  implements Serializable {

	/**
	 * The persistence ID of the object.
	 * XXX: we must generate unique ids ourselves, if type is not ObjectId,
	 * so we do it in the constructor
	 */
	@Id
	private Long id;

	/**
	 * XXX: The constructor is only necessary for enforcing unique ids. If/When
	 * id is converted to ObjectId this will no longer be necessary.
	 */
	public Group() {
		id = new Random().nextLong();
	}

	/**
	 * Version field for optimistic locking.
	 */
	@SuppressWarnings("unused")
	@Version
	private long version;

	/**
	 * The audit information.
	 */
	@Embedded
	private AuditInfo auditInfo;

	/**
	 * The name of the group.
	 */
	private String name;

	/**
	 * The user that owns this group.
	 */
	@Reference
	private User owner;

	/**
	 * The set of users that belong to this group.
	 */
	@Reference
	private Set<User> members;

	/**
	 * A constructor that creates a group with the specified name.
	 * XXX: If/When id is converted to ObjectId setting it here will no longer
	 * be necessary.
	 */
	public Group(String aName) {
		name = aName;
		id = new Random().nextLong();
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Retrieve the id.
	 *
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Retrieve the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Modify the name.
	 *
	 * @param newName the name to set
	 */
	public void setName(String newName) {
		name = newName;
	}

	/**
	 * Retrieve the owner.
	 *
	 * @return the owner
	 */
	public User getOwner() {
		return owner;
	}

	/**
	 * Modify the owner.
	 *
	 * @param newOwner the owner to set
	 */
	public void setOwner(User newOwner) {
		owner = newOwner;
	}

	/**
	 * Retrieve the members.
	 *
	 * @return the members
	 */
	public Set<User> getMembers() {
		return members;
	}

	/**
	 * Replace the member set.
	 *
	 * @param newMembers the new members
	 */
	public void setMembers(Set<User> newMembers) {
		members = newMembers;
	}

	/**
	 * Retrieve the audit info.
	 *
	 * @return the audit info
	 */
	public AuditInfo getAuditInfo() {
		return auditInfo;
	}

	/**
	 * Modify the audit info.
	 *
	 * @param newAuditInfo the new audit info
	 */
	public void setAuditInfo(AuditInfo newAuditInfo) {
		auditInfo = newAuditInfo;
	}

	/**
	 * Returns a Data Transfer Object for this Group.
	 *
	 * @return GroupDTO
	 */
	public GroupDTO getDTO() {
		GroupDTO g = new GroupDTO();
		g.setId(id);
		g.setName(name);
		g.setOwner(owner.getDTO());
		for (User u : members)
			g.getMembers().add(u.getDTO());
		return g;
	}

	/**
	 * Checks if this groups contains the specified user.
	 *
	 * @param user
	 * @return boolean
	 */
	public boolean contains(User user) {
		return members.contains(user);
	}

	/**
	 * Removes the specified member from the group.
	 */
	public void removeMember(User member) {
		getMembers().remove(member);
		member.getGroupsMember().remove(this);
	}

	/**
	 * Adds the specified member to the group.
	 */
	public void addMember(User member) {
		getMembers().add(member);
		member.getGroupsMember().add(this);
	}

	@Override
	public int hashCode() {
		return 31 + (id == null ? 0 : id.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Group other = (Group) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
