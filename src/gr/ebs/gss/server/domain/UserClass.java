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

import gr.ebs.gss.server.domain.dto.UserClassDTO;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Version;

/**
 * A group of users with common attributes.
 *
 * @author droutsis
 */
@Entity
public class UserClass  implements Serializable{

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
	private int version;

	/**
	 * A name for this class.
	 */
	private String name;

	/**
	 * The disk quota of this user class.
	 */
	private long quota;

	/**
	 * The users belonging to this class
	 */
//	@OneToMany(cascade = CascadeType.ALL, mappedBy = "userClass")
	private List<User> users;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String aName) {
		name = aName;
	}

	public long getQuota() {
		return quota;
	}

	public void setQuota(long aQuota) {
		quota = aQuota;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Return a new Data Transfer Object for this user class.
	 *
	 * @return a new DTO with the same contents as this object
	 */
	public UserClassDTO getDTO() {
		UserClassDTO u = new UserClassDTO();
		u.setId(id);
		u.setName(name);
		u.setQuota(quota);
		for (final User user : users)
			u.getUsers().add(user.getDTO());
		return u;
	}

	/**
	 * Return a new Data Transfer Object for this user class.
	 *
	 * @return a new DTO with the same contents as this object
	 */
	public UserClassDTO getDTOWithoutUsers() {
		UserClassDTO u = new UserClassDTO();
		u.setId(id);
		u.setName(name);
		u.setQuota(quota);
		return u;
	}

	/**
	 * Return the quota size in a humanly readable form.
	 */
	public String getQuotaAsString() {
		if (quota < 1024)
			return String.valueOf(quota) + " B";
		else if (quota < 1024*1024)
			return getSize(quota, 1024D) + " KB";
		else if (quota < 1024*1024*1024)
			return getSize(quota,(1024D*1024D)) + " MB";
		return getSize(quota , (1024D*1024D*1024D)) + " GB";
	}

	private String getSize(Long size, Double divisor){
		DecimalFormat formatter = new DecimalFormat("######");
		return formatter.format((Double) (size.doubleValue()/divisor));
	}
}
