package net.mineando.teams;

import net.mineando.teams.database.models.Team;
import net.mineando.teams.database.models.TeamMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TeamsAPI {
    private final Teams plugin;

    public TeamsAPI(Teams plugin) {
        this.plugin = plugin;
    }

    public boolean isPlayerInTeam(UUID playerUUID) {
        return plugin.getTeamManager().getPlayerTeam(playerUUID).isPresent();
    }

    public Optional<Team> getPlayerTeam(UUID playerUUID) {
        return plugin.getTeamManager().getPlayerTeam(playerUUID);
    }

    public boolean arePlayersInSameTeam(UUID player1UUID, UUID player2UUID) {
        Optional<Team> team1 = getPlayerTeam(player1UUID);
        Optional<Team> team2 = getPlayerTeam(player2UUID);
        return team1.isPresent() && team2.isPresent() && team1.get().getId() == team2.get().getId();
    }

    public Optional<Team> createTeam(String teamName, UUID leaderUUID) {
        return plugin.getTeamManager().createTeam(teamName, leaderUUID);
    }

    public boolean disbandTeam(UUID leaderUUID) {
        return plugin.getTeamManager().disbandTeam(leaderUUID);
    }

    public boolean renameTeam(UUID leaderUUID, String newName) {
        return plugin.getTeamManager().renameTeam(leaderUUID, newName);
    }

    public boolean invitePlayerToTeam(UUID inviterUUID, UUID invitedUUID) {
        return plugin.getTeamManager().invitePlayer(inviterUUID, invitedUUID);
    }

    public boolean kickPlayerFromTeam(UUID leaderUUID, UUID kickedUUID) {
        return plugin.getTeamManager().kickPlayer(leaderUUID, kickedUUID);
    }

    public boolean leaveTeam(UUID playerUUID) {
        return plugin.getTeamManager().leaveTeam(playerUUID);
    }

    public List<Team> getAllTeams() {
        return plugin.getTeamManager().getAllTeams();
    }

    public Optional<Team> getTeamById(int teamId) {
        return plugin.getTeamManager().getTeam(teamId);
    }

    public boolean isTeamLeader(UUID playerUUID) {
        Optional<Team> team = getPlayerTeam(playerUUID);
        return team.isPresent() && team.get().getLeader().equals(playerUUID);
    }

    public int getTeamSize(UUID playerUUID) {
        Optional<Team> team = getPlayerTeam(playerUUID);
        return team.map(value -> value.getMembers().size()).orElse(0);
    }

    public List<UUID> getTeamMembers(UUID playerUUID) {
        Optional<Team> team = getPlayerTeam(playerUUID);
        return team.map(value -> value.getMembers().stream()
                .map(TeamMember::getPlayerUUID)
                .toList()).orElse(List.of());
    }
}
