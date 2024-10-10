package com.mirakl.sfcc;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeatureSwitchesPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(FeatureSwitchesPage.class);

    private final Locator scapiHookExecutionFlag;
    private final Locator applyButton;

    public FeatureSwitchesPage(Page page) {
        super(page);
        scapiHookExecutionFlag = page.locator("//input[@name='ScapiHookExecutionEnabled']");
        applyButton = page.locator("//td[@data-automation='apply-button']");
    }

    public void enableScapiHookExecutionFlag() {
        logger.info("Enabling ScapiHookExecutionFlag");
        if (!scapiHookExecutionFlag.isChecked()) {
            scapiHookExecutionFlag.click();
            logger.info("Enabled ScapiHookExecutionFlag successfully");
        } else {
            logger.info("ScapiHookExecutionFlag already enabled");
        }
    }

    public void clickApplyButton() {
        logger.info("Clicking Apply button");
        applyButton.click();
        logger.info("Clicked Apply button successfully");
    }

}
