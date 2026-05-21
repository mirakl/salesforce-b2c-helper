package com.mirakl.sfcc;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ConfigureSandboxPermissionsPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(ConfigureSandboxPermissionsPage.class);
    private static final long TWO_SECONDS = TimeUnit.SECONDS.toMillis(2);

    private final Locator fileContentTextarea;
    private final Locator saveButton;

    public ConfigureSandboxPermissionsPage(Page page) {
        super(page);
        fileContentTextarea = page.locator("textarea[name='FileContent']");
        saveButton = page.locator("button[name='saveSettings']");
    }

    public void fillAndSave(String jsonContent, String settingsName) throws InterruptedException {
        logger.info("Configuring {} permissions...", settingsName);
        fileContentTextarea.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        fileContentTextarea.click();
        fileContentTextarea.fill(jsonContent);
        Thread.sleep(500);
        saveButton.click();
        Thread.sleep(TWO_SECONDS);
        logger.info("{} permissions configured successfully", settingsName);
    }
}
