package gr.ebs.gss.admin.client;

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

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AdminServiceAsync {
	void getUsers(AsyncCallback<List<UserDTO>> callback);

	void getUserStatistics(Long userId, AsyncCallback<StatsDTO> callback);

	void toggleActiveUser(Long userId, AsyncCallback<Void> callback);

	void setFilePermissions(String uri, Set<PermissionDTO> permissions, AsyncCallback<Void> callback);

	void searchUsers(String query, AsyncCallback<List<UserDTO>> callback);

	void  getSystemStatistics(AsyncCallback<SystemStatsDTO> callback);

	void getLastLoggedInUsers(Date lastLoginDate, AsyncCallback<List<UserDTO>> callback);

	void logout(AsyncCallback<Void> callback);

	void searchFiles(String query, AsyncCallback<List<FileHeaderDTO>> callback);

	void getUser(String username, AsyncCallback<UserDTO> callback);

	void getFile(long fileId, AsyncCallback<FileHeaderDTO> callback);

	void getVersions(Long userId, Long fileId, AsyncCallback<List<FileBodyDTO>> callback);

	void getUsersWaitingActivation(AsyncCallback<List<UserDTO>> callback);

	void changeUserClass(Long userId, Long userClassId, AsyncCallback<Void> callback);

	void getUserClasses(AsyncCallback<List<UserClassDTO>> callback);

	void saveOrUpdateUserClass(UserClassDTO userClass, AsyncCallback<Void> callback);

	void removeUserClass(UserClassDTO userClass, AsyncCallback<Void> callback);

	void removeUser(Long userId, AsyncCallback<Void> callback);
}
