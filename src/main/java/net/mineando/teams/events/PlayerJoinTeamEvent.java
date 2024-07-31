package net.mineando.teams.events;

import net.mineando.teams.database.models.Team;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PlayerJoinTeamEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Team team;
    private final UUID playerUUID;

    public PlayerJoinTeamEvent(Team team, UUID playerUUID) {
        this.team = team;
        this.playerUUID = playerUUID;
    }

    public Team getTeam() {
        return team;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
