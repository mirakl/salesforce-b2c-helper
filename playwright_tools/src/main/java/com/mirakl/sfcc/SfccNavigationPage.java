package com.mirakl.sfcc;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SfccNavigationPage extends BasePage {
    private static final Logger logger = LoggerFactory.getLogger(SfccNavigationPage.class);

    private final Locator appLauncherButton;
    private final Locator appLauncherMenu;
    private final Locator administrationLink;
    private final Locator globalPreferencesButton;
    private final Locator featureSwitchesLink;

    public SfccNavigationPage(Page page) {
        super(page);
        this.appLauncherButton = page.locator("button[title^='Open App Launcher']");
        this.appLauncherMenu = page.locator("[data-app-launcher-menu]");
        this.administrationLink = page.locator("a[data-automation*='appLauncher'][data-automation*='__bm_admin']")
                .or(page.locator("a:has-text('Administration')"));
        this.globalPreferencesButton = page.locator("button:has-text('Global Preferences')")
                .or(page.locator("button[title*='Global Preferences']"))
                .or(page.locator("button:has(span:has-text('Global Preferences'))"));
        this.featureSwitchesLink = page.locator("#feature_switch_manager")
                .or(page.locator("a.vertnav-menu-action:has-text('Feature Switches')"));
    }

    public void openAppLauncher() {
        logger.info("Opening App Launcher");
        appLauncherButton.waitFor();
        appLauncherButton.click();
        appLauncherMenu.waitFor();
    }

    public void clickAdministration() {
        logger.info("Navigating to Administration from App Launcher");
        administrationLink.waitFor();
        administrationLink.click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public void expandGlobalPreferencesIfCollapsed() {
        logger.info("Expanding Global Preferences if collapsed");
        globalPreferencesButton.waitFor();
        String expanded = globalPreferencesButton.getAttribute("aria-expanded");
        if (expanded == null || "false".equalsIgnoreCase(expanded)) {
            globalPreferencesButton.click();
        }
    }

    public void clickFeatureSwitches() {
        logger.info("Opening Feature Switches");
        featureSwitchesLink.waitFor();
        featureSwitchesLink.click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
}
