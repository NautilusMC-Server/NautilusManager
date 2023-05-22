package org.nautilusmc.nautilusmanager.gui.components;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class GuiComponent {

    public abstract ItemStack render();
    public abstract void handleClick(InventoryClickEvent e);
}
