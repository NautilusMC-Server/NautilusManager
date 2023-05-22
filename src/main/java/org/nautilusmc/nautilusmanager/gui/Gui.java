package org.nautilusmc.nautilusmanager.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.nautilusmc.nautilusmanager.gui.page.GuiPage;

import java.util.UUID;

public class Gui implements Listener {

    private UUID player;

    private GuiPage root;
    private GuiPage open;

    public Gui() {}

    public Gui setRoot(GuiPage root) {
        this.root = root.setGui(this);
        return this;
    }

    public void openPage(GuiPage page) {
        this.open = page;
        if (!page.load(Bukkit.getPlayer(player))) Bukkit.getPlayer(player).openInventory(page.getInventory());
    }

    public Gui display(Player player) {
        this.open = root;

        this.player = player.getUniqueId();
        player.openInventory(root.getInventory());

        return this;
    }

    public UUID getPlayer() {
        return player;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getUniqueId() == player) {
            if (e.getView().getTopInventory().getHolder() instanceof GuiPage page && page.getGui() == this) {
                e.setCancelled(true);
                if (e.getView().getTopInventory() == e.getClickedInventory()) open.handleClick(e);
            }
        }
    }
}
