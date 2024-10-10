package com.mirakl.sfcc;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.jboss.aerogear.security.otp.Totp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminVerifyPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(AdminVerifyPage.class);

    private final Locator authenticatorFormSubmitBtn;
    private final Locator verificationCodeInput2;

    public AdminVerifyPage(Page page) {
        super(page);
        authenticatorFormSubmitBtn = page.locator("//button[@vaas-buttonbrand_buttonbrand]");
        verificationCodeInput2 = page.locator("//input[@id='input-9']");
    }

    public void fillAuthenticatorForm(String secretKey) {
        logger.info("Filling authenticator form");
        var totp = new Totp(secretKey);
        var twoFactorCode = totp.now();
        verificationCodeInput2.fill(twoFactorCode);
        authenticatorFormSubmitBtn.click();
        logger.info("Filled authenticator form successfully");
    }

}
