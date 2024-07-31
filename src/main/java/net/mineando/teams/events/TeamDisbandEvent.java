package net.mineando.teams.events;

import net.mineando.teams.database.models.Team;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeamDisbandEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Team team;

    public TeamDisbandEvent(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
