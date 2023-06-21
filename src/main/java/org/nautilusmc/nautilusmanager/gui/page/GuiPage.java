package org.nautilusmc.nautilusmanager.gui.page;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.InventoryHolder;
import org.nautilusmc.nautilusmanager.gui.Gui;

public abstract class GuiPage implements InventoryHolder {
    private GuiPage parent;
    private Gui gui;

    public boolean load(Player player) {
        return false;
    }

    public void open() {
        gui.setPage(this);
    }

    public Gui getGui() {
        return gui;
    }

    public GuiPage setGui(Gui gui) {
        this.gui = gui;
        return this;
    }

    protected GuiPage setParent(GuiPage parent) {
        this.parent = parent;
        return this;
    }

    public GuiPage getParent() {
        return parent;
    }

    public abstract void handleClick(InventoryClickEvent e);

    public void handleAnvil(PrepareAnvilEvent e) {}
}
