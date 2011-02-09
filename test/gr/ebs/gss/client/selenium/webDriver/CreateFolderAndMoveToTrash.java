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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class CreateFolderAndMoveToTrash {
	
	public static void main(String[] args) throws InterruptedException{
		
		WebDriver driver = new FirefoxDriver();
		
		ActionFactory action = new ActionFactory(driver);
					
		String url = "http://127.0.0.1:8080/pithos/login?next=http://127.0.0.1:8080/pithos/";
		
		String folderName = "Tatooine2";
		
		//Create a new folder
		NewFolder newFolder = new NewFolder(action, driver);
		
		action.getUrl(url);
		
		// Necessary delay in order all dom elements to be created
		Thread.sleep(4000);
		
		newFolder.makeNewFolder(folderName);
		
		System.out.println("Successful CREATION of " + folderName);
		
		// Necessary delay in order the page to be refreshed
		Thread.sleep(3000);
				
		//Move to Trash 
		MoveToTrash move = new MoveToTrash(action, driver);
				
		move.moveToTrash(folderName);
		
		Thread.sleep(3000);
		
		move.emptyTrash();
				
		
		action.quit();
	}
}
