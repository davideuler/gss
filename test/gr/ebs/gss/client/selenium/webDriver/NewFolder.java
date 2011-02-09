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
package gr.ebs.gss.client.selenium.webDriver;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;



public class NewFolder {
		
	private static Log logger = LogFactory.getLog(NewFolder.class);
	
	private ActionFactory action;
	
	String folderName;
	
	/**
	 * @param aDriver
	 */
	public NewFolder(ActionFactory anAction, WebDriver aDriver){
		action = anAction;
		action = new ActionFactory(aDriver);		
	}
	
	public void makeNewFolder(String aFolderName){
		
		try{
			//Click the home folder
			action.click(By.id("past@ebs.gr"));
					
			//Click the top file menu
			action.click(By.id("topMenu.file"));			
			
			//Click the New Folder option from the file menu
			action.click(By.id("topMenu.file.newFolder"));
						
			//In the popup dialog click on the Name textBox and type the folder name		
			action.type(By.id("folderPropertiesDialog.textBox.name"), aFolderName);
			
			//Submit 
			action.click(By.id("folderPropertiesDialog.button.ok"));
			
		}catch(Exception e){
			logger.debug("The creation of a new folder didn't succeeded");
			e.printStackTrace();
		}
	}
	
	public String checkNewFolder(String aFolderName){
		String res = action.getText(By.id("past@ebs.gr." + aFolderName));
		if(res == null){
			return ("Creation of the folder " + aFolderName + "FAILED!!");
		}
		return ("SUCCESSful creation of " + res);
		
	}
	
	public void quit(){
		action.quit();		
	}

}
