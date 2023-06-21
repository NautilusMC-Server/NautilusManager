package org.nautilusmc.nautilusmanager.gui.page;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.gui.Gui;

import java.util.ArrayList;
import java.util.List;

public class PagedGuiPage extends GuiPage {
    public enum Direction {
        NEXT,
        PREVIOUS
    }

    private final List<BasicGuiPage> pages = new ArrayList<>();
    private int currentPage = 0;

    @Override
    protected GuiPage setParent(GuiPage parent) {
        for (GuiPage page : pages) {
            page.setParent(parent);
        }
        return super.setParent(parent);
    }

    @Override
    public GuiPage setGui(Gui gui) {
        for (GuiPage page : pages) {
            page.setGui(gui);
        }
        return super.setGui(gui);
    }

    public BasicGuiPage getPage() {
        return pages.get(currentPage);
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

    public void changePage(Direction direction) {
        switch (direction) {
            case NEXT -> nextPage();
            case PREVIOUS -> previousPage();
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return getPage().getInventory();
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        getPage().handleClick(e);
    }
}
