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
package gr.ebs.gss.client.selenium.group;


import gr.ebs.gss.client.selenium.folder.ActionUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class TestGroup {
	
	ActionUtils action;
	
	WebDriver driver;
	
	String url = "http://127.0.0.1:8080/pithos/login?next=http://127.0.0.1:8080/pithos/";
	
	String groupName = "Wookiees";
	
	String userName = "past@ebs.gr";
	
	String addUserName = "lakis@ebs.gr";
		
	

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		
		action = new ActionUtils(driver);
		
		action.getUrl(url);
		
		// Necessary delay in order all dom elements to be created
		Thread.sleep(4000);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		action.quit();
	}
	
	@Test
	public void createNewGroup() throws InterruptedException{
		
		action.click(By.id(userName));
		
		Thread.sleep(1000);
		
		//Select Group option from top menu
		action.click(By.id("topMenu.group"));
		
		Thread.sleep(1000);
		
		//Select New Group option
		action.click(By.id("topMenu.group.newGroup"));
		
		Thread.sleep(1000);

		//type the group name in the input box of the group dialog
		action.type(By.id("groupDialog.textBox.name"), groupName);
		
		//click ok button
		action.click(By.id("groupDialog.button.ok"));
		
		Thread.sleep(2000);
		
		//assert that the newly created group has been created		
		Assert.assertEquals(groupName, action.getText(By.id("groupsList."+groupName)));
//		 action.click(By.id("permissionsList."+groupName));
		
	}
	
	public void selectGroupsTab(){
		//select the Group tab
		action.click(By.id("Groups"));
	}
	
	public void deleteGroup(String aGroupName) throws InterruptedException{		
		
		selectGroupsTab();
		
		//right click on the groupName		
		//TODO: Problem on displaying the context menu 
		action.sendRightClick(By.id("groupsList." + aGroupName));
		
		Thread.sleep(1000);
		 
		//select 'Delete' option from right click context menu
		action.click(By.id("groupContextMenu.delete"));
		
		//click ok
		action.click(By.id("deleteGroup.button.ok"));

	}
		
//	@Test(expected=org.openqa.selenium.NoSuchElementException.class)
	public void testDeleteGroup() throws InterruptedException{
	
		deleteGroup(groupName);
		//TODO: test assertion
		Assert.assertEquals(groupName, action.getText(By.id("groupsList."+groupName)));		
	
	}
	
	public void addUserToGroup(String aGroupName, String aUserName) throws InterruptedException{
		//select an existing group
		action.click(By.id("groupsList." + aGroupName));
		
		//right click on it			
		//TODO: Problem on displaying the context menu 
		action.sendRightClick(By.id("groupsList."+groupName));
		
		Thread.sleep(1000);
		
		//select 'Add User' option from right click context menu
		action.click(By.id("groupContextMenu.addUser"));
		
		//type a username
		action.type(By.id("addUser.textBox"), aUserName);
		
		//click ok
		action.click(By.id("deleteGroup.button.ok"));
	}
	
//	@Test
	public void testAddUserToGroup() throws InterruptedException{
		selectGroupsTab();
		
		Thread.sleep(1000);
		
		addUserToGroup(groupName, addUserName);
		
		//TODO: test assert addition
		Assert.assertEquals(groupName, action.getText(By.id("groupsList."+groupName)));
	}

}
