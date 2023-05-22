package org.nautilusmc.nautilusmanager.gui.components;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.nautilusmc.nautilusmanager.gui.page.PagedGuiPage;

public class PagingGuiComponent extends ItemGuiComponent {

    private Direction direction;

    public PagingGuiComponent setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        if (e.getClickedInventory().getHolder() instanceof PagedGuiPage page) {
            if (direction == Direction.FORWARD) page.nextPage();
            else if (direction == Direction.BACK) page.previousPage();

            page.open();
        }
    }

    public enum Direction {
        FORWARD,
        BACK
    }
}
