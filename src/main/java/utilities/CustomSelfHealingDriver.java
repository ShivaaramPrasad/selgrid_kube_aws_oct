package utilities;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

/**
 * Self-Healing Driver Wrapper
 *
 * Wraps element finding with intelligent retry and fallback logic:
 * - Retries on StaleElementReferenceException
 * - Retries on ElementClickInterceptedException (scrolls into view)
 * - Auto-waits with configurable timeout
 * - Generates alternate locators when primary locator fails
 * - Logs all healing actions for debugging
 */
public class CustomSelfHealingDriver {

    private static final Logger logger = Logger.getLogger(CustomSelfHealingDriver.class.getName());

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 500;
    private static final int DEFAULT_TIMEOUT_SECONDS = 15;
    private static final int POLLING_INTERVAL_MS = 300;

    private final WebDriver driver;

    public CustomSelfHealingDriver(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Find element with self-healing: retries on stale/not-found, tries alternate locators
     */
    public WebElement findElement(By locator) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return getFluentWait().until(ExpectedConditions.presenceOfElementLocated(locator));
            } catch (StaleElementReferenceException e) {
                logger.warning("[SELF-HEAL] StaleElementReference for " + locator + " (attempt " + attempt + "/" + MAX_RETRIES + ")");
                sleep(RETRY_DELAY_MS);
            } catch (TimeoutException e) {
                logger.warning("[SELF-HEAL] Timeout finding " + locator + " (attempt " + attempt + "/" + MAX_RETRIES + ")");
                // Try alternate locators on last attempt
                if (attempt == MAX_RETRIES) {
                    WebElement healed = tryAlternateLocators(locator);
                    if (healed != null) return healed;
                }
                sleep(RETRY_DELAY_MS);
            } catch (NoSuchElementException e) {
                logger.warning("[SELF-HEAL] NoSuchElement for " + locator + " (attempt " + attempt + "/" + MAX_RETRIES + ")");
                if (attempt == MAX_RETRIES) {
                    WebElement healed = tryAlternateLocators(locator);
                    if (healed != null) return healed;
                }
                sleep(RETRY_DELAY_MS);
            }
        }
        throw new NoSuchElementException("[SELF-HEAL] Could not find element after " + MAX_RETRIES + " attempts: " + locator);
    }

    /**
     * Find multiple elements with self-healing retry
     */
    public List<WebElement> findElements(By locator) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                List<WebElement> elements = getFluentWait()
                        .until(d -> {
                            List<WebElement> found = d.findElements(locator);
                            return found.isEmpty() ? null : found;
                        });
                if (elements != null) return elements;
            } catch (StaleElementReferenceException e) {
                logger.warning("[SELF-HEAL] StaleElementReference for findElements " + locator + " (attempt " + attempt + ")");
                sleep(RETRY_DELAY_MS);
            } catch (TimeoutException e) {
                logger.warning("[SELF-HEAL] Timeout for findElements " + locator + " (attempt " + attempt + ")");
                sleep(RETRY_DELAY_MS);
            }
        }
        // Return empty list instead of throwing - caller decides what to do
        return driver.findElements(locator);
    }

    /**
     * Click with self-healing: handles intercepted clicks, stale elements, scrolling
     */
    public void click(By locator) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                WebElement element = findElement(locator);
                getFluentWait().until(ExpectedConditions.elementToBeClickable(element));
                element.click();
                return;
            } catch (ElementClickInterceptedException e) {
                logger.warning("[SELF-HEAL] Click intercepted for " + locator + " - scrolling into view (attempt " + attempt + ")");
                try {
                    WebElement element = driver.findElement(locator);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
                    sleep(RETRY_DELAY_MS);
                    element.click();
                    return;
                } catch (Exception scrollEx) {
                    // Try JS click as last resort
                    if (attempt == MAX_RETRIES) {
                        logger.warning("[SELF-HEAL] Using JavaScript click for " + locator);
                        jsClick(locator);
                        return;
                    }
                }
            } catch (StaleElementReferenceException e) {
                logger.warning("[SELF-HEAL] Stale element on click " + locator + " - re-finding (attempt " + attempt + ")");
                sleep(RETRY_DELAY_MS);
            }
        }
    }

    /**
     * SendKeys with self-healing: handles stale elements, clears before typing
     */
    public void sendKeys(By locator, String text) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                WebElement element = findElement(locator);
                getFluentWait().until(ExpectedConditions.elementToBeClickable(element));
                element.clear();
                element.sendKeys(text);
                // Verify text was entered
                String currentValue = element.getAttribute("value");
                if (currentValue != null && currentValue.equals(text)) {
                    return;
                }
                // If value doesn't match, try again with JS
                if (attempt == MAX_RETRIES && currentValue != null && !currentValue.equals(text)) {
                    logger.warning("[SELF-HEAL] SendKeys verification failed, using JS for " + locator);
                    jsSendKeys(locator, text);
                }
                return;
            } catch (StaleElementReferenceException e) {
                logger.warning("[SELF-HEAL] Stale element on sendKeys " + locator + " (attempt " + attempt + ")");
                sleep(RETRY_DELAY_MS);
            } catch (ElementNotInteractableException e) {
                logger.warning("[SELF-HEAL] Element not interactable for sendKeys " + locator + " - scrolling (attempt " + attempt + ")");
                try {
                    WebElement element = driver.findElement(locator);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
                    sleep(RETRY_DELAY_MS);
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Get text with self-healing retry
     */
    public String getText(By locator) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                WebElement element = findElement(locator);
                return element.getText();
            } catch (StaleElementReferenceException e) {
                logger.warning("[SELF-HEAL] Stale on getText " + locator + " (attempt " + attempt + ")");
                sleep(RETRY_DELAY_MS);
            }
        }
        return driver.findElement(locator).getText();
    }

    /**
     * Get attribute with self-healing retry
     */
    public String getAttribute(By locator, String attribute) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                WebElement element = findElement(locator);
                return element.getAttribute(attribute);
            } catch (StaleElementReferenceException e) {
                logger.warning("[SELF-HEAL] Stale on getAttribute " + locator + " (attempt " + attempt + ")");
                sleep(RETRY_DELAY_MS);
            }
        }
        return driver.findElement(locator).getAttribute(attribute);
    }

    /**
     * Check if element is displayed with self-healing
     */
    public boolean isDisplayed(By locator) {
        try {
            WebElement element = getFluentWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
            return element.isDisplayed();
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        } catch (StaleElementReferenceException e) {
            logger.warning("[SELF-HEAL] Stale on isDisplayed " + locator + " - retrying");
            sleep(RETRY_DELAY_MS);
            try {
                return getFluentWait().until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
            } catch (Exception ex) {
                return false;
            }
        }
    }

    /**
     * Wait for element to become invisible with retry
     */
    public void waitForInvisible(By locator) {
        try {
            getFluentWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (Exception e) {
            logger.warning("[SELF-HEAL] waitForInvisible timed out for " + locator + " - continuing");
        }
    }

    // ==================== Alternate Locator Strategy ====================

    /**
     * Try to find an element using alternate locator strategies
     * when the primary locator fails. Generates CSS/XPath/ID/name alternates.
     */
    private WebElement tryAlternateLocators(By primaryLocator) {
        String locatorString = primaryLocator.toString();
        logger.info("[SELF-HEAL] Trying alternate locators for: " + locatorString);

        // Extract the locator value
        String value = extractLocatorValue(locatorString);
        if (value == null) return null;

        // Strategy 1: If CSS selector with data-test attribute, try XPath equivalent
        if (locatorString.contains("By.cssSelector")) {
            WebElement found = tryXPathFromCss(value);
            if (found != null) {
                logger.info("[SELF-HEAL] HEALED using XPath alternate for: " + locatorString);
                return found;
            }
        }

        // Strategy 2: If XPath, try CSS equivalent
        if (locatorString.contains("By.xpath")) {
            WebElement found = tryCssFromXPath(value);
            if (found != null) {
                logger.info("[SELF-HEAL] HEALED using CSS alternate for: " + locatorString);
                return found;
            }
        }

        // Strategy 3: Try by partial text content
        WebElement found = tryByPartialMatch(value);
        if (found != null) {
            logger.info("[SELF-HEAL] HEALED using partial match for: " + locatorString);
            return found;
        }

        logger.severe("[SELF-HEAL] All alternate locator strategies failed for: " + locatorString);
        return null;
    }

    private WebElement tryXPathFromCss(String cssValue) {
        try {
            // Handle data-test attribute selectors: [data-test='value'] -> //*[@data-test='value']
            if (cssValue.contains("[data-test=")) {
                String dataTestValue = cssValue.replaceAll(".*\\[data-test=['\"]([^'\"]+)['\"]\\].*", "$1");
                String xpath = "//*[@data-test='" + dataTestValue + "']";
                List<WebElement> elements = driver.findElements(By.xpath(xpath));
                if (!elements.isEmpty()) return elements.get(0);
            }

            // Handle input[data-test='value'] -> //input[@data-test='value']
            if (cssValue.matches("^\\w+\\[.*")) {
                String tag = cssValue.replaceAll("^(\\w+)\\[.*", "$1");
                String attr = cssValue.replaceAll("^\\w+\\[(.*)\\]$", "$1");
                attr = attr.replace("=", "='").replaceAll("([^'\\]])$", "$1'");
                if (!attr.endsWith("']")) attr += "'";
                String xpath = "//" + tag + "[@" + attr + "]";
                List<WebElement> elements = driver.findElements(By.xpath(xpath));
                if (!elements.isEmpty()) return elements.get(0);
            }

            // Handle class-based selectors: [class*='value'] -> //*[contains(@class,'value')]
            if (cssValue.contains("[class*=")) {
                String classValue = cssValue.replaceAll(".*\\[class\\*=['\"]([^'\"]+)['\"]\\].*", "$1");
                String xpath = "//*[contains(@class,'" + classValue + "')]";
                List<WebElement> elements = driver.findElements(By.xpath(xpath));
                if (!elements.isEmpty()) return elements.get(0);
            }

            // Handle ID-based selectors: #id -> //*[@id='id']
            if (cssValue.startsWith("#")) {
                String id = cssValue.substring(1);
                List<WebElement> elements = driver.findElements(By.id(id));
                if (!elements.isEmpty()) return elements.get(0);
            }
        } catch (Exception e) {
            logger.fine("[SELF-HEAL] XPath alternate failed: " + e.getMessage());
        }
        return null;
    }

    private WebElement tryCssFromXPath(String xpathValue) {
        try {
            // Handle //tag[@attr='value'] -> tag[attr='value']
            if (xpathValue.matches("^//\\w+\\[@[^]]+\\]$")) {
                String tag = xpathValue.replaceAll("^//(\\w+).*", "$1");
                String attr = xpathValue.replaceAll("^//\\w+\\[@([^]]+)\\]$", "$1");
                String css = tag + "[" + attr.replace("'", "") + "]";
                List<WebElement> elements = driver.findElements(By.cssSelector(css));
                if (!elements.isEmpty()) return elements.get(0);
            }

            // Handle //label[contains(text(),'value')] -> try by text content
            if (xpathValue.contains("contains(text()")) {
                String textValue = xpathValue.replaceAll(".*contains\\(text\\(\\),['\"]([^'\"]+)['\"]\\).*", "$1");
                String xpath = "//*[contains(text(),'" + textValue + "')]";
                List<WebElement> elements = driver.findElements(By.xpath(xpath));
                if (!elements.isEmpty()) return elements.get(0);
            }
        } catch (Exception e) {
            logger.fine("[SELF-HEAL] CSS alternate failed: " + e.getMessage());
        }
        return null;
    }

    private WebElement tryByPartialMatch(String value) {
        try {
            // Try to find by any attribute containing the value
            if (value.contains("data-test")) {
                String attrValue = value.replaceAll(".*['\"]([^'\"]+)['\"].*", "$1");
                String xpath = "//*[contains(@data-test,'" + attrValue + "')]";
                List<WebElement> elements = driver.findElements(By.xpath(xpath));
                if (!elements.isEmpty()) return elements.get(0);
            }
        } catch (Exception e) {
            logger.fine("[SELF-HEAL] Partial match failed: " + e.getMessage());
        }
        return null;
    }

    private String extractLocatorValue(String locatorString) {
        // Format: "By.cssSelector: value" or "By.xpath: value" etc.
        int colonIdx = locatorString.indexOf(": ");
        if (colonIdx > 0) {
            return locatorString.substring(colonIdx + 2).trim();
        }
        return null;
    }

    // ==================== JavaScript Fallbacks ====================

    private void jsClick(By locator) {
        WebElement element = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    private void jsSendKeys(By locator, String text) {
        WebElement element = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value=''; arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('input'));",
                element, text);
    }

    // ==================== Utilities ====================

    private FluentWait<WebDriver> getFluentWait() {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
                .pollingEvery(Duration.ofMillis(POLLING_INTERVAL_MS))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public WebDriver getWrappedDriver() {
        return driver;
    }
}
