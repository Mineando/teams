package net.mineando.teams.database.models;

import java.util.UUID;

public class TeamMember {
    private int id;
    private int teamId;
    private UUID playerUUID;

    public TeamMember(int id, int teamId, UUID playerUUID) {
        this.id = id;
        this.teamId = teamId;
        this.playerUUID = playerUUID;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public UUID getPlayerUUID() { return playerUUID; }
    public void setPlayerUUID(UUID playerUUID) { this.playerUUID = playerUUID; }
}
