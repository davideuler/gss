/*
 * Copyright 2011 Electronic Business Systems Ltd.
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
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * @author kman
 *
 */
@Entity
public class WebDavNonce {
	@Id
	String id;
	@Temporal(TemporalType.TIMESTAMP)
	Date issued;
    long nonceCount=0;
	
	/**
	 * Retrieve the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Modify the id.
	 *
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Retrieve the issued.
	 *
	 * @return the issued
	 */
	public Date getIssued() {
		return issued;
	}
	
	/**
	 * Modify the issued.
	 *
	 * @param issued the issued to set
	 */
	public void setIssued(Date issued) {
		this.issued = issued;
	}
	
	/**
	 * Retrieve the nonceCount.
	 *
	 * @return the nonceCount
	 */
	public long getNonceCount() {
		return nonceCount;
	}
	
	/**
	 * Modify the nonceCount.
	 *
	 * @param nonceCount the nonceCount to set
	 */
	public void setNonceCount(long nonceCount) {
		this.nonceCount = nonceCount;
	}
    
    
}
