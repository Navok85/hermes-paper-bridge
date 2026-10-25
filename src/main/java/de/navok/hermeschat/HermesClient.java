package de.navok.hermeschat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.logging.Logger;

/** Minimal OpenAI-compatible client for the Hermes API server. */
public final class HermesClient {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final URI endpoint;
    private final String apiKey;
    private final String model;
    private final Duration timeout;
    private final Logger log;

    public HermesClient(String baseUrl, String apiKey, String model, int timeoutSeconds, Logger log) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.endpoint = URI.create(base + "/chat/completions");
        this.apiKey = apiKey;
        this.model = model;
        this.timeout = Duration.ofSeconds(Math.max(1, timeoutSeconds));
        this.log = log;
    }

    /** Sends one prompt; exactly one of the callbacks runs, on an HTTP worker thread. */
    public void ask(String sessionId, String prompt, Consumer<String> onAnswer, Consumer<String> onError) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder(endpoint)
                    .timeout(timeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("X-Hermes-Session-Id", sessionId)
                    .POST(HttpRequest.BodyPublishers.ofString(body(prompt), StandardCharsets.UTF_8))
                    .build();
        } catch (RuntimeException e) {
            log.warning("Anfrage konnte nicht gebaut werden: " + e);
            onError.accept("Anfrage konnte nicht gebaut werden (Details in der Server-Konsole).");
            return;
        }

        HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((response, error) -> {
                    if (error != null) {
                        onError.accept(describe(error));
                        return;
                    }
                    if (response.statusCode() != 200) {
                        log.warning("Hermes antwortete mit HTTP " + response.statusCode() + ": " + snippet(response.body()));
                        onError.accept("HTTP " + response.statusCode() + " (Details in der Server-Konsole).");
                        return;
                    }
                    String answer = extractAnswer(response.body());
                    if (answer.isBlank()) {
                        log.warning("Hermes-Antwort ohne Text: " + snippet(response.body()));
                        onError.accept("Hermes hat eine leere Antwort geliefert (Details in der Server-Konsole).");
                    } else {
                        onAnswer.accept(answer);
                    }
                });
    }

    private String body(String prompt) {
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject root = new JsonObject();
        root.addProperty("model", model);
        root.addProperty("stream", false);
        root.add("messages", messages);
        return root.toString();
    }

    static String extractAnswer(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                return "";
            }
            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null) {
                return "";
            }
            JsonElement content = message.get("content");
            return content == null || content.isJsonNull() ? "" : content.getAsString();
        } catch (RuntimeException e) {
            return "";
        }
    }

    private String describe(Throwable error) {
        Throwable root = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
        if (root instanceof HttpTimeoutException) {
            return "Hermes hat nicht rechtzeitig geantwortet (Timeout).";
        }
        if (root instanceof ConnectException) {
            return "Hermes-Gateway nicht erreichbar.";
        }
        log.warning("Verbindungsfehler zu Hermes: " + root);
        return "Verbindungsfehler (Details in der Server-Konsole).";
    }

    private static String snippet(String body) {
        if (body == null) {
            return "";
        }
        String flat = body.replaceAll("\\s+", " ").strip();
        return flat.length() > 200 ? flat.substring(0, 200) + "…" : flat;
    }
}
