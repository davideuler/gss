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
package org.gss_project.gss.web.client.selenium.tests;

import org.openqa.selenium.By;

public class TestFileProperties extends TestFileBasics{
	
	public void renameFile() throws InterruptedException{
		action.click(By.id("filePropertiesDialog.textBox.name"));
		Thread.sleep(1000);
		action.type(By.id("filePropertiesDialog.textBox.name"), fileToDownload);
		Thread.sleep(2000);
		action.click(By.id("filePropertiesDialog.button.ok"));
		
	}
	
	
//	@Test
	public void testRenameFile() throws InterruptedException{
		
		selectFolderBelowHome(userName, folderName);
		Thread.sleep(1000);
		selectFile(fileName);
		Thread.sleep(1000);
		selectTopMenu("file");
		Thread.sleep(1000);
		selectAnOptionInTopMenu("file", "properties");
		Thread.sleep(1000);
		renameFile();
		
		//TODO:assert new name
	}
	   
	/** TODO:. Add a Tag to a file
	 * > select a file
	 * > select File > 'Properties'
	 * > type a word that represents a tag to the tag input box
	 * > click 'Ok'
	 */	        
	   
	/** TODO:. Add a Version to a file
	 * > select a file
	 * > select File > 'Properties'
	 * > select the 'Versions' tab
	 * > click on the Versioned check box
	 * > click 'Ok'
	 */
	
	/** TODO: 
	 * . Add a new version in an already versioned file
	 */
	
	/** TODO:. Remove a Version from a file
	 * 
	 */
	
}
