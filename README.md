# Hermes-Paper-Bridge

Paper plugin (Minecraft Java 26.3) that lets players talk to their
[Hermes agent](https://hermes-agent.nousresearch.com/) in chat.

```
/hermes How many diamonds do I need for a full set of armor?
```

## Requirements

- Paper 26.3 (Java 25)
- Hermes with the API server enabled (`~/.hermes/.env`):

```bash
API_SERVER_ENABLED=true
API_SERVER_KEY=your-secret-key
```

Then start `hermes gateway` – the API server listens on `http://127.0.0.1:8642`.

## Installation

1. Copy `target/hermes-chat-1.2.0.jar` into `plugins/` (remove any old `hermes-chat-*.jar` first).
2. Start the server (a restart is preferable to `/reload`).
3. Edit `plugins/HermesChat/config.yml` – at least `api-key`.
4. Restart the server: `base-url`, `api-key`, `model`, `timeout-seconds` and `language` are only read when the plugin is enabled. `max-chars`, `cooldown-seconds`, `still-thinking-interval-seconds`, `broadcast` and `session-prefix` apply live – changing the prefix changes all session IDs (the history is then lost).

```yaml
language: en                          # chat and console messages: en or de
base-url: http://127.0.0.1:8642/v1   # Hermes API server incl. /v1
api-key: your-secret-key              # = API_SERVER_KEY
model: hermes-agent                   # without provider, Hermes uses the gateway default
timeout-seconds: 300
max-chars: 1500
cooldown-seconds: 10                  # minimum gap between two questions per player, measured from the start of the question (0 = off)
still-thinking-interval-seconds: 0    # while a request runs, remind the asker every N seconds that Hermes is still working (0 = off)
broadcast: false                      # true: all players see the answer
session-prefix: mc-                   # session ID: mc-<uuid>
```

## Usage

| Command | Effect |
|---|---|
| `/hermes <question>` (alias `/h`) | Ask Hermes; the answer goes only to the asker (or to everyone with `broadcast: true`) |
| `/hermes reset` (exactly this one word) | new conversation (new Hermes session ID); `/hermes reset foo` is a normal question |

Permissions: `hermes.chat`, `hermes.reset` (both default to `op`).

Every player has their own Hermes session (`X-Hermes-Session-Id: mc-<uuid>`),
so Hermes keeps the conversation history, including tool results.
Answers are cut at `max-chars` and sent to the chat line by line.
Only players and the server console can use `/hermes`; the console has its own session (`mc-console`). RCON and command blocks are rejected, because they would otherwise share a session.

## Build

```bash
export JAVA_HOME=~/.local/tools/jdk-25.0.5+8
~/.local/tools/apache-maven-3.9.9/bin/mvn package
# → target/hermes-chat-1.2.0.jar
```

## Limitations

- No streaming, no tool progress display, no approvals from in-game.
- A turn that runs longer than `timeout-seconds` aborts with an error message.
- On an API error (e.g. 409, because the session belongs to another interface), `/hermes reset` helps.
