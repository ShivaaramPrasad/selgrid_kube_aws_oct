package Base;

import com.epam.healenium.SelfHealingDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import utilities.PropertyHandler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Enhanced DriverManager with Healenium Self-Healing integration.
 *
 * Provides:
 * - Healenium SelfHealingDriver wrapper (auto-heals broken locators)
 * - Automatic driver initialization retry (handles grid connection issues)
 * - Driver health check before returning
 * - Safe quit with null checks
 * - Grid/Local auto-detection with recovery
 *
 * Healenium Integration:
 * - Wraps WebDriver with SelfHealingDriver.create() when heal-enabled=true
 * - Requires Healenium backend services running (docker-compose-healenium.yml)
 * - Falls back to standard WebDriver if Healenium backend is unavailable
 * - Configuration in src/main/resources/healenium.properties
 */
public class DriverManager {

    private static final Logger logger = Logger.getLogger(DriverManager.class.getName());
    private static final int MAX_INIT_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;

    private static ThreadLocal<WebDriver> threadLocalDriver = new ThreadLocal<>();

    /**
     * Initialize driver with self-healing retry logic and Healenium wrapper.
     * Retries up to MAX_INIT_RETRIES times if driver creation fails
     * (common with Selenium Grid under load).
     * After successful creation, wraps with Healenium SelfHealingDriver.
     */
    public static WebDriver initDriver(){
        String gridUrl = getGridUrl();
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_INIT_RETRIES; attempt++) {
            try {
                WebDriver delegate;
                if (gridUrl != null && !gridUrl.isEmpty()) {
                    logger.info("[DRIVER] Initializing RemoteWebDriver -> " + gridUrl + " (attempt " + attempt + ")");
                    delegate = new RemoteWebDriver(new URL(gridUrl), getChromeOptions());
                } else {
                    logger.info("[DRIVER] Initializing local ChromeDriver (attempt " + attempt + ")");
                    delegate = new ChromeDriver(getChromeOptions());
                }
                delegate.manage().window().maximize();

                // Wrap with Healenium SelfHealingDriver
                WebDriver healeniumDriver = wrapWithHealenium(delegate);
                threadLocalDriver.set(healeniumDriver);

                logger.info("[DRIVER] Driver initialized successfully" +
                        (healeniumDriver instanceof SelfHealingDriver ? " [Healenium ENABLED]" : " [Healenium DISABLED - fallback]"));
                return threadLocalDriver.get();

            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid grid URL: " + gridUrl, e);
            } catch (WebDriverException e) {
                lastException = e;
                logger.warning("[SELF-HEAL DRIVER] Failed to initialize driver (attempt " + attempt + "/" + MAX_INIT_RETRIES + "): "
                        + e.getMessage());
                cleanupFailedDriver();
                if (attempt < MAX_INIT_RETRIES) {
                    sleep(RETRY_DELAY_MS);
                }
            }
        }
        throw new RuntimeException("[SELF-HEAL DRIVER] Could not initialize driver after "
                + MAX_INIT_RETRIES + " attempts", lastException);
    }

    /**
     * Wrap a WebDriver with Healenium's SelfHealingDriver.
     * Falls back to the original driver if Healenium is unavailable or fails.
     */
    private static WebDriver wrapWithHealenium(WebDriver delegate) {
        try {
            SelfHealingDriver healingDriver = SelfHealingDriver.create(delegate);
            logger.info("[HEALENIUM] Successfully wrapped driver with Healenium SelfHealingDriver");
            return healingDriver;
        } catch (Exception e) {
            logger.warning("[HEALENIUM] Failed to wrap with Healenium (backend may not be running): "
                    + e.getMessage());
            logger.info("[HEALENIUM] Falling back to standard WebDriver (custom self-healing still active via BasePage)");
            return delegate;
        }
    }

    public static ChromeOptions getChromeOptions(){
        ChromeOptions chromeOptions = new ChromeOptions();
//        chromeOptions.addArguments("--no-sandbox");
//        chromeOptions.addArguments("--disable-dev-shm-usage");
         chromeOptions.addArguments("--headless");
        return chromeOptions;
    }

    private static String getGridUrl(){
        String gridUrl = null;
        String gridUrlProp = PropertyHandler.getProperty(PropertyHandler.SELGRID_URL_KEY);
        if (gridUrlProp != null && !gridUrlProp.isEmpty()) {
            gridUrl = String.format("http://%s:4444", gridUrlProp);
        }
        return gridUrl;
    }

    /**
     * Get driver with health check. If driver is dead, returns null
     * so caller can handle gracefully.
     */
    public static WebDriver getDriver(){
        WebDriver driver = threadLocalDriver.get();
        if (driver != null) {
            try {
                // Health check: try to get current URL
                driver.getCurrentUrl();
            } catch (WebDriverException e) {
                logger.warning("[SELF-HEAL DRIVER] Driver health check failed: " + e.getMessage());
                // Driver is dead - clean up
                cleanupFailedDriver();
                return null;
            }
        }
        return driver;
    }

    /**
     * Safe quit with null check and cleanup
     */
    public static void quitDriver(){
        WebDriver driver = threadLocalDriver.get();
        if (driver != null) {
            try {
                driver.quit();
                logger.info("[DRIVER] Driver quit successfully");
            } catch (WebDriverException e) {
                logger.warning("[DRIVER] Error during quit (may already be closed): " + e.getMessage());
            } finally {
                threadLocalDriver.remove();
            }
        } else {
            threadLocalDriver.remove();
        }
    }

    /**
     * Re-initialize driver if it has crashed mid-test.
     * Returns new driver navigated to the given URL.
     */
    public static WebDriver recoverDriver(String url) {
        logger.warning("[SELF-HEAL DRIVER] Attempting driver recovery...");
        cleanupFailedDriver();
        WebDriver newDriver = initDriver();
        if (url != null && !url.isEmpty()) {
            newDriver.get(url);
        }
        logger.info("[SELF-HEAL DRIVER] Driver recovered successfully");
        return newDriver;
    }

    private static void cleanupFailedDriver() {
        try {
            WebDriver driver = threadLocalDriver.get();
            if (driver != null) {
                driver.quit();
            }
        } catch (Exception ignored) {
        } finally {
            threadLocalDriver.remove();
        }
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
