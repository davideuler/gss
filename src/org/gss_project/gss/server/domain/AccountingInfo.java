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
package org.gss_project.gss.server.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * This class holds information about bandwidth usage by users.
 * This information is broken down in time periods.
 */
@Entity
@Table(name="accountinginfo", uniqueConstraints=@UniqueConstraint(columnNames={"user_id", "dateFrom", "dateTo"}))
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class AccountingInfo  implements Serializable{

	/**
	 * The persistence ID of the object.
	 */
	@SuppressWarnings("unused")
	@Id
	@GeneratedValue
	private Long id;

	/**
	 * Version field for optimistic locking.
	 * XXX: the columnDefinition is postgres specific, if deployment database is changed this shall be changed too
	 */
	@SuppressWarnings("unused")
	@Version
	@Column(columnDefinition=" integer DEFAULT 0")
	private int version;

	/**
	 * The user whose usage we are noting. We can never change it after
	 * creation.
	 */
	@ManyToOne
	@JoinColumn(name="user_id", updatable = false, nullable = false)
	private User user;

	/**
	 * The start of the time period. We can never change it after
	 * creation.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="dateFrom", updatable = false, nullable = false)
	private Date dateFrom;


	/**
	 * The end of the time period. We can never change it after
	 * creation.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="dateTo", updatable = false, nullable = false)
	private Date dateTo;


	/**
	 * The usage itself, in bytes.
	 */
	@Column(nullable = false)
	private long bandwidthUsed = 0;


	/**
	 * Default constructor. Required by Hibernate,
	 * but shouldn't be called.
	 */
	@SuppressWarnings("unused")
	private AccountingInfo() {
	}


	/**
	 * Constructor
	 */
	public AccountingInfo(User _user, Date _dateFrom, Date _dateTo) {
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

}
