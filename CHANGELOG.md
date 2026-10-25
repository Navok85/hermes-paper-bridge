# Changelog

## 1.1.0

### Neu
- `cooldown-seconds` (Standard 10, `0` = aus): Mindestabstand zwischen zwei Fragen pro Spieler, gilt live.
- Tab-Completion für `/hermes reset` (nur mit `hermes.reset`).

### Geändert
- `api-key` ist in der Standard-Config leer; das Plugin bleibt ohne gesetzten Key deaktiviert statt später mit „HTTP 401“ zu antworten.
- Nur Spieler und die Server-Konsole dürfen `/hermes` nutzen. RCON und Command-Blöcke werden abgelehnt, da sie sich sonst eine Session geteilt hätten.
- `/hermes reset` wird abgelehnt, solange eine Anfrage läuft.
- Der Reset-Suffix der Session ist jetzt ein Zähler statt einer Zufallszahl (keine Kollisionen). Bestehende `data.yml`-Einträge bleiben gültig.
- Fehlerdetails (HTTP-Body, Exception-Texte) stehen nur noch in der Server-Konsole, nicht mehr im Spieler-Chat.
- Eine Antwort ohne Text wird mit den ersten 200 Zeichen des Bodys in der Konsole protokolliert.
- Die Plugin-Version in `plugin.yml` wird aus der `pom.xml` übernommen.

### Behoben
- `/hermes reset <text>` setzte die Session zurück, statt die Frage zu stellen. Der Reset greift nur noch bei genau einem Argument.
- Ein Fehler beim Absenden oder Zustellen konnte den Spieler dauerhaft sperren („Ich warte noch…“). Die Sperre wird jetzt immer freigegeben.
- Das Kürzen langer Antworten konnte ein Emoji in der Mitte durchschneiden.

## 1.0.0

- Erste Version: `/hermes <frage>` und `/hermes reset`, eine Hermes-Session pro Spieler, Antwort nur an den Fragenden oder per `broadcast` an alle.
