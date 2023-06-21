package org.nautilusmc.nautilusmanager.gui.components;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FillerGuiComponent implements GuiComponent {
    public static final Material FILLER_MATERIAL = Material.CYAN_STAINED_GLASS_PANE;

    @Override
    public ItemStack render() {
        ItemStack item = new ItemStack(FILLER_MATERIAL);

        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.space());
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        // do nothing
    }
}
