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

import org.openqa.selenium.By;


public class FileUtils extends GeneralPurposeUtils{

	public void rightClickOnFile(String aFileName){
		action.sendRightClick(By.id("fileList." + aFileName));
		
	}
	
	public void selectFileContextMenuOption(String anOption){
		action.click(By.id("fileContextMenu." + anOption));
	}
	

	public void downloadFile() throws InterruptedException, AWTException{
		selectTopMenu("file");
		
		Thread.sleep(1000);
		
		selectAnOptionInTopMenu("file","download");
		
		/**
		 * TODO: Solve the problem of not opening the dialog boxes
		 */
		Thread.sleep(3000);
		Robot robot = new Robot();
		//Alt + S to select save 
		robot.keyPress(KeyEvent.VK_ALT);
		robot.keyPress(KeyEvent.VK_S);
		robot.keyRelease(KeyEvent.VK_ALT);
		System.out.println("window = " + driver.getWindowHandle());
		Thread.sleep(3000);
		//Then press enter
		robot.keyPress(KeyEvent.VK_ENTER);
			
		// and save the file in the indicated location 
		// using Alt + S
		robot.keyPress(KeyEvent.VK_ALT);
		robot.keyPress(KeyEvent.VK_S);
		robot.keyPress(KeyEvent.VK_S);
		robot.keyRelease(KeyEvent.VK_ALT); 
		
		
		
		Thread.sleep(3000);
		//TODO: confirm download - how??
	}

	
	/**
	 * This action performs the same results to the above 'Download' option
	 */
	
	public void saveAsFile() throws InterruptedException{
		selectTopMenu("file");
		
		Thread.sleep(1000);
		
		selectAnOptionInTopMenu("file","saveAs");
		
		//TODO: confirm save As - how??
		
	}

	public void refreshFile(String aUserName, String aFolderName, String aFileName){		
		selectFolderBelowHome(aUserName, aFolderName);
		
		selectFile(aFileName);
		
		//Click top edit menu
		selectTopMenu("file");
		
		//Click move to trash option
		selectAnOptionInTopMenu("file","refresh");
		
		//TODO: confirm refresh
		
	}

	public void cutFile(String aFileName){
		
		//Click a file
		selectFile(aFileName);
				
		//Click the top file menu
		selectTopMenu("edit");		
		
		//Click the New Folder option from the file menu
		selectAnOptionInTopMenu("edit","cut");
				
	}
	
	public void copyFile(String aFileName){
		
		//Click a file
		selectFile(aFileName);
				
		//Click the top file menu
		selectTopMenu("edit");		
		
		//Click the New Folder option from the file menu
		selectAnOptionInTopMenu("edit","copy");
				
	}
	
	/**
	 * Method that pastes a file
	 * of a user using @param aUserName
	 * to a	@param aFolderDestination
	 */
	public void pasteFile(String aUserName, String aFolderDestination, String aFileName){
		//Click a file
		selectFile(aFileName);
		
		//Click on the folder destination which is below home folder
		selectFolderBelowHome(aUserName, aFolderDestination);
		
		//Click the top file menu
		selectTopMenu("edit");
				
		//Click the New Folder option from the file menu
		selectAnOptionInTopMenu("edit","paste");
		
		/**
		 * TODO: expand the folder that paste option was performed
		 * and confirm the existence of the file
		 */
		
	}
	   	
	public void moveToTrashFile(String aFileName){
		
		//Click a file
		selectFile(aFileName);
				
		//Click the top file menu
		selectTopMenu("edit");		
		
		//Click the New Folder option from the file menu
		selectAnOptionInTopMenu("edit","moveToTrash");
		
		/**
		 * TODO: expand Trash and confirm the existence of the file
		 */
				
	}
	
	public void deleteFile(String aFileName) throws InterruptedException{
		
		selectFile(aFileName);
		
		selectTopMenu("edit");		
					
		//Click move to trash option
		selectAnOptionInTopMenu("edit","delete");
		Thread.sleep(1000);
		
		//Click ok button in the confirmation dialog box
		selectConfirmation("ok");
		
		/**
		 * TODO: assert deletion
		 */	
	
	}
	
	public void selectAllFile(String aFileName) throws InterruptedException{
		
		selectFile(aFileName);
		
		selectTopMenu("edit");		
					
		//Click move to trash option
		selectAnOptionInTopMenu("edit","selectAll");
		Thread.sleep(1000);
		
		/**
		 * TODO: assert 
		 */	
		
	}
	   
	public void unSelectAllFile(String aFileName) throws InterruptedException{
		
		selectFile(aFileName);
		
		selectTopMenu("edit");		
					
		//Click move to trash option
		selectAnOptionInTopMenu("edit","unSelectAll");
		Thread.sleep(1000);
		
		/**
		 * TODO: assert 
		 */			
	}

}
