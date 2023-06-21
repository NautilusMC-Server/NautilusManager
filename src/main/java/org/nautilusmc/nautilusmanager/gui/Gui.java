package org.nautilusmc.nautilusmanager.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.nautilusmc.nautilusmanager.gui.page.GuiPage;

import java.util.UUID;

public class Gui implements Listener {
    private UUID playerID;
    private GuiPage rootPage;
    private GuiPage currentPage;

    public UUID getPlayerID() {
        return playerID;
    }

    public Gui setRootPage(GuiPage rootPage) {
        this.rootPage = rootPage.setGui(this);
        return this;
    }

    public Gui setPage(GuiPage page) {
        currentPage = page;
        Player player = Bukkit.getPlayer(playerID);
        if (player != null && !page.load(player)) {
            player.openInventory(page.getInventory());
        }
        return this;
    }

    public Gui display(Player player) {
        playerID = player.getUniqueId();
        setPage(rootPage);
        return this;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getPlayer().getUniqueId().equals(playerID)) {
            if (e.getInventory() instanceof AnvilInventory) {
                e.getInventory().clear();
            }

            if (!e.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) {
                InventoryCloseEvent.getHandlerList().unregister(this);
                InventoryClickEvent.getHandlerList().unregister(this);
                PrepareAnvilEvent.getHandlerList().unregister(this);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getUniqueId().equals(playerID)) {
            e.setCancelled(true);
            if (e.getClickedInventory() == e.getView().getTopInventory()) {
                currentPage.handleClick(e);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPrepareAnvil(PrepareAnvilEvent e) {
        if (e.getView().getPlayer().getUniqueId().equals(playerID)) {
            currentPage.handleAnvil(e);
        }
    }
}
