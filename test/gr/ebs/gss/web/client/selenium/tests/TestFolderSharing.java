/*
 * Copyright 2011 Electronic Business Systems Ltd.
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
package gr.ebs.gss.web.client.selenium.tests;


import org.junit.Test;
import org.openqa.selenium.By;



public class TestFolderSharing extends TestFolderBasics {
	

	
	@Test
	protected void testAddGroupToPermissionFolder() throws InterruptedException{
		
		//Click sharing option for the folder @folderName
		clickFolderSharing(userName, folderName);
		
		//Click the "Add Group" option in the FolderProperties dialog box
		action.click(By.id("folderPropertiesDialog.button.addGroup"));
		
		//Select from drop down menu the preferred group
		//TODO: add an id in the elements in the dropDown menu in order this click in the droppDown to work
//		action.click(By.id("addPermission.dropDown"));
		
		//Add a read permission to the group
		action.click(By.id("addPermission.read"));
		
		//Press OK
		action.click(By.id("addPermission.button.ok"));
		
		//Press ok to close the dialog box
		
		//Assert that the group name you added permission to is displayed to the permissions table
		action.click(By.id("permissionsList.Wookiees"));
		
	}
	/**
	 * TODO: Share a folder to a user
	 * > select a folder
	 * > select File > 'Sharing'
	 * > click Add User
	 * > Type a valid username (a full version e.g john@doe.com)
	 * > Add permissions to the selected user
	 * > click OK
	 * > click Update
	 */
//	@Test
	protected void testAddUserToPermissionFolder() throws InterruptedException{
		
		//Click sharing option for the folder @folderName
		clickFolderSharing(userName, folderName);
		
		//Click the "Add User" option in the FolderProperties dialog box
		action.click(By.id("folderPropertiesDialog.button.addUser"));
		
		//type the username in the input textBox
		action.type(By.id("addPermission.textBox"), addUserName);
		
		//Add a read permission to the group
		//TODO: this should be toggle :(
		action.click(By.id("addPermission.read"));
		
		//Press OK
		action.click(By.id("addPermission.button.ok"));
		
		//Press ok to close the dialog box
		
		//Assert that the group name you added permission to is displayed to the permissions table
		action.click(By.id("permissionsList." + addUserName));
		
		//folderPropertiesDialog.button.ok
		
	}
	
	/**
	 * TODO: . Make a folder public for all
	 * > select a folder
	 * > select File > 'Sharing'
	 * > check 'Public' check box
	 * > click OK
	 * > click Update
	 */
		
//	@Test
	protected void testMakePublicFolder() throws InterruptedException{
		
		//Click sharing option for the folder @folderName
		clickFolderSharing(userName, folderName);
		
		//Click the "Public" checkBox
		//TODO: this should be toggle :(
		action.click(By.id("folderPropertiesDialog.checkBox.public"));
		
		//Press OK
		action.click(By.id("folderPropertiesDialog.button.ok"));
				
		/**
		 * TODO: Check public link validity
		 * > select a folder
		 * > select File > 'Sharing'
		 * > Select pubic link
		 * > click Cancel
		 * > Open a new browser with the above copied link
		 */
		
	}
	

}
