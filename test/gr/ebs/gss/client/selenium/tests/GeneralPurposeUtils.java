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



public class GeneralPurposeUtils extends SetUpUtils{
	
	/**
	 * Click the top e.g file menu, edit menu etc
	 */
	public void selectTopMenu(String aMenu){
		action.click(By.id("topMenu."+aMenu));
	}
		
	/**
	 * Click on e.g File > New Folder
	 * @param aMenu
	 * @param anOption
	 */
	public void selectAnOptionInTopMenu(String aMenu, String anOption) {
		action.click(By.id("topMenu." + aMenu + "."+ anOption));
		
	}
	
	public void selectConfirmation(String anOption){
		
		action.click(By.id("confirmation."+ anOption));
		
	}
	
	/**
	 * Select the user's home folder
	 * 
	 * @param aUserName
	 */
	public void selectHomeFolder(String aUserName){
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
	public void selectTrash(){
		action.click(By.id("Trash"));		
	}

	/**
	 * Select the My Shared folder
	 */
	public void selectMyShared(){
		action.click(By.id("My Shared"));
	}
	
	/**
	 * Select the Other's Shared folder
	 */
	public void selectOthersShared(){
		action.click(By.id("others"));
	}
	
	public void selectElementFromFolderPropertiesDialog(String anElement){
		action.click(By.id("folderPropertiesDialog." + anElement));
	}
	
	public void typeInFolderPropertiesDialog(String anElement, String input){
		action.type(By.id("folderPropertiesDialog." + anElement), input);
	}
	
	/**
	 * Emptying the trash folder using right click action
	 * 
	 * @throws InterruptedException
	 */	
	public void emptyTrash() throws InterruptedException{
		
		selectTrash();
		
		Thread.sleep(1000);
		
		selectTopMenu("file");
				
		Thread.sleep(1000);
		
		selectAnOptionInTopMenu("file","emptyTrash");
		

	}

}
