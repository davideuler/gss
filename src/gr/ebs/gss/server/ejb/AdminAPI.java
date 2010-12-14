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
package gr.ebs.gss.server.ejb;

import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.server.domain.dto.FileBodyDTO;
import gr.ebs.gss.server.domain.dto.FileHeaderDTO;
import gr.ebs.gss.server.domain.dto.PermissionDTO;
import gr.ebs.gss.server.domain.dto.StatsDTO;
import gr.ebs.gss.server.domain.dto.SystemStatsDTO;
import gr.ebs.gss.server.domain.dto.UserClassDTO;
import gr.ebs.gss.server.domain.dto.UserDTO;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;


/**
 * @author kman
 *
 */
@Local
public interface AdminAPI {

	public List<UserDTO> searchUsers(String query);

	public StatsDTO getUserStatistics(Long userId) throws ObjectNotFoundException;

	public UserDTO getUser(Long userId) throws ObjectNotFoundException;

	public void toggleActiveUser(Long userId) throws ObjectNotFoundException;

	public FileHeaderDTO getFile(String uri) throws ObjectNotFoundException;

	public void setFilePermissions(String uri, Set<PermissionDTO> permissions) throws ObjectNotFoundException;

	public SystemStatsDTO getSystemStatistics();

	public List<UserDTO> getLastLoggedInUsers(Date lastLoginDate);

	List<FileBodyDTO> getVersions(Long userId, Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException;

	UserDTO getUser(String username) throws ObjectNotFoundException;

	FileHeaderDTO getFile(Long fileId) throws ObjectNotFoundException;

	List<FileHeaderDTO> getFiles(String uri) throws ObjectNotFoundException, InsufficientPermissionsException;

	List<UserDTO> getUsersWaitingActivation();

	void changeUserClass(Long userId, Long userClassId) throws ObjectNotFoundException;

	List<UserClassDTO> getUserClasses();

	void saveOrUpdateUserClass(UserClassDTO dto) throws ObjectNotFoundException;

	void removeUserClass(UserClassDTO dto) throws ObjectNotFoundException;

	public List<FileHeaderDTO> searchFileByFilename(String fileName);

	public void removeUser(Long userId) throws ObjectNotFoundException, InsufficientPermissionsException;

	/**
	 * 
	 */
	void fixSharedFlagForAllFoldersAndFiles();

}
