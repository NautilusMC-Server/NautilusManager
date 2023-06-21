package org.nautilusmc.nautilusmanager.gui.components;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.nautilusmc.nautilusmanager.gui.page.BasicGuiPage;

public class OpenPageGuiComponent extends ItemGuiComponent {
    private int childIndex;

    public OpenPageGuiComponent setChildIndex(int childIndex) {
        this.childIndex = childIndex;
        return this;
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        if (e.getClickedInventory().getHolder() instanceof BasicGuiPage page) {
            page.getChild(childIndex).open();
        }
    }
}
