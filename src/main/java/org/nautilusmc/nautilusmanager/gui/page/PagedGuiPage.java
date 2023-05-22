package org.nautilusmc.nautilusmanager.gui.page;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.nautilusmc.nautilusmanager.gui.Gui;

import java.util.ArrayList;
import java.util.List;

public class PagedGuiPage extends GuiPage {

    private final List<BasicGuiPage> pages = new ArrayList<>();
    private int currentPage = 0;

    @Override
    protected GuiPage setParent(GuiPage parent) {
        pages.forEach(page -> page.setParent(parent));

        return super.setParent(parent);
    }

    @Override
    public GuiPage setGui(Gui gui) {
        for (GuiPage page : pages) page.setGui(gui);
        return super.setGui(gui);
    }

    public PagedGuiPage addPage(BasicGuiPage page) {
        pages.add(page);
        return this;
    }

    public void nextPage() {
        currentPage = Math.min(currentPage + 1, pages.size() - 1);
    }

    public void previousPage() {
        currentPage = Math.max(currentPage - 1, 0);
    }

    @Override
    public Inventory getInventory() {
        return pages.get(currentPage).getInventory();
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        pages.get(currentPage).handleClick(e);
    }
}
