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
package gr.ebs.gss.server.domain.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.i18n.client.NumberFormat;


/**
 * @author kman
 *
 */
public class SystemStatsDTO implements Serializable{

	private Long userCount=0L;
	private Long fileCount=0L;
	private Long fileSize=0L;
	private Long lastWeekUsers=0L;
	private Long lastMonthUsers=0L;
	private Long bandwithUsed=0L;

	List<UserClassDTO> userClasses=new ArrayList<UserClassDTO>();

	/**
	 * Retrieve the lastWeekUsers.
	 *
	 * @return the lastWeekUsers
	 */
	public Long getLastWeekUsers() {
		return lastWeekUsers;
	}

	/**
	 * Modify the lastWeekUsers.
	 *
	 * @param theLastWeekUsers the lastWeekUsers to set
	 */
	public void setLastWeekUsers(Long theLastWeekUsers) {
		lastWeekUsers = theLastWeekUsers;
	}

	/**
	 * Retrieve the lastMonthUsers.
	 *
	 * @return the lastMonthUsers
	 */
	public Long getLastMonthUsers() {
		return lastMonthUsers;
	}

	/**
	 * Modify the lastMonthUsers.
	 *
	 * @param theLastMonthUsers the lastMonthUsers to set
	 */
	public void setLastMonthUsers(Long theLastMonthUsers) {
		lastMonthUsers = theLastMonthUsers;
	}

	/**
	 * Retrieve the userCount.
	 *
	 * @return the userCount
	 */
	public Long getUserCount() {
		return userCount;
	}

	/**
	 * Modify the userCount.
	 *
	 * @param theUserCount the userCount to set
	 */
	public void setUserCount(Long theUserCount) {
		userCount = theUserCount;
	}

	/**
	 * Retrieve the fileCount.
	 *
	 * @return the fileCount
	 */
	public Long getFileCount() {
		return fileCount;
	}

	/**
	 * Modify the fileCount.
	 *
	 * @param aFileCount the fileCount to set
	 */
	public void setFileCount(Long aFileCount) {
		fileCount = aFileCount;
	}

	/**
	 * Retrieve the fileSize.
	 *
	 * @return the fileSize
	 */
	public Long getFileSize() {
		return fileSize;
	}

	/**
	 * Modify the fileSize.
	 *
	 * @param aFileSize the fileSize to set
	 */
	public void setFileSize(Long aFileSize) {
		fileSize = aFileSize;
	}

	/**
	 * Retrieve the userClasses.
	 *
	 * @return the userClasses
	 */
	public List<UserClassDTO> getUserClasses() {
		return userClasses;
	}

	/**
	 * Retrieve the bandwithUsed.
	 *
	 * @return the bandwithUsed
	 */
	public Long getBandwithUsed() {
		return bandwithUsed;
	}

	/**
	 * Modify the bandwithUsed.
	 *
	 * @param aBandwithUsed the bandwithUsed to set
	 */
	public void setBandwithUsed(Long aBandwithUsed) {
		bandwithUsed = aBandwithUsed;
	}

	public String getBandwithUsedAsString() {
		if(fileSize==null)
			return 0+" B";
		if (bandwithUsed < 1024)
			return String.valueOf(bandwithUsed) + " B";
		else if (bandwithUsed <= 1024*1024)
			return getSize(bandwithUsed, 1024D) + " KB";
		else if (bandwithUsed <= 1024*1024*1024)
			return getSize(bandwithUsed,(1024D*1024D)) + " MB";
		return getSize(bandwithUsed , (1024D*1024D*1024D)) + " GB";
	}

	public String getFileSizeAsString() {
		if(fileSize==null)
			return 0+" B";
		if (fileSize < 1024)
			return String.valueOf(fileSize) + " B";
		else if (fileSize <= 1024*1024)
			return getSize(fileSize, 1024D) + " KB";
		else if (fileSize <= 1024*1024*1024)
			return getSize(fileSize,(1024D*1024D)) + " MB";
		return getSize(fileSize , (1024D*1024D*1024D)) + " GB";
	}

	private String getSize(Long size, Double division){
		Double res = Double.valueOf(size.toString())/division;
		NumberFormat nf = NumberFormat.getFormat("######.#");
		return nf.format(res);
	}
}
