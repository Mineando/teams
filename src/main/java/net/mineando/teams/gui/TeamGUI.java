package net.mineando.teams.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineando.teams.Teams;
import net.mineando.teams.database.models.Team;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Optional;

public class TeamGUI {
    private final Teams plugin;
    private final Player player;

    public TeamGUI(Teams plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        Gui gui = Gui.gui()
                .title(plugin.getLangManager().get("gui.team.title"))
                .rows(3)
                .create();

        Optional<Team> playerTeam = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());

        if (playerTeam.isPresent()) {
            addTeamInfoItems(gui, playerTeam.get());
        } else {
            addCreateTeamItem(gui);
        }

        gui.open(player);
    }

    private void addTeamInfoItems(Gui gui, Team team) {
        GuiItem teamInfoItem = ItemBuilder.from(Material.BOOK)
                .name(plugin.getLangManager().get("gui.team.info_title", "team", team.getName()))
                .lore(
                        Component.text("Líder: " + plugin.getTeamUtils().formatPlayerName(team.getLeader()), NamedTextColor.GRAY),
                        Component.text("Miembros: " + team.getMembers().size(), NamedTextColor.GRAY)
                )
                .asGuiItem(event -> event.setCancelled(true));

        GuiItem membersItem = ItemBuilder.from(Material.PLAYER_HEAD)
                .name(plugin.getLangManager().get("gui.team.members_button"))
                .asGuiItem(event -> new MembersGUI(plugin, team, player).open());

        gui.setItem(11, teamInfoItem);
        gui.setItem(13, membersItem);

        if (team.getLeader().equals(player.getUniqueId())) {
            GuiItem disbandItem = ItemBuilder.from(Material.BARRIER)
                    .name(plugin.getLangManager().get("gui.team.disband_button"))
                    .asGuiItem(event -> {
                        plugin.getTeamManager().disbandTeam(player.getUniqueId());
                        player.closeInventory();
                        open(); // Reopen the GUI to refresh
                    });

            gui.setItem(15, disbandItem);
        } else {
            GuiItem leaveItem = ItemBuilder.from(Material.RED_BED)
                    .name(plugin.getLangManager().get("gui.team.leave_button"))
                    .asGuiItem(event -> {
                        plugin.getTeamManager().leaveTeam(player.getUniqueId());
                        player.closeInventory();
                        open(); // Reopen the GUI to refresh
                    });

            gui.setItem(15, leaveItem);
        }
    }

    private void addCreateTeamItem(Gui gui) {
        GuiItem createTeamItem = ItemBuilder.from(Material.EMERALD)
                .name(plugin.getLangManager().get("gui.team.create_button"))
                .asGuiItem(event -> {
                    player.closeInventory();
                    player.sendMessage(plugin.getLangManager().getChatMessage("gui.team.enter_name"));
                    plugin.addPendingChatAction(player, new Teams.ChatAction(Teams.ChatActionType.CREATE_TEAM));
                });

        gui.setItem(13, createTeamItem);
    }
}
