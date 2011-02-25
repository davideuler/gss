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


import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;




public class TestFileBasics extends FileUtils{
	
	@Test
	public void testClickAFile(){
		selectFolderBelowHome(userName, folderName);
		clickAFile(fileName);
		
		rightClickOnFile(fileName);
		
		selectFileContextMenuOption("refresh");
	}
	
	@Test
	public void testRightClickOnFile() throws InterruptedException{
		/**
		 * Q: The "onchange" event doesn't fire after a call "sendKeys"
		 * A: WebDriver leaves the focus in the element you called "sendKeys" on.
		 * The "onchange" event will only fire when focus leaves that element.
		 * As such, you need to move the focus, perhaps using a "click" on another element.
		 * 
		 *  http://code.google.com/p/selenium/wiki/FrequentlyAskedQuestions
		 */
		action.click(By.id("lakis@ebs.gr.Alderaan"));
		action.click(By.id("fileList.natasa.gif"));
		WebElement element = driver.findElement(By.id("fileList.natasa.gif"));
		element.sendKeys(Keys.chord(Keys.SHIFT,Keys.F10));	
		Thread.sleep(2000);
		action.click(By.id("fileContextMenu.properties"));
	}
	
//	@Test
	public void testEmptyTrash() throws InterruptedException{
			
		emptyTrash();
		
		/**
		 * TODO: Confirm that the trash folder is empty 
		 */
		
	}
	
}
