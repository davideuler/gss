package gr.ebs.gss.admin.client;

import gr.ebs.gss.common.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.common.exceptions.ObjectNotFoundException;
import gr.ebs.gss.common.exceptions.RpcException;
import gr.ebs.gss.common.dto.FileBodyDTO;
import gr.ebs.gss.common.dto.FileHeaderDTO;
import gr.ebs.gss.common.dto.PermissionDTO;
import gr.ebs.gss.common.dto.StatsDTO;
import gr.ebs.gss.common.dto.SystemStatsDTO;
import gr.ebs.gss.common.dto.UserClassDTO;
import gr.ebs.gss.common.dto.UserDTO;

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
