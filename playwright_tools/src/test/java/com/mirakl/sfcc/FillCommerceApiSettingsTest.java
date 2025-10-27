package com.mirakl.sfcc;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FillCommerceApiSettingsTest extends PlaywrightBase {

    private static final Logger logger = LoggerFactory.getLogger(FillCommerceApiSettingsTest.class);
    private static final String SFCC_AUTOMATED_TESTS_USERNAME = System.getProperty("SFCC_AUTOMATED_TESTS_USERNAME");
    private static final String SFCC_AUTOMATED_TESTS_PASSWORD = System.getProperty("SFCC_AUTOMATED_TESTS_PASSWORD");
    private static final String SFCC_AUTOMATED_TESTS_SECRET_KEY = System.getProperty("SFCC_AUTOMATED_TESTS_SECRET_KEY");
    private static final String SFCC_BASE_URL = System.getProperty("SFCC_BASE_URL");
    public static final String BASE_URL = "https://" + SFCC_BASE_URL;

    public FillCommerceApiSettingsTest() throws IOException {
        // Nothing to do
    }

    @Override
    protected String getDefaultUrl() {
        return BASE_URL + "/on/demandware.store/Sites-Site/";
    }

    @Test
    void FillCommerceApiSettings() throws InterruptedException {
        int maxError = 3;
        int currentError = 0;
        do {
            try {
                // Login
                Thread.sleep(TWO_SECONDS);
                sfccAdminLoginPage.setUsername(SFCC_AUTOMATED_TESTS_USERNAME);
                sfccAdminLoginPage.clickSkipForNowButton();
                Thread.sleep(TWO_SECONDS);
                sfccAdminLoginPage.setPassword(SFCC_AUTOMATED_TESTS_PASSWORD);
                sfccAdminLoginPage.clickSkipForNowButton();
                Thread.sleep(TWO_SECONDS);
                sfccAdminVerifyPage.fillAuthenticatorForm(SFCC_AUTOMATED_TESTS_SECRET_KEY);
                logger.info("Logged in successfully");
                sfccAdminVerifyPage.clickSkipForNowButton();
                Thread.sleep(TEN_SECONDS);
                var preferenceUrl = BASE_URL + "/on/demandware.store/Sites-Site/default%3bapp%3d__bm_admin/ViewFeatureSwitchPreferences-Show";
                logger.info("Navigating to Feature Switches page from url: " + preferenceUrl);
                sfccAdminVerifyPage.getPage().navigate(preferenceUrl).url();
                // Feature Switches
                Thread.sleep(TWO_SECONDS);
                featureSwitchesPage.clickSkipForNowButton();
                Thread.sleep(TWO_SECONDS);
                featureSwitchesPage.enableScapiHookExecutionFlag();
                Thread.sleep(TWO_SECONDS);
                featureSwitchesPage.clickSkipForNowButton();
                Thread.sleep(TWO_SECONDS);
                featureSwitchesPage.clickApplyButton();
                return;
            } catch (Exception e) {
                logger.error("Error while filling Commerce API settings", e);
                currentError++;
            }
            takeScreenshot(sfccAdminLoginPage);
        } while (currentError < maxError);
    }

}
