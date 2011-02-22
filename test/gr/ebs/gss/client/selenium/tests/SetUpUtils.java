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
package gr.ebs.gss.client.selenium.tests;


import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;



public class SetUpUtils {
	
	public ActionUtils action;
	
	public WebDriver driver;
	
	public String url = "http://127.0.0.1:8080/pithos/login?next=http://127.0.0.1:8080/pithos/";
	
	public String fileName = "darth_vader.jpg";
	
	public String groupName = "Wookiees";
	
	public String userName = "lakis@ebs.gr";
	
	public String addUserName = "past@ebs.gr";
		
	public String folderName = "Alderaan";
	
	public String folderDestination = "Naboo";
	
	public String newFolderName = "Mustafar";
	
	/**
	 * FirefoxProfile profile = new FirefoxProfile();
	 * 
	 */
	
	@Before
	public void setUp() throws Exception {
		
		/**
		 * set up for using firefox add on in WebDriver
		 * driver = new FirefoxDriver(profile);
		 * profile.addExtension("","");
		 */
		
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
	
	public Set<String> getWindowHandles(){
		return driver.getWindowHandles();
	}

}
