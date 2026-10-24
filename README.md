# HermesChat

Paper-Plugin (Minecraft Java 26.3), mit dem Spieler im Chat mit ihrem
[Hermes-Agenten](https://hermes-agent.nousresearch.com/) sprechen.

```
/hermes Wie viele Diamanten brauche ich für eine volle Rüstung?
```

## Voraussetzungen

- Paper 26.3 (Java 25)
- Hermes mit aktiviertem API-Server (`~/.hermes/.env`):

```bash
API_SERVER_ENABLED=true
API_SERVER_KEY=dein-geheimer-key
```

Danach `hermes gateway` starten – der API-Server lauscht auf `http://127.0.0.1:8642`.

## Installation

1. `target/hermes-chat-1.0.0.jar` nach `plugins/` kopieren.
2. Server starten (oder `/reload confirm`).
3. `plugins/HermesChat/config.yml` anpassen – mindestens `api-key`.
4. Server neu starten (Config wird nur beim Enable gelesen).

```yaml
base-url: http://127.0.0.1:8642/v1   # Hermes API-Server inkl. /v1
api-key: dein-geheimer-key            # = API_SERVER_KEY
model: hermes-agent                   # ohne provider nutzt Hermes den Gateway-Default
timeout-seconds: 300
max-chars: 1500
broadcast: false                      # true: alle Spieler sehen die Antwort
session-prefix: mc-                   # Session-ID: mc-<uuid>
```

## Benutzung

| Befehl | Wirkung |
|---|---|
| `/hermes <frage>` (Alias `/h`) | Frage an Hermes; Antwort nur an den Fragenden (bzw. alle bei `broadcast: true`) |
| `/hermes reset` | neue Konversation (neue Hermes-Session-ID) |

Berechtigungen: `hermes.chat`, `hermes.reset` (beide default `op`).

Jeder Spieler hat eine eigene Hermes-Session (`X-Hermes-Session-Id: mc-<uuid>`);
Hermes behält damit den Gesprächsverlauf inklusive Tool-Ergebnissen bei.
Antworten werden bei `max-chars` gekürzt und zeilenweise in den Chat geschickt.

## Build

```bash
export JAVA_HOME=~/.local/tools/jdk-25.0.5+8
~/.local/tools/apache-maven-3.9.9/bin/mvn package
# → target/hermes-chat-1.0.0.jar
```

## Grenzen

- Kein Streaming, keine Tool-Progress-Anzeige, keine Approvals aus dem Spiel.
- Ein Turn, der länger als `timeout-seconds` läuft, bricht mit Fehlermeldung ab.
- Bei einem API-Fehler (z. B. 409, weil die Session einer anderen Oberfläche gehört) hilft `/hermes reset`.
