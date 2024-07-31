package net.mineando.teams.listeners;

import net.mineando.teams.Teams;
import net.mineando.teams.events.PlayerJoinTeamEvent;
import net.mineando.teams.events.PlayerLeaveTeamEvent;
import net.mineando.teams.events.TeamCreateEvent;
import net.mineando.teams.events.TeamDisbandEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TeamListener implements Listener {
    private final Teams plugin;

    public TeamListener(Teams plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTeamCreate(TeamCreateEvent event) {
        // Handle team creation event
    }

    @EventHandler
    public void onTeamDisband(TeamDisbandEvent event) {
        // Handle team disband event
    }

    @EventHandler
    public void onPlayerJoinTeam(PlayerJoinTeamEvent event) {
        // Handle player join team event
    }

    @EventHandler
    public void onPlayerLeaveTeam(PlayerLeaveTeamEvent event) {
        // Handle player leave team event
    }
}
