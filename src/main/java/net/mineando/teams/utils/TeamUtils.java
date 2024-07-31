package net.mineando.teams.utils;

import net.mineando.teams.Teams;
import net.mineando.teams.database.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class TeamUtils {
    private final Teams plugin;

    public TeamUtils(Teams plugin) {
        this.plugin = plugin;
    }

    public List<Player> getOnlineTeamMembers(Team team) {
        return team.getMembers().stream()
                .map(member -> Bukkit.getPlayer(member.getPlayerUUID()))
                .filter(player -> player != null && player.isOnline())
                .collect(Collectors.toList());
    }

    public List<OfflinePlayer> getOfflineTeamMembers(Team team) {
        return team.getMembers().stream()
                .map(member -> Bukkit.getOfflinePlayer(member.getPlayerUUID()))
                .filter(player -> !player.isOnline())
                .collect(Collectors.toList());
    }

    public boolean isTeamFull(Team team) {
        int maxTeamSize = plugin.getConfigManager().getMaxTeamSize();
        return team.getMembers().size() >= maxTeamSize;
    }

    public Optional<Player> getOnlineTeamLeader(Team team) {
        return Optional.ofNullable(Bukkit.getPlayer(team.getLeader()));
    }

    public boolean arePlayersInSameTeam(Player player1, Player player2) {
        Optional<Team> team1 = plugin.getTeamManager().getPlayerTeam(player1.getUniqueId());
        Optional<Team> team2 = plugin.getTeamManager().getPlayerTeam(player2.getUniqueId());
        return team1.isPresent() && team2.isPresent() && team1.get().getId() == team2.get().getId();
    }

    public String formatTeamName(Team team) {
        return plugin.getLangManager().getPlain("team.name_format", "name", team.getName());
    }

    public String formatPlayerName(UUID playerUUID) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        return offlinePlayer.getName() != null ? offlinePlayer.getName() : playerUUID.toString();
    }

    public boolean canInvitePlayer(Player inviter, Player invited) {
        Optional<Team> inviterTeam = plugin.getTeamManager().getPlayerTeam(inviter.getUniqueId());

        if (!inviterTeam.isPresent()) {
            inviter.sendMessage(plugin.getLangManager().getChatMessage("error.not_in_team"));
            return false;
        }

        if (!inviterTeam.get().getLeader().equals(inviter.getUniqueId())) {
            inviter.sendMessage(plugin.getLangManager().getChatMessage("error.not_team_leader"));
            return false;
        }

        if (isTeamFull(inviterTeam.get())) {
            inviter.sendMessage(plugin.getLangManager().getChatMessage("error.team_full"));
            return false;
        }

        if (plugin.getTeamManager().getPlayerTeam(invited.getUniqueId()).isPresent()) {
            inviter.sendMessage(plugin.getLangManager().getChatMessage("error.player_already_in_team", "player", invited.getName()));
            return false;
        }

        return true;
    }
}
