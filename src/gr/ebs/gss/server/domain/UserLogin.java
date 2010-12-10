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

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
/**
 * 
 * The class that holds information about the login dates that user logins to the system.
 * 
 */
@Entity
@Table(name="userlogin", uniqueConstraints=@UniqueConstraint(columnNames={"user_id", "logindate"}))
public class UserLogin {

	/**
	 * The persistence ID of the object.
	 */
	@Id
	@GeneratedValue
	private Long id;
	
	private Date loginDate;
	
	/**
	 * The user to whom the login date belongs to
	 * 
	 */	
	@ManyToOne
	private User user;
	
	public UserLogin(){}
	
	/**
	 * Modify the id.
	 *
	 * @param _id the id to set
	 */
	public void setId(Long _id) {
		this.id = _id;
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
	 * Modify the user.
	 *
	 * @param aUser the user to set
	 */
	public void setUser(User aUser) {
		this.user = aUser;
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
	 * Modify the lastLoginDate.
	 *
	 * @param _lastLogin the lastLoginDate to set
	 */
	public void setLoginDate(Date _lastLogin) {
		this.loginDate = _lastLogin;
	}

	/**
	 * Retrieve the lastLoginDate.
	 *
	 * @return the lastLoginDate
	 */
	public Date getLoginDate() {
		return loginDate;
	}
	
}
