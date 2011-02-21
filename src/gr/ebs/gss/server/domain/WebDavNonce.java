/*
 * Copyright (c) 2011. Electronic Business Systems Ltd
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
