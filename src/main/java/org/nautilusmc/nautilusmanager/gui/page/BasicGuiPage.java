package org.nautilusmc.nautilusmanager.gui.page;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.nautilusmc.nautilusmanager.gui.Gui;
import org.nautilusmc.nautilusmanager.gui.components.FillerGuiComponent;
import org.nautilusmc.nautilusmanager.gui.components.GuiComponent;

import java.util.ArrayList;
import java.util.List;

public class BasicGuiPage extends GuiPage {
    private int rows;
    private String name;
    private GuiComponent[][] components;
    private final List<GuiPage> children = new ArrayList<>();

    public BasicGuiPage addChild(GuiPage child) {
        children.add(child.setParent(this).setGui(this.getGui()));
        return this;
    }

    @Override
    public GuiPage setGui(Gui gui) {
        for (GuiPage child : children) {
            child.setGui(gui);
        }
        return super.setGui(gui);
    }

    public GuiPage getChild(int index) {
        return children.get(index);
    }

    public BasicGuiPage setSize(int rows) {
        this.rows = rows;
        components = new GuiComponent[rows][9];
        return this;
    }

    public BasicGuiPage addComponent(GuiComponent component, int row, int column) {
        components[row][column] = component;
        return this;
    }

    public BasicGuiPage setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, rows*9, Component.text(name));

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < 9; c++) {
                GuiComponent component = components[r][c];
                if (component == null) component = new FillerGuiComponent();

                inv.setItem(r * 9 + c, component.render());
            }
        }

        return inv;
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        GuiComponent component = components[e.getSlot() / 9][e.getSlot() % 9];

        if (component != null) component.handleClick(e);
    }
}
