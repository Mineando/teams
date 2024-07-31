package net.mineando.teams;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mineando.teams.database.DatabaseManager;
import net.mineando.teams.database.models.Team;
import net.mineando.teams.database.models.TeamMember;
import net.mineando.teams.events.PlayerJoinTeamEvent;
import net.mineando.teams.events.PlayerLeaveTeamEvent;
import net.mineando.teams.events.TeamCreateEvent;
import net.mineando.teams.events.TeamDisbandEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeamManager {
    private final Teams plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, Team> playerTeamCache;
    private final Map<Integer, Team> teamCache;
    private final Map<UUID, Map<UUID, Long>> pendingInvitations;

    public TeamManager(Teams plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.playerTeamCache = new ConcurrentHashMap<>();
        this.teamCache = new ConcurrentHashMap<>();
        loadTeamsFromDatabase();
        this.pendingInvitations = new ConcurrentHashMap<>();
    }

    private void loadTeamsFromDatabase() {
        playerTeamCache.clear(); // Limpiamos el caché actual
        teamCache.clear(); // Limpiamos el caché actual

        List<Team> teams = databaseManager.getAllTeams();
        for (Team team : teams) {
            teamCache.put(team.getId(), team);
            playerTeamCache.put(team.getLeader(), team);
            for (TeamMember member : team.getMembers()) {
                playerTeamCache.put(member.getPlayerUUID(), team);
            }
        }
    }

    public Optional<Team> getPlayerTeam(UUID playerUUID) {
        return Optional.ofNullable(playerTeamCache.get(playerUUID));
    }

    public Optional<Team> getTeam(int teamId) {
        return Optional.ofNullable(teamCache.get(teamId));
    }

    public Optional<Team> createTeam(String teamName, UUID leaderUUID) {
        if (playerTeamCache.containsKey(leaderUUID)) {
            return Optional.empty();
        }

        Optional<Team> newTeam = databaseManager.createTeam(teamName, leaderUUID);
        if (newTeam.isPresent()) {
            Team team = newTeam.get();
            teamCache.put(team.getId(), team);
            playerTeamCache.put(leaderUUID, team);
            // Fire team creation event
            plugin.getServer().getPluginManager().callEvent(new TeamCreateEvent(team));
        }
        return newTeam;
    }

    public boolean disbandTeam(UUID leaderUUID) {
        Optional<Team> teamOpt = getPlayerTeam(leaderUUID);
        if (teamOpt.isPresent() && teamOpt.get().getLeader().equals(leaderUUID)) {
            Team team = teamOpt.get();
            if (databaseManager.disbandTeam(team.getId())) {
                teamCache.remove(team.getId());
                for (TeamMember member : team.getMembers()) {
                    playerTeamCache.remove(member.getPlayerUUID());
                }
                playerTeamCache.remove(leaderUUID); // También eliminamos el líder del caché
                plugin.getServer().getPluginManager().callEvent(new TeamDisbandEvent(team));
                return true;
            }
        }
        return false;
    }

    public boolean renameTeam(UUID leaderUUID, String newName) {
        Optional<Team> teamOpt = getPlayerTeam(leaderUUID);
        if (teamOpt.isPresent() && teamOpt.get().getLeader().equals(leaderUUID)) {
            Team team = teamOpt.get();
            if (databaseManager.renameTeam(team.getId(), newName)) {
                team.setName(newName);
                return true;
            }
        }
        return false;
    }

    public boolean invitePlayer(UUID inviterUUID, UUID invitedUUID) {
        Optional<Team> teamOpt = getPlayerTeam(inviterUUID);
        if (teamOpt.isPresent() && !playerTeamCache.containsKey(invitedUUID)) {
            Team team = teamOpt.get();
            long expirationTime = System.currentTimeMillis() + (plugin.getConfigManager().getInviteExpirationTime() * 1000);
            pendingInvitations.computeIfAbsent(invitedUUID, k -> new ConcurrentHashMap<>()).put(inviterUUID, expirationTime);

            // Enviar mensaje interactivo
            Player invitedPlayer = Bukkit.getPlayer(invitedUUID);
            Player inviter = Bukkit.getPlayer(inviterUUID);
            if (invitedPlayer != null && inviter != null) {
                String inviteMessage = plugin.getLangManager().getPlain("team.invite_received_interactive",
                        "inviter", inviter.getName(),
                        "team", team.getName());
                invitedPlayer.sendMessage(MiniMessage.miniMessage().deserialize(inviteMessage));
            }

            return true;
        }
        return false;
    }

    public boolean acceptInvitation(UUID invitedUUID, UUID inviterUUID) {
        Map<UUID, Long> invitations = pendingInvitations.get(invitedUUID);
        if (invitations != null && invitations.containsKey(inviterUUID)) {
            long expirationTime = invitations.get(inviterUUID);
            if (System.currentTimeMillis() <= expirationTime) {
                Optional<Team> teamOpt = getPlayerTeam(inviterUUID);
                if (teamOpt.isPresent()) {
                    Team team = teamOpt.get();
                    if (databaseManager.addMemberToTeam(team.getId(), invitedUUID)) {
                        team.addMember(new TeamMember(-1, team.getId(), invitedUUID));
                        playerTeamCache.put(invitedUUID, team);
                        invitations.remove(inviterUUID);
                        plugin.getServer().getPluginManager().callEvent(new PlayerJoinTeamEvent(team, invitedUUID));
                        return true;
                    }
                }
            }
            invitations.remove(inviterUUID);
        }
        return false;
    }

    public boolean rejectInvitation(UUID invitedUUID, UUID inviterUUID) {
        Map<UUID, Long> invitations = pendingInvitations.get(invitedUUID);
        if (invitations != null) {
            return invitations.remove(inviterUUID) != null;
        }
        return false;
    }

    public boolean kickPlayer(UUID leaderUUID, UUID kickedUUID) {
        Optional<Team> teamOpt = getPlayerTeam(leaderUUID);
        if (teamOpt.isPresent() && teamOpt.get().getLeader().equals(leaderUUID)) {
            Team team = teamOpt.get();
            if (databaseManager.removeMemberFromTeam(team.getId(), kickedUUID)) {
                team.removeMember(kickedUUID);
                playerTeamCache.remove(kickedUUID);
                // Fire player leave team event
                plugin.getServer().getPluginManager().callEvent(new PlayerLeaveTeamEvent(team, kickedUUID));
                return true;
            }
        }
        return false;
    }

    public boolean leaveTeam(UUID playerUUID) {
        Optional<Team> teamOpt = getPlayerTeam(playerUUID);
        if (teamOpt.isPresent()) {
            Team team = teamOpt.get();
            if (!team.getLeader().equals(playerUUID)) {
                if (databaseManager.removeMemberFromTeam(team.getId(), playerUUID)) {
                    team.removeMember(playerUUID);
                    playerTeamCache.remove(playerUUID);
                    plugin.getServer().getPluginManager().callEvent(new PlayerLeaveTeamEvent(team, playerUUID));
                    return true;
                }
            } else {
                // Si el líder quiere dejar el equipo, disolvemos el equipo.
                return disbandTeam(playerUUID);
            }
        }
        return false;
    }

    public boolean transferTeamLeadership(UUID currentLeaderUUID, UUID newLeaderUUID) {
        Optional<Team> teamOpt = getPlayerTeam(currentLeaderUUID);
        if (teamOpt.isPresent() && teamOpt.get().getLeader().equals(currentLeaderUUID)) {
            Team team = teamOpt.get();
            if (team.getMembers().stream().anyMatch(member -> member.getPlayerUUID().equals(newLeaderUUID))) {
                if (databaseManager.transferTeamLeadership(team.getId(), newLeaderUUID)) {
                    team.setLeader(newLeaderUUID);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean cancelInvitation(UUID inviterUUID, UUID invitedUUID) {
        Map<UUID, Long> invitations = pendingInvitations.get(invitedUUID);
        if (invitations != null) {
            return invitations.remove(inviterUUID) != null;
        }
        return false;
    }

    public List<Team> getAllTeams() {
        return new ArrayList<>(teamCache.values());
    }
}
