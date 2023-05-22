package org.nautilusmc.nautilusmanager.gui.page;

import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AnvilMenu;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TextInputGuiPage extends GuiPage {

    private ItemStack item;
    private String windowName;
    private Consumer<InventoryClickEvent> action;
    private Function<AnvilMenu, net.minecraft.world.item.ItemStack> generateResult;
    private boolean closeOnClick = false;


    public TextInputGuiPage setItem(ItemStack item) {
        return setItem(item, null);
    }

    public TextInputGuiPage setItem(ItemStack item, net.kyori.adventure.text.Component name) {
        return setItem(item, name, new net.kyori.adventure.text.Component[]{});
    }

    public TextInputGuiPage setItem(ItemStack item, net.kyori.adventure.text.Component name, net.kyori.adventure.text.Component[] lore) {
        ItemMeta meta = item.getItemMeta();
        if (name != null) meta.displayName(name);
        if (lore.length > 0) meta.lore(List.of(lore));
        item.setItemMeta(meta);

        this.item = item;
        return this;
    }

    public TextInputGuiPage setWindowName(String name) {
        this.windowName = name;
        return this;
    }

    public TextInputGuiPage setAction(Consumer<InventoryClickEvent> action) {
        this.action = action;
        return this;
    }

    public TextInputGuiPage setCloseOnClick(boolean closeOnClick) {
        this.closeOnClick = closeOnClick;
        return this;
    }

    public TextInputGuiPage setGenerateResult(Function<AnvilMenu, net.minecraft.world.item.ItemStack> generateResult) {
        this.generateResult = generateResult;
        return this;
    }

    private AnvilMenu generateMenu(int syncId, Player p) {
        return new AnvilMenu(syncId, ((CraftPlayer) p).getHandle().getInventory()) {
            @Override
            public void createResult() {
                if (generateResult == null) {
                    super.createResult();
                } else if (this.itemName != null) {
                    this.resultSlots.setItem(0, generateResult.apply(this));

                    this.sendAllDataToRemote();
                    this.broadcastChanges();
                }
            }

            @Override
            public CraftInventoryView getBukkitView() {
                if (inputSlots.getOwner() == null) {
                    try {
                        Field f = SimpleContainer.class.getDeclaredField("bukkitOwner");
                        f.setAccessible(true);

                        f.set(inputSlots, TextInputGuiPage.this);

                        f.setAccessible(false);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

                return super.getBukkitView();
            }
        };
    }

    @Override
    public boolean load(Player player) {
        ItemStack i = item.clone();
        ItemMeta meta = i.getItemMeta();
        List<net.kyori.adventure.text.Component> lore = meta.lore();
        if (lore == null) lore = new ArrayList<>();
        else lore.add(0, net.kyori.adventure.text.Component.text(""));
        lore.add(0, net.kyori.adventure.text.Component.text("Click to reset")
                .decoration(TextDecoration.ITALIC, false)
                .color(net.kyori.adventure.text.format.TextColor.color(255, 120, 118)));
        meta.lore(lore);
        i.setItemMeta(meta);

        ((CraftPlayer) player).getHandle().openMenu(new SimpleMenuProvider((syncId, playerInventory, playerEntity) -> {
            AnvilMenu menu = generateMenu(syncId, player);
            menu.setItem(0, menu.incrementStateId(), CraftItemStack.asNMSCopy(i));
            return menu;
        }, Component.literal(windowName == null ? "Custom Input" : windowName)));

        return true;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (e.getSlotType() == InventoryType.SlotType.RESULT && clickedItem != null && !clickedItem.getType().isAir()) {
            action.accept(e);
            if (closeOnClick) {
                InventoryClickEvent.getHandlerList().unregister(getGui());
                e.getWhoClicked().closeInventory();
            }
        }
    }
}
