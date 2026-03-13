package utilities;

import Base.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Self-Healing Test Listener
 *
 * Provides:
 * - Screenshot capture on test failure
 * - Detailed logging of test lifecycle
 * - Screenshot capture on test skip (for debugging retries)
 * - Automatic screenshot directory creation
 */
public class TestListener implements ITestListener {

    private static final Logger logger = Logger.getLogger(TestListener.class.getName());
    private static final String SCREENSHOT_DIR = "target/screenshots";

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("[TEST] STARTED: " + result.getTestClass().getName() + "." + result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        logger.info("[TEST] PASSED: " + result.getName() + " (Duration: " + duration + "ms)");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        logger.severe("[TEST] FAILED: " + result.getName() + " (Duration: " + duration + "ms)");
        logger.severe("[TEST] Error: " + result.getThrowable().getClass().getSimpleName()
                + " - " + result.getThrowable().getMessage());

        captureScreenshot(result, "FAILURE");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warning("[TEST] SKIPPED: " + result.getName());
        if (result.getThrowable() != null) {
            logger.warning("[TEST] Skip reason: " + result.getThrowable().getMessage());
        }
        captureScreenshot(result, "SKIPPED");
    }

    /**
     * Capture screenshot and save to target/screenshots directory
     */
    private void captureScreenshot(ITestResult result, String status) {
        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver == null) {
                logger.warning("[SCREENSHOT] Driver is null - cannot capture screenshot");
                return;
            }

            // Create screenshot directory
            Path screenshotDir = Paths.get(SCREENSHOT_DIR);
            if (!Files.exists(screenshotDir)) {
                Files.createDirectories(screenshotDir);
            }

            // Generate filename: STATUS_ClassName_MethodName_timestamp.png
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String className = result.getTestClass().getRealClass().getSimpleName();
            String methodName = result.getName();
            String fileName = String.format("%s_%s_%s_%s.png", status, className, methodName, timestamp);

            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destination = screenshotDir.resolve(fileName);
            Files.copy(screenshotFile.toPath(), destination);

            logger.info("[SCREENSHOT] Saved: " + destination.toAbsolutePath());
        } catch (IOException e) {
            logger.severe("[SCREENSHOT] Failed to save screenshot: " + e.getMessage());
        } catch (Exception e) {
            logger.warning("[SCREENSHOT] Could not capture screenshot: " + e.getMessage());
        }
    }
}
