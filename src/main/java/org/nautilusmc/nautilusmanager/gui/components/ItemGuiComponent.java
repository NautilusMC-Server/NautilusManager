package org.nautilusmc.nautilusmanager.gui.components;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class ItemGuiComponent extends GuiComponent {

    private ItemStack item;

    public ItemGuiComponent setItem(ItemStack item) {
        return setItem(item, null);
    }

    public ItemGuiComponent setItem(ItemStack item, Component name) {
        return setItem(item, name, new Component[]{});
    }

    public ItemGuiComponent setItem(ItemStack item, Component name, Component[] lore) {
        ItemMeta meta = item.getItemMeta();
        if (name != null) meta.displayName(name);
        meta.lore(List.of(lore));
        item.setItemMeta(meta);

        this.item = item;
        return this;
    }

    @Override
    public ItemStack render() {
        return item;
    }
}
