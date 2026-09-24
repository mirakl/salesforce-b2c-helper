package com.mirakl.sfcc;

import com.microsoft.playwright.Route;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeatureSwitchesPageTest extends PlaywrightBase {

    private static final String FEATURE_SWITCHES_PAGE = """
            <form action="https://sandbox.example/on/demandware.store/Sites-Site/default%3bapp%3d__bm_admin/ViewFeatureSwitchPreferences-Update" method="post" name="FormUpdateBetaFeatureSwitchPreferences">
            <table class="aldi" border="0" cellpadding="4" cellspacing="0" width="100%">
            <tbody><tr id="ScapiHookExecutionEnabled">
            <td class="table_detail2 fielditem2 left" nowrap="nowrap"><p>Enable Salesforce Commerce API Hook Execution</p></td>
            <td class="table_detail2 top" id="ScapiHookExecutionEnabledCheckbox">
            <input type="checkbox" value="true" name="ScapiHookExecutionEnabled">
            </td>
            </tr></tbody>
            </table>
            <div class="buttonspacing right">
            <button type="submit" name="ActionButton" value="Apply" class="button" data-automation="apply-button">Apply</button>
            </div>
            </form>
            """;

    public FeatureSwitchesPageTest() throws IOException {}

    @Override
    protected String getDefaultUrl() {
        return "about:blank";
    }

    @Test
    void applySubmitsEnabledScapiHookExecutionFlag() {
        var submittedForm = new AtomicReference<String>();
        page.route(url -> url.contains("ViewFeatureSwitchPreferences-Update"), route -> {
            submittedForm.set(route.request().postData());
            route.fulfill(new Route.FulfillOptions().setContentType("text/html").setBody(FEATURE_SWITCHES_PAGE));
        });
        page.setDefaultTimeout(TEN_SECONDS);
        page.setContent(FEATURE_SWITCHES_PAGE);

        featureSwitchesPage.enableScapiHookExecutionFlag();
        featureSwitchesPage.clickApplyButton();

        assertEquals("ScapiHookExecutionEnabled=true&ActionButton=Apply", submittedForm.get());
    }
}
