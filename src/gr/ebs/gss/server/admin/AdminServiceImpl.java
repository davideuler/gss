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
package gr.ebs.gss.server.admin;

import static gr.ebs.gss.server.configuration.GSSConfigurationFactory.getConfiguration;
import gr.ebs.gss.admin.client.AdminService;
import gr.ebs.gss.common.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.common.exceptions.ObjectNotFoundException;
import gr.ebs.gss.common.exceptions.RpcException;
import gr.ebs.gss.server.configuration.GSSConfigurationFactory;
import gr.ebs.gss.common.dto.FileBodyDTO;
import gr.ebs.gss.common.dto.FileHeaderDTO;
import gr.ebs.gss.common.dto.PermissionDTO;
import gr.ebs.gss.common.dto.StatsDTO;
import gr.ebs.gss.common.dto.SystemStatsDTO;
import gr.ebs.gss.common.dto.UserClassDTO;
import gr.ebs.gss.common.dto.UserDTO;
import gr.ebs.gss.server.ejb.AdminAPI;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AdminServiceImpl extends RemoteServiceServlet implements AdminService {
	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(AdminServiceImpl.class);

	/**
	 * A helper method that retrieves a reference to the AdminAPI bean and
	 * stores it for future use.
	 *
	 * @return an AdminAPI instance
	 * @throws RpcException in case an error occurs
	 */
	protected AdminAPI getService() throws RpcException {
		try {
			final Context ctx = new InitialContext();
			final Object ref = ctx.lookup(getConfiguration().getString("adminApiPath"));
			return (AdminAPI) PortableRemoteObject.narrow(ref, AdminAPI.class);
		} catch (final NamingException e) {
			logger.error("Unable to retrieve the AdminAPI EJB", e);
			throw new RpcException("An error occurred while contacting the naming service");
		}
	}

	@Override
	public List<UserDTO> getUsers() throws RpcException, ObjectNotFoundException {
		return getService().searchUsers("");
	}

	@Override
	public StatsDTO getUserStatistics(Long userId) throws RpcException, ObjectNotFoundException {
		return getService().getUserStatistics(userId);
	}



	@Override
	public void toggleActiveUser(Long userId) throws RpcException, ObjectNotFoundException {
		getService().toggleActiveUser(userId);

	}

	@Override
	public void setFilePermissions(String uri, Set<PermissionDTO> permissions) throws ObjectNotFoundException, RpcException {
		getService().setFilePermissions(uri, permissions);
	}

	@Override
	public List<UserDTO> searchUsers(String query) throws RpcException, ObjectNotFoundException {
		return getService().searchUsers(query);
	}

	@Override
	public SystemStatsDTO getSystemStatistics() throws RpcException {
		return getService().getSystemStatistics();
	}

	@Override
	public List<UserDTO> getLastLoggedInUsers(Date lastLoginDate) throws RpcException {
		return getService().getLastLoggedInUsers(lastLoginDate);
	}

	@Override
	public void logout() {
		getThreadLocalRequest().getSession().invalidate();
	}

	@Override
	public List<FileHeaderDTO> searchFiles(String query) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		if(query==null)
			throw new ObjectNotFoundException("Invalid query");
		String restUrl = GSSConfigurationFactory.getConfiguration().getString("restUrl");
		if(query.startsWith(restUrl))
			query= query.substring(restUrl.length(),query.length());
		if(query.indexOf("/")==-1)//do only a name search
			return getService().searchFileByFilename(query);
		return getService().getFiles(query);
	}

	@Override
	public UserDTO getUser(String username) throws ObjectNotFoundException, RpcException{
		return getService().getUser(username);
	}

	@Override
	public FileHeaderDTO getFile(long fileId) throws ObjectNotFoundException, RpcException {
		return getService().getFile(fileId);
	}

	@Override
	public List<FileBodyDTO> getVersions(Long userId, Long fileId) throws ObjectNotFoundException, InsufficientPermissionsException, RpcException {
		return getService().getVersions(userId, fileId);
	}

	@Override
	public List<UserDTO> getUsersWaitingActivation() throws RpcException {
		return getService().getUsersWaitingActivation();
	}

	@Override
	public void changeUserClass(Long userId, Long userClassId) throws RpcException, ObjectNotFoundException {
		getService().changeUserClass(userId, userClassId);

	}

	@Override
	public List<UserClassDTO> getUserClasses() throws RpcException {
		return getService().getUserClasses();
	}

	@Override
	public void saveOrUpdateUserClass(UserClassDTO userClass) throws RpcException, ObjectNotFoundException {
		getService().saveOrUpdateUserClass(userClass);
	}

	@Override
	public void removeUserClass(UserClassDTO userClass) throws RpcException, ObjectNotFoundException {
		getService().removeUserClass(userClass);
	}

	@Override
	public void removeUser(Long userId) throws RpcException, ObjectNotFoundException, InsufficientPermissionsException {
		getService().removeUser(userId);

	}

}
