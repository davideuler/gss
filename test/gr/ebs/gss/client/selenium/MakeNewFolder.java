package gr.ebs.gss.client.selenium;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import junit.framework.TestCase;


public class MakeNewFolder extends TestCase {	
	public void testMakeNewFolder() throws InterruptedException {
		WebDriver driver = new FirefoxDriver();
		driver.get("http://127.0.0.1:8080/pithos/login?next=http://127.0.0.1:8080/pithos/");
		Thread.sleep(4000);
	    WebElement element1 = driver.findElement(By.id("past@ebs.gr"));
        element1.click();
        try{
        	if(driver.findElement(By.id("past@ebs.gr.Hoooray")) != null){
        	     WebElement element5 = driver.findElement(By.id("past@ebs.gr.Hoooray"));            
                 String elemText = element5.getText();
                 System.out.println("Found a folder named "+elemText);
                 Assert.assertEquals(elemText, "Hoooray");                 
        	}
//             else{
//        		driver.findElement(By.id("topMenu.file")).click();
//            	driver.findElement(By.id("topMenu.file.newFolder")).click();
//                WebElement element4 = driver.findElement(By.id("folderPropertiesDialog.textBox.name"));
//                element4.click();
//                element4.sendKeys("Hoooray");
//                driver.findElement(By.id("folderPropertiesDialog.button.ok")).click();
//                
//                Thread.sleep(3000);
//                WebElement element5 = driver.findElement(By.id("past@ebs.gr.Hoooray"));            
//                String elemText = element5.getText();
//                System.out.println("Created a new folder named "+elemText);
//                Assert.assertEquals(elemText, "Hoooray");
//        	}
            
        }catch (Exception e) {
        	driver.findElement(By.id("topMenu.file")).click();
        	driver.findElement(By.id("topMenu.file.newFolder")).click();
            WebElement element4 = driver.findElement(By.id("folderPropertiesDialog.textBox.name"));
            element4.click();
            element4.sendKeys("Hoooray");
            driver.findElement(By.id("folderPropertiesDialog.button.ok")).click();
            
            Thread.sleep(3000);
            WebElement element5 = driver.findElement(By.id("past@ebs.gr.Hoooray"));            
            String elemText = element5.getText();
            System.out.println("Created a new folder named "+elemText);
            Assert.assertEquals(elemText, "Hoooray");     	
		}
        Thread.sleep(2000);
        driver.quit();
	}
}
