package net.mineando.teams.commands;

import net.mineando.teams.Teams;
import net.mineando.teams.gui.TeamGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamCommand implements CommandExecutor {
    private final Teams plugin;

    public TeamCommand(Teams plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangManager().getChatMessage("error.player_only_command"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            new TeamGUI(plugin, player).open();
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "accept":
            case "reject":
                if (args.length < 2) {
                    player.sendMessage(plugin.getLangManager().getChatMessage("error.missing_player"));
                    return true;
                }
                handleInvitationResponse(player, args[0].toLowerCase(), args[1]);
                break;
            case "cancel":
                if (args.length < 2) {
                    player.sendMessage(plugin.getLangManager().getChatMessage("error.missing_player"));
                    return true;
                }
                handleCancelInvitation(player, args[1]);
                break;
            default:
                new TeamGUI(plugin, player).open();
                break;
        }

        return true;
    }

    private void handleInvitationResponse(Player player, String action, String inviterName) {
        Player inviter = Bukkit.getPlayer(inviterName);
        if (inviter == null) {
            player.sendMessage(plugin.getLangManager().getChatMessage("error.player_not_found", "player", inviterName));
            return;
        }

        UUID invitedUUID = player.getUniqueId();
        UUID inviterUUID = inviter.getUniqueId();

        if (action.equals("accept")) {
            if (plugin.getTeamManager().acceptInvitation(invitedUUID, inviterUUID)) {
                player.sendMessage(plugin.getLangManager().getChatMessage("team.invitation_accepted", "player", inviterName));
                inviter.sendMessage(plugin.getLangManager().getChatMessage("team.player_joined", "player", player.getName()));
            } else {
                player.sendMessage(plugin.getLangManager().getChatMessage("error.invalid_invitation"));
            }
        } else {
            if (plugin.getTeamManager().rejectInvitation(invitedUUID, inviterUUID)) {
                player.sendMessage(plugin.getLangManager().getChatMessage("team.invitation_rejected", "player", inviterName));
                inviter.sendMessage(plugin.getLangManager().getChatMessage("team.invitation_rejected_by", "player", player.getName()));
            } else {
                player.sendMessage(plugin.getLangManager().getChatMessage("error.invalid_invitation"));
            }
        }
    }

    private void handleCancelInvitation(Player player, String invitedPlayerName) {
        Player invitedPlayer = Bukkit.getPlayer(invitedPlayerName);
        if (invitedPlayer == null) {
            player.sendMessage(plugin.getLangManager().getChatMessage("error.player_not_found", "player", invitedPlayerName));
            return;
        }

        UUID invitedUUID = invitedPlayer.getUniqueId();
        UUID inviterUUID = player.getUniqueId();

        if (plugin.getTeamManager().cancelInvitation(inviterUUID, invitedUUID)) {
            player.sendMessage(plugin.getLangManager().getChatMessage("team.invitation_cancelled", "player", invitedPlayerName));
            invitedPlayer.sendMessage(plugin.getLangManager().getChatMessage("team.invitation_cancelled_by", "player", player.getName()));
        } else {
            player.sendMessage(plugin.getLangManager().getChatMessage("error.no_pending_invitation", "player", invitedPlayerName));
        }
    }
}
