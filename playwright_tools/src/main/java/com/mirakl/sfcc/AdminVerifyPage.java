package com.mirakl.sfcc;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.jboss.aerogear.security.otp.Totp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminVerifyPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(AdminVerifyPage.class);

    private final Locator authenticatorFormSubmitBtn;
    private final Locator verificationCodeInput2;

    public AdminVerifyPage(Page page) {
        super(page);
        this.authenticatorFormSubmitBtn = page.locator("button[vaas-buttonbrand_buttonbrand]").first()
                .or(page.locator("button:has-text('Verify')").first());
        this.verificationCodeInput2 = page.locator("#input-9");
    }

    public void fillAuthenticatorForm(String secretKey) {
        logger.info("Filling authenticator form");
        var totp = new Totp(secretKey);
        var code = totp.now();
        verificationCodeInput2.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        verificationCodeInput2.click();
        verificationCodeInput2.fill("");
        for (char ch : code.toCharArray()) {
            verificationCodeInput2.type(Character.toString(ch));
            page.waitForTimeout(80);
        }
        verificationCodeInput2.press("Tab");
        page.waitForTimeout(150);
        tryClick();
        if (!waitSubmitEffect()) {
            page.keyboard().press("Enter");
            page.waitForTimeout(200);
        }
        logger.info("Filling verification code");
        if (!waitSubmitEffect()) {
            ElementHandle handle = authenticatorFormSubmitBtn.elementHandle();
            if (handle != null) {
                handle.evaluate("el => el.click()");
            }
            page.waitForTimeout(200);
        }
        logger.info("After click submit button");
        waitSubmitEffect();
        logger.info("Filled authenticator form flow finished");
    }

    private void tryClick() {
        boolean visible = safeIsVisible(authenticatorFormSubmitBtn);
        boolean enabled = safeIsEnabled(authenticatorFormSubmitBtn);
        logger.info("Verify button visible={}, enabled={}", visible, enabled);
        if (visible) {
            try {
                authenticatorFormSubmitBtn.scrollIntoViewIfNeeded();
            } catch (Throwable ignored) {
            }
            try {
                authenticatorFormSubmitBtn.click();
            } catch (Throwable e) {
                logger.warn("Click failed: {}", e.toString());
            }
            try {
                authenticatorFormSubmitBtn.click(new Locator.ClickOptions().setForce(true));
            } catch (Throwable e) {
                logger.warn("Force click failed: {}", e.toString());
            }
        }
    }

    private boolean waitSubmitEffect() {
        String url = page.url();
        String value = "";
        try {
            value = verificationCodeInput2.inputValue();
        } catch (Throwable ignored) {
        }
        logger.info("Waiting submit effect url={}, codeLen={}", url, value == null ? -1 : value.length());
        page.waitForTimeout(400);
        boolean changed = !page.url().equals(url);
        logger.info("Submit effect changedUrl={}", changed);
        return changed;
    }

    private boolean safeIsVisible(Locator loc) {
        try {
            return loc.isVisible();
        } catch (Throwable e) {
            return false;
        }
    }

    private boolean safeIsEnabled(Locator loc) {
        try {
            return loc.isEnabled();
        } catch (Throwable e) {
            return false;
        }
    }

}
