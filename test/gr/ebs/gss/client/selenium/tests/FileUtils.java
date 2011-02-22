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


import org.openqa.selenium.By;


public class FileUtils extends GeneralPurposeUtils{

	public void clickAFile(String aFileName){		
		action.click(By.id("fileList."+ aFileName));
	}

	public void rightClickOnFile(String aFileName){
		action.sendRightClick(By.id("fileList." + aFileName));
		
	}
	
	public void selectFileContextMenuOption(String anOption){
		action.click(By.id("fileContextMenu." + anOption));
	}
	
	/**
	 * TODO: Download a file below in a folder
	 * > select a file
	 * > select File > 'Download'
	 * > save file from the popup window to your hdd
	 * 	
	 */
	
	public void downloadFile() throws InterruptedException{
		selectTopMenu("file");
		
		Thread.sleep(1000);
		
		selectAnOptionInTopMenu("file","download");
		
		selectConfirmation("ok");
	}

	
	/** TODO: Save As Assert
	 * This action performs the same results to the above 'Download' option
	 */
	
	public void saveAsFile() throws InterruptedException{
		selectTopMenu("file");
		
		Thread.sleep(1000);
		
		selectAnOptionInTopMenu("file","saveAs");
		
	}
	/**
	 * TODO: assert somehow
	 * @param aUserName
	 * @param aFolderName
	 * @param aFileName
	 */
	public void refreshFile(String aUserName, String aFolderName, String aFileName){		
		selectFolderBelowHome(aUserName, aFolderName);
		
		clickAFile(aFileName);
		
		//Click top edit menu
		selectTopMenu("file");
		
		//Click move to trash option
		selectAnOptionInTopMenu("file","refresh");
		
	}

	/**
	 * TODO: . Cut file
	 * > select a file
	 * > select Edit > 'Cut'
	 * > select a destination folder
	 * > click Edit > Paste on the destination folder 
	 */
	public void cutFile(String aFileName){
		
		//Click a file
		clickAFile(aFileName);
				
		//Click the top file menu
		selectTopMenu("edit");		
		
		//Click the New Folder option from the file menu
		selectAnOptionInTopMenu("edit","cut");
				
	}
	
	/** TODO: . Copy file
	 * > select a file
	 * > select Edit > 'Copy'
	 * > select a destination folder
	 * > click Edit > 'Paste' on the destination folder
	 * 
	 */
	public void copyFile(String aFileName){
		
		//Click a file
		clickAFile(aFileName);
				
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
		clickAFile(aFileName);
		
		//Click on the folder destination which is below home folder
		selectFolderBelowHome(aUserName, aFolderDestination);
		
		//Click the top file menu
		selectTopMenu("edit");
				
		//Click the New Folder option from the file menu
		selectAnOptionInTopMenu("edit","paste");
		
	}
	   
	/** TODO: . Move to Trash
	 * > select a file
	 * > select Edit > 'Move to Trash'
	 * > select Trash folder and your file is in the Trash
	 */
	
	public void moveToTrashFile(String aFileName){
		
		//Click a file
		clickAFile(aFileName);
				
		//Click the top file menu
		selectTopMenu("edit");		
		
		//Click the New Folder option from the file menu
		selectAnOptionInTopMenu("edit","moveToTrash");
				
	}
	/**. Delete
	 * > select a file
	 * > select Edit > 'Move to Trash'
	 * > click Delete to confirm
	 * */
	
	public void deleteFile(String aFileName) throws InterruptedException{
		
		clickAFile(aFileName);
		
		selectTopMenu("edit");		
					
		//Click move to trash option
		selectAnOptionInTopMenu("edit","delete");
		Thread.sleep(1000);
		
		//Click ok button in the confirmation dialog box
		selectConfirmation("ok");
	
	}

	/** TODO:. Select All
	 * > select a file
	 * > select Edit > 'Select All' this action results to select all files that are inside home folder
	 */
	public void selectAllFile(String aFileName) throws InterruptedException{
		
		clickAFile(aFileName);
		
		selectTopMenu("edit");		
					
		//Click move to trash option
		selectAnOptionInTopMenu("edit","selectAll");
		Thread.sleep(1000);
		
	}
	   
	/** TODO:. Unselect All
	 * > select a file
	 * > select Edit > 'Unselect All'     this action results to unselect all files that are inside home folder
	 */
	public void unSelectAllFile(String aFileName) throws InterruptedException{
		
		clickAFile(aFileName);
		
		selectTopMenu("edit");		
					
		//Click move to trash option
		selectAnOptionInTopMenu("edit","unSelectAll");
		Thread.sleep(1000);
		
	}

}
