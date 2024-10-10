package com.mirakl.sfcc;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SfccNavigationPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(SfccNavigationPage.class);

    // Administration
    private final Locator administrationSubMenus;

    // Administration > Global Preferences > Feature Switches
    private final Locator viewFeatureSwitchPrefsSubMenus;

    public SfccNavigationPage(Page page) {
        super(page);
        administrationSubMenus = page.locator("//button[@data-automation='[BMGlobalNavigation] administrationDropdownButton']");
        viewFeatureSwitchPrefsSubMenus = page.locator("//div[@title='View feature switch preferences.']");
    }

    public void clickAdministrationSubMenus() {
        logger.info("Click Administration Sub Menus");
        administrationSubMenus.click();
        logger.info("Click Administration Sub Menus successfully");
    }

    public void clickViewFeatureSwitchPrefsSubMenus() {
        logger.info("Click View Feature Switch Preferences Sub Menus");
        viewFeatureSwitchPrefsSubMenus.click();
        logger.info("Click View Feature Switch Preferences Sub Menus successfully");
    }

}
