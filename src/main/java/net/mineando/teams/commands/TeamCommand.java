package net.mineando.teams.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.mineando.teams.Teams;
import net.mineando.teams.gui.TeamGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("team")
public class TeamCommand extends BaseCommand {
    private final Teams plugin;

    public TeamCommand(Teams plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onTeam(Player player) {
        new TeamGUI(plugin, player).open();
    }

    @Subcommand("accept")
    @CommandCompletion("@players")
    public void onAccept(Player player, String inviterName) {
        Player inviter = Bukkit.getPlayer(inviterName);
        if (inviter == null) {
            player.sendMessage(plugin.getLangManager().getChatMessage("error.player_not_found", "player", inviterName));
            return;
        }
        plugin.getTeamManager().acceptInvitation(player.getUniqueId(), inviter.getUniqueId());
    }

    @Subcommand("reject")
    @CommandCompletion("@players")
    public void onReject(Player player, String inviterName) {
        Player inviter = Bukkit.getPlayer(inviterName);
        if (inviter == null) {
            player.sendMessage(plugin.getLangManager().getChatMessage("error.player_not_found", "player", inviterName));
            return;
        }
        plugin.getTeamManager().rejectInvitation(player.getUniqueId(), inviter.getUniqueId());
    }

    @Subcommand("cancel")
    @CommandCompletion("@players")
    public void onCancel(Player player, String invitedPlayerName) {
        Player invitedPlayer = Bukkit.getPlayer(invitedPlayerName);
        if (invitedPlayer == null) {
            player.sendMessage(plugin.getLangManager().getChatMessage("error.player_not_found", "player", invitedPlayerName));
            return;
        }
        plugin.getTeamManager().cancelInvitation(player.getUniqueId(), invitedPlayer.getUniqueId());
    }
}
