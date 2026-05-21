package com.mirakl.sfcc;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

class InspectBmPagesTest extends PlaywrightBase {

    private static final Logger logger = LoggerFactory.getLogger(InspectBmPagesTest.class);
    private static final String USERNAME = System.getProperty("SFCC_AUTOMATED_TESTS_USERNAME");
    private static final String PASSWORD = System.getProperty("SFCC_AUTOMATED_TESTS_PASSWORD");
    private static final String SECRET_KEY = System.getProperty("SFCC_AUTOMATED_TESTS_SECRET_KEY");
    private static final String SFCC_BASE_URL = System.getProperty("SFCC_BASE_URL");
    private static final String BASE_URL = "https://" + SFCC_BASE_URL;
    private static final String BM_BASE = BASE_URL + "/on/demandware.store/Sites-Site/default%3bapp%3d__bm_admin";

    public InspectBmPagesTest() throws IOException {}

    @Override
    protected String getDefaultUrl() {
        return BASE_URL + "/on/demandware.store/Sites-Site/";
    }

    private void login() throws InterruptedException {
        Thread.sleep(TWO_SECONDS);
        sfccAdminLoginPage.setUsername(USERNAME);
        sfccAdminLoginPage.clickSkipForNowButton();
        Thread.sleep(TWO_SECONDS);
        sfccAdminLoginPage.setPassword(PASSWORD);
        sfccAdminLoginPage.clickSkipForNowButton();
        Thread.sleep(TWO_SECONDS);
        sfccAdminVerifyPage.fillAuthenticatorForm(SECRET_KEY);
        sfccAdminVerifyPage.clickSkipForNowButton();
        Thread.sleep(TEN_SECONDS);
        logger.info("Logged in - current URL: {}", page.url());
    }

    private void inspectPage(String url, String pageName) throws InterruptedException {
        logger.info("========== INSPECTING {} ==========", pageName);
        page.navigate(url);
        Thread.sleep(TWO_SECONDS);
        logger.info("Page URL: {}", page.url());

        List<String> textareas = page.evaluate(
            "() => Array.from(document.querySelectorAll('textarea')).map(el => " +
            "'name=' + el.name + ' | id=' + el.id + ' | class=' + el.className + ' | value=' + el.value.substring(0, 200))"
        );
        logger.info("--- TEXTAREAS ({}) ---", textareas.size());
        textareas.forEach(t -> logger.info("  {}", t));

        List<String> buttons = page.evaluate(
            "() => Array.from(document.querySelectorAll('input[type=submit], button')).map(el => " +
            "'tag=' + el.tagName + ' | name=' + el.name + ' | id=' + el.id + ' | value=' + el.value + ' | text=' + (el.innerText||'').trim().substring(0,60) + ' | class=' + el.className)"
        );
        logger.info("--- BUTTONS ({}) ---", buttons.size());
        buttons.forEach(b -> logger.info("  {}", b));

        List<String> selects = page.evaluate(
            "() => Array.from(document.querySelectorAll('select')).map(el => " +
            "'name=' + el.name + ' | id=' + el.id + ' | options=[' + Array.from(el.options).map(o => o.value+':'+o.text).join(', ') + ']')"
        );
        logger.info("--- SELECTS ({}) ---", selects.size());
        selects.forEach(s -> logger.info("  {}", s));

        List<String> forms = page.evaluate(
            "() => Array.from(document.querySelectorAll('form')).map(el => " +
            "'id=' + el.id + ' | name=' + el.name + ' | action=' + el.action + ' | method=' + el.method)"
        );
        logger.info("--- FORMS ({}) ---", forms.size());
        forms.forEach(f -> logger.info("  {}", f));

        logger.info("========== END {} ==========", pageName);
    }

    @Test
    void inspectWebdavAndOcapiPages() throws InterruptedException {
        login();
        inspectPage(BM_BASE + "/ViewWebdavClientPermissions-Start", "WebDAV");
        inspectPage(BM_BASE + "/ViewWapiSettings-Start", "OCAPI");
    }
}
