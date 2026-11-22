# Security

## Reporting a vulnerability

Report vulnerabilities privately through
[GitHub security advisories](https://github.com/dkarski/diceroller/security/advisories/new), not in public issues.
I will reply as soon as I can.

DiceRoller has no network access, permissions or accounts. The most relevant issues are in how the app is built,
signed and released.

## Verifying a download

Every release is signed with the same key.

To check an APK, run `apksigner verify --print-certs diceroller-X.Y.Z.apk` (Android SDK Build-Tools).
Compare the signing certificate's `SHA-256 digest` with a trusted copy of the app, for example one installed
from InkApps. The `.sha256` file attached to each release only confirms that the download is complete.
