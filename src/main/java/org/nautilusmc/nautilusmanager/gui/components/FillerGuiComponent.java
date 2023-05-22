package org.nautilusmc.nautilusmanager.gui.components;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FillerGuiComponent extends GuiComponent {

    @Override
    public ItemStack render() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public void handleClick(InventoryClickEvent e) {}
}
