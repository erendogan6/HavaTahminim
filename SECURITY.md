# Security Policy

## Supported versions

Only the latest release published on Google Play receives security fixes.

| Version | Supported |
|---------|-----------|
| Latest (Play Store) | ✅ |
| Older | ❌ |

## Reporting a vulnerability

Please **do not** open a public issue for security vulnerabilities.

Report privately through GitHub's [Security Advisories](https://github.com/erendogan6/HavaTahminim/security/advisories/new) ("Report a vulnerability"). Include:

- affected component and version,
- steps to reproduce or a proof of concept,
- the impact you observed.

You can expect an acknowledgement within a few days. Once a fix is released, the advisory is published with credit to the reporter unless anonymity is requested.

## Handling of secrets

The app ships **no API keys**: ZekAI reaches Gemini through Firebase AI Logic with App Check (Play Integrity) attesting the caller, and the weather/geocoding provider (Open-Meteo) needs no key. Release signing material lives only in CI secrets. Please report any credential you believe has been committed to the repository.
