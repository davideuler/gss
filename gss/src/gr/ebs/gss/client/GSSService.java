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
package gr.ebs.gss.client;

import gr.ebs.gss.client.domain.FileHeaderDTO;
import gr.ebs.gss.client.domain.UploadStatusDTO;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.RpcException;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * The GSS service RPC interface.
 *
 * @author past
 */
public interface GSSService extends RemoteService {

	/**
	 * Maximum number of files that are visible in the file list
	 */
	static final int VISIBLE_FILE_COUNT = 100;

	/**
	 * Maximum number of groups visible in the group list
	 */
	static final int VISIBLE_GROUP_COUNT = 3;

	/**
	 * Maximum number of users visible in the user list
	 */
	static final int VISIBLE_USERS_COUNT = 4;


	//List<UserDTO> getUsersByUserNameLike(String username) throws RpcException;

	UploadStatusDTO getUploadStatus(String userId, String fileName) throws RpcException, ObjectNotFoundException;

	List<FileHeaderDTO> searchFiles(Long userId, String query) throws ObjectNotFoundException, RpcException;
}
