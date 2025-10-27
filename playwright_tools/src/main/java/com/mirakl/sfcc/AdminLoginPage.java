package com.mirakl.sfcc;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminLoginPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(AdminLoginPage.class);

    private Locator passwordInput;
    private final Locator loginButton;

    public AdminLoginPage(Page page) {
        super(page);
        loginButton = page.locator("#loginButton_0");
    }

    public void setUsername(String username) {
        logger.info("Setting user");
        passwordInput.fill(username);
        loginButton.click();
        logger.info("Set user successfully");
    }

    public void setPassword(String password) {
        passwordInput = page.locator("#idToken2");
        logger.info("Setting password");
        passwordInput.fill(password);
        loginButton.click();
        logger.info("Set password successfully");
    }

}
