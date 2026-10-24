# HermesChat Implementation Plan

> **For agentic workers:** Execute inline (execution method supplied: native). Steps use checkbox syntax.

**Goal:** Paper-26.3-Plugin, das `/hermes <frage>` an den Hermes-API-Server schickt und die Antwort im Chat ausgibt.

**Architecture:** Ein Plugin, vier Klassen: Plugin-Lifecycle/Config, async HTTP-Client, Command-Handler, reiner Text-Splitter. Antworten kommen via Bukkit-Scheduler zurück auf den Main-Thread.

**Tech Stack:** Java 25, Maven, paper-api 26.3.build.222-stable, Gson (von Paper geliefert), java.net.http.

**Spec:** `docs/superpowers/specs/2026-10-25-hermes-chat-plugin-design.md`

## Global Constraints

- Java release 25; paper-api `26.3.build.222-stable`, scope `provided`.
- `plugin.yml`: `api-version: '26.3'`, main `de.navok.hermeschat.HermesChatPlugin`.
- Kein Shading, keine zusätzlichen Runtime-Dependencies.
- Endpoint `POST {base-url}/chat/completions`, Header `Authorization: Bearer`, `X-Hermes-Session-Id`.
- Permissions: `hermes.chat`, `hermes.reset` (default `op`).

## Review Focus

- Hermes-Gateway offline → Spieler sieht Klartext, Serverlog hat Details, kein Stacktrace im Chat.
- Antwort mit Zeilenumbrüchen / sehr lang → mehrere Chat-Zeilen, Gesamtlänge ≤ `max-chars`.
- Doppeltes `/hermes` während laufender Anfrage → Busy-Hinweis, kein zweiter Request.
- Leere/fehlende `api-key` → Plugin deaktiviert sich, statt Requests ohne Auth zu senden.
- Console-Sender (kein UUID) → stabiler Session-Key `mc-console`.

---

### Task 1: Build-Gerüst + Text-Splitter

**Files:** `pom.xml`, `src/main/resources/plugin.yml`, `src/main/resources/config.yml`, `src/main/java/de/navok/hermeschat/ChatText.java`, `src/test/java/de/navok/hermeschat/ChatTextTest.java`

**Interfaces:**
- Produces: `static List<String> ChatText.split(String text, int maxChars)` — trimmt, kürzt auf `maxChars` + Marker ` … [gekürzt]`, teilt an Zeilenumbrüchen, verwirft Leerzeilen; `null`/leer → leere Liste.

- [ ] Test schreiben (`a\nb` → 2 Zeilen; 2000×`x` mit `maxChars=100` → eine Zeile, endet auf `[gekürzt]`; leer → leer)
- [ ] Test rot laufen lassen
- [ ] `ChatText.split` implementieren
- [ ] `mvn test` grün

### Task 2: HermesClient

**Files:** `src/main/java/de/navok/hermeschat/HermesClient.java`

**Interfaces:**
- Produces: `HermesClient(String baseUrl, String apiKey, String model, int timeoutSeconds)`; `void ask(String sessionId, String prompt, Consumer<String> onAnswer, Consumer<String> onError)` — non-blocking via `HttpClient.sendAsync`; Fehlertexte: Timeout, nicht erreichbar, `HTTP <code>`, leere Antwort.

- [ ] Request-Body `{model, messages:[{role:user, content}], stream:false}` bauen, Header setzen
- [ ] Antwort parsen: `choices[0].message.content`
- [ ] Fehlerfälle auf Klartext abbilden

### Task 3: Command + Plugin-Verdrahtung

**Files:** `src/main/java/de/navok/hermeschat/HermesChatPlugin.java`, `src/main/java/de/navok/hermeschat/HermesCommand.java`

**Interfaces:**
- Produces: `String sessionId(String owner)`, `void resetSession(String owner)`, `boolean acquire(String owner)`, `void release(String owner)`, `HermesClient client()`
- Consumes: `ChatText.split`, `HermesClient.ask`

- [ ] Config-Validierung, bei fehlendem Key Plugin deaktivieren
- [ ] `data.yml` für Reset-Suffixe, `/hermes reset`
- [ ] Busy-Guard, Antwort-Versand im Main-Thread, `broadcast`-Option

### Task 4: Smoke-Test auf echtem Paper-26.3-Server

**Files:** `server/` (Wegwerf-Testserver), `stub_hermes.py` (Wegwerf-Stub), `README.md`

- [ ] Paper 26.3 herunterladen, `eula=true`, Server starten
- [ ] Jar nach `plugins/`, Stub auf `:8642` belegen Header/Body
- [ ] `/hermes Hallo` in der Serverkonsole → Antwort im Log, `X-Hermes-Session-Id: mc-console` im Stub-Log
- [ ] Stub stoppen → `/hermes Hallo` liefert „nicht erreichbar“
- [ ] README mit Installations-/Konfig-Schritten
