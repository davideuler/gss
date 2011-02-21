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

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


public class ActionUtils {
	
	private final WebDriver driver;

	public ActionUtils(WebDriver aDriver) {
		this.driver = aDriver;
		
	}
	
	/**
	 * Opens the application url and wait some secs
	 * @param aUrl
	 */
	
	public void getUrl(String aUrl){
		driver.get(aUrl);		
				
	}
	
	public void click(By locator) {
		driver.findElement(locator).click();		
		
	}
	
	public String clickAndSendText(By locator) {
		WebElement elem = driver.findElement(locator);
		elem.click();		
		return elem.getAttribute("style");
		
	}
	public void submit(By locator) {
		driver.findElement(locator).submit();		
				
	}
	/**
	 * Type something into an input field. WebDriver doesn't normally clear these
	 * before typing, so this method does that first. It also sends a return key
	 * to move the focus out of the element.
	 */
	public void type(By locator, String text) {
		WebElement element = driver.findElement(locator);
		element.clear();
		//element.sendKeys(text + "\n");
		element.sendKeys(text);
		
	}
	
	public void sendRightClick(By locator) {
		WebElement element = driver.findElement(locator);
		element.sendKeys(Keys.chord(Keys.SHIFT,Keys.F10));			

	}
	
	public String getText(By locator){
		WebElement element = driver.findElement(locator);
		return element.getText();
		
	}
	
	public void quit() throws InterruptedException{
		Thread.sleep(2000);
		driver.quit();			
				
	}
	
	public void toggle(By locator){
		WebElement element = driver.findElement(locator);
		element.toggle();
	}

}
