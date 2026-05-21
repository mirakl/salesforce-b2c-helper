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
            "        {\"methods\": [\"get\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\", \"resource_id\": \"/code_versions\"},\n" +
            "        {\"methods\": [\"patch\", \"delete\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\", \"resource_id\": \"/code_versions/*\"},\n" +
            "        {\"resource_id\": \"/custom_objects/MiraklAsynchronousOfferImportTracking/*\", \"methods\": [\"get\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\", \"cache_time\": 900, \"version_range\": {\"from\": \"19.5\"}},\n" +
            "        {\"methods\": [\"post\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\", \"resource_id\": \"/jobs/*/executions\"},\n" +
            "        {\"methods\": [\"get\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\", \"resource_id\": \"/jobs/*/executions/*\"},\n" +
            "        {\"methods\": [\"post\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\", \"resource_id\": \"/sites/*/cartridges\"},\n" +
            "        {\"resource_id\": \"/system_object_definitions/Product/attribute_definition_search\", \"methods\": [\"post\"], \"read_attributes\": \"(next,hits.(id,description,display_name,localizable,multi_value_type))\", \"write_attributes\": \"(**)\"},\n" +
            "        {\"resource_id\": \"/products/*\", \"methods\": [\"put\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(id, name, last_modified, online_flag, owning_catalog_id, searchable, type)\"},\n" +
            "        {\"resource_id\": \"/catalogs/*/categories/*/products/*\", \"methods\": [\"put\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(catalog_id, category_id, product_id)\"},\n" +
            "        {\"resource_id\": \"/inventory_lists/*/product_inventory_records/*\", \"methods\": [\"put\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(perpetual_flag, product_id)\"},\n" +
            "        {\"resource_id\": \"/products/*/variations/*\", \"methods\": [\"delete\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(product_id)\"}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"client_id\": \"be589f18-fc9e-4176-b1b7-7aff5724c363\",\n" +
            "      \"resources\": [\n" +
            "        {\"resource_id\": \"/code_versions/*\", \"methods\": [\"put\", \"patch\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\"},\n" +
            "        {\"resource_id\": \"/jobs/*/executions\", \"methods\": [\"post\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\"},\n" +
            "        {\"resource_id\": \"/jobs/*/executions/*\", \"methods\": [\"get\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\"},\n" +
            "        {\"resource_id\": \"/sites/*/cartridges\", \"methods\": [\"post\"], \"read_attributes\": \"(**)\", \"write_attributes\": \"(**)\"}\n" +
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
        configurePage.selectDataTypeAndFillAndSave(buildOcapiDataJson(), "OCAPI Data API");

        String savedValue = page.locator("textarea[name='FileContent']").inputValue();
        logger.info("OCAPI textarea value after save (first 200 chars): {}", savedValue.substring(0, Math.min(200, savedValue.length())));
    }
}
