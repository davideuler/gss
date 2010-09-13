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
package gr.ebs.gss.server.domain;

import java.io.Serializable;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Version;

/**
 * The class that holds information about a user invitation for the system.
 *
 * @author past
 */
@Entity
public class Invitation implements Serializable {
	/**
	 * The persistence ID of the object.
	 */
	@Id
	private Long id;

	/**
	 * Version field for optimistic locking.
	 */
	@SuppressWarnings("unused")
	@Version
	private long version;

	/**
	 * The invitation code.
	 */
//	@Column(unique = true)
	private String code;

	/**
	 * The first name of the user.
	 */
	private String firstname;

	/**
	 * The last name of the user.
	 */
	private String lastname;

	/**
	 * The full name of the user.
	 */
	private String name;

	/**
	 * The e-mail address of the user.
	 */
	private String email;

	/**
	 * The user that used this invitation.
	 */
//	@ManyToOne
//	@JoinColumn
	private User user;

	/**
	 * Retrieve the firstname.
	 *
	 * @return the firstname
	 */
	public String getFirstname() {
		return firstname;
	}

	/**
	 * Modify the firstname.
	 *
	 * @param newFirstname the firstname to set
	 */
	public void setFirstname(final String newFirstname) {
		firstname = newFirstname;
	}

	/**
	 * Retrieve the lastname.
	 *
	 * @return the lastname
	 */
	public String getLastname() {
		return lastname;
	}

	/**
	 * Modify the lastname.
	 *
	 * @param newLastname the lastname to set
	 */
	public void setLastname(final String newLastname) {
		lastname = newLastname;
	}

	/**
	 * Retrieve the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name != null ? name : firstname + " " + lastname;
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
	 * Retrieve the email.
	 *
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Modify the email.
	 *
	 * @param newEmail the email to set
	 */
	public void setEmail(final String newEmail) {
		email = newEmail;
	}

	/**
	 * Retrieve the code.
	 *
	 * @return the code
	 */
	public String getCode() {
		return code;
	}


	/**
	 * Modify the code.
	 *
	 * @param aCode the code to set
	 */
	public void setCode(String aCode) {
		code = aCode;
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
	 * Retrieve the user.
	 *
	 * @return the user
	 */
	public User getUser() {
		return user;
	}


	/**
	 * Modify the user.
	 *
	 * @param aUser the user to set
	 */
	public void setUser(User aUser) {
		user = aUser;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Invitation)) return false;
		Invitation invite = (Invitation) o;
		return invite.getCode().equals(code) && invite.getName().equals(getName());
	}

	@Override
	public int hashCode() {
		return 37 * code.hashCode() + getName().hashCode();
	}
}
