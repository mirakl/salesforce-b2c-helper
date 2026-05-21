package com.mirakl.sfcc;

import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class ConfigureSandboxPermissionsTest extends PlaywrightBase {

    private static final Logger logger = LoggerFactory.getLogger(ConfigureSandboxPermissionsTest.class);
    private static final String USERNAME = System.getProperty("SFCC_AUTOMATED_TESTS_USERNAME");
    private static final String PASSWORD = System.getProperty("SFCC_AUTOMATED_TESTS_PASSWORD");
    private static final String SECRET_KEY = System.getProperty("SFCC_AUTOMATED_TESTS_SECRET_KEY");
    private static final String SFCC_BASE_URL = System.getProperty("SFCC_BASE_URL");
    private static final String ADMIN_OCAPI_KEY = System.getProperty("ADMIN_OCAPI_KEY");
    private static final String BASE_URL = "https://" + SFCC_BASE_URL;
    private static final String BM_BASE = BASE_URL + "/on/demandware.store/Sites-Site/default%3bapp%3d__bm_admin";

    public ConfigureSandboxPermissionsTest() throws IOException {}

    @Override
    protected String getDefaultUrl() {
        return BASE_URL + "/on/demandware.store/Sites-Site/";
    }

    private void login() {
        page.locator("#username").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        sfccAdminLoginPage.setUsername(USERNAME);
        sfccAdminLoginPage.clickSkipForNowButton();

        page.locator("#password").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        sfccAdminLoginPage.setPassword(PASSWORD);
        sfccAdminLoginPage.clickSkipForNowButton();

        page.locator("#input-9").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        sfccAdminVerifyPage.fillAuthenticatorForm(SECRET_KEY);
        sfccAdminVerifyPage.clickSkipForNowButton();

        page.locator("#input-9").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        logger.info("Logged in successfully");
    }

    private String buildWebdavJson() {
        return "{\n" +
            "  \"clients\": [\n" +
            "    {\n" +
            "      \"client_id\": \"" + ADMIN_OCAPI_KEY + "\",\n" +
            "      \"permissions\": [\n" +
            "        {\"path\": \"/impex\", \"operations\": [\"read_write\"]},\n" +
            "        {\"path\": \"/cartridges\", \"operations\": [\"read_write\"]},\n" +
            "        {\"path\": \"/static\", \"operations\": [\"read_write\"]}\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    }

    private String buildOcapiDataJson() {
        return "{\n" +
            "  \"_v\": \"23.2\",\n" +
            "  \"clients\": [\n" +
            "    {\n" +
            "      \"client_id\": \"" + ADMIN_OCAPI_KEY + "\",\n" +
            "      \"resources\": [\n" +
            "        {\"resource_id\": \"/code_versions\", \"methods\": [\"get\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\"},\n" +
            "        {\"resource_id\": \"/code_versions/*\", \"methods\": [\"patch\", \"delete\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\"},\n" +
            "        {\"resource_id\": \"/jobs/*/executions\", \"methods\": [\"post\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\"},\n" +
            "        {\"resource_id\": \"/jobs/*/executions/*\", \"methods\": [\"get\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\"},\n" +
            "        {\"resource_id\": \"/sites/*/cartridges\", \"methods\": [\"post\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\"},\n" +
            "        {\"resource_id\": \"/products/*\", \"methods\": [\"put\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\"},\n" +
            "        {\"resource_id\": \"/catalogs/*/categories/*/products/*\", \"methods\": [\"put\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\"},\n" +
            "        {\"resource_id\": \"/inventory_lists/*/product_inventory_records/*\", \"methods\": [\"put\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\"},\n" +
            "        {\"resource_id\": \"/system_object_definitions/Product/attribute_definition_search\", \"methods\": [\"post\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\"},\n" +
            "        {\"resource_id\": \"/products/*/variations/*\", \"methods\": [\"delete\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\"}\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    }

    @Test
    void configureSandboxPermissions() {
        login();

        var configurePage = new ConfigureSandboxPermissionsPage(page);

        page.navigate(BM_BASE + "/ViewWebdavClientPermissions-Start");
        configurePage.fillAndSave(buildWebdavJson(), "WebDAV");

        page.navigate(BM_BASE + "/ViewWapiSettings-Start");
        takeScreenshot(new com.mirakl.sfcc.BasePage(page) {});
        logger.info("OCAPI page URL: {}", page.url());
        @SuppressWarnings("unchecked")
        var allInputs = (java.util.List<String>) page.evaluate(
            "() => Array.from(document.querySelectorAll('input')).map(el => 'type=' + el.type + ' name=' + el.name + ' id=' + el.id + ' value=' + el.value)"
        );
        logger.info("ALL INPUTS on OCAPI page: {}", allInputs);
        @SuppressWarnings("unchecked")
        var allAnchors = (java.util.List<String>) page.evaluate(
            "() => Array.from(document.querySelectorAll('a')).map(a => 'text=' + a.innerText.trim().substring(0,40) + ' href=' + a.href).filter(l => l.length > 10)"
        );
        logger.info("ALL ANCHORS on OCAPI page: {}", allAnchors);
        configurePage.fillAndSave(buildOcapiDataJson(), "OCAPI Data API");
        takeScreenshot(new com.mirakl.sfcc.BasePage(page) {});
        logger.info("OCAPI textarea value after save: {}", page.locator("textarea[name='FileContent']").inputValue().substring(0, Math.min(200, page.locator("textarea[name='FileContent']").inputValue().length())));
    }
}
