package de.navok.hermeschat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class HermesChatPlugin extends JavaPlugin {

    private HermesClient client;
    private File storeFile;
    private YamlConfiguration store;
    private Messages messages;
    private final Map<String, Integer> suffixes = new HashMap<>();
    private final Map<String, Long> lastAskNanos = new HashMap<>();
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String language = getConfig().getString("language", "en").trim().toLowerCase(Locale.ROOT);
        if (!Messages.supports(language)) {
            getLogger().warning("Unknown language '" + language + "', using 'en'.");
            language = "en";
        }
        messages = new Messages(this, language);

        String baseUrl = getConfig().getString("base-url", "").trim();
        String apiKey = getConfig().getString("api-key", "").trim();
        String model = getConfig().getString("model", "hermes-agent").trim();
        int timeoutSeconds = getConfig().getInt("timeout-seconds", 300);

        if (baseUrl.isEmpty() || apiKey.isEmpty()) {
            getLogger().severe(messages.get("config-missing"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            client = new HermesClient(baseUrl, apiKey, model, timeoutSeconds, getLogger(), messages);
        } catch (IllegalArgumentException e) {
            getLogger().severe(messages.get("config-invalid-url", baseUrl, e.getMessage()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        storeFile = new File(getDataFolder(), "data.yml");
        store = YamlConfiguration.loadConfiguration(storeFile);
        ConfigurationSection stored = store.getConfigurationSection("suffixes");
        if (stored != null) {
            for (String key : stored.getKeys(false)) {
                suffixes.put(key, stored.getInt(key));
            }
        }

        PluginCommand command = getCommand("hermes");
        if (command == null) {
            getLogger().severe(messages.get("command-missing"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(new HermesCommand(this));
        getLogger().info(messages.get("enabled", baseUrl));
    }

    public String sessionId(String owner) {
        String base = getConfig().getString("session-prefix", "mc-") + owner;
        Integer suffix = suffixes.get(owner);
        return suffix == null ? base : base + "-" + suffix;
    }

    public void resetSession(String owner) {
        int next = suffixes.getOrDefault(owner, 0) + 1;
        suffixes.put(owner, next);
        store.set("suffixes." + owner, next);
        try {
            store.save(storeFile);
        } catch (IOException e) {
            getLogger().warning(messages.get("store-save-failed", e.getMessage()));
        }
    }

    /** Seconds the owner still has to wait, 0 if free. Main thread only. */
    public long cooldownLeft(String owner) {
        Long last = lastAskNanos.get(owner);
        if (last == null) {
            return 0;
        }
        long cooldownNanos = getConfig().getLong("cooldown-seconds", 10) * 1_000_000_000L;
        long leftNanos = cooldownNanos - (System.nanoTime() - last);
        return leftNanos <= 0 ? 0 : (leftNanos + 999_999_999L) / 1_000_000_000L;
    }

    public void markAsked(String owner) {
        lastAskNanos.put(owner, System.nanoTime());
    }

    public boolean isBusy(String owner) {
        return inFlight.contains(owner);
    }

    public boolean acquire(String owner) {
        return inFlight.add(owner);
    }

    public void release(String owner) {
        inFlight.remove(owner);
    }

    public HermesClient client() {
        return client;
    }

    public Messages messages() {
        return messages;
    }
}
