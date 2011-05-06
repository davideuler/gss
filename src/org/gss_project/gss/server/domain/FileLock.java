/*
 * Copyright (c) 2011. Electronic Business Systems Ltd
 */
package org.gss_project.gss.server.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;

import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockInfo.LockDepth;
import com.bradmcevoy.http.LockInfo.LockScope;
import com.bradmcevoy.http.LockInfo.LockType;


/**
 * @author kman
 *
 */
@Entity
public class FileLock {
	@Id
	String id;
	public String lockedByUser;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="from_date")
	Date from;
    String tokenId;
    
    public LockScope scope;
    public LockType type;
    public LockDepth depth;
    
    Long seconds;
    @CollectionOfElements
    @IndexColumn(name="secId")
    Long[] otherSeconds;
    @Version
    private int version;
    
	/**
	 * 
	 */
	public FileLock() {
		// TODO Auto-generated constructor stub
	}
	
	public FileLock(String id,LockToken token){
		this.id=id;
		this.tokenId=token.tokenId;
		if(token.info!=null){
			this.depth=token.info.depth;
			this.scope=token.info.scope;
			this.type=token.info.type;
			this.lockedByUser=token.info.lockedByUser;
		}
		if(token.timeout!=null){
			this.seconds=token.timeout.getSeconds();
			this.otherSeconds=token.timeout.getOtherSeconds();
		}
		this.from=token.getFrom();
		
	}
	
    public LockToken toToken(){
    	LockToken res = new LockToken();
    	res.tokenId=tokenId;
    	LockInfo info = new LockInfo(scope,type,lockedByUser,depth);
    	LockTimeout timeout = new LockTimeout(seconds);
    	res.timeout=timeout;
    	res.info=info;
    	res.setFrom(from);
    	return res;
    }

	
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
	 * Retrieve the lockedByUser.
	 *
	 * @return the lockedByUser
	 */
	public String getLockedByUser() {
		return lockedByUser;
	}

	
	/**
	 * Modify the lockedByUser.
	 *
	 * @param lockedByUser the lockedByUser to set
	 */
	public void setLockedByUser(String lockedByUser) {
		this.lockedByUser = lockedByUser;
	}

	
	/**
	 * Retrieve the from.
	 *
	 * @return the from
	 */
	public Date getFrom() {
		return from;
	}

	
	/**
	 * Modify the from.
	 *
	 * @param from the from to set
	 */
	public void setFrom(Date from) {
		this.from = from;
	}

	
	/**
	 * Retrieve the tokenId.
	 *
	 * @return the tokenId
	 */
	public String getTokenId() {
		return tokenId;
	}

	
	/**
	 * Modify the tokenId.
	 *
	 * @param tokenId the tokenId to set
	 */
	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	
	/**
	 * Retrieve the scope.
	 *
	 * @return the scope
	 */
	public LockScope getScope() {
		return scope;
	}

	
	/**
	 * Modify the scope.
	 *
	 * @param scope the scope to set
	 */
	public void setScope(LockScope scope) {
		this.scope = scope;
	}

	
	/**
	 * Retrieve the type.
	 *
	 * @return the type
	 */
	public LockType getType() {
		return type;
	}

	
	/**
	 * Modify the type.
	 *
	 * @param type the type to set
	 */
	public void setType(LockType type) {
		this.type = type;
	}

	
	/**
	 * Retrieve the depth.
	 *
	 * @return the depth
	 */
	public LockDepth getDepth() {
		return depth;
	}

	
	/**
	 * Modify the depth.
	 *
	 * @param depth the depth to set
	 */
	public void setDepth(LockDepth depth) {
		this.depth = depth;
	}

	
	/**
	 * Retrieve the seconds.
	 *
	 * @return the seconds
	 */
	public Long getSeconds() {
		return seconds;
	}

	
	/**
	 * Modify the seconds.
	 *
	 * @param seconds the seconds to set
	 */
	public void setSeconds(Long seconds) {
		this.seconds = seconds;
	}

	
	/**
	 * Retrieve the otherSeconds.
	 *
	 * @return the otherSeconds
	 */
	public Long[] getOtherSeconds() {
		return otherSeconds;
	}

	
	/**
	 * Modify the otherSeconds.
	 *
	 * @param otherSeconds the otherSeconds to set
	 */
	public void setOtherSeconds(Long[] otherSeconds) {
		this.otherSeconds = otherSeconds;
	}
    
    
    
    
}
