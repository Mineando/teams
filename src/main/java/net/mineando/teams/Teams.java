package net.mineando.teams;

import net.kyori.adventure.title.Title;
import net.mineando.teams.commands.TeamAdminCommand;
import net.mineando.teams.commands.TeamCommand;
import net.mineando.teams.config.ConfigManager;
import net.mineando.teams.database.DatabaseManager;
import net.mineando.teams.gui.TeamGUI;
import net.mineando.teams.listeners.TeamListener;
import net.mineando.teams.utils.LangManager;
import net.mineando.teams.utils.TeamUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import co.aikar.commands.PaperCommandManager;
import java.time.Duration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Teams extends JavaPlugin implements Listener {
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private TeamManager teamManager;
    private LangManager langManager;
    private TeamUtils teamUtils;
    private TeamsAPI api;
    private PaperCommandManager commandManager;

    private Map<UUID, ChatAction> pendingChatActions;

    @Override
    public void onEnable() {
        // Initialize managers
        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this);
        teamManager = new TeamManager(this, databaseManager);
        langManager = new LangManager(this);
        teamUtils = new TeamUtils(this);
        api = new TeamsAPI(this);

        this.commandManager = new PaperCommandManager(this);
        registerCommands();

        // Register listeners
        getServer().getPluginManager().registerEvents(new TeamListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);

        getServer().getServicesManager().register(TeamsAPI.class, api, this, ServicePriority.Normal);

        pendingChatActions = new HashMap<>();

        getLogger().info("Teams plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Perform cleanup if necessary
        getLogger().info("Teams plugin has been disabled!");
    }
    private void registerCommands() {
        commandManager.registerCommand(new TeamCommand(this));
        commandManager.registerCommand(new TeamAdminCommand(this));
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public TeamUtils getTeamUtils() {
        return teamUtils;
    }

    public TeamsAPI getAPI() {
        return api;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (pendingChatActions.containsKey(playerUUID)) {
            event.setCancelled(true);
            ChatAction action = pendingChatActions.remove(playerUUID);
            String message = event.getMessage();

            if (message.equalsIgnoreCase("cancelar")) {
                player.sendMessage(langManager.getChatMessage("action.cancelled"));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
                player.clearTitle();
                return;
            }

            Bukkit.getScheduler().runTask(this, () -> {
                switch (action.getType()) {
                    case CREATE_TEAM:
                        if (teamManager.createTeam(message, playerUUID).isPresent()) {
                            player.sendMessage(langManager.getChatMessage("team.created", "name", message));
                            player.playSound(player.getLocation(), Sound.valueOf(getConfig().getString("sounds.team_create")), 1.0f, 1.0f);
                        }
                        break;
                    case INVITE_PLAYER:
                        Player invitedPlayer = Bukkit.getPlayer(message);
                        if (invitedPlayer != null && invitedPlayer.isOnline()) {
                            if (teamUtils.canInvitePlayer(player, invitedPlayer)) {
                                teamManager.invitePlayer(playerUUID, invitedPlayer.getUniqueId());
                                player.sendMessage(langManager.getChatMessage("team.player_invited", "player", invitedPlayer.getName()));
                                player.playSound(player.getLocation(), Sound.valueOf(getConfig().getString("sounds.invite_send")), 1.0f, 1.0f);
                                invitedPlayer.playSound(invitedPlayer.getLocation(), Sound.valueOf(getConfig().getString("sounds.invite_receive")), 1.0f, 1.0f);
                            }
                        } else {
                            player.sendMessage(langManager.getChatMessage("error.player_not_found", "player", message));
                        }
                        break;
                }
                player.clearTitle();
                new TeamGUI(this, player).open();
            });
        }
    }

    public void addPendingChatAction(Player player, ChatAction action) {
        pendingChatActions.put(player.getUniqueId(), action);
        player.showTitle(Title.title(
                Component.empty(),
                langManager.get("action.input_required"),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(10), Duration.ofSeconds(1))
        ));
        player.sendMessage(langManager.getChatMessage("action.cancel_instruction"));
    }

    public enum ChatActionType {
        CREATE_TEAM,
        INVITE_PLAYER
    }

    public static class ChatAction {
        private final ChatActionType type;

        public ChatAction(ChatActionType type) {
            this.type = type;
        }

        public ChatActionType getType() {
            return type;
        }
    }
}
