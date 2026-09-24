---
name: bm-automation-fix
description: Repair a salesforce-b2c-helper Business Manager automation after a Salesforce B2C Commerce release changed the BM or login markup — pin down the locator that stopped matching, capture the new markup, write an offline *PageTest that fails on it, fix the page object under playwright_tools/src/main/java/com/mirakl/sfcc/, and build with Maven. Use for "set-feature-switches is green but the feature switch is off", "configure-sandbox-permissions times out", "the helper's BM login or MFA broke", "a locator no longer matches after the BM release", "fix the helper's Playwright automation".
argument-hint: "<action or page object> [failing run URL or log excerpt]"
allowed-tools: "Bash(mvn*) Bash(PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn*) Bash(git*) Bash(gh*) Bash(grep*) Bash(playwright-cli*) Bash(base64*) Read Grep Glob Edit Write AskUserQuestion"
---

# Repair a Business Manager automation

A BM release renames an id, swaps a tag or reshapes a form, and a page object's locator stops
matching. The action then either times out (loud) or, for `set-feature-switches`, ends green with
the setting unchanged (silent). The goal: name exactly what changed, fix the page object, and leave
an offline test that fails on the old locator and passes on the new one. The reference fix is #10:
BM 26.9 rendered the Feature Switches Apply control as a `<button>` instead of a `<td>`, and
`FeatureSwitchesPageTest` now pins that markup.

## Ground rules

- **Public repository.** The diff, the captured markup and the PR text are world-readable: no real
  hosts (use `sandbox.example`), no credentials, TOTP seeds or client ids, no customer, tenant or
  sandbox names (AGENTS.md § Public repository).
- **Live flows change a real sandbox** and need the BM automation user's password and TOTP seed.
  Run one only on a sandbox the user names, and let the user supply the values in their own shell
  or run the command themselves: never write them to a file, a commit or the conversation.
  `ConfigureSandboxPermissionsTest` replaces the sandbox's whole WebDAV and OCAPI Data API
  permission documents: ask before running it.
- Always `-Dtest=<class>`. A bare `mvn test` runs the live flows too.

## 1. Pin down what broke

Cheapest evidence first:

- **The failing run's log** of the action step, e.g.
  `gh run view <run-id> --repo <owner>/<consumer-repo> --log`. A Playwright `TimeoutError` names
  the locator that stopped matching. For `set-feature-switches`, look for
  `Error while filling Commerce API settings` (hidden behind a green step) versus
  `Clicked Apply button successfully`; after each failed attempt that flow also logs a base64 PNG
  screenshot: decode it with `base64 -d` to see where it was stuck.
- **The code**: locators in `playwright_tools/src/main/java/com/mirakl/sfcc/` (login:
  `AdminLoginPage`, `AdminVerifyPage`; screens: `FeatureSwitchesPage`,
  `ConfigureSandboxPermissionsPage`) and the flow in `playwright_tools/src/test/java/com/mirakl/sfcc/`.
- **A reproduction** with the flow's live command (AGENTS.md § Common commands), under the rules
  above.

State the broken step and locator before changing anything.

## 2. Capture the current markup

Get the HTML of what the page object touches, from a logged-in BM session on the affected sandbox:
the whole `<form>` for a BM screen, the input and its submit button for a login step.

- **`playwright-cli`** (npm package `@playwright/cli`; ask before installing it globally):
  `playwright-cli open --browser=chrome --headed https://<bm-host>/on/demandware.store/Sites-Site/`,
  let the user finish login and MFA in that window, then `playwright-cli goto` the pipeline URL
  (`.../Sites-Site/default%3bapp%3d__bm_admin/<Pipeline>-<Action>`), `playwright-cli snapshot` to
  find the element's ref, and `playwright-cli eval "el => el.outerHTML" <ref>`.
- **Without it**, the Playwright CLI bundled with the Maven dependency opens a headed Chrome in
  which the user logs in and copies the element's outer HTML from DevTools:
  `mvn -f playwright_tools/pom.xml exec:java -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="open --channel chrome https://<bm-host>/on/demandware.store/Sites-Site/"`.

Work from a scratch directory outside the checkout: snapshots, storage state and HAR files carry
session cookies and must never be committed. Diff the captured markup against the locators: that
difference is the fix.

## 3. Write the offline test first

Follow `FeatureSwitchesPageTest`:

- One class per page object, named `<PageObject>Test` so that `-Dtest='*PageTest'` selects it (a
  live flow must never end in `PageTest`), extending `PlaywrightBase`, with `getDefaultUrl()`
  returning `"about:blank"`.
- The captured markup, trimmed to the form and scrubbed (host replaced by `sandbox.example`, CSRF
  tokens and instance-specific values removed), as a text block loaded with `page.setContent(...)`.
- `page.route(...)` on the pipeline the page object submits to: record
  `route.request().postData()` and `route.fulfill(...)` with HTML, so the page object's
  `waitForResponse` completes.
- `page.setDefaultTimeout(TEN_SECONDS)` once the page objects exist: `BasePage` sets 120 s.
  A page object that `PlaywrightBase` does not create (`ConfigureSandboxPermissionsPage`) is
  constructed in the test before that call.
- Drive the page object's public methods and assert on what reaches the server, e.g.
  `assertEquals("ScapiHookExecutionEnabled=true&ActionButton=Apply", submittedForm.get())`.

Run it against the unfixed page object and keep the failing `Tests run:` line.

## 4. Fix the page object

- The smallest locator change that matches the new markup and stays specific. Prefer the stable
  attributes BM exposes (`name`, `data-automation`) over positions or generated ids; when the tag is
  what changed, match the attribute on any tag (`//*[@data-automation='apply-button']`).
- Keep waiting on the server's response to a save (`ViewFeatureSwitchPreferences-Update`,
  `-Dispatch`), so the browser context never closes before the save lands.
- If the flow you touch still hides its final failure (`FillCommerceApiSettingsTest` catches every
  exception and ends normally after its third attempt), make it fail after the last attempt in the
  same PR, keeping the retries and the screenshot, so the next drift turns the action red.

## 5. Build and verify

```bash
mvn -q -f playwright_tools/pom.xml test-compile
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -f playwright_tools/pom.xml test -Dtest='*PageTest'
```

The offline tests launch the `chrome` channel: Google Chrome must be installed. Report the
`Tests run:` lines before and after the fix. The final proof is a live run of the fixed flow on the
sandbox the user named: `Clicked Apply button successfully` or
`OCAPI Data API permissions configured successfully` in the log, and the setting visible in BM
after a reload.

## 6. Ship

- PR title: plain sentence-case imperative, no type prefix or ticket key
  (`Fix feature switch activation`); branch: a bare slug. The body says what BM changed (old and
  new markup, in words), the locator change, the test, and how it was verified.
- Consumers get nothing until a release: after the merge, use the `release` skill.
