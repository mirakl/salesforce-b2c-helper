package com.mirakl.sfcc;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.microsoft.playwright.Playwright.create;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlaywrightBase {

    private static final Logger logger = LoggerFactory.getLogger(PlaywrightBase.class);
    protected static final long TWO_SECONDS = TimeUnit.SECONDS.toMillis(2);
    protected static final long TEN_SECONDS = TimeUnit.SECONDS.toMillis(10);
    protected static final List<String> BROWSER_DEFAULT_ARGS = List.of("--start-maximized", "--start-fullscreen", "--incognito", "--disable-save-password-bubble" ,"--js-flags=--max-old-space-size=4096");
    protected static final String BROWSER_TYPE = "chrome";

    // Shared between all tests in this class.
    protected static Playwright playwright;
    protected static Browser browser;

    // New instance for each test method.
    protected BrowserContext context;
    protected Page page;

    // Pages declaration
    protected AdminLoginPage sfccAdminLoginPage;
    protected AdminPage sfccAdminPage;
    protected AdminVerifyPage sfccAdminVerifyPage;
    protected FeatureSwitchesPage featureSwitchesPage;

    public PlaywrightBase() throws IOException {
        // Nothing to do
    }

    protected String getDefaultUrl() {
        return "";
    }

    protected static void clickSkipForNowBtn(Page page) {
        page.keyboard().press("Escape");
        page.keyboard().press("Tab");
    }

    protected static void takeScreenshot(BasePage basePage) {
        try {
            byte[] buffer = basePage.getPage().screenshot();
            logger.info("Screenshot taken from the page", Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).toArray());
            logger.info(Base64.getEncoder().encodeToString(buffer));
        } catch (Exception e) {
            logger.error("Failed to take screenshot: " + e.getMessage());
        }
    }

    @BeforeAll
    static void launchBrowser() {
        playwright = create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setTimeout(TEN_SECONDS).setChannel(BROWSER_TYPE).setArgs(BROWSER_DEFAULT_ARGS).setHeadless(true));
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));
        page = context.newPage();
        page.navigate(getDefaultUrl());

        // Pages
        sfccAdminLoginPage = new AdminLoginPage(page);
        sfccAdminPage = new AdminPage(page);
        sfccAdminVerifyPage = new AdminVerifyPage(page);
        featureSwitchesPage = new FeatureSwitchesPage(page);
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

}
