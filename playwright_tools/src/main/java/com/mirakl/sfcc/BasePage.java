package com.mirakl.sfcc;

import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class BasePage {

    private static final Logger logger = LoggerFactory.getLogger(BasePage.class);

    private static int currentScreenshotNumber = 0;
    protected final Page page;

    public BasePage(Page page) {
        this.page = page;
        page.setDefaultTimeout(120_000);
        page.setDefaultNavigationTimeout(240_000);
    }

    public void clickSkipForNowButton() {
        try {
            page.keyboard().press("Escape");
            page.keyboard().press("Tab");
        } catch (Exception e) {
            logger.warn("No 'Skip for now' button to click.");
        }
    }

    public void takeScreenshot() {
        try {
            var screenshotNumber = "screenshot_" + (currentScreenshotNumber++) + ".png";
            logger.info("Taking screenshot " + screenshotNumber);
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get(screenshotNumber))
                    .setFullPage(true));
            logger.info("Screenshot taken successfully.");
        } catch (Exception e) {
            logger.error("Failed to take screenshot: " + e.getMessage());
        }
    }

    public Page getPage() {
        return page;
    }

}
