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

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The GSS service RPC interface for clients that will be calling
 * asynchronously.
 *
 * @author past
 */
public interface GSSServiceAsync {


	//void getUsersByUserNameLike(String username, AsyncCallback c);

	void getUploadStatus(String userId, String fileName, AsyncCallback c);

	void searchFiles(Long userId, String query, AsyncCallback c);
}
