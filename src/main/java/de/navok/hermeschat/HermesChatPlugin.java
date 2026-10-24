package de.navok.hermeschat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class HermesChatPlugin extends JavaPlugin {

    private HermesClient client;
    private File storeFile;
    private YamlConfiguration store;
    private final Map<String, String> suffixes = new HashMap<>();
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String baseUrl = getConfig().getString("base-url", "").trim();
        String apiKey = getConfig().getString("api-key", "").trim();
        String model = getConfig().getString("model", "hermes-agent").trim();
        int timeoutSeconds = getConfig().getInt("timeout-seconds", 300);

        if (baseUrl.isEmpty() || apiKey.isEmpty()) {
            getLogger().severe("base-url oder api-key fehlt in plugins/HermesChat/config.yml – Plugin wird deaktiviert.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            client = new HermesClient(baseUrl, apiKey, model, timeoutSeconds);
        } catch (IllegalArgumentException e) {
            getLogger().severe("Ungültige base-url '" + baseUrl + "': " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        storeFile = new File(getDataFolder(), "data.yml");
        store = YamlConfiguration.loadConfiguration(storeFile);
        ConfigurationSection stored = store.getConfigurationSection("suffixes");
        if (stored != null) {
            for (String key : stored.getKeys(false)) {
                suffixes.put(key, stored.getString(key));
            }
        }

        PluginCommand command = getCommand("hermes");
        if (command == null) {
            getLogger().severe("Command 'hermes' fehlt in plugin.yml – Plugin wird deaktiviert.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(new HermesCommand(this));
        getLogger().info("HermesChat aktiv – Gateway: " + baseUrl);
    }

    public String sessionId(String owner) {
        String base = getConfig().getString("session-prefix", "mc-") + owner;
        String suffix = suffixes.get(owner);
        return suffix == null ? base : base + "-" + suffix;
    }

    public void resetSession(String owner) {
        suffixes.put(owner, Integer.toString(ThreadLocalRandom.current().nextInt(0x10000, 0x100000)));
        store.set("suffixes." + owner, suffixes.get(owner));
        try {
            store.save(storeFile);
        } catch (IOException e) {
            getLogger().warning("data.yml konnte nicht gespeichert werden: " + e.getMessage());
        }
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
}
