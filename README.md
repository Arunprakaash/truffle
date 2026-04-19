# Truffle

A quiet, local-first Android journal for everyday money: accounts, transactions, bills, goals, and simple backups—**no server; your data stays on the device** unless you export it.

**Install (sideload):** [GitHub Releases](https://github.com/Arunprakaash/truffle/releases) — maintainer: [draft a release](https://github.com/Arunprakaash/truffle/releases/new) when publishing a new APK.

## Requirements

- **Android Studio** (recent stable) or Android SDK + Gradle
- **JDK 17+** recommended (project targets Java **11** bytecode; use the JBR bundled with Android Studio if unsure)
- Device or emulator running **Android 13 (API 33)+** (`minSdk` **33**); always verify against `app/build.gradle.kts`

## Build

Debug:

```bash
./gradlew :app:assembleDebug
```

### Release APK (R8 + smaller resources)

Release builds turn on **code shrinking** and **resource shrinking** (`isMinifyEnabled` / `isShrinkResources` in `app/build.gradle.kts`).

**Signing (step-by-step for every release):** see [`docs/SIGNING.md`](docs/SIGNING.md).

**Signing (local only, not committed):**

1. One-time keystore (example; adjust alias/passwords):

   ```bash
   keytool -genkeypair -v -keystore truffle-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias truffle
   ```

2. Copy `keystore.properties.example` → **`keystore.properties`** at the repo root (this file is **gitignored**). Set `storeFile` (path to the `.jks`), passwords, and `keyAlias`.

3. Build:

   ```bash
   ./gradlew :app:assembleRelease
   ```

4. Find the APK under **`app/build/outputs/apk/release/`**: with `keystore.properties` you get a **signed** `truffle-release.apk`. Without it, Gradle still builds an optimized **`truffle-release-unsigned.apk`** (R8 + shrunk resources)—sign it with `apksigner` (or add `keystore.properties` and rebuild) before publishing so installs and updates work normally.

5. Upload that signed APK to GitHub Releases.

Do **not** commit the keystore or `keystore.properties`.

### Publish on GitHub Releases

1. Commit and push your release branch (usually `main`).
2. Open **[Draft a new release](https://github.com/Arunprakaash/truffle/releases/new)** (or **Releases → Draft a new release**).
3. **Choose a tag** (e.g. `v1.0.0`) and create it if needed; title can match the tag.
4. Describe changes in the release notes.
5. **Attach** the signed `truffle-release.apk` (drag-and-drop).
6. Publish the release.

Users who sideload get updates only if future APKs are signed with the **same** key as the one they installed first.

### GitHub code security (recommended)

On the repo: **Settings → Code security and analysis** (and **Security** where noted):

- **Private vulnerability reporting**: **Security → Code security** → enable so researchers can report issues without a public issue.
- **Dependabot alerts** + **Dependabot security updates**: turn on; this repo includes [`.github/dependabot.yml`](.github/dependabot.yml) for weekly Gradle dependency PRs.
- **Secret scanning** (and push protection if offered): enabled by default on **public** repos—leave on so accidental token commits are flagged.
- **Branch protection** (**Settings → Branches**): for `main`, enable **Require a pull request before merging** (and optionally required checks when you add CI).

Use **two-factor authentication** on maintainer GitHub accounts.

## Data & privacy

- Ledger data is stored in a **local Room** database on device.
- **Export backup** produces a JSON snapshot you can share or archive.
- **Import backup** **replaces the entire ledger** on that device after you confirm in the UI—export first if you need a copy of the current data.

There is no analytics SDK in this repository; network use is whatever the OS and libraries do by default (e.g. font or dependency downloads during **your** dev builds—not a runtime “phone home” for end users).

## License

See [`LICENSE`](LICENSE) (Apache-2.0).

## Security

If you discover a security issue, please read [`SECURITY.md`](SECURITY.md).
