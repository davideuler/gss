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
package gr.ebs.gss.client.selenium.ide.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;



public class IDERightClick extends SeleneseTestCase{
	
	/**
	 * TIP: Start the Selenium RC serve using
	 * in the dependencies directory
	 * java -jar selenium-server-standalone-2.0b1.jar
	 */
	
	@Before
	public void setUp() throws Exception {
		selenium = new DefaultSelenium("localhost", 4444, "*chrome", "http://127.0.0.1:8080/pithos/login?next=http://127.0.0.1:8080/pithos/");
//		setUp("http://127.0.0.1:8080/pithos/login?next=http://127.0.0.1:8080/pithos/", "*firefox");

		selenium.start();
	}
	
	@After
	public void tearDown() throws Exception {
		selenium.stop();
	}

	@Test
	public void testRestoreTrash() throws Exception {
//		selenium.open("/pithos/#Files");
		selenium.open("http://127.0.0.1:8080/pithos/login?next=http://127.0.0.1:8080/pithos/");
		selenium.waitForPageToLoad("300000");
		selenium.click("lakis@ebs.gr");		
		selenium.contextMenuAt("Trash", "");		
		selenium.click("folderContextMenu.restore");
	
	}

}
