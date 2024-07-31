package net.mineando.teams.config;

import net.mineando.teams.Teams;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    private final Teams plugin;
    private FileConfiguration config;
    private File configFile;
    private FileConfiguration langConfig;
    private File langFile;

    public ConfigManager(Teams plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        reloadConfig();
        loadLangConfig();
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultStream = plugin.getResource("config.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save config to " + configFile);
        }
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
    }

    private void loadLangConfig() {
        String langFileName = getConfig().getString("settings.default-language", "en_US") + ".yml";
        langFile = new File(plugin.getDataFolder() + File.separator + "lang", langFileName);

        if (!langFile.exists()) {
            plugin.saveResource("lang/" + langFileName, false);
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public FileConfiguration getLangConfig() {
        if (langConfig == null) {
            loadLangConfig();
        }
        return langConfig;
    }

    public String getLangString(String path) {
        return getLangConfig().getString(path, "Missing language string: " + path);
    }

    // Métodos de utilidad para obtener configuraciones específicas
    public int getMaxTeamSize() {
        return getConfig().getInt("settings.max-team-size", 4);
    }

    public int getInviteExpirationTime() {
        return getConfig().getInt("settings.invite-expiration-time", 60);
    }

    public String getDatabaseHost() {
        return getConfig().getString("database.host", "localhost");
    }

    public int getDatabasePort() {
        return getConfig().getInt("database.port", 3306);
    }

    public String getDatabaseName() {
        return getConfig().getString("database.database", "teams_db");
    }

    public String getDatabaseUsername() {
        return getConfig().getString("database.username", "user");
    }

    public String getDatabasePassword() {
        return getConfig().getString("database.password", "password");
    }
}
