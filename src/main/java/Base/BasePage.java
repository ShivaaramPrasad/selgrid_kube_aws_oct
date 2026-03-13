package Base;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import utilities.PropertyHandler;
import utilities.CustomSelfHealingDriver;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

/**
 * Enhanced BasePage with Self-Healing capabilities.
 *
 * All element interactions are routed through SelfHealingDriver which provides:
 * - Automatic retry on StaleElementReferenceException
 * - Alternate locator strategies when primary locator fails
 * - JavaScript fallback for click/sendKeys
 * - Scroll-into-view for intercepted clicks
 * - FluentWait with configurable polling
 *
 * Original methods preserved for backward compatibility.
 */
public abstract class BasePage {
    private static final Logger logger = Logger.getLogger(BasePage.class.getName());

    protected String url;
    protected WebDriver driver;
    protected CustomSelfHealingDriver healingDriver;
    WebDriverWait webDriverWait;

    public BasePage(WebDriver driver){
        this.driver = driver;
        this.healingDriver = new CustomSelfHealingDriver(driver);
        url = PropertyHandler.getProperty("url");
        webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Self-healing click: retries on stale, scrolls on intercepted, JS fallback
     */
    protected void waitAndClick(By locator){
        healingDriver.click(locator);
    }

    /**
     * Self-healing isDisplayed: handles stale elements, returns false on timeout
     */
    protected boolean isDisplayed(By locator){
        boolean displayed = healingDriver.isDisplayed(locator);
        if (!displayed) {
            logger.info("Element with locator : " + locator + " is not displayed");
        }
        return displayed;
    }

    /**
     * Self-healing sendKeys: retries on stale, verifies input, JS fallback
     */
    protected void waitAndSendKeys(By locator, String keysToSend){
        healingDriver.sendKeys(locator, keysToSend);
    }

    /**
     * Self-healing waitForVisible: retries with alternate locators
     */
    protected WebElement waitForVisible(By locator){
        return healingDriver.findElement(locator);
    }

    /**
     * Self-healing waitForInVisible: handles timeout gracefully
     */
    protected void waitForInVisible(By locator){
        healingDriver.waitForInvisible(locator);
    }

    /**
     * Self-healing getText: retries on stale element
     */
    protected String getText(By locator){
        return healingDriver.getText(locator);
    }

    /**
     * Self-healing getValue: retries on stale element
     */
    protected String getValue(By locator){
        return healingDriver.getAttribute(locator, "value");
    }

    /**
     * Self-healing findElements: returns list with retry
     */
    protected List<WebElement> findElements(By locator) {
        return healingDriver.findElements(locator);
    }

    /**
     * Self-healing getSelectedOption: handles stale dropdown elements
     */
    protected String getSelectedOption(By locator){
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement element = healingDriver.findElement(locator);
                Select dropdown = new Select(element);
                return dropdown.getFirstSelectedOption().getText();
            } catch (StaleElementReferenceException e) {
                logger.warning("[SELF-HEAL] Stale dropdown on getSelectedOption (attempt " + attempt + ")");
                sleep(500);
            }
        }
        // Final attempt without healing
        WebElement element = driver.findElement(locator);
        Select dropdown = new Select(element);
        return dropdown.getFirstSelectedOption().getText();
    }

    /**
     * Self-healing waitAndSelect: handles stale dropdown, retries selection
     */
    protected void waitAndSelect(By locator, String value){
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement element = healingDriver.findElement(locator);
                webDriverWait.until(ExpectedConditions.elementToBeClickable(element));
                Select dropdown = new Select(element);
                dropdown.selectByVisibleText(value);
                return;
            } catch (StaleElementReferenceException e) {
                logger.warning("[SELF-HEAL] Stale dropdown on waitAndSelect (attempt " + attempt + ")");
                sleep(500);
            }
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
