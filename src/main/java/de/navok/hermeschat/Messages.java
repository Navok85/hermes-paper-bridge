package de.navok.hermeschat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/** Chat and log texts from messages_<language>.yml. Keys missing in the chosen language fall back to English. */
public final class Messages {

    private final YamlConfiguration texts;
    private final YamlConfiguration english;

    public Messages(JavaPlugin plugin, String language) {
        this.english = load(plugin, "en");
        this.texts = language.equals("en") ? english : load(plugin, language);
    }

    public static boolean supports(String language) {
        return language.equals("en") || language.equals("de");
    }

    public String get(String key, Object... args) {
        String template = texts.getString(key, english.getString(key, key));
        return String.format(template, args);
    }

    private static YamlConfiguration load(JavaPlugin plugin, String language) {
        String name = "messages_" + language + ".yml";
        try (InputStream in = plugin.getResource(name)) {
            if (in == null) {
                throw new IllegalStateException(name + " missing from plugin jar");
            }
            return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + name, e);
        }
    }
}
