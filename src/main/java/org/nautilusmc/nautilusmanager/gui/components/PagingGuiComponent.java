package org.nautilusmc.nautilusmanager.gui.components;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.nautilusmc.nautilusmanager.gui.page.PagedGuiPage;

public class PagingGuiComponent extends ItemGuiComponent {
    private PagedGuiPage.Direction direction;

    public PagingGuiComponent setDirection(PagedGuiPage.Direction direction) {
        this.direction = direction;
        return this;
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        if (e.getClickedInventory().getHolder() instanceof PagedGuiPage page) {
            page.changePage(direction);
            page.open();
        }
    }
}
