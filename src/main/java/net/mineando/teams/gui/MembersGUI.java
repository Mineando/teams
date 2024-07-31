package net.mineando.teams.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineando.teams.Teams;
import net.mineando.teams.database.models.Team;
import net.mineando.teams.database.models.TeamMember;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MembersGUI {
    private final Teams plugin;
    private final Team team;
    private final Player viewer;

    public MembersGUI(Teams plugin, Team team, Player viewer) {
        this.plugin = plugin;
        this.team = team;
        this.viewer = viewer;
    }

    public void open() {
        Gui gui = Gui.gui()
                .title(plugin.getLangManager().get("gui.members.title", "team", team.getName()))
                .rows(4)
                .create();

        addMemberHeads(gui);
        addInviteButton(gui);
        addBackButton(gui);

        gui.open(viewer);
    }

    private void addMemberHeads(Gui gui) {
        List<UUID> memberUUIDs = new ArrayList<>();
        memberUUIDs.add(team.getLeader()); // Añadir el líder al principio
        memberUUIDs.addAll(team.getMembers().stream()
                .map(TeamMember::getPlayerUUID)
                .filter(uuid -> !uuid.equals(team.getLeader())) // Excluir al líder de los miembros
                .collect(Collectors.toList()));

        for (int i = 0; i < memberUUIDs.size(); i++) {
            UUID memberUUID = memberUUIDs.get(i);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberUUID);

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(offlinePlayer);
            skull.setItemMeta(meta);

            GuiItem guiItem = ItemBuilder.from(skull)
                    .name(Component.text(offlinePlayer.getName(), NamedTextColor.YELLOW))
                    .lore(
                            Component.text(memberUUID.equals(team.getLeader()) ? "Líder" : "Miembro", NamedTextColor.GRAY),
                            Component.text(offlinePlayer.isOnline() ? "Online" : "Offline", offlinePlayer.isOnline() ? NamedTextColor.GREEN : NamedTextColor.RED)
                    )
                    .asGuiItem(event -> {
                        if (team.getLeader().equals(viewer.getUniqueId()) && !memberUUID.equals(team.getLeader())) {
                            plugin.getTeamManager().kickPlayer(viewer.getUniqueId(), memberUUID);
                            open(); // Reopen the GUI to refresh
                        } else {
                            event.setCancelled(true);
                        }
                    });

            gui.setItem(i + 10, guiItem);
        }
    }

    private void addBackButton(Gui gui) {
        GuiItem backItem = ItemBuilder.from(Material.ARROW)
                .name(plugin.getLangManager().get("gui.members.back_button"))
                .asGuiItem(event -> new TeamGUI(plugin, viewer).open());

        gui.setItem(31, backItem);
    }

    private void addInviteButton(Gui gui) {
        if (team.getLeader().equals(viewer.getUniqueId())) {
            GuiItem inviteItem = ItemBuilder.from(Material.PAPER)
                    .name(plugin.getLangManager().get("gui.members.invite_button"))
                    .asGuiItem(event -> {
                        viewer.closeInventory();
                        viewer.sendMessage(plugin.getLangManager().getChatMessage("gui.members.enter_name"));
                        plugin.addPendingChatAction(viewer, new Teams.ChatAction(Teams.ChatActionType.INVITE_PLAYER));
                    });

            gui.setItem(35, inviteItem);
        }
    }
}
