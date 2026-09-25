# salesforce-b2c-helper — Agent Guide

## TL;DR

`salesforce-b2c-helper` is CI helper tooling for the Mirakl Salesforce B2C Commerce (SFCC)
connector, `connector-sfcc-plugin`. It contains **no connector code**. It automates the few
Business Manager (BM) settings that have no API, by driving headless Google Chrome with
**Playwright for Java**: log into BM (username, password, TOTP), then change a setting. Each
automation is packaged as a **GitHub composite action**:

| Action | Flow (JUnit class) | What it changes on the sandbox |
|--------|--------------------|--------------------------------|
| [`set-feature-switches`](set-feature-switches/action.yml) | `FillCommerceApiSettingsTest` | Turns on the `ScapiHookExecutionEnabled` feature switch ("Enable Salesforce Commerce API Hook Execution") |
| [`configure-sandbox-permissions`](configure-sandbox-permissions/action.yml) | `ConfigureSandboxPermissionsTest` | Rewrites the WebDAV client permissions and the OCAPI Data API settings for the `ADMIN_OCAPI_KEY` client, after a sandbox reset |

Consumers pin `mirakl/salesforce-b2c-helper/<action>@vN`. A merged change reaches them only after
a new `vN` release **and** a bump of their pin (see [Releases and consumers](#releases-and-consumers)).

> **Read before editing:** `playwright_tools/src/test` holds two kinds of JUnit classes.
> The **live flows** (`FillCommerceApiSettingsTest`, `ConfigureSandboxPermissionsTest`) *are* the
> automation: running them logs into a real sandbox and changes its configuration. The **offline
> markup tests** (`*PageTest`, today `FeatureSwitchesPageTest`) are real tests: they drive a page
> object against captured BM HTML and assert on what it submits. Never run `mvn test` without
> `-Dtest`.

## What this is / is not

- **IS** out-of-band CI automation that puts a freshly deployed or reset SFCC sandbox into the
  state the connector's tests need.
- **IS** a leaf: nothing here depends on the connector; the connector's CI depends on this repo.
- **IS NOT** the connector. The connector runs as SFCC cartridges inside the SFCC instance.
- **IS NOT** a Spring or Kafka application. `src/main/resources/logback-spring*.xml` were copied
  from another project and are **not loaded**: Logback only auto-loads `logback.xml` /
  `logback-test.xml`, so the tool logs with Logback's default console configuration.
- **IS NOT** a Node project: pure Java/Maven, no `package.json`.
- **HAS NO CI.** There is no `.github/` directory: nothing compiles or tests a pull request here.
  Run the [build gate](#common-commands) yourself before asking for review or releasing.

## Tech stack & prerequisites

From [`playwright_tools/pom.xml`](playwright_tools/pom.xml) and the two `action.yml` files:

- **Java 21**: `<java.version>21</java.version>`, compiler `<source>21</source>` /
  `<target>21</target>`; both actions use `actions/setup-java@v6` with `distribution: zulu`,
  `java-version: 21`.
- **Maven**, no wrapper ([`playwright_tools/README.md`](playwright_tools/README.md) names 3.8.1).
  `maven-compiler-plugin` 3.16.0, `maven-surefire-plugin` 3.6.0 (`forkCount` 1).
- **Playwright for Java 1.62.0**, launched on the **`chrome` channel** (installed Google Chrome,
  not Playwright's bundled Chromium), headless.
- **JUnit Jupiter 6.1.3**, **SLF4J 2.0.19**, **Logback 1.6.3**.
- **aerogear-otp-java 1.0.0** generates the TOTP code, so `SFCC_AUTOMATED_TESTS_SECRET_KEY` is the
  BM user's **authenticator seed**, not a password.

Local prerequisites:

- **Maven running on JDK 21 or later**: `mvn -v` prints the JDK it uses, which can differ from
  `java -version`. An older JDK fails the compile with `invalid target release: 21`.
- **Google Chrome** installed, for both kinds of tests. Install it through Playwright if needed
  (see [Common commands](#common-commands)).
- **`PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1`** in the environment of every Maven run that starts
  Playwright; otherwise `Playwright.create()` downloads the bundled Chromium, Firefox and WebKit,
  which are never launched. The actions set it.
- Live flows only: a sandbox BM host, a BM user whose TOTP seed you hold, and (permissions flow)
  the OCAPI client id to grant.

## Repository map

```
salesforce-b2c-helper/
├── AGENTS.md / CLAUDE.md             # this guide (CLAUDE.md imports it)
├── README.md                         # 2-line stub
├── license.md                        # Mirakl SAS BSD-style license
├── .claude/skills/                   # agent skills: bm-automation-fix, release
├── set-feature-switches/action.yml             # composite action -> FillCommerceApiSettingsTest
├── configure-sandbox-permissions/action.yml    # composite action -> ConfigureSandboxPermissionsTest
└── playwright_tools/                 # Maven project com.mirakl.sfcc:playwright_tools:0.1-SNAPSHOT
    ├── pom.xml
    ├── README.md                     # live run command + secrets (set-feature-switches only)
    └── src/
        ├── main/java/com/mirakl/sfcc/          # page objects
        │   ├── BasePage.java                       # 120 s action / 240 s navigation timeouts
        │   ├── AdminLoginPage.java                 # #username, #password, #kc-login
        │   ├── AdminVerifyPage.java                # TOTP into #input-9 + fallback submits
        │   ├── AdminPage.java                      # empty
        │   ├── FeatureSwitchesPage.java            # ScapiHookExecutionEnabled + Apply
        │   └── ConfigureSandboxPermissionsPage.java  # FileContent textarea + saveSettings
        ├── main/resources/logback-spring*.xml  # not loaded (see above)
        └── test/java/com/mirakl/sfcc/
            ├── PlaywrightBase.java                 # browser/context lifecycle, page object wiring
            ├── FillCommerceApiSettingsTest.java    # LIVE flow of set-feature-switches
            ├── ConfigureSandboxPermissionsTest.java  # LIVE flow of configure-sandbox-permissions
            └── FeatureSwitchesPageTest.java        # OFFLINE markup test of FeatureSwitchesPage
```

## Common commands

**Build gate** — no credentials, no network besides Maven; run it before every review and release:

```bash
mvn -q -f playwright_tools/pom.xml test-compile
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -f playwright_tools/pom.xml test -Dtest='*PageTest'
```

**Install Chrome through Playwright** (from `playwright_tools/`; the actions prefix it with `sudo`):

```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --force chrome"
```

**Live flows** — they change a real sandbox. The values are placeholders: never commit, log or
paste real ones.

```bash
cd playwright_tools
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn clean test -Dtest=FillCommerceApiSettingsTest \
  -DSFCC_AUTOMATED_TESTS_USERNAME=<user> -DSFCC_AUTOMATED_TESTS_PASSWORD=<password> \
  -DSFCC_AUTOMATED_TESTS_SECRET_KEY=<totp-seed> -DSFCC_BASE_URL=<bm-host>
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn clean test -Dtest=ConfigureSandboxPermissionsTest \
  -DSFCC_AUTOMATED_TESTS_USERNAME=<user> -DSFCC_AUTOMATED_TESTS_PASSWORD=<password> \
  -DSFCC_AUTOMATED_TESTS_SECRET_KEY=<totp-seed> -DSFCC_BASE_URL=<bm-host> \
  -DADMIN_OCAPI_KEY=<ocapi-client-id>
```

`SFCC_BASE_URL` is a bare host without scheme (for example
`<instance>.dx.commercecloud.salesforce.com`); the flows prepend `https://`.

Without `-Dtest`, surefire runs **every** `*Test` class, live flows included. In v5 and v6,
`set-feature-switches` did exactly that, so it also ran `ConfigureSandboxPermissionsTest` with no
`ADMIN_OCAPI_KEY`, which writes `"client_id": "null"` into both permission documents.

## How it works

**Page objects + JUnit as the runner.** Each BM screen is a class extending `BasePage` (120 s
default action timeout, 240 s navigation timeout). `PlaywrightBase` owns the lifecycle:
`@BeforeAll` launches headless Chrome (`setChannel("chrome")`, incognito, 4 GB JS heap);
`@BeforeEach` opens a fresh context and page, navigates to `getDefaultUrl()` and creates
`AdminLoginPage`, `AdminPage`, `AdminVerifyPage` and `FeatureSwitchesPage`.
`ConfigureSandboxPermissionsPage` is created inside its flow.

**Inputs** are Maven `-D` system properties read with `System.getProperty(...)`:
`SFCC_AUTOMATED_TESTS_USERNAME`, `SFCC_AUTOMATED_TESTS_PASSWORD`,
`SFCC_AUTOMATED_TESTS_SECRET_KEY`, `SFCC_BASE_URL`, and `ADMIN_OCAPI_KEY` (permissions flow only).

**Login** (`AdminLoginPage`, `AdminVerifyPage`): fill `#username`, click `#kc-login`, fill
`#password`, click `#kc-login`, then type the TOTP code digit by digit into `#input-9` and try
several submits in turn (Tab, click, forced click, Enter, JS `el.click()`), treating a URL change
as success. This is the most fragile part of both flows.

**BM screens** are pipelines under
`https://<bm-host>/on/demandware.store/Sites-Site/default%3bapp%3d__bm_admin/`:
`ViewFeatureSwitchPreferences-Show` (its form posts to `ViewFeatureSwitchPreferences-Update`),
`ViewWebdavClientPermissions-Start` and `ViewWapiSettings-Start` (both save through a `-Dispatch`
request).

1. **`FillCommerceApiSettingsTest`** (set-feature-switches). Logs in with fixed `Thread.sleep`
   waits, opens `ViewFeatureSwitchPreferences-Show`, checks
   `//input[@name='ScapiHookExecutionEnabled']` if unchecked, clicks
   `//*[@data-automation='apply-button']` and waits for the `ViewFeatureSwitchPreferences-Update`
   response. The locator matches any tag because BM 26.9 renders Apply as a `<button>` (it was a
   `<td>`; fixed in #10, released as v7). Up to three attempts, a base64 PNG screenshot logged
   after each failed one, and then the method **returns normally** (see gotchas).
2. **`ConfigureSandboxPermissionsTest`** (configure-sandbox-permissions). Explicit waits
   throughout. Writes the WebDAV JSON (`/impex`, `/cartridges`, `/static` = `read_write`), then on
   the OCAPI settings screen selects type `data` (the select auto-submits the page), writes the
   Data API JSON (`_v` 23.2) into `textarea[name='FileContent']` and clicks
   `button[name='saveSettings']`. Both documents **replace** what was there: a client missing from
   the JSON loses its permissions. No retry: a failure fails the step.

**Composite actions.** Both run from `${{ github.action_path }}/../playwright_tools`:
`actions/setup-java@v6` (zulu 21), then `sudo mvn exec:java ... install --force chrome` (Chrome
only: v7 dropped `install-deps`, since the Chrome package brings its own system dependencies), then
`mvn clean test -Dtest=<flow>` with `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1`. `set-feature-switches`
adds `-e -X`. Inputs: `SFCC_AUTOMATED_TESTS_USERNAME`, `SFCC_AUTOMATED_TESTS_PASSWORD`,
`SFCC_AUTOMATED_TESTS_SECRET_KEY`, `SFCC_BASE_URL`, plus `ADMIN_OCAPI_KEY` for
`configure-sandbox-permissions`.

## Releases and consumers

- A version is a **GitHub release** `vN` on `master` (`v1` to `v7` today), with a lightweight
  tag of the same name. `list` is a stray tag, not a version. `v5` and `v6` point to the same
  commit: v6 was published before #10 merged.
- The known consumer is `connector-sfcc-plugin`, whose CI and deploy workflows reference
  `mirakl/salesforce-b2c-helper/<action>@vN`. GitHub code search does not find those references,
  so check the consumer's own checkout with `git grep`.
- **Never move or delete a published tag.** Every consumer pinned to it would silently change
  behaviour. A fix ships as the next `vN`.
- Changes that consumers never run (this guide, `.claude/`, READMEs) need no release. Changes to
  the actions or to `playwright_tools/` (pom and sources) do.
- Use the [`release`](.claude/skills/release/SKILL.md) skill: build gate, publish the next `vN`,
  then bump every consumer pin in one PR.

## Conventions & gotchas

- **Live flows are not tests.** No assertions; "run the tests" means "reconfigure a sandbox".
  Always pass `-Dtest`.
- **`set-feature-switches` can be green while the switch stays off.** `FillCommerceApiSettingsTest`
  catches every exception and, after its third failed attempt, ends without failing. That is how
  the BM 26.9 Apply-button change went unnoticed until #10. A green step proves nothing: look for
  `Clicked Apply button successfully` versus `Error while filling Commerce API settings` in the
  log.
- **Selectors are BM and login-page DOM details**: `#username`, `#password`, `#kc-login`,
  `#input-9`, `button[vaas-buttonbrand_buttonbrand]`, `//input[@name='ScapiHookExecutionEnabled']`,
  `//*[@data-automation='apply-button']`, `textarea[name='FileContent']`, `select[name='Type']`,
  `button[name='saveSettings']`. A Salesforce BM release can break any of them. Repair them with
  the [`bm-automation-fix`](.claude/skills/bm-automation-fix/SKILL.md) skill and pin the new
  markup with an offline `*PageTest`.
- **Offline tests shorten the timeout.** `FeatureSwitchesPageTest` calls
  `page.setDefaultTimeout(TEN_SECONDS)` after the page objects are built, since `BasePage`'s 120 s
  default would make a missing element hang for two minutes.
- **`mvn exec:java` works although `exec-maven-plugin` is not declared** in `pom.xml`: Maven
  resolves the `exec` prefix itself. Do not "fix" the actions by pointing at a pom plugin.
- **`clickSkipForNowButton()` presses Escape then Tab** to dismiss BM onboarding popups; it clicks
  nothing.
- **Screenshots**: `PlaywrightBase.takeScreenshot(BasePage)` logs a base64 PNG (used by
  `FillCommerceApiSettingsTest`); `BasePage.takeScreenshot()` writes `screenshot_N.png` to the
  working directory and is not called by any flow.
- **Secrets travel as `-D` arguments**, and `set-feature-switches` runs Maven with `-X`. Callers
  must pass every credential from GitHub secrets so the runner masks it in the log.
- **The OCAPI Data JSON grants a second, hard-coded client id** besides `ADMIN_OCAPI_KEY`. It came
  with the action in #8 and its user is not documented here: do not remove it as a cleanup.

## Code comments

- Match the surrounding density; a comment states why a wait, a retry or a selector exists — the
  Business Manager behaviour it absorbs — never what the next Playwright call does.
- No commented-out code; nothing in a comment that must not be public (hosts beyond examples,
  client ids, credentials): [this repository is public](#public-repository).
- Existing comments stay unless the code they describe is gone.
- Stack rule: `docs/CODING-CONVENTIONS.md` § Code comments in the meta-repo that pins this
  repository as a submodule.

## Public repository

This repository is **public**; everything committed is world-readable.

- Commit no real hostnames (use `<bm-host>` or `sandbox.example`), no credentials, TOTP seeds,
  OCAPI client ids or other secrets, and no customer, tenant or sandbox names.
- Captured BM markup goes through the same filter: host replaced by `sandbox.example`, CSRF tokens
  and user- or instance-specific values removed, trimmed to the element the page object touches.
- Credentials stay action inputs; the consumer passes them from its own secrets.

## Pull requests

- Follow the recent pull requests (#7, #10, #11): the title is a plain sentence-case imperative
  with no type prefix or ticket key (`Fix feature switch activation`, `Speed up Playwright setup in
  composite actions`), and the branch is a kebab-case slug (`fix-feature-switch-activation`).
  Older history is mixed: underscore branches (`update_dependencies`) and one `feat:` title (#8).
- The repository only allows squash merges.

## Where to make a change

| Task | Location |
|------|----------|
| A BM screen changed and a locator no longer matches | the page object in `playwright_tools/src/main/java/com/mirakl/sfcc/` + an offline `*PageTest` (skill `bm-automation-fix`) |
| Feature switch toggled / Apply behaviour | [`FeatureSwitchesPage.java`](playwright_tools/src/main/java/com/mirakl/sfcc/FeatureSwitchesPage.java), flow in [`FillCommerceApiSettingsTest.java`](playwright_tools/src/test/java/com/mirakl/sfcc/FillCommerceApiSettingsTest.java) |
| Granted WebDAV / OCAPI Data API permissions | `buildWebdavJson()` / `buildOcapiDataJson()` in [`ConfigureSandboxPermissionsTest.java`](playwright_tools/src/test/java/com/mirakl/sfcc/ConfigureSandboxPermissionsTest.java) |
| Permission screen interaction (type select, textarea, save) | [`ConfigureSandboxPermissionsPage.java`](playwright_tools/src/main/java/com/mirakl/sfcc/ConfigureSandboxPermissionsPage.java) |
| Login or MFA | [`AdminLoginPage.java`](playwright_tools/src/main/java/com/mirakl/sfcc/AdminLoginPage.java), [`AdminVerifyPage.java`](playwright_tools/src/main/java/com/mirakl/sfcc/AdminVerifyPage.java) |
| Browser launch args, timeouts, page object wiring | [`PlaywrightBase.java`](playwright_tools/src/test/java/com/mirakl/sfcc/PlaywrightBase.java), [`BasePage.java`](playwright_tools/src/main/java/com/mirakl/sfcc/BasePage.java) |
| Action inputs or steps | [`set-feature-switches/action.yml`](set-feature-switches/action.yml), [`configure-sandbox-permissions/action.yml`](configure-sandbox-permissions/action.yml) |
| Dependency or Java version | [`playwright_tools/pom.xml`](playwright_tools/pom.xml), and `java-version` in both actions |
| Publish a change to consumers | skill [`release`](.claude/skills/release/SKILL.md) |

## Further reading

- [README.md](README.md): 2-line stub.
- [playwright_tools/README.md](playwright_tools/README.md): the original run command and the
  secrets `set-feature-switches` needs; it predates `configure-sandbox-permissions` and
  `ADMIN_OCAPI_KEY`.
- [license.md](license.md): Mirakl SAS BSD-style license.
