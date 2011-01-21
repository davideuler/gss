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
package gr.ebs.gss.client.commands;

import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.resource.UserResource;
import gr.ebs.gss.client.rest.resource.UserSearchResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;


/**
 * This command manages usernames and the corresponding user's Full Name
 * along with the HashMap collection in the GSS.java class
 *
 * @author natasa
 *
 */
public class GetUserCommand implements Command{

	/**
	 * User's username e.g johndoe@somewhere.com
	 */
	private String userName;

	public GetUserCommand(String _userName){
		userName = _userName;
	}

	@Override
	public void execute() {
		String path = GSS.get().getApiPath() + "users/" + userName; 
		GetCommand<UserSearchResource> gg = new GetCommand<UserSearchResource>(UserSearchResource.class,
					path, false ,null) {
			@Override
			public void onComplete() {
				final UserSearchResource result = getResult();
				for (UserResource user : result.getUsers()){
					String username = user.getUsername();
					String _userFullName = user.getName();
					GSS.get().putUserToMap(username, _userFullName);
				}
			}
			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				GSS.get().displayError("Unable to fetch user's full name from the given username " + userName);
			}
		};
		DeferredCommand.addCommand(gg);

	}

}
