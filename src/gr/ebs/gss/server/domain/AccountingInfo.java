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

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Reference;
import com.google.code.morphia.annotations.Version;


/**
 * This class holds information about bandwidth usage by users.
 * This information is broken down in time periods.
 */
@Entity
public class AccountingInfo  implements Serializable {
	/**
	 * The persistence ID of the object.
	 * XXX: we must generate unique ids ourselves, if type is not ObjectId,
	 * so we do it in the constructor
	 */
	@SuppressWarnings("unused")
	@Id
	private Long id;

	/**
	 * Version field for optimistic locking.
	 */
	@SuppressWarnings("unused")
	@Version
	private long version;

	/**
	 * The user whose usage we are noting. We can never change it after
	 * creation.
	 */
	@Reference
	private User user;

	/**
	 * The start of the time period. We can never change it after
	 * creation.
	 */
	@Indexed
	private Date dateFrom;

	/**
	 * The end of the time period. We can never change it after
	 * creation.
	 */
	@Indexed
	private Date dateTo;

	/**
	 * The usage itself, in bytes.
	 */
	private long bandwidthUsed = 0;

	/**
	 * XXX: The constructor is only necessary for enforcing unique ids. If/When
	 * id is converted to ObjectId this will no longer be necessary.
	 */
	public AccountingInfo() {
		id = new Random().nextLong();
	}

	/**
	 * XXX: If/When id is converted to ObjectId the random number generation
	 * will no longer be necessary.
	 */
	public AccountingInfo(User _user, Date _dateFrom, Date _dateTo) {
		id = new Random().nextLong();
		user = _user;
		dateFrom = _dateFrom;
		dateTo = _dateTo;
	}

	/**
	 * Retrieve the user
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Retrieve the start of the time period.
	 * @return the start of the time period
	 */
	public Date getDateFrom() {
		return dateFrom;
	}

	/**
	 * Retrieve the end of the time period.
	 * @return the end of the time period
	 */
	public Date getDateTo() {
		return dateTo;
	}

	/**
	 * Retrieve the bandwidth used.
	 * @return the bandwidth used
	 */
	public long getBandwidthUsed() {
		return bandwidthUsed;
	}

	/**
	 * Update bandwidth used upwards or downwards with new amount.
	 * @param bandwidthDiff The amount by which to update;
	 * positive for increase, negative for decrease.
	 */
	public void updateBandwidth(long bandwidthDiff) {
		bandwidthUsed += bandwidthDiff;
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
		AccountingInfo other = (AccountingInfo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
