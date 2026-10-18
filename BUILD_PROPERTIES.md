# DriveFlow Driver - Build Properties, Signing & Keystore Guide

---

## 1. Build Properties & Configuration Reference

All sensitive parameters and owner contacts are passed via Gradle build properties or environment variables during release assembly.

| Property / Env Var | Default in Code | Purpose | Where Defined |
|--------------------|-----------------|---------|---------------|
| `driveflow.license.public.key` | Contract Default | Public ECDSA P-256 SPKI base64 key matching the owner's key tool | `app/build.gradle.kts` / CI |
| `driveflow.owner.whatsapp` | `201128440045` | Owner WhatsApp number (international format) for subscription links | `app/build.gradle.kts` / CI |
| `driveflow.owner.instapay` | `01122104999` | Owner InstaPay account / phone number displayed to drivers | `app/build.gradle.kts` / CI |
| `driveflow.price.text` | `150 EGP / month` | Subscription price label displayed on the subscription card | `app/build.gradle.kts` / CI |
| `driveflow.free.daily.list` | `20` | Daily allowed request list reads in FREE mode | `app/build.gradle.kts` / CI |
| `driveflow.free.daily.details` | `5` | Daily allowed request detail reads in FREE mode | `app/build.gradle.kts` / CI |
| `KEYSTORE_PATH` | `${rootDir}/my-upload-key.jks` | Path to the production release keystore file | Environment Variable |
| `STORE_PASSWORD` | *(Secret)* | Password protecting the release keystore | Environment Variable |
| `KEY_PASSWORD` | *(Secret)* | Password protecting the release signing key | Environment Variable |

---

## 2. CRITICAL: Keystore Preservation & Backup Rule for the Owner

> ⚠️ **MANDATORY WARNING FOR THE OWNER:**
>
> You **MUST** sign every update of DriveFlow Driver with the **EXACT SAME KEYSTORE** and alias for the entire lifetime of the product.
>
> **Why?**
> Android identifies an application by its package name (`com.driveflow.driver`) AND the digital signature of its signing certificate. If you lose the keystore, change passwords, or generate a new keystore:
> 1. Drivers **WILL NOT** be able to install new APK updates over the old version. Android will reject the installation with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`.
> 2. Drivers would be forced to uninstall the old app, which **permanently erases their saved settings, custom policies, and active license keys**!
>
> **Owner Action Required:**
> 1. Store your `release.jks` file in a secure password manager (e.g., 1Password, Bitwarden).
> 2. Keep **AT LEAST TWO SEPARATE OFFLINE BACKUPS** of the keystore file and its passwords (e.g., on an encrypted USB flash drive stored in a safe, and in an encrypted cloud vault).

---

## 3. Version Code Bumping
Every APK sent to drivers must have a strictly higher `versionCode` than the previous build. Use the automated bash script:
```bash
# Increment patch (e.g., 1.0.0 -> 1.0.1, versionCode 1 -> 2)
./scripts/bump_version.sh patch

# Increment minor (e.g., 1.0.1 -> 1.1.0, versionCode 2 -> 3)
./scripts/bump_version.sh minor

# Increment major (e.g., 1.1.0 -> 2.0.0, versionCode 3 -> 4)
./scripts/bump_version.sh major
```

---

## 4. Manual Release Build Command
To build a signed release APK locally on your machine:
```bash
KEYSTORE_PATH="/path/to/my-release.jks" \
STORE_PASSWORD="your_store_password" \
KEY_PASSWORD="your_key_password" \
gradle :app:assembleRelease \
  -Pdriveflow.license.public.key="YOUR_SPKI_PUBLIC_KEY" \
  -Pdriveflow.owner.whatsapp="201128440045" \
  -Pdriveflow.owner.instapay="01122104999"
```
The output APK is generated at:
`app/build/outputs/apk/release/app-release.apk`
