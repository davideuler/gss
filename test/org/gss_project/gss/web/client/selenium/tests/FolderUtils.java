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

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;


public class FolderUtils extends GeneralPurposeUtils{
	
	public String expandTree(){
		WebElement element = driver.findElement(By.xpath("//span[@id='lakis@ebs.gr.hasta luego']/../../div[1]"));
		String res = element.getAttribute("style");
		return res;
	}
	
	public void expandFolder(){
		driver.findElement(By.xpath("//span[@id='lakis@ebs.gr.hasta luego']/../../div[1]")).click();
		
	}
	
	/**
	 * Methods thats sends right click to the selected folder
	 * @param aUserName
	 * @param aFolderName
	 */
	
	public void rightClickOnFolder(String aUserName, String aFolderName){
		System.out.println("userName = " +aUserName);
		System.out.println("folderName = " +aFolderName);
		action.sendRightClick(By.id(aUserName+ "." + aFolderName));
		
		
	}
	
	public void selectFolderContextMenuOption(String anOption){
		action.click(By.id("folderContextMenu." + anOption));
	}
	
	/**
	 * Create a new folder under the root folder
	 * @param aFolderName
	 * @throws InterruptedException 
	 */	
	public void makeNewFolder(String aUserName, String aFolderName) throws InterruptedException{		
		//Click the home folder
		selectHomeFolder(aUserName);
				
		//Click the top file menu
		selectTopMenu("file");		
		
		Thread.sleep(1000);
		
		//Click the New Folder option from the file menu
		selectAnOptionInTopMenu("file","newFolder");
		
		Thread.sleep(1000);
		
		//In the popup dialog click on the Name textBox and type the folder name		
		//action.type(By.id("folderPropertiesDialog.textBox.name"), aFolderName);
		typeInFolderPropertiesDialog("textBox.name", aFolderName);
					
		//Submit 
		selectElementFromFolderPropertiesDialog("button.ok");
		//action.click(By.id("folderPropertiesDialog.button.ok"));			
		
	}
	
	/**
	 * Delete a folder with the given folderName 
	 * that lies below the root folder 
	 * 
	 * @param aFolderName
	 * @throws InterruptedException 
	 */	
	public void deleteFolder(String aUserName, String aFolderName) throws InterruptedException{
		//Click the home folder		
		selectFolderBelowHome(aUserName, aFolderName);
		
		selectTopMenu("edit");		
					
		//Click move to trash option
		selectAnOptionInTopMenu("edit","delete");
		Thread.sleep(1000);
		
		//Click ok button in the confirmation dialog box
		selectConfirmation("ok");
	
	}
	/**
	 * Moving a folder which lies under the root folder to trash folder
	 * 
	 * @param aFolderName
	 */	
	public void moveToTrash(String aUserName, String aFolderName){
		//Click the home folder
		selectFolderBelowHome(aUserName, aFolderName);
		
		//Click top edit menu
		selectTopMenu("edit");
		
		//Click move to trash option
		selectAnOptionInTopMenu("edit","moveToTrash");
		
	}
	
	public void refresh(String aUserName, String aFolderName){		
		selectFolderBelowHome(aUserName, aFolderName);
		
		//Click top edit menu
		selectTopMenu("file");
		
		//Click move to trash option
		selectAnOptionInTopMenu("file","refresh");
		
	}
	
	//TODO: upload file to a folder
	public void upload() throws AWTException{
		action.click(By.id("fileUploadDiallog.uploadPanel"));
		
		Robot robot = new Robot(); 
		robot.keyPress(KeyEvent.VK_ALT);
		robot.keyPress(KeyEvent.VK_S);
		robot.keyRelease(KeyEvent.VK_ALT); 
		
		//<button type="button" class="gwt-Button">Browse...</button>
		
		/**
		 * /home/Desktop/links.txt
		 */ 
		
	}
	@Test
	public void testUpload() throws InterruptedException{
		selectFolderBelowHome(userName, folderName);
		selectTopMenu("file");
		Thread.sleep(1000);
		selectAnOptionInTopMenu("file","upload");
		action.click(By.id("fileUploadDiallog.uploadPanel"));
		Thread.sleep(5000);
		
	}
	
	/**
	 * Method that cuts a folder below home folder
	 * using the top Edit menu
	 * @param aUserName
	 */
	public void cutFolder(String aUserName, String aFolderName){
		
		//Click a folder
		selectFolderBelowHome(aUserName, aFolderName);
				
		//Click the top file menu
		selectTopMenu("edit");		
		
		//Click the New Folder option from the file menu
		selectAnOptionInTopMenu("edit","cut");
				
	}
	
	/**
	 * Method that copies a folder name 
	 * @param aUserName
	 * @param aFolderName
	 */
	public void copy(String aUserName, String aFolderName){
		//Click a folder
		selectFolderBelowHome(aUserName, aFolderName);
				
		//Click the top file menu
		selectTopMenu("edit");		
		
		//Click the New Folder option from the file menu
		selectAnOptionInTopMenu("edit","copy");
				
	}
	
	/**
	 * Method that pastes a folder of a user using @param aUserName
	 * to a	@param aFolderDestination
	 */
	public void paste(String aUserName, String aFolderDestination){
		//Click on the folder destination which is below home folder
		selectFolderBelowHome(aUserName, aFolderDestination);
		
		//Click the top file menu
		selectTopMenu("edit");
				
		//Click the New Folder option from the file menu
		selectAnOptionInTopMenu("edit","paste");
		
	}
	
	/**
	 * Method that clicks on the folder and the from the top menu File > Properties
	 * 
	 * @param aUserName
	 * @param aFolderName
	 * @throws InterruptedException
	 */
	public void clickFolderProperties(String aUserName, String aFolderName) throws InterruptedException{
		//Click on a folder 
		selectFolderBelowHome(aUserName, aFolderName);
		
		selectTopMenu("file");
		
		Thread.sleep(1000);
		
		selectAnOptionInTopMenu("file","properties");
		
		Thread.sleep(1000);
	}
	
	public void clickFolderSharing(String aUserName, String aFolderName) throws InterruptedException{
		//Click on a folder 
		selectFolderBelowHome(aUserName, aFolderName);
				
		selectTopMenu("file");
		
		Thread.sleep(1000);
		
		selectAnOptionInTopMenu("file","sharing");
		Thread.sleep(1000);
	}
	

	
//	@Test
	public void testRightClickOnFolder1() throws InterruptedException{
		action.click(By.id(userName+ "." + folderName));
		
		
		System.out.println("userName = " +userName);
		System.out.println("folderName = " +folderName);
		
		Thread.sleep(1000);
		
		WebElement element = driver.findElement(By.id(userName+ "." + folderName));				
		element.sendKeys(Keys.chord(Keys.SHIFT,Keys.F10));	
		Thread.sleep(2000);
//		action.sendRightClick(By.id(userName+ "." + folderName));
		
		action.click(By.id("folderContextMenu.newFolder"));
		
		Thread.sleep(1000);
	}
	


}
