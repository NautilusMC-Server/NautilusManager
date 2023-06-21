package org.nautilusmc.nautilusmanager.gui.page;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class TextInputGuiPage extends GuiPage {
    private ItemStack item;
    private String windowName;
    private Consumer<InventoryClickEvent> action;
    private Function<AnvilInventory, ItemStack> resultGenerator;
    private boolean closeOnClick = false;

    public TextInputGuiPage setItem(ItemStack item) {
        return setItem(item, null);
    }

    public TextInputGuiPage setItem(ItemStack item, Component name) {
        return setItem(item, name, null);
    }

    public TextInputGuiPage setItem(ItemStack item, Component name, List<Component> lore) {
        ItemMeta meta = item.getItemMeta();
        if (name != null) {
            meta.displayName(name);
        }
        if (lore != null && !lore.isEmpty()) {
            meta.lore(lore);
        }
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

    public TextInputGuiPage setResultGenerator(Function<AnvilInventory, ItemStack> resultGenerator) {
        this.resultGenerator = resultGenerator;
        return this;
    }

    /*private AnvilMenu generateMenu(int syncId, Player p) {
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
    }*/

    @Override
    public boolean load(Player player) {
        InventoryView menu = player.openAnvil(null, true);
        if (menu == null) return false;
        menu.setTitle(Objects.requireNonNullElse(windowName, "Text Input"));
        menu.setItem(0, Util.addActionLore(
                item.clone(),
                Component.text("Click to reset", Command.ERROR_COLOR)
        ));

        return true;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null; // shhhh it's okay this class shouldn't be used as an InventoryHolder anyway
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (e.getSlotType() == InventoryType.SlotType.RESULT && clickedItem != null && !clickedItem.getType().isAir()) {
            action.accept(e);

            if (closeOnClick) {
                e.getWhoClicked().closeInventory();
            }
        }
    }

    @Override
    public void handleAnvil(PrepareAnvilEvent e) {
        e.getInventory().setRepairCost(0);
        e.getInventory().setRepairCostAmount(0);

        if (resultGenerator != null) {
            e.setResult(Util.addActionLore(
                    resultGenerator.apply(e.getInventory()),
                    Component.text("Click to accept", Command.SUCCESS_COLOR)
            ));
        }
    }
}
