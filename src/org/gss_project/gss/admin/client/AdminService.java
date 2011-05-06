package org.gss_project.gss.admin.client;

import org.gss_project.gss.common.exceptions.InsufficientPermissionsException;
import org.gss_project.gss.common.exceptions.ObjectNotFoundException;
import org.gss_project.gss.common.exceptions.RpcException;
import org.gss_project.gss.common.dto.FileBodyDTO;
import org.gss_project.gss.common.dto.FileHeaderDTO;
import org.gss_project.gss.common.dto.PermissionDTO;
import org.gss_project.gss.common.dto.StatsDTO;
import org.gss_project.gss.common.dto.SystemStatsDTO;
import org.gss_project.gss.common.dto.UserClassDTO;
import org.gss_project.gss.common.dto.UserDTO;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("admin")
public interface AdminService extends RemoteService {
	List<UserDTO> getUsers() throws RpcException, ObjectNotFoundException;

	List<UserDTO> searchUsers(String query) throws RpcException, ObjectNotFoundException;

	StatsDTO getUserStatistics(Long userId) throws RpcException, ObjectNotFoundException;

	void toggleActiveUser(Long userId) throws RpcException,ObjectNotFoundException;

	void setFilePermissions(String uri, Set<PermissionDTO> permissions)	throws RpcException, ObjectNotFoundException;

	SystemStatsDTO getSystemStatistics() throws RpcException,ObjectNotFoundException;

	public List<UserDTO> getLastLoggedInUsers(Date lastLoginDate) throws RpcException;

	public void logout();

	public List<FileHeaderDTO> searchFiles(String query) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException;

	UserDTO getUser(String username) throws ObjectNotFoundException, RpcException;

	FileHeaderDTO getFile(long fileId) throws ObjectNotFoundException, RpcException;

	public List<FileBodyDTO> getVersions(Long userId, Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException,RpcException;

	List<UserDTO> getUsersWaitingActivation() throws RpcException;

	void changeUserClass(Long userId,Long userClassId) throws RpcException,ObjectNotFoundException;

	List<UserClassDTO> getUserClasses() throws RpcException;

	void saveOrUpdateUserClass(UserClassDTO userClass) throws RpcException,ObjectNotFoundException;

	void removeUserClass(UserClassDTO userClass) throws RpcException,ObjectNotFoundException;

	void removeUser(Long userId) throws RpcException,ObjectNotFoundException,InsufficientPermissionsException;
	
}
