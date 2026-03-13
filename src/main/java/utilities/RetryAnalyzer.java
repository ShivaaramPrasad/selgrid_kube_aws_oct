package utilities;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.logging.Logger;

/**
 * Self-Healing Retry Analyzer
 *
 * Automatically retries failed tests up to MAX_RETRY_COUNT times.
 * This handles transient failures caused by:
 * - Network latency in Selenium Grid
 * - Slow page loads
 * - Temporary element staleness
 * - Race conditions in async UI updates
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger logger = Logger.getLogger(RetryAnalyzer.class.getName());
    private static final int MAX_RETRY_COUNT = 2;

    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            logger.warning("[SELF-HEAL RETRY] Test '" + result.getName()
                    + "' FAILED - Retrying (attempt " + retryCount + "/" + MAX_RETRY_COUNT + ")");
            logger.warning("[SELF-HEAL RETRY] Failure reason: " + result.getThrowable().getClass().getSimpleName()
                    + " - " + result.getThrowable().getMessage());
            return true;
        }
        logger.severe("[SELF-HEAL RETRY] Test '" + result.getName()
                + "' FAILED after " + MAX_RETRY_COUNT + " retries. Marking as failed.");
        return false;
    }
}
