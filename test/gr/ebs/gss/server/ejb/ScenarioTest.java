/*
 * Copyright 2008, 2009 Electronic Business Systems Ltd.
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

import static gr.ebs.gss.server.configuration.GSSConfigurationFactory.getConfiguration;
import gr.ebs.gss.common.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.common.exceptions.ObjectNotFoundException;
import gr.ebs.gss.server.domain.Folder;
import gr.ebs.gss.server.domain.Permission;
import gr.ebs.gss.server.domain.User;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author kman
 */
public class ScenarioTest extends TestCase {

	User user1 = null;

	User user2 = null;

	/**
	 *
	 */
	public ScenarioTest() {

		try {
			user1 = getService().createUser("test1", "test user 1", "test1@ebs.gr", "http://my.idp.com/foo", "bar", "");
			user2 = getService().createUser("test2", "test user 2", "test2@ebs.gr", "http://my.idp.com/foo", "baz", "");
		} catch (Exception e) {
			try {
				user1 = getService().findUser("test1");
				user2 = getService().findUser("test2");
				e.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
				fail("Error initializing users");
			}
		}

	}

	/**
	 * Utility method for creating and returning a NamingContext for looking
	 * EJBs up in the JNDI
	 *
	 * @return Context
	 * @throws NamingException
	 */
	private Context getInitialContext() throws NamingException {
		final Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
		env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
		env.put(Context.PROVIDER_URL, "jnp://localhost:1099");
		return new InitialContext(env);
	}

	/**
	 * Utility method for looking up the remote service to be tested
	 *
	 * @return ExternalAPIRemote
	 * @throws NamingException
	 */
	private ExternalAPIRemote getService() throws NamingException {
		final Context ctx = getInitialContext();
		final Object ref = ctx.lookup(getConfiguration().getString("externalApiPath"));
		final ExternalAPIRemote service = (ExternalAPIRemote) PortableRemoteObject.narrow(ref, ExternalAPIRemote.class);
		return service;
	}

	/**
	 * Test method for
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getRootFolder(java.lang.Long)}
	 * .
	 */
	public void testGetRootFolder() {
		try {
			Folder root = getService().getRootFolder(user1.getId());
			Assert.assertNotNull(root);
			List<Folder> subfolders = getService().getSubfolders(user1.getId(), root.getId());
			for(Folder f : subfolders)
				getService().deleteFolder(user1.getId(), f.getId());

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}


	//test create folder , folder permissions, copySharedFolders
	public void testFolderOperations(){
		try{
			Folder root = getService().getRootFolder(user1.getId());
			Assert.assertNotNull(root);
			getService().createFolder(user1.getId(), root.getId(), "subfolder1");
			List<Folder> subfolders = getService().getSubfolders(user1.getId(), root.getId());
			Assert.assertTrue(subfolders.size() == 1);
			Folder subFolder1 = subfolders.get(0);
			getService().createFolder(user1.getId(), subFolder1.getId(), "subfolder2");
			Permission permission = new Permission();
			permission.setUser(user2);
			permission.setRead(true);
			permission.setWrite(true);
			Set<Permission> perms = getService().getFolderPermissions(user1.getId(), subFolder1.getId());
			perms.add(permission);
			getService().updateFolder(user1.getId(), subFolder1.getId(), null, subFolder1.isReadForAll(), perms);
			List<Folder> sharedFolders = getService().getSharedRootFolders(user1.getId());
			assertTrue(sharedFolders.size() == 1 && sharedFolders.get(0).getId().equals(subFolder1.getId()));
			List<Folder> sharedForUser2 = getService().getSharedRootFolders(user1.getId(), user2.getId());
			assertTrue(sharedFolders.get(0).getId().equals(sharedForUser2.get(0).getId()));

			Folder root2 = getService().getRootFolder(user2.getId());
			getService().copyFolderStructure(user2.getId(), subFolder1.getId(), root2.getId(), subFolder1.getName());
			List<Folder> subfolders2 = getService().getSubfolders(user2.getId(), root2.getId());
			Assert.assertTrue(subfolders2.size() == 1);
			Folder subFolder2 = subfolders2.get(0);
			assertTrue(subFolder2.getSubfolders().get(0).getName().equals("subfolder2"));
			perms.remove(permission);
			getService().updateFolder(user1.getId(), subFolder1.getId(), null, subFolder1.isReadForAll(), perms);
			sharedForUser2 = getService().getSharedRootFolders(user1.getId(), user2.getId());
			assertTrue(sharedForUser2.size() == 0);
			canUserGetFolderWithoutPermissions(user2.getId(), subFolder1.getId());
			getService().deleteFolder(user1.getId(), subFolder1.getId());
			getService().deleteFolder(user2.getId(), subFolder2.getId());

		}
		catch(Exception ex){
			ex.printStackTrace();
			Assert.fail();
		}
	}

	private void canUserGetFolderWithoutPermissions(Long userId,Long folderId){
		try {
			getService().getFolder(userId, folderId);
			Assert.fail();
		} catch (ObjectNotFoundException e) {
			Assert.fail();
			e.printStackTrace();
		} catch (InsufficientPermissionsException e) {
			//e.printStackTrace();
		} catch (NamingException e) {
			Assert.fail();
		}
	}



}
