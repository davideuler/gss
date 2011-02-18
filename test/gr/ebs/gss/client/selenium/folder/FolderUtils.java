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

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;



/**
 * @author natasa
 *
 */
public class FolderUtils {
	
	public ActionUtils action;
	
	public WebDriver driver;
	
	protected String url = "http://127.0.0.1:8080/pithos/login?next=http://127.0.0.1:8080/pithos/";
	
	public String folderName = "Alderaan";
	
	public String userName = "lakis@ebs.gr";
		//"past@ebs.gr";
	
	protected String folderDestination = "Naboo";
	
	protected String newFolderName = "Mustafar";

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
	 * Quit the driver
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
	protected void selectHomeFolder(String aUserName){
		action.click(By.id(aUserName));
	}
	
	/**
	 * Select any folder below the user's home folder
	 * 
	 * @param aUserName
	 * @param aFolderName
	 */
	public void selectFolderBelowHome(String aUserName, String aFolderName){
		action.click(By.id(aUserName+ "." + aFolderName));
	}
	
	/**
	 * Select the Trash folder
	 */
	protected void selectTrash(){
		action.click(By.id("Trash"));		
	}
	
	/**
	 * Select the My Shared folder
	 */
	protected void selectMyShared(){
		action.click(By.id("My Shared"));
	}
	
	/**
	 * Select the Other's Shared folder
	 */
	protected void selectOthersShared(){
		action.click(By.id("others"));
	}
	
	protected void expandTree(){
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
	protected void makeNewFolder(String aUserName, String aFolderName) throws InterruptedException{		
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
	 * @throws InterruptedException 
	 */	
	protected void deleteFolder(String aUserName, String aFolderName) throws InterruptedException{
		//Click the home folder		
		selectFolderBelowHome(aUserName, aFolderName);
		
		//Click top edit menu
		action.click(By.id("topMenu.edit"));
		
					
		//Click move to trash option
		action.click(By.id("topMenu.edit.delete"));
		Thread.sleep(1000);
		
		//Click ok button in the confirmation dialog box
		action.click(By.id("confirmation.ok"));
	
	}
	/**
	 * Moving a folder which lies under the root folder to trash folder
	 * 
	 * @param aFolderName
	 */	
	protected void moveToTrash(String aUserName, String aFolderName){
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
	protected void emptyTrash() throws InterruptedException{
		
		selectTrash();
		
		Thread.sleep(1000);
		
		action.click(By.id("topMenu.file"));
		
		Thread.sleep(1000);
		
		action.click(By.id("topMenu.file.emptyTrash"));

		/**
		 * TODO: Examine right click on a folder
		 */
		
//		action.sendRightClick(By.id("Trash"));
		
		//Select emptyTrash from context menu
//		action.click(By.id("folderContextMenu.emptyTrash"));

	}
	
	protected void refresh(String aUserName, String aFolderName){		
		//Click a folder
		selectFolderBelowHome(aUserName, aFolderName);
		
		//Click top edit menu
		action.click(By.id("topMenu.file"));
		
		//Click move to trash option
		action.click(By.id("topMenu.file.refresh"));
		
	}
	
	//TODO: upload file to a folder
	protected void upload(){
		
	}
	
	/**
	 * Method that cuts a folder below home folder
	 * using the top Edit menu
	 * @param aUserName
	 */
	protected void cutFolder(String aUserName, String aFolderName){
		
		//Click a folder
		selectFolderBelowHome(aUserName, aFolderName);
				
		//Click the top file menu
		action.click(By.id("topMenu.edit"));		
		
		//Click the New Folder option from the file menu
		action.click(By.id("topMenu.edit.cut"));
		
	}
	
	/**
	 * Method that copies a folder name 
	 * @param aUserName
	 * @param aFolderName
	 */
	protected void copy(String aUserName, String aFolderName){
		//Click a folder
		selectFolderBelowHome(aUserName, aFolderName);
				
		//Click the top file menu
		action.click(By.id("topMenu.edit"));		
		
		//Click the New Folder option from the file menu
		action.click(By.id("topMenu.edit.copy"));
		
	}
	
	/**
	 * Method that pastes a folder of a user using @param aUserName
	 * to a	@param aFolderDestination
	 */
	protected void paste(String aUserName, String aFolderDestination){
		//Click on the folder destination which is below home folder
		selectFolderBelowHome(aUserName, aFolderDestination);
		
		//Click the top file menu
		action.click(By.id("topMenu.edit"));		
		
		//Click the New Folder option from the file menu
		action.click(By.id("topMenu.edit.paste"));
	}
	
	/**
	 * Method that clicks on the folder and the from the top menu File > Properties
	 * 
	 * @param aUserName
	 * @param aFolderName
	 * @throws InterruptedException
	 */
	protected void clickFolderProperties(String aUserName, String aFolderName) throws InterruptedException{
		//Click on a folder 
		selectFolderBelowHome(aUserName, aFolderName);
				
		action.click(By.id("topMenu.file"));
		Thread.sleep(1000);
		action.click(By.id("topMenu.file.properties"));
		Thread.sleep(1000);
	}
	
	/**
	 * Methods thats sends right click to the selected folder
	 * @param aUserName
	 * @param aFolderName
	 */
	
	protected void rightClickOnFolder(String aUserName, String aFolderName){
		//Click on a folder 
		selectFolderBelowHome(aUserName, aFolderName);
		
		action.sendRightClick(By.id(aUserName+ "." + aFolderName));
		
	}
	
	protected void selectFolderContextMenuOption(String anOption){
		action.click(By.id("folderContextMenu." + anOption));
	}
	


}
