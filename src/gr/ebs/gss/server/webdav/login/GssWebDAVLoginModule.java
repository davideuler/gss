/*
 * Copyright 2005, 2008, 2009 Electronic Business Systems Ltd.
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
package gr.ebs.gss.server.webdav.login;

import static gr.ebs.gss.server.configuration.GSSConfigurationFactory.getConfiguration;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.domain.UserLogin;
import gr.ebs.gss.server.ejb.ExternalAPI;
import gr.ebs.gss.server.ejb.TransactionHelper;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.Callable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.auth.spi.UsernamePasswordLoginModule;


/**
 * The custom login module for the GSS WebDAV implementation.
 */
public class GssWebDAVLoginModule extends UsernamePasswordLoginModule {

	/**
	 * Logger for this class
	 */
	private static final Log logger = LogFactory.getLog(GssWebDAVLoginModule.class);

	/**
	 * A helper method that retrieves a reference to the ExternalAPI bean and
	 * stores it for future use.
	 *
	 * @return an ExternalAPI instance
	 * @throws RpcException in case an error occurs
	 */
	private ExternalAPI getService() throws RpcException {
		try {
			final Context ctx = new InitialContext();
			final Object ref = ctx.lookup(getConfiguration().getString("externalApiPath"));
			return (ExternalAPI) PortableRemoteObject.narrow(ref, ExternalAPI.class);
		} catch (final NamingException e) {
			logger.error("Unable to retrieve the ExternalAPI EJB", e);
			throw new RpcException("An error occurred while contacting the naming service");
		}
	}

	@Override
	protected String getUsersPassword() throws LoginException {
		String username = getUsername();
		try {
			final User user = getService().findUser(username);
			if (user == null) throw new FailedLoginException("User '" + username + "' not found.");
			if (!user.isActive()) throw new FailedLoginException("User '" + username + "' is disabled.");
			if (user.getWebDAVPassword() != null && user.getWebDAVPassword().length() > 0)
				return user.getWebDAVPassword();
			// If no password has ever been generated, use token instead
			String tokenEncoded = new String(Base64.encodeBase64(user.getAuthToken()), "US-ASCII");
			user.setWebDAVPassword(tokenEncoded);
			new TransactionHelper<Void>().tryExecute(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					getService().updateUser(user);
					return null;
				}
			});
			return tokenEncoded;
		} catch (RpcException e) {
			String error = "An error occurred while communicating with the service";
			logger.error(error, e);
			throw new LoginException(e.getMessage());
		} catch (UnsupportedEncodingException e) {
            logger.error("", e);
            throw new LoginException(e.getMessage());
		} catch (Exception e) {
            logger.error("", e);
			throw new LoginException(e.getMessage());
		}
	}

	/**
	 * Overrides parent's implementation by returning only the simpleUser
	 * role for any successful login.
	 *
	 * @return Group[] that contains only the authenticatedUser group (role)
	 * @throws LoginException
	 * @see org.jboss.security.auth.spi.AbstractServerLoginModule#getRoleSets()
	 */
	@Override
	protected Group[] getRoleSets() throws LoginException {
		Principal principal;
		try {
			principal = createIdentity("simpleUser");
		} catch (Exception e) {
			logger.error("", e);
			throw new LoginException(e.getMessage());
		}
		Group rolesGroup = null;
		rolesGroup = createGroup("Roles", new HashSet());
		rolesGroup.addMember(principal);
		Group[] roles = new Group[1];
		roles[0] = rolesGroup;
		// Update the last login.
		//TODO: Handle the userlogins via WebDAV
//		try {
//			new TransactionHelper<Void>().tryExecute(new Callable<Void>() {
//				@Override
//				public Void call() throws Exception {
//					User user = getService().findUser(getUsername());
//					UserLogin userLogin = new UserLogin();
//					userLogin.setLoginDate(new Date());
//					getService().addUserLogin(userLogin);
//					getService().updateUser(user);
//					return null;
//				}
//			});
//		} catch (Exception e) {
//			logger.error("", e);
//			throw new LoginException(e.getMessage());
//		}
		return roles;
	}

}
