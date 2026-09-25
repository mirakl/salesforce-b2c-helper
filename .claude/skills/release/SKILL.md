---
name: release
description: Release a new salesforce-b2c-helper version — scope the change since the last vN, run the build gate on the exact master commit, publish the next vN GitHub release on it, then bump every consumer pin (mirakl/salesforce-b2c-helper/<action>@vN) in one PR and check the action in a real run. Use for "release the helper", "cut v8", "tag a new helper version", "publish the fix so the connector gets it", "bump the helper actions in the connector".
argument-hint: "[commit SHA on master; default origin/master]"
---

# Release the helper and move its consumers

Consumers run `mirakl/salesforce-b2c-helper/<action>@vN`: a merged change does nothing until a
new `vN` exists **and** every consumer pin points at it. This skill does both, in that order.

## Guardrails

- **Run every command from the root of a `salesforce-b2c-helper` checkout.** Check first:
  `git remote get-url origin` must name `mirakl/salesforce-b2c-helper`, and
  `git rev-parse --show-prefix` must print nothing. Otherwise stop: another repository's tags and
  `master` would scope and publish the wrong version.
- **A published tag is permanent.** Never move, delete or re-create a tag or release: every
  consumer pinned to it would silently change behaviour. A fix ships as the next `vN`.
- **Publishing is public and immediate.** Show the target SHA, the tag, the title and the commit
  list, and get an explicit yes before `gh release create`.
- Release only commits already on `origin/master`, never a branch.

## 1. Scope the release

```bash
git fetch origin master --tags --quiet
LAST=$(git tag -l 'v*' --sort=-v:refname | head -1)
: "${LAST:?no vN tag: not a salesforce-b2c-helper checkout, or tags not fetched}"
NEXT=v$(( ${LAST#v} + 1 ))
SHA=$(git rev-parse origin/master)
echo "last=$LAST next=$NEXT sha=$SHA"
git log --oneline "$LAST..$SHA"
git diff --stat "$LAST..$SHA"
```

Later steps write these values as `<next>` and `<sha>`: substitute the printed ones.

- `v*` leaves out the stray `list` tag; versions are bare majors (`v7` → `v8`).
- No `vN` tag: the guard above stops the block. Never publish `v1`.
- Empty range: nothing to release, stop. If the user expects a change, it has not reached
  `origin/master` yet: check its pull request with
  `gh pr view <N> --repo mirakl/salesforce-b2c-helper --json state,baseRefName,mergedAt` (a pull
  request merged into another branch is not on `master`). That is how `v6` happened: it was
  published while #10 was still open, so it tagged the `v5` commit.
- A range that only touches what consumers never run (`AGENTS.md`, `CLAUDE.md`, `.claude/`,
  READMEs): say so and stop unless the user still wants a version.
- When the user names a commit instead, use its full SHA, and only if
  `git merge-base --is-ancestor <sha> origin/master` succeeds.
- Publish on `<sha>`, never on `master`: `master` can move between the build gate and the
  publish, and the release would then ship a commit the gate never built.

## 2. Build gate on the target

The helper has no CI, so this is the only check before consumers get the code:

```bash
git worktree add --detach <scratch-dir>/helper-release <sha>
mvn -q -f <scratch-dir>/helper-release/playwright_tools/pom.xml test-compile
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -f <scratch-dir>/helper-release/playwright_tools/pom.xml test -Dtest='*PageTest'
git worktree remove <scratch-dir>/helper-release
```

Maven must run on JDK 21 or later (`mvn -v`), and the offline tests launch the `chrome` channel:
Google Chrome must be installed. Report the `Tests run:` line; stop on any failure. These tests
cover markup, not a live BM: when the range changes a live flow, the login or an action, ask
whether it was run on a sandbox before merging, and say so in the release notes.

## 3. Publish

After the explicit yes:

```bash
gh release create <next> --repo mirakl/salesforce-b2c-helper --target <sha> \
  --title "<short summary>" --notes "<one line per pull request: #N title>"
git fetch origin --tags --quiet
git rev-parse "<next>^{commit}"
```

The last command must print `<sha>`. Name any change to an action's inputs in the notes: consumers
must adapt their `with:` block in the same bump.

## 4. Bump the consumers

- The known consumer is `connector-sfcc-plugin`. GitHub code search does not return its workflow
  references, so list them from a checkout of its up-to-date default branch:
  `git grep -n 'mirakl/salesforce-b2c-helper/' -- .github`.
- One branch, one PR per consumer, moving **every** reference (both actions) from the old ref to
  `@<next>`, so no workflow mixes versions. A reference on a branch or a SHA instead of a tag moves
  too; say so in the PR.
- Follow that repository's own branch, commit and PR-title conventions (its `AGENTS.md` /
  `CLAUDE.md`), never commit to its default branch, and open the PR when the user says so. The body
  lists each workflow and action with its old and new ref, what the release changes, and how it was
  verified.

## 5. Check a real run

A green action step proves nothing: `FillCommerceApiSettingsTest` swallows its last failure. On
the first consumer run that executes the new version:

```bash
gh run view <run-id> --repo <owner>/<consumer-repo> --log \
  | grep -E 'Clicked Apply button successfully|Error while filling Commerce API settings|permissions configured successfully'
```

Report the run URL and the lines found. `Error while filling Commerce API settings` without a
later `Clicked Apply button successfully` means the switch was not saved: start the
`bm-automation-fix` skill.
