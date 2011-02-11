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
package gr.ebs.gss.client.selenium.folder;


import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;


public class TestFolder {
	
	ActionUtils action;
	
	WebDriver driver;
	
	String url = "http://127.0.0.1:8080/pithos/login?next=http://127.0.0.1:8080/pithos/";
	
	String folderName = "Alderaan";
	
	String userName = "past@ebs.gr";

	/**
	 * Initialize the driver, the actionUtils, the application's url
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
	
	/**
	 * Select the user's home folder
	 * 
	 * @param aUserName
	 */
	private void selectHomeFolder(String aUserName){
		action.click(By.id(aUserName));
	}
	
	/**
	 * Select any folder below the user's home folder
	 * 
	 * @param aUserName
	 * @param aFolderName
	 */
	private void selectFolderBelowHome(String aUserName, String aFolderName){
		action.click(By.id(aUserName+ "." + aFolderName));
	}
	
	/**
	 * Select the Trash folder
	 */
	private void selectTrash(){
		action.click(By.id("Trash"));		
	}
	
	/**
	 * Select the My Shared folder
	 */
	private void selectMyShared(){
		action.click(By.id("My Shared"));
	}
	
	/**
	 * Select the Other's Shared folder
	 */
	private void selectOthersShared(){
		action.click(By.id("others"));
	}
	
	private void expandTree(){
//		ERROR: Compound class names are not supported. Consider searching for one class name and filtering the results.
//		action.click(By.className("GK31MSKBDF GK31MSKBLF"));
//		action.click(By.xpath("//span[@id='Trash']/../../.."));
//		action.click(By.xpath("//div[@id='']/img"));
//		action.click(By.xpath("//span[@id='Trash']/.."));
//		action.click(By.xpath("ancestor:://span[@id='Trash']"));// Error not legal expression
//		action.click(By.xpath("//div[@class='GK31MSKBCF GK31MSKBKF gwtQuery-draggable gwtQuery-droppable']"));
		action.click(By.xpath("/html/body/table/tbody/tr[4]/td/div/div/div/div/div/div/div/div[2]/div/div"));
		//selenium.click("//*/img[contains(@src,'telpfeil.gif')]");

//		return action.clickAndSendText(By.xpath("//span[@id='Trash']/.."));
		
	}
	
	
	/**
	 * Create a new folder under the root folder
	 * @param aFolderName
	 * @throws InterruptedException 
	 */	
	private void makeNewFolder(String aUserName, String aFolderName) throws InterruptedException{		
		//Click the home folder
		selectHomeFolder(aUserName);
				
		//Click the top file menu
		action.click(By.id("topMenu.file"));		
		
		Thread.sleep(1000);
		
		//Click the New Folder option from the file menu
		action.click(By.id("topMenu.file.newFolder"));
		
		Thread.sleep(1000);
		
		//In the popup dialog click on the Name textBox and type the folder name		
		action.type(By.id("folderPropertiesDialog.textBox.name"), aFolderName);
			
		//Submit 
		action.click(By.id("folderPropertiesDialog.button.ok"));			
		
	}
	
	/**
	 * Delete a folder with the given folderName 
	 * that lies below the root folder 
	 * 
	 * @param aFolderName
	 */	
	private void deleteFolder(String aUserName, String aFolderName){
		//Click the home folder		
		selectFolderBelowHome(aUserName, aFolderName);
		
		//Click top edit menu
		action.click(By.id("topMenu.edit"));
		
					
		//Click move to trash option
		action.click(By.id("topMenu.edit.delete"));
					
		//Click ok button in the confirmation dialog box
		action.click(By.id("confirmation.ok"));
	
	}
	/**
	 * Moving a folder which lies under the root folder to trash folder
	 * 
	 * @param aFolderName
	 */	
	private void moveToTrash(String aUserName, String aFolderName){
		//Click the home folder
		selectFolderBelowHome(aUserName, aFolderName);
		
		//Click top edit menu
		action.click(By.id("topMenu.edit"));
		
		//Click move to trash option
		action.click(By.id("topMenu.edit.moveToTrash"));
		
	}
	
	/**
	 * Emptying the trash folder using right click action
	 * 
	 * @throws InterruptedException
	 */	
	private void emptyTrash() throws InterruptedException{
		
		selectTrash();
			
		action.sendRightClick(By.id("Trash"));
		
//		Thread.sleep(8000);
		
		//Select emptyTrash from context menu
		action.click(By.id("folderContextMenu.emptyTrash"));

	}
	
//	@Test
//	public void testMakeNewFolder() throws InterruptedException{
//		//Create the folder with the given folderName
//		makeNewFolder(userName, folderName);
//		
//		System.out.println(action.getText(By.id("past@ebs.gr."+folderName)));
//		
//		//the test is the folder exists
//		Assert.assertEquals(folderName, action.getText(By.id("past@ebs.gr."+folderName)));	
//		
//	}
	
//	@Test
//	public void testDeleteFolder(){
//		
//		selectFolderBelowHome(userName, folderName);
//		
//		//Delete the folder
//		deleteFolder(userName, folderName);
//
//	}
	
//	@Test (expected=org.openqa.selenium.NoSuchElementException.class)
//	public void testMakeNewFolderAndDelete() throws InterruptedException{
//		
//		//Create the folder with the given folderName
//		makeNewFolder(userName, folderName);
//		
//		//Necessary delay 
//		Thread.sleep(2000);
//		
//		//Delete the folder
//		deleteFolder(userName, folderName);
//		
//		//the test is successful if that condition fails
//		Assert.assertEquals(folderName, action.getText(By.id("past@ebs.gr."+folderName)));	
//						
//	}
    
//	@Test
//	public void testCreateFolderAndMoveToTrash() throws InterruptedException{
//		//Create a new folder
//		makeNewFolder(userName, folderName);
//		
//		//Necessary delay 
//		Thread.sleep(2000);
//				
//		//Move to Trash 
//		moveToTrash(userName, folderName);
//		
//		/**
//		 * TODO: In order to confirm that 
//		 * the new folder exists in Trash folder
//		 * trash folder should be expanded first 
//		 * and then select the element
//		 */
//		
//	}
	
//	@Test
//	public void testCreateFolderAndMoveToTrashAndEmptyTrash() throws InterruptedException{
//		
//		//Create a new folder
//		makeNewFolder(userName, folderName);
//		
//		//Necessary delay 
//		Thread.sleep(3000);
//		
//		//Move to Trash 
//		moveToTrash(userName, folderName);
//		
//		emptyTrash();
//		
//		/**
//		 * TODO: In order to confirm that 
//		 * the new folder exists in Trash folder
//		 * trash folder should be expanded first 
//		 * and then select the element
//		 */
//		
//	}
	
	/**
	 * Method that tries to expand the '+' symbol 
	 * in order to reveal folder's containing folders 
	 * @throws InterruptedException 
	 */	
	@Test
	public void testExpand() throws InterruptedException{
//		selectTrash();
		expandTree();		
		Thread.sleep(3000);
				
	}

}
