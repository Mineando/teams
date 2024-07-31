package net.mineando.teams.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mineando.teams.Teams;
import org.bukkit.configuration.file.FileConfiguration;

public class LangManager {
    private final Teams plugin;
    private final MiniMessage miniMessage;
    private final String prefix;

    public LangManager(Teams plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.prefix = plugin.getConfig().getString("messages.prefix", "");
    }

    public Component get(String key) {
        return get(key, new Object[0]);
    }

    public Component get(String key, Object... args) {
        String message = getMessageString(key, args);
        return miniMessage.deserialize(message);
    }

    public Component getChatMessage(String key) {
        return getChatMessage(key, new Object[0]);
    }

    public Component getChatMessage(String key, Object... args) {
        String message = prefix + getMessageString(key, args);
        return miniMessage.deserialize(message);
    }

    public String getPlain(String key) {
        return getPlain(key, new Object[0]);
    }

    public String getPlain(String key, Object... args) {
        return getMessageString(key, args);
    }

    private String getMessageString(String key, Object... args) {
        FileConfiguration langConfig = plugin.getConfigManager().getLangConfig();
        String message = langConfig.getString(key);

        if (message == null) {
            return "Missing language key: " + key;
        }

        message = replaceColorPlaceholders(message);

        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 < args.length) {
                message = message.replace("{" + args[i] + "}", String.valueOf(args[i + 1]));
            }
        }

        return message;
    }

    private String replaceColorPlaceholders(String message) {
        FileConfiguration config = plugin.getConfig();
        message = message.replace("{primary}", config.getString("messages.colors.primary", "<#55FFFF>"));
        message = message.replace("{secondary}", config.getString("messages.colors.secondary", "<#AAFFFF>"));
        message = message.replace("{error}", config.getString("messages.colors.error", "<#FF5555>"));
        return message;
    }
}