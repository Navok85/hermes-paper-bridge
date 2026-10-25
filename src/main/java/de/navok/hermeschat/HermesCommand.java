package de.navok.hermeschat;

import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class HermesCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = "[Hermes] ";

    private final HermesChatPlugin plugin;

    public HermesCommand(HermesChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
            reply(sender, "Nur Spieler und die Server-Konsole dürfen /" + label + " nutzen.", NamedTextColor.RED);
            return true;
        }
        if (!sender.hasPermission("hermes.chat")) {
            reply(sender, "Keine Berechtigung (hermes.chat).", NamedTextColor.RED);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            if (!sender.hasPermission("hermes.reset")) {
                reply(sender, "Keine Berechtigung (hermes.reset).", NamedTextColor.RED);
                return true;
            }
            if (plugin.isBusy(owner(sender))) {
                reply(sender, "Ich warte noch auf die vorherige Antwort.", NamedTextColor.YELLOW);
                return true;
            }
            plugin.resetSession(owner(sender));
            reply(sender, "Neue Konversation gestartet.", NamedTextColor.GRAY);
            return true;
        }
        if (args.length == 0) {
            reply(sender, "Nutzung: /" + label + " <frage>  |  /" + label + " reset", NamedTextColor.GRAY);
            return true;
        }

        String owner = owner(sender);
        if (!plugin.acquire(owner)) {
            reply(sender, "Ich warte noch auf die vorherige Antwort.", NamedTextColor.YELLOW);
            return true;
        }
        long wait = plugin.cooldownLeft(owner);
        if (wait > 0) {
            plugin.release(owner);
            reply(sender, "Bitte noch " + wait + " s warten.", NamedTextColor.YELLOW);
            return true;
        }
        plugin.markAsked(owner);

        String prompt = String.join(" ", args);
        reply(sender, "denkt nach …", NamedTextColor.GRAY);
        try {
            plugin.client().ask(plugin.sessionId(owner), prompt,
                    answer -> {
                        plugin.release(owner);
                        deliver(sender, answer);
                    },
                    error -> {
                        plugin.release(owner);
                        plugin.getServer().getScheduler().runTask(plugin,
                                () -> reply(sender, error, NamedTextColor.RED));
                    });
        } catch (RuntimeException e) {
            plugin.release(owner);
            reply(sender, "Anfrage fehlgeschlagen: " + e.getClass().getSimpleName(), NamedTextColor.RED);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, String @NotNull [] args) {
        if (args.length == 1 && sender.hasPermission("hermes.reset")
                && "reset".startsWith(args[0].toLowerCase(Locale.ROOT))) {
            return List.of("reset");
        }
        return List.of();
    }

    private void deliver(CommandSender sender, String answer) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            List<String> lines = ChatText.split(answer, plugin.getConfig().getInt("max-chars", 1500));
            if (lines.isEmpty()) {
                reply(sender, "(leere Antwort)", NamedTextColor.GRAY);
                return;
            }
            if (plugin.getConfig().getBoolean("broadcast", false) && sender instanceof Player) {
                for (int i = 0; i < lines.size(); i++) {
                    Component line = i == 0
                            ? Component.text(PREFIX + lines.get(i))
                            : Component.text(lines.get(i));
                    Bukkit.broadcast(line);
                }
                return;
            }
            for (int i = 0; i < lines.size(); i++) {
                reply(sender, i == 0 ? PREFIX + lines.get(i) : lines.get(i), NamedTextColor.WHITE);
            }
        });
    }

    /** Players get chat messages; console/RCON output is logged (late RCON messages would be lost). */
    private void reply(CommandSender sender, String text, NamedTextColor color) {
        if (sender instanceof Player player) {
            player.sendMessage(Component.text(text, color));
        } else {
            plugin.getLogger().info(text);
        }
    }

    private static String owner(CommandSender sender) {
        return sender instanceof Player player ? player.getUniqueId().toString() : "console";
    }
}
