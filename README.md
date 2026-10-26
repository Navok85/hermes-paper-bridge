# Hermes-Paper-Bridge

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

1. `target/hermes-chat-1.1.0.jar` nach `plugins/` kopieren (eine alte `hermes-chat-*.jar` vorher entfernen).
2. Server starten (ein Neustart ist `/reload` vorzuziehen).
3. `plugins/HermesChat/config.yml` anpassen – mindestens `api-key`.
4. Server neu starten: `base-url`, `api-key`, `model` und `timeout-seconds` werden nur beim Enable gelesen. `max-chars`, `cooldown-seconds`, `broadcast` und `session-prefix` gelten live – wer den Prefix ändert, ändert damit alle Session-IDs (der Verlauf ist dann weg).

```yaml
base-url: http://127.0.0.1:8642/v1   # Hermes API-Server inkl. /v1
api-key: dein-geheimer-key            # = API_SERVER_KEY
model: hermes-agent                   # ohne provider nutzt Hermes den Gateway-Default
timeout-seconds: 300
max-chars: 1500
cooldown-seconds: 10                  # Mindestabstand zwischen zwei Fragen pro Spieler, ab Fragebeginn (0 = aus)
broadcast: false                      # true: alle Spieler sehen die Antwort
session-prefix: mc-                   # Session-ID: mc-<uuid>
```

## Benutzung

| Befehl | Wirkung |
|---|---|
| `/hermes <frage>` (Alias `/h`) | Frage an Hermes; Antwort nur an den Fragenden (bzw. alle bei `broadcast: true`) |
| `/hermes reset` (nur genau dieses eine Wort) | neue Konversation (neue Hermes-Session-ID); `/hermes reset foo` ist eine normale Frage |

Berechtigungen: `hermes.chat`, `hermes.reset` (beide default `op`).

Jeder Spieler hat eine eigene Hermes-Session (`X-Hermes-Session-Id: mc-<uuid>`);
Hermes behält damit den Gesprächsverlauf inklusive Tool-Ergebnissen bei.
Antworten werden bei `max-chars` gekürzt und zeilenweise in den Chat geschickt.
Nur Spieler und die Server-Konsole können `/hermes` nutzen; die Konsole hat eine eigene Session (`mc-console`). RCON und Command-Blöcke werden abgelehnt, weil sie sich sonst eine Session teilen würden.

## Build

```bash
export JAVA_HOME=~/.local/tools/jdk-25.0.5+8
~/.local/tools/apache-maven-3.9.9/bin/mvn package
# → target/hermes-chat-1.1.0.jar
```

## Grenzen

- Kein Streaming, keine Tool-Progress-Anzeige, keine Approvals aus dem Spiel.
- Ein Turn, der länger als `timeout-seconds` läuft, bricht mit Fehlermeldung ab.
- Bei einem API-Fehler (z. B. 409, weil die Session einer anderen Oberfläche gehört) hilft `/hermes reset`.
