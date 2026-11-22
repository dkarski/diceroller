# Security

## Reporting a vulnerability

Report vulnerabilities privately through
[GitHub security advisories](https://github.com/dkarski/diceroller/security/advisories/new)
or email daniel.karski5q@gmail.com. Please avoid publishing exploit details in public issues.
I will reply as soon as I can.

DiceRoller has no network access, requested permissions or accounts. Build, signing and distribution issues
are especially relevant. Security fixes will target the latest stable release.

## Verifying a download

The first production signing certificate has not been configured. Before the first release,
the maintainer must add its SHA-256 fingerprint here. Do not use the debug certificate or Lifepoints certificate.

For a published APK, run `apksigner verify --print-certs diceroller-X.Y.Z.apk` using Android SDK Build-Tools
and compare its signing certificate fingerprint with the one published here.
Use `shasum -a 256 -c diceroller-X.Y.Z.apk.sha256` to check the downloaded file's integrity.
The checksum alone does not authenticate its publisher.

Every production version must use the same DiceRoller signing key.
