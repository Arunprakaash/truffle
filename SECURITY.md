# Security policy

## Supported versions

Only the **latest tagged release** on this repository is supported with security fixes. Older tags may not receive backports.

## Reporting a vulnerability

Please **do not** open a public GitHub issue for undisclosed security problems.

Use **GitHub → Security → Report a vulnerability** (private advisory) if enabled for this repo, or open a **draft security advisory** with details. If neither is available, contact the repository owner privately with:

- A short description of the issue and its impact
- Steps to reproduce (or a proof-of-concept), if possible
- Affected version / commit, if known

We will acknowledge receipt when we can and coordinate disclosure (including a fix release or advisory) before public discussion.

Repository owners: enable **Settings → Security → Code security → Private vulnerability reporting** so reporters can use the built-in flow.

## Scope

In scope: issues in **this app’s code** shipped from this repo (e.g. unsafe handling of imported backup files, insecure storage of secrets—there should be none—or obvious Android WebView / file-provider misconfiguration if introduced later).

Out of scope: generic Android OS bugs, compromised devices, or social engineering against users who sideload builds from unofficial sources.
