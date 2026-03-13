package Base;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import utilities.PropertyHandler;
import utilities.RetryTransformer;
import utilities.TestListener;

import java.util.logging.Logger;

/**
 * Enhanced BaseTest with Self-Healing capabilities.
 *
 * Features:
 * - TestListener: automatic screenshot capture on failure
 * - RetryTransformer: automatic retry of failed tests (2 retries)
 * - Driver recovery: re-initializes driver if it crashes during setup
 * - Safe teardown: handles null/dead drivers gracefully
 */
@Listeners({TestListener.class, RetryTransformer.class})
public class BaseTest {

    private static final Logger logger = Logger.getLogger(BaseTest.class.getName());
    protected String url;

    @BeforeSuite
    public void initSuite(){
        String gridUrlProp = PropertyHandler.getProperty(PropertyHandler.SELGRID_URL_KEY);
        String executionMode = (gridUrlProp == null || gridUrlProp.isEmpty()) ? "Local Execution" : "Grid Execution";
        System.out.println(executionMode);
        logger.info("[SUITE] Execution mode: " + executionMode);
        logger.info("[SUITE] Self-healing enabled: RetryAnalyzer (2 retries), SelfHealingDriver, Screenshot on failure");
    }

    @BeforeMethod
    public void init(){
        DriverManager.initDriver();
        url = PropertyHandler.getProperty("url");

        WebDriver driver = DriverManager.getDriver();
        if (driver == null) {
            logger.warning("[SELF-HEAL] Driver was null after init - attempting recovery");
            driver = DriverManager.recoverDriver(url);
        } else {
            driver.get(url);
        }
    }

    @AfterMethod
    public void tearDown(){
        DriverManager.quitDriver();
    }
}
