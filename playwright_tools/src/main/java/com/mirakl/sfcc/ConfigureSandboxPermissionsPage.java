package com.mirakl.sfcc;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigureSandboxPermissionsPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(ConfigureSandboxPermissionsPage.class);

    private final Locator fileContentTextarea;
    private final Locator saveButton;
    private final Locator typeSelect;

    public ConfigureSandboxPermissionsPage(Page page) {
        super(page);
        fileContentTextarea = page.locator("textarea[name='FileContent']");
        saveButton = page.locator("button[name='saveSettings']");
        typeSelect = page.locator("select[name='Type']");
    }

    public void selectDataTypeAndFillAndSave(String jsonContent, String settingsName) {
        logger.info("Configuring {} permissions...", settingsName);

        // Select Data type - triggers onchange which auto-submits siteForm and reloads page
        typeSelect.waitFor();
        if (!"data".equals(typeSelect.inputValue())) {
            page.waitForResponse(
                response -> response.url().contains("-Dispatch"),
                () -> typeSelect.selectOption("data")
            );
            fileContentTextarea.waitFor();
        }

        // Fill and save
        fileContentTextarea.click();
        fileContentTextarea.fill(jsonContent);
        page.waitForResponse(
            response -> response.url().contains("-Dispatch"),
            () -> saveButton.click()
        );
        fileContentTextarea.waitFor();
        logger.info("{} permissions configured successfully", settingsName);
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
