package org.nautilusmc.nautilusmanager.gui.components;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.nautilusmc.nautilusmanager.gui.page.BasicGuiPage;

public class OpenPageGuiComponent extends ItemGuiComponent {

    private int childIdx;

    public OpenPageGuiComponent setChildIdx(int childIdx) {
        this.childIdx = childIdx;
        return this;
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        if (e.getClickedInventory().getHolder() instanceof BasicGuiPage page) {
            page.getChild(childIdx).open();
        }
    }
}
