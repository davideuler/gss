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
package gr.ebs.gss.server.domain;

import gr.ebs.gss.server.domain.dto.GroupDTO;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A group of users of the GSS service.
 *
 * @author past
 */
@Entity
@Table(name = "GSS_Group")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Group  implements Serializable{

	/**
	 * The persistence ID of the object.
	 */
	@Id
	@GeneratedValue
	private Long id;

	/**
	 * Version field for optimistic locking.
	 */
	@SuppressWarnings("unused")
	@Version
	private int version;

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
	@ManyToOne(optional = false)
	@JoinColumn(nullable = false)
	private User owner;

	/**
	 * The set of users that belong to this group.
	 */
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(joinColumns = {@JoinColumn(nullable = false)}, inverseJoinColumns = {@JoinColumn(nullable = false)})
	private Set<User> members;

	@OneToMany(mappedBy="group", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	private Set<Permission> permissions;

	/**
	 * A default constructor.
	 */
	public Group() {
		// Empty.
	}


	/**
	 * Retrieve the permissions.
	 *
	 * @return the permissions
	 */
	public Set<Permission> getPermissions() {
		return permissions;
	}


	/**
	 * Modify the permissions.
	 *
	 * @param permissions the permissions to set
	 */
	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	/**
	 * A constructor that creates a group with the specified name.
	 *
	 * @param aName
	 */
	public Group(final String aName) {
		name = aName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
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
	public void setName(final String newName) {
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
	public void setOwner(final User newOwner) {
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
	public void setMembers(final Set<User> newMembers) {
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
	public void setAuditInfo(final AuditInfo newAuditInfo) {
		auditInfo = newAuditInfo;
	}

	/**
	 * Returns a Data Transfer Object for this Group
	 *
	 * @return GroupDTO
	 */
	public GroupDTO getDTO() {
		final GroupDTO g = new GroupDTO();
		g.setId(id);
		g.setName(name);
		g.setOwner(owner.getDTO());
		for (final User u : members)
			g.getMembers().add(u.getDTO());
		return g;
	}

	/**
	 * Checks if this groups contains the specified user
	 *
	 * @param user
	 * @return boolean
	 */
	public boolean contains(final User user) {
		return members.contains(user);
	}

	public void removeMemberFromGroup(final User member){
		getMembers().remove(member);
		member.getGroupsMember().remove(this);
	}


}
