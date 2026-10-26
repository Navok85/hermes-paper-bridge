# Changelog

## 1.1.0

### Added
- `language` (default `en`, or `de`): language of chat and console messages. Existing installs need a manual `language` entry in `plugins/HermesChat/config.yml`; without it the default `en` applies.
- `cooldown-seconds` (default 10, `0` = off): minimum delay between two questions per player, applies live.
- Tab completion for `/hermes reset` (only with `hermes.reset`).

### Changed
- Chat and console messages default to English; German is available via `language: de`.
- `api-key` is empty in the default config; the plugin stays disabled without a key instead of later answering with "HTTP 401".
- Only players and the server console may use `/hermes`. RCON and command blocks are rejected, since they would otherwise share a session.
- `/hermes reset` is rejected while a request is running.
- The session reset suffix is now a counter instead of a random number (no collisions). Existing `data.yml` entries remain valid.
- Error details (HTTP body, exception texts) now appear only in the server console, not in player chat.
- An answer without text is logged to the console with the first 200 characters of the body.
- The plugin version in `plugin.yml` is taken from `pom.xml`.

### Fixed
- `/hermes reset <text>` reset the session instead of asking the question. The reset now only applies to exactly one argument.
- An error while sending or delivering could block a player permanently ("Still waiting…"). The lock is now always released.
- Truncating long answers could split an emoji in the middle.

## 1.0.0

- First version: `/hermes <question>` and `/hermes reset`, one Hermes session per player, answer only to the asker or to all players via `broadcast`.
