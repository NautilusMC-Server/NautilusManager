package org.nautilusmc.nautilusmanager.gui.components;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface GuiComponent {
    ItemStack render();
    void handleClick(InventoryClickEvent e);
}
