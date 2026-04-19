# Signing release APKs (Truffle)

Use this when you ship a new version: a **signed** release APK installs on devices; an **unsigned** `truffle-release-unsigned.apk` does not.

## What this repo expects

- **`keystore.properties`** at the **repo root** (next to `settings.gradle.kts`). This file is **gitignored**—never commit it.
- A **`.jks`** keystore file; `storeFile` in `keystore.properties` is resolved from the **repo root** (see `app/build.gradle.kts`).
- Gradle task: **`./gradlew :app:assembleRelease`**

Signed output: **`app/build/outputs/apk/release/truffle-release.apk`**

---

## First-time setup (create a keystore)

### 1. Generate the keystore

From the repo root:

```bash
keytool -genkeypair -v \
  -keystore truffle-release.jks \
  -alias truffle \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Truffle, OU=Mobile, O=YourName, C=US"
```

You will be prompted for **keystore password** and **key password** (they may be the same). Remember them.

Keep **`truffle-release.jks`** in a **backup** location (USB, encrypted backup, password manager attachment). If you lose it, you cannot publish updates that replace installs signed with that key.

### 2. Create `keystore.properties`

Copy the example and edit:

```bash
cp keystore.properties.example keystore.properties
```

Set real values, for example:

```properties
storeFile=truffle-release.jks
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=truffle
keyPassword=YOUR_KEY_PASSWORD
```

If the `.jks` lives outside the repo, use a **full path** for `storeFile`.

### 3. Build the signed APK

```bash
./gradlew :app:assembleRelease
```

(Use Android Studio’s JBR if `java` is not on your PATH, e.g.  
`JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleRelease`.)

---

## Every future release

1. Bump **`versionCode`** / **`versionName`** in `app/build.gradle.kts` when you want a new version number.
2. Ensure **`keystore.properties`** and the **`.jks`** are still present (same machine, or restore from backup).
3. Run **`./gradlew :app:assembleRelease`**.
4. Upload **`app/build/outputs/apk/release/truffle-release.apk`** to [GitHub Releases](https://github.com/Arunprakaash/truffle/releases).

**Same keystore for every public release** so users who sideloaded an older APK can upgrade in place.

---

## Without `keystore.properties`

Gradle still builds **`truffle-release-unsigned.apk`**. That file is **not installable** until you sign it (e.g. with `zipalign` + `apksigner`). Easiest path: add `keystore.properties` and rebuild so Gradle emits **`truffle-release.apk`** signed.

---

## Troubleshooting

| Symptom | Likely cause |
|--------|----------------|
| **App not installed** on top of an old build | Different signing key (e.g. debug vs release). Uninstall the old app, or always ship updates with the **same** release keystore. |
| Gradle cannot find keystore | Wrong **`storeFile`** path; use repo-root-relative name or an absolute path. |
| Want to avoid passwords in shell history | Prefer editing `keystore.properties` in an editor instead of `export` + CLI flags. |

---

## Related

- **`README.md`** — build overview and GitHub release steps.
- **`keystore.properties.example`** — template for the properties file.
