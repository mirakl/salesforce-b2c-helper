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
    }

    public void clickSkipForNowButton() {
        page.keyboard().press("Escape");
        page.keyboard().press("Tab");
    }

    public void takeScreenshot() {
        var screenshotNumber = "screenshot_" + (currentScreenshotNumber++) + ".png";
        logger.info("Taking screenshot " + screenshotNumber);
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get(screenshotNumber))
                .setFullPage(true));
    }

    public Page getPage() {
        return page;
    }

}
