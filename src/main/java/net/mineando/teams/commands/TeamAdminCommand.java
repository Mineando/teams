package net.mineando.teams.commands;

import net.mineando.teams.Teams;
import net.mineando.teams.database.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class TeamAdminCommand implements CommandExecutor {
    private final Teams plugin;

    public TeamAdminCommand(Teams plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sendHelpMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLangManager().getChatMessage("admin.error.missing_player"));
                    return true;
                }
                handleInfoCommand(sender, args[1]);
                break;
            case "disband":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLangManager().getChatMessage("admin.error.missing_team"));
                    return true;
                }
                handleDisbandCommand(sender, args[1]);
                break;
            case "rename":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getLangManager().getChatMessage("admin.error.missing_arguments"));
                    return true;
                }
                handleRenameCommand(sender, args[1], args[2]);
                break;
            case "transfer":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getLangManager().getChatMessage("admin.error.missing_arguments"));
                    return true;
                }
                handleTransferCommand(sender, args[1], args[2]);
                break;
            default:
                sendHelpMessage(sender);
                break;
        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(plugin.getLangManager().getChatMessage("admin.help.header"));
        sender.sendMessage(plugin.getLangManager().getChatMessage("admin.help.info"));
        sender.sendMessage(plugin.getLangManager().getChatMessage("admin.help.disband"));
        sender.sendMessage(plugin.getLangManager().getChatMessage("admin.help.rename"));
        sender.sendMessage(plugin.getLangManager().getChatMessage("admin.help.transfer"));
    }

    private void handleInfoCommand(CommandSender sender, String playerName) {
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

    private void handleDisbandCommand(CommandSender sender, String teamName) {
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

    private void handleRenameCommand(CommandSender sender, String teamName, String newName) {
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

    private void handleTransferCommand(CommandSender sender, String teamName, String newLeaderName) {
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
