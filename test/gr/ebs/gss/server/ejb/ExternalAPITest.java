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
package gr.ebs.gss.server.ejb;

import gr.ebs.gss.client.exceptions.DuplicateNameException;
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.server.domain.dto.FileHeaderDTO;
import gr.ebs.gss.server.domain.dto.FolderDTO;
import gr.ebs.gss.server.domain.dto.GroupDTO;
import gr.ebs.gss.server.domain.dto.UserDTO;

import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;
import javax.persistence.NoResultException;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author chstath
 */
public class ExternalAPITest extends TestCase {

	/**
	 * Utility method for looking up the remote service to be tested
	 *
	 * @return ExternalAPI
	 * @throws NamingException
	 */
	private ExternalAPI getService() throws NamingException {
		return new ExternalAPIBean();
	}

	/**
	 * Test method for
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getRootFolder(java.lang.Long)}.
	 * Tests with normal userId, fails if null is returned
	 */
	public final void testGetRootFolderNormal() {
		try {
			final ExternalAPI service = getService();
			final FolderDTO f = service.getRootFolder(Long.valueOf(1));
			Assert.assertNotNull(f);
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Test method for
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getRootFolder(java.lang.Long)}.
	 * Tests with null userId, fails if {@link IllegalArgumentException} is not
	 * thrown
	 */
	public final void testGetRootFolderWithNullUserId() {
		try {
			final ExternalAPI service = getService();
			service.getRootFolder(null);
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Test method for
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getRootFolder(java.lang.Long)}.
	 * Tests with userId which has no folder, fails if {@link NoResultException}
	 * is not thrown
	 */
	public final void testGetRootFolderWithUserWithoutFolder() {
		try {
			final ExternalAPI service = getService();
			service.getRootFolder(Long.valueOf(2));
			Assert.fail();
		} catch (final ObjectNotFoundException e) {
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Test method for
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getRootFolder(java.lang.Long)}.
	 * Tests with non-existent userId, fails if {@link NoResultException} is not
	 * thrown
	 */
	public final void testGetRootFolderWithNonExistentUserId() {
		try {
			final ExternalAPI service = getService();
			service.getRootFolder(Long.valueOf(-1));
			Assert.fail();
		} catch (final ObjectNotFoundException e) {
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Test method for
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getFiles(java.lang.Long,java.lang.Long,boolean)}.
	 * Tests with normal folderId, fails if null or empty list is returned
	 */
	public final void testGetFilesNormal() {
		try {
			final ExternalAPI service = getService();
			final List<FileHeaderDTO> files = service.getFiles(new Long(1L), Long.valueOf(1), true);
			Assert.assertNotNull(files);
			Assert.assertFalse(files.isEmpty());
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Test method for
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getFiles(java.lang.Long,java.lang.Long,boolean)}.
	 * Tests with folderId of empty folder, fails if null or not empty list is
	 * returned
	 */
	public final void testGetFilesWithEmptyFolder() {
		try {
			final ExternalAPI service = getService();
			final List<FileHeaderDTO> files = service.getFiles(new Long(1L), Long.valueOf(2), true);
			Assert.assertNotNull(files);
			Assert.assertTrue(files.isEmpty());
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Test method for
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getFiles(java.lang.Long,java.lang.Long,boolean)}.
	 * Tests with null folderId, fails if {@link IllegalArgumentException} is
	 * not thrown
	 */
	public final void testGetFilesWithNullFolderId() {
		try {
			final ExternalAPI service = getService();
			service.getFiles(new Long(1L), null, true);
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Test method for
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getFiles(java.lang.Long,java.lang.Long,boolean)}.
	 * Tests with folderId of non-existent folder, fails if null or not empty
	 * list is returned
	 */
	public final void testGetFilesWithNonExistentFolder() {
		try {
			final ExternalAPI service = getService();
			final List<FileHeaderDTO> files = service.getFiles(new Long(1L), Long.valueOf(-1), true);
			Assert.assertNotNull(files);
			Assert.assertTrue(files.isEmpty());
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getGroups(Long)} for a
	 * normal user with groups
	 */
	public final void testGetGroupsNormal() {
		try {
			final ExternalAPI service = getService();
			final List<GroupDTO> groups = service.getGroups(Long.valueOf(1));
			Assert.assertNotNull(groups);
			Assert.assertFalse(groups.isEmpty());
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getGroups(Long)} for a
	 * normal user with no groups
	 */
	public final void testGetGroupsForUserWithNoGroups() {
		try {
			final ExternalAPI service = getService();
			final List<GroupDTO> groups = service.getGroups(Long.valueOf(2));
			Assert.assertNotNull(groups);
			Assert.assertTrue(groups.isEmpty());
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getGroups(Long)} for a
	 * null userId
	 */
	public final void testGetGroupsForNullUserId() {
		try {
			final ExternalAPI service = getService();
			service.getGroups(null);
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getGroups(Long)} for a
	 * non-existent user
	 */
	public final void testGetGroupsForNonExistentUser() {
		try {
			final ExternalAPI service = getService();
			final List<GroupDTO> groups = service.getGroups(Long.valueOf(-1));
			Assert.assertNotNull(groups);
			Assert.assertTrue(groups.isEmpty());
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getUsers(java.lang.Long,java.lang.Long)} for a
	 * normal group with users
	 */
	public final void testGetUsersNormal() {
		try {
			final ExternalAPI service = getService();
			final List<UserDTO> users = service.getUsers(Long.valueOf(1), Long.valueOf(1));
			Assert.assertNotNull(users);
			Assert.assertFalse(users.isEmpty());
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getUsers(java.lang.Long,java.lang.Long)}
	 * for an empty group.
	 */
	public final void testGetUsersForEmptyGroup() {
		try {
			final ExternalAPI service = getService();
			final List<UserDTO> users = service.getUsers(Long.valueOf(1L), Long.valueOf(2));
			Assert.assertNotNull(users);
			Assert.assertTrue(users.isEmpty());
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getUsers(java.lang.Long,java.lang.Long)}
	 * for a non-existent group.
	 */
	public final void testGetUsersNonExistentGroup() {
		try {
			final ExternalAPI service = getService();
			final List<UserDTO> users = service.getUsers(Long.valueOf(1L), Long.valueOf(-1));
			Assert.assertNotNull(users);
			Assert.assertTrue(users.isEmpty());
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests {@link gr.ebs.gss.server.ejb.ExternalAPIBean#getUsers(java.lang.Long,java.lang.Long)}
	 * for a null groupId.
	 */
	public final void testGetUsersWithNullGroupId() {
		try {
			final ExternalAPI service = getService();
			service.getUsers(Long.valueOf(1L), null);
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#createFolder(Long, Long, String)}
	 * with normal parameters
	 */
	public final void testCreateFolderNormal() {
		String name = "junitTestFolder";
		try {
			final ExternalAPI service = getService();
			final List rootSubfolders = service.getRootFolder(Long.valueOf(1)).getSubfolders();
			boolean ok = false;
			while (!ok) {
				final Iterator i = rootSubfolders.iterator();
				ok = true;
				while (i.hasNext()) {
					final FolderDTO f = (FolderDTO) i.next();
					if (f.getName().equals(name)) {
						name = name + "!";
						ok = false;
						break;
					}
				}
			}
			service.createFolder(Long.valueOf(1), Long.valueOf(1), name);
			final Iterator i = service.getRootFolder(Long.valueOf(1)).getSubfolders().iterator();
			while (i.hasNext()) {
				final FolderDTO f = (FolderDTO) i.next();
				if (f.getName().equals(name))
					return;
			}
			Assert.fail();
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#createFolder(Long, Long, String)}
	 * with an existing folder name
	 */
	public final void testCreateFolderWithExistingName() {
		try {
			final ExternalAPI service = getService();
			final List rootSubfolders = service.getRootFolder(Long.valueOf(1)).getSubfolders();
			service.createFolder(Long.valueOf(1), Long.valueOf(1), ((FolderDTO) rootSubfolders.get(0)).getName());
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof DuplicateNameException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#createFolder(Long, Long, String)}
	 * with an empty folder name
	 */
	public final void testCreateFolderWithEmptyName() {
		try {
			final ExternalAPI service = getService();
			service.createFolder(Long.valueOf(1), Long.valueOf(1), "");
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#createFolder(Long, Long, String)}
	 * with a null owner
	 */
	public final void testCreateFolderWithNullOwner() {
		try {
			final ExternalAPI service = getService();
			service.createFolder(null, Long.valueOf(1), "test");
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#createFolder(Long, Long, String)}
	 * with a non-existent owner
	 */
	public final void testCreateFolderWithNonExistentOwner() {
		try {
			final ExternalAPI service = getService();
			service.createFolder(Long.valueOf(-1), Long.valueOf(1), "test");
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#createFolder(Long, Long, String)}
	 * with a null parent
	 */
	public final void testCreateFolderWithNullParent() {
		try {
			final ExternalAPI service = getService();
			service.createFolder(Long.valueOf(1), null, "test");
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#createFolder(Long, Long, String)}
	 * with a non-existent parent
	 */
	public final void testCreateFolderWithNonExistentParent() {
		try {
			final ExternalAPI service = getService();
			service.createFolder(Long.valueOf(1), Long.valueOf(-1), "testFolder");
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#deleteFolder(Long, Long)}
	 * with normal parameters
	 */
	public final void testDeleteFolderNormal() {
		try {
			final String name = "junitTestFolder";
			final ExternalAPI service = getService();
			final Iterator i = service.getRootFolder(Long.valueOf(1)).getSubfolders().iterator();
			while (i.hasNext()) {
				final FolderDTO f = (FolderDTO) i.next();
				if (f.getName().equals(name))
					service.deleteFolder(Long.valueOf(1), f.getId());
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#deleteFolder(Long, Long)}
	 * with null userId
	 */
	public final void testDeleteFolderWithNullUserId() {
		try {
			final String name = "deletedFolder";
			final ExternalAPI service = getService();
			Long folderId = null;
			try {
				service.createFolder(Long.valueOf(1), Long.valueOf(1), name);
				final Iterator i = service.getRootFolder(Long.valueOf(1)).getSubfolders().iterator();
				while (i.hasNext()) {
					final FolderDTO f = (FolderDTO) i.next();
					if (f.getName().equals(name)) {
						folderId = f.getId();
						service.deleteFolder(null, f.getId());
						Assert.fail();
					}
				}
			} catch (final Exception e) {
				service.deleteFolder(Long.valueOf(1), folderId);
				if (!(e instanceof ObjectNotFoundException)) {
					e.printStackTrace();
					Assert.fail();
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#deleteFolder(Long, Long)}
	 * with non-existent user
	 */
	public final void testDeleteFolderWithNonExistentUser() {
		try {
			final String name = "deletedFolder";
			final ExternalAPI service = getService();
			Long folderId = null;
			try {
				service.createFolder(Long.valueOf(1), Long.valueOf(1), name);
				final Iterator i = service.getRootFolder(Long.valueOf(1)).getSubfolders().iterator();
				while (i.hasNext()) {
					final FolderDTO f = (FolderDTO) i.next();
					if (f.getName().equals(name)) {
						folderId = f.getId();
						service.deleteFolder(Long.valueOf(-1), f.getId());
						Assert.fail();
					}
				}
			} catch (final Exception e) {
				service.deleteFolder(Long.valueOf(1), folderId);
				if (!(e instanceof ObjectNotFoundException)) {
					e.printStackTrace();
					Assert.fail();
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#deleteFolder(Long, Long)}
	 * with null folderId
	 */
	public final void testDeleteFolderWithNullFolderId() {
		try {
			final ExternalAPI service = getService();
			service.deleteFolder(Long.valueOf(1), null);
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#deleteFolder(Long, Long)}
	 * with null folderId
	 */
	public final void testDeleteFolderWithNonExistentFolder() {
		try {
			final ExternalAPI service = getService();
			service.deleteFolder(Long.valueOf(1), Long.valueOf(-1));
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests
	 * {@link gr.ebs.gss.server.ejb.ExternalAPIBean#deleteFolder(Long, Long)}
	 * with no delete permission
	 */
	public final void testDeleteFolderNoPermission() {
		try {
			final String name = "deletedFolder";
			final ExternalAPI service = getService();
			Long folderId = null;
			try {
				service.createFolder(Long.valueOf(1), Long.valueOf(1), name);
				final Iterator i = service.getRootFolder(Long.valueOf(1)).getSubfolders().iterator();
				while (i.hasNext()) {
					final FolderDTO f = (FolderDTO) i.next();
					if (f.getName().equals(name)) {
						folderId = f.getId();
						service.deleteFolder(Long.valueOf(2), f.getId());
						Assert.fail();
					}
				}
			} catch (final Exception e) {
				service.deleteFolder(Long.valueOf(1), folderId);
				if (!(e instanceof InsufficientPermissionsException)) {
					e.printStackTrace();
					Assert.fail();
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests {@link ExternalAPIBean#getUserDTO(Long)} with normal parameters
	 */
	public final void testGetUserNormal() {
		try {
			final ExternalAPI service = getService();
			final UserDTO user = service.getUserDTO(Long.valueOf(1));
			assertNotNull(user);
			assertNotNull(user.getId());
			assertEquals(user.getId().longValue(), 1L);
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests {@link ExternalAPIBean#getUserDTO(Long)} with null userId
	 */
	public final void testGetUserNullId() {
		try {
			final ExternalAPI service = getService();
			service.getUserDTO(null);
			Assert.fail();
		} catch (final NamingException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests {@link ExternalAPIBean#getUserDTO(Long)} with non-existent userId
	 */
	public final void testGetUserNonExistentId() {
		try {
			final ExternalAPI service = getService();
			service.getUserDTO(Long.valueOf(-1));
			Assert.fail();
		} catch (final NamingException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests {@link ExternalAPIBean#createTag(Long, Long, String)} with normal
	 * parameters
	 */
	public final void testCreateTagNormal() {
		try {
			final ExternalAPI service = getService();
			service.createTag(Long.valueOf(1), Long.valueOf(165), "testTag");
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests {@link ExternalAPIBean#createTag(Long, Long, String)} with null
	 * userId
	 */
	public final void testCreateTagNullUser() {
		try {
			final ExternalAPI service = getService();
			service.createTag(null, Long.valueOf(162), "testTag");
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests {@link ExternalAPIBean#createTag(Long, Long, String)} with null
	 * file
	 */
	public final void testCreateTagNullFile() {
		try {
			final ExternalAPI service = getService();
			service.createTag(Long.valueOf(1), null, "testTag");
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests {@link ExternalAPIBean#createTag(Long, Long, String)} with null tag
	 */
	public final void testCreateTagNullTag() {
		try {
			final ExternalAPI service = getService();
			service.createTag(Long.valueOf(1), Long.valueOf(162), null);
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests {@link ExternalAPIBean#createTag(Long, Long, String)} with empty
	 * tag
	 */
	public final void testCreateTagEmptyTag() {
		try {
			final ExternalAPI service = getService();
			service.createTag(Long.valueOf(1), Long.valueOf(162), "");
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests {@link ExternalAPIBean#createTag(Long, Long, String)} with non
	 * existing user
	 */
	public final void testCreateTagNonExistingUser() {
		try {
			final ExternalAPI service = getService();
			service.createTag(Long.valueOf(-1), Long.valueOf(162), "testTag");
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests {@link ExternalAPIBean#createTag(Long, Long, String)} with non
	 * existing file
	 */
	public final void testCreateTagNonExistingFile() {
		try {
			final ExternalAPI service = getService();
			service.createTag(Long.valueOf(1), Long.valueOf(-1), "testTag");
			Assert.fail();
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests {@link ExternalAPIBean#copyFile}
	 *
	 */
	public final void testCopyFile(){
		try {
			final ExternalAPI service = getService();
			service.copyFile(Long.valueOf(1), Long.valueOf(14065), Long.valueOf(14067), "papa.txt");
		} catch (final Exception e) {
			if (!(e instanceof ObjectNotFoundException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests {@link ExternalAPIBean#indexFile}
	 */
	public final void testIndexFile() {
		try {
			final ExternalAPI service = getService();
			service.indexFile(Long.valueOf(599), false);
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests {@link ExternalAPIBean#indexFile} with delete
	 */
	public final void testDeleteIndexedFile() {
		try {
			final ExternalAPI service = getService();
			service.indexFile(Long.valueOf(14076), true);
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Tests {@link ExternalAPIBean#searchFiles}
	 */
	public final void testSearchFiles() {
		try {
			final ExternalAPI service = getService();
			service.searchFiles(Long.valueOf(1), "κείμενο");
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
