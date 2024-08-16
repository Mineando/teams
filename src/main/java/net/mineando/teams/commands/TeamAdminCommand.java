package net.mineando.teams.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.mineando.teams.Teams;
import net.mineando.teams.database.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

@CommandAlias("teamadmin")
@CommandPermission("teams.admin")
public class TeamAdminCommand extends BaseCommand {
    private final Teams plugin;

    public TeamAdminCommand(Teams plugin) {
        this.plugin = plugin;
    }

    @Subcommand("info")
    @CommandCompletion("@players")
    public void onInfo(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(plugin.getLangManager().getChatMessage("admin.error.player_not_found", "player", playerName));
            return;
        }

        Optional<Team> teamOpt = plugin.getTeamManager().getPlayerTeam(target.getUniqueId());
        if (!teamOpt.isPresent()) {
            sender.sendMessage(plugin.getLangManager().getChatMessage("admin.info.no_team", "player", playerName));
            return;
        }

        Team team = teamOpt.get();
        sender.sendMessage(plugin.getLangManager().getChatMessage("admin.info.header", "player", playerName));
        sender.sendMessage(plugin.getLangManager().getChatMessage("admin.info.team_name", "name", team.getName()));
        sender.sendMessage(plugin.getLangManager().getChatMessage("admin.info.team_leader", "leader", plugin.getTeamUtils().formatPlayerName(team.getLeader())));
        sender.sendMessage(plugin.getLangManager().getChatMessage("admin.info.team_members", "count", team.getMembers().size()));
    }

    @Subcommand("disband")
    @CommandCompletion("@teams")
    public void onDisband(CommandSender sender, String teamName) {
        Optional<Team> teamOpt = plugin.getTeamManager().getAllTeams().stream()
                .filter(team -> team.getName().equalsIgnoreCase(teamName))
                .findFirst();

        if (!teamOpt.isPresent()) {
            sender.sendMessage(plugin.getLangManager().getChatMessage("admin.error.team_not_found", "team", teamName));
            return;
        }

        Team team = teamOpt.get();
        if (plugin.getTeamManager().disbandTeam(team.getLeader())) {
            sender.sendMessage(plugin.getLangManager().getChatMessage("admin.disband.success", "team", teamName));
        } else {
            sender.sendMessage(plugin.getLangManager().getChatMessage("admin.error.disband_failed", "team", teamName));
        }
    }

    @Subcommand("rename")
    @CommandCompletion("@teams")
    public void onRename(CommandSender sender, String teamName, String newName) {
        Optional<Team> teamOpt = plugin.getTeamManager().getAllTeams().stream()
                .filter(team -> team.getName().equalsIgnoreCase(teamName))
                .findFirst();

        if (!teamOpt.isPresent()) {
            sender.sendMessage(plugin.getLangManager().getChatMessage("admin.error.team_not_found", "team", teamName));
            return;
        }

        Team team = teamOpt.get();
        if (plugin.getTeamManager().renameTeam(team.getLeader(), newName)) {
            sender.sendMessage(plugin.getLangManager().getChatMessage("admin.rename.success", "old", teamName, "new", newName));
        } else {
            sender.sendMessage(plugin.getLangManager().getChatMessage("admin.error.rename_failed", "team", teamName));
        }
    }

    @Subcommand("transfer")
    @CommandCompletion("@teams @players")
    public void onTransfer(CommandSender sender, String teamName, String newLeaderName) {
        Optional<Team> teamOpt = plugin.getTeamManager().getAllTeams().stream()
                .filter(team -> team.getName().equalsIgnoreCase(teamName))
                .findFirst();

        if (!teamOpt.isPresent()) {
            sender.sendMessage(plugin.getLangManager().getChatMessage("admin.error.team_not_found", "team", teamName));
            return;
        }

        Team team = teamOpt.get();
        Player newLeader = Bukkit.getPlayer(newLeaderName);

        if (newLeader == null) {
            sender.sendMessage(plugin.getLangManager().getChatMessage("admin.error.player_not_found", "player", newLeaderName));
            return;
        }

        UUID newLeaderUUID = newLeader.getUniqueId();
        if (!team.getMembers().stream().anyMatch(member -> member.getPlayerUUID().equals(newLeaderUUID))) {
            sender.sendMessage(plugin.getLangManager().getChatMessage("admin.error.not_team_member", "player", newLeaderName, "team", teamName));
            return;
        }

        if (plugin.getTeamManager().transferTeamLeadership(team.getLeader(), newLeaderUUID)) {
            sender.sendMessage(plugin.getLangManager().getChatMessage("admin.transfer.success", "team", teamName, "leader", newLeaderName));
        } else {
            sender.sendMessage(plugin.getLangManager().getChatMessage("admin.error.transfer_failed", "team", teamName));
        }
    }
}
