package com.mirakl.sfcc;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigureSandboxPermissionsPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(ConfigureSandboxPermissionsPage.class);

    private final Locator fileContentTextarea;
    private final Locator saveButton;

    public ConfigureSandboxPermissionsPage(Page page) {
        super(page);
        fileContentTextarea = page.locator("textarea[name='FileContent']");
        saveButton = page.locator("button[name='saveSettings']");
    }

    public void fillAndSave(String jsonContent, String settingsName) {
        logger.info("Configuring {} permissions...", settingsName);
        fileContentTextarea.waitFor();
        fileContentTextarea.click();
        fileContentTextarea.fill(jsonContent);
        page.waitForResponse(
            response -> response.url().contains("-Dispatch"),
            () -> saveButton.click()
        );
        fileContentTextarea.waitFor();
        logger.info("{} permissions configured successfully", settingsName);
    }
}
