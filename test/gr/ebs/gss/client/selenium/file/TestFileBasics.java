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
package gr.ebs.gss.client.selenium.file;


import gr.ebs.gss.client.selenium.folder.ActionUtils;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;


/**
 * @author natasa
 *
 */
public class TestFileBasics {
	
	ActionUtils action;
	
	WebDriver driver;
	
	String url = "http://127.0.0.1:8080/pithos/login?next=http://127.0.0.1:8080/pithos/";
	
	String fileName = "";
	
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
		Thread.sleep(3000);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		action.quit();
	}
	
	public void clickAFile(String aFileName){
		action.click(By.id(""));
	}
	public void selectFileMenu(){
		
	}
	
	/**
	 * TODO: Download a file below in a folder
	 * > select a file
	 * > select File > 'Download'
	 * > save file from the popup window to your hdd
	 * 	
	 */
	
	/** TODO: Save As
	 * **** This action performs the same results to the above 'Download' option
	 */
	 
	/** TODO:Empty Trash
	 * > select a file
	 * > select File > 'Empty Trash' 
	 * *** Notice: There is no confirmation before performing this action
	 */
	   

	/** TODO:. Refresh
	 * > select a file
	 * > select File > 'Refresh'
	 */
	
	/**
	 * TODO: . Cut file
	 * > select a file
	 * > select Edit > 'Cut'
	 * > select a destination folder
	 * > click Edit > Paste on the destination folder 
	 */
	
	/** TODO: . Copy file
	 * > select a file
	 * > select Edit > 'Copy'
	 * > select a destination folder
	 * > click Edit > 'Paste' on the destination folder
	 * 
	 */
	   
	/** TODO: . Move to Trash
	 * > select a file
	 * > select Edit > 'Move to Trash'
	 * > select Trash folder and your file is in the Trash
	 */
	/**. Delete
	 * > select a file
	 * > select Edit > 'Move to Trash'
	 * > click Delete to confirm
	 * */

	/** TODO:. Select All
	 * > select a file
	 * > select Edit > 'Select All' this action results to select all files that are inside home folder
	 */
	   
	/** TODO:. Unselect All
	 * > select a file
	 * > select Edit > 'Unselect All'     this action results to unselect all files that are inside home folder
	 */

}
