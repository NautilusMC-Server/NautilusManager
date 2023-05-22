package org.nautilusmc.nautilusmanager.gui.components;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.nautilusmc.nautilusmanager.gui.page.GuiPage;

public class BackGuiComponent extends ItemGuiComponent {
    @Override
    public void handleClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof GuiPage page) {
            if (page.getParent() != null) {
                page.getParent().open();
            } else {
                InventoryClickEvent.getHandlerList().unregister(page.getGui());
                e.getWhoClicked().closeInventory();
            }
        }
    }
}
