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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


public class ActionFactory {
	
	private static Log logger = LogFactory.getLog(ActionFactory.class);
	
	private final WebDriver driver;

	public ActionFactory(WebDriver aDriver) {
		this.driver = aDriver;
		
	}
	
	/**
	 * Opens the application url and wait some secs
	 * @param aUrl
	 */
	
	public void getUrl(String aUrl){
		try{
			driver.get(aUrl);
		}catch(Exception e){
			logger.debug("Could not open the application url " + aUrl);
			e.printStackTrace();
		}
		
	}
	
	public void click(By locator) {
		try{
			driver.findElement(locator).click();
		}catch(Exception e){
			logger.debug("Cound not locate " + locator.toString());
			e.printStackTrace();
		}
		
	}
	
	public void submit(By locator) {
		try{
			driver.findElement(locator).submit();		
		}catch(Exception e){
			logger.debug("Cound not submit " + locator.toString());
			e.printStackTrace();
		}
		
	}
	/**
	 * Type something into an input field. WebDriver doesn't normally clear these
	 * before typing, so this method does that first. It also sends a return key
	 * to move the focus out of the element.
	 */
	public void type(By locator, String text) {
		try{
			WebElement element = driver.findElement(locator);
			element.clear();
			//element.sendKeys(text + "\n");
			element.sendKeys(text);
			
		}catch(Exception e){
			logger.debug("Cound not sendKeys " +text + " to " + locator.toString());
			e.printStackTrace();
		}
		
	}
	
	public void sendRightClick(By locator) {
		try{
			WebElement element = driver.findElement(locator);
			element.sendKeys(Keys.chord(Keys.SHIFT,Keys.F10));			
			
		}catch(Exception e){
			logger.debug("Cound not send Right Click "+ " to " + locator.toString());
			e.printStackTrace();
		}
		
	}
	public String getText(By locator){
		try{
			WebElement element = driver.findElement(locator);
			return element.getText();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public void quit(){
		try{
			Thread.sleep(2000);
			driver.quit();			
		}catch(Exception e){
			logger.debug("Could not quit driver");
			e.printStackTrace();
		}
		
	}
	
	

}
