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



public class MoveToTrash {
	
	private static Log logger = LogFactory.getLog(NewFolder.class);
	
	private ActionFactory action;
	
	String folderName;
	
	public MoveToTrash(ActionFactory anAction, WebDriver aDriver){
		action = anAction;
		action = new ActionFactory(aDriver);		
	}
	
	public void moveToTrash(String aFolderName){
		try{			
			//Click the home folder
			action.click(By.id("past@ebs.gr." + aFolderName));
			
			//Click top edit menu
			action.click(By.id("topMenu.edit"));
			
			//Click move to trash option
			action.click(By.id("topMenu.edit.moveToTrash"));
			
			logger.info("SUCCESS");	
			
		}catch(Exception e){
			logger.debug("Deletion of " + aFolderName + "FAILED");
			e.printStackTrace();
		}
		
	}
	
	public void emptyTrash(){
		try{			
			Thread.sleep(1000);
			//Click the home folder
			action.click(By.id("Trash"));
			
			action.sendRightClick(By.id("Trash"));
			
			System.out.println("Send Right click");
			Thread.sleep(5000);
			//Select emptyTrash from context menu
			action.click(By.id("folderContextMenu.emptyTrash"));
	
			logger.debug("SUCCESS");	
			
		}catch(Exception e){
			logger.debug("Empty trash FAILED");
			e.printStackTrace();
		}
	}
		
}
