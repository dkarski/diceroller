# Release checklist

## Before the first public release

- Finish the separate localization visual audit and incorporate its corrections.
- Review the final device build: icons, font scaling, haptics, ghosting, navigation and settings.
- Create the public `dkarski/diceroller` repository only when publication is authorized.
- Use `main`, squash merge and pull requests. Require the `Build` check; block deletion and force pushes.
- Enable private vulnerability reporting and GitHub Actions creating pull requests.
- Create an environment named `release`, restricted to `main`, with a required maintainer reviewer.
  Keep self-review available for a solo maintainer. This review is the final release approval gate.
- Create a dedicated DiceRoller signing key if none exists. Keep an offline backup of its file and passwords.
  Never use the debug key or copy the Lifepoints key.
- In the `release` environment, configure secrets `RELEASE_KEYSTORE_BASE64`, `RELEASE_STORE_PASSWORD`,
  `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`; set variable `RELEASE_CERT_SHA256` to the certificate SHA-256.
- Put the same certificate fingerprint in SECURITY.md before publication of the APK.

No signing key or credentials are included in this repository. Configuration above is done on GitHub,
not by copying files. No tag or release was created during the local import.

## First 1.0.0

`version.txt` is 1.0.0. The Release Please manifest is empty because nothing has been released.
The configuration selects initial version 1.0.0; it must not start at 1.1.0 or copy Lifepoints' manifest.
The `bootstrap-sha` excludes the shell-only import from the first changelog.

1. Publish the reviewed source when authorized, then verify Actions on GitHub.
2. Review the generated release PR and rewrite its initial changelog as one coherent description of 1.0.0.
   Imported feature commits are implementation groups, not separate previously shipped versions.
3. Run CI on the release PR; if the bot's token did not trigger it, dispatch CI on that PR branch.
4. Merge the release PR. The workflow creates a tag and draft release, builds the unsigned APK, then waits
   for approval of the signing environment. Review the draft and approve that job only when publication is intended.
5. The signing job verifies the certificate, package, version and alignment, attaches the APK and checksum,
   then publishes the release. Test this exact APK on the Kompakt, including updating it without uninstalling.
6. Update README's installation section after the release exists. Add InkApps installation only after acceptance.
7. Upload the exact GitHub release APK to InkApps. Use its notes as the store changelog; do not rebuild it.

## Subsequent releases and recovery

Use Conventional Commits and the generated release PR as documented in CONTRIBUTING.md.
A failed build/signing leaves a draft. Rerun Release manually on `main` with its existing draft tag.
The workflow validates that the tagged commit belongs to trusted main history before executing its build.
Published releases are never rebuilt. If the app code must change, prepare a new version.

Version code follows `major * 1000000 + minor * 1000 + patch` (1.0.0 = 1000000).
Debug builds keep the `.debug` package suffix and can coexist with production.
