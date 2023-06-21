package org.nautilusmc.nautilusmanager.cosmetics;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.gui.Gui;
import org.nautilusmc.nautilusmanager.gui.components.BackGuiComponent;
import org.nautilusmc.nautilusmanager.gui.components.ButtonGuiComponent;
import org.nautilusmc.nautilusmanager.gui.components.OpenPageGuiComponent;
import org.nautilusmc.nautilusmanager.gui.page.BasicGuiPage;
import org.nautilusmc.nautilusmanager.gui.page.GuiPage;
import org.nautilusmc.nautilusmanager.gui.page.TextInputGuiPage;
import org.nautilusmc.nautilusmanager.util.Emoji;
import org.nautilusmc.nautilusmanager.util.FancyText;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CosmeticsMenu {
    public static final Component SPONSOR_LOCK_MESSAGE = noItalic(Component.text("Become a sponsor to unlock! (")
            .append(Component.text("/sponsor", Command.ERROR_ACCENT_COLOR))
            .append(Component.text(")"))
            .color(Command.ERROR_COLOR));

    private static Component noItalic(Component component) {
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    private static GuiPage getColorSelectPage(Consumer<InventoryClickEvent> action, int colorIndex) {
        BasicGuiPage page = new BasicGuiPage().setSize(5)
                .setName("Color " + (colorIndex + 1))
                .addComponent(new BackGuiComponent()
                        .setItem(
                                new ItemStack(Material.ENDER_PEARL),
                                noItalic(Component.text(Emoji.LEFT + " Back"))
                        ), 4, 4)
                .addComponent(new OpenPageGuiComponent()
                        .setChildIndex(0)
                        .setItem(
                                new ItemStack(Material.PAPER),
                                noItalic(Component.text(Emoji.RIGHT + " Custom"))
                        ), 3, 7)
                .addChild(new TextInputGuiPage()
                        .setItem(
                                new ItemStack(Material.WHITE_DYE),
                                noItalic(Component.text("#ffffff")))
                        .setWindowName("Custom Color")
                        .setAction(action)
                        .setResultGenerator(anvil -> {
                            String renameText = anvil.getRenameText();
                            if (renameText == null) return null;
                            if (!renameText.startsWith("#")) {
                                renameText = '#' + renameText;
                            }
                            TextColor color = TextColor.fromCSSHexString(renameText);
                            if (color == null) return null;

                            ItemStack item = new ItemStack(FancyText.getClosestDye(color));
                            ItemMeta meta = item.getItemMeta();
                            meta.displayName(noItalic(Component.text(color.asHexString(), color)));
                            item.setItemMeta(meta);
                            return item;
                        }));

        int index = 0;
        for (Map.Entry<Material, TextColor> entry : FancyText.DYE_COLORS.entrySet()) {
            page.addComponent(new ButtonGuiComponent()
                    .setAction(action)
                    .setItem(
                            new ItemStack(entry.getKey()),
                            noItalic(Component.text(WordUtils.capitalizeFully(entry.getKey().name().split("_DYE")[0].replace('_', ' ')), TextColor.color(entry.getValue())))
                    ), (index / 7) + 1, (index % 7) + 1);
            index++;
        }

        return page;
    }

    private static GuiPage selectColorsMenu(FancyText.ColorType type, NameColor current, int numColors, Consumer<TextColor[]> callback) {
        TextColor[] colors = new TextColor[numColors];
        Arrays.fill(colors, NamedTextColor.WHITE);

        BasicGuiPage page = new BasicGuiPage()
                .setSize(5)
                .setName("Select Colors")
                .addComponent(new BackGuiComponent()
                        .setItem(
                                new ItemStack(Material.ENDER_PEARL),
                                noItalic(Component.text(Emoji.LEFT + " Back"))
                        ), 4, 4);

        BiConsumer<Integer, TextColor> colorUpdater = (index, color) -> {
            colors[index] = color;
            page.addComponent(new OpenPageGuiComponent()
                            .setChildIndex(index)
                            .setItem(
                                    new ItemStack(FancyText.getClosestDye(color)),
                                    noItalic(Component.text("Color " + (index + 1))),
                                    List.of(noItalic(Component.text(color.asHexString(), color)))
                            ), 1 + (index / 4), 1 + (index % 4) * 2)
                    .addComponent(new ButtonGuiComponent()
                            .setAction(e -> callback.accept(colors))
                            .setCloseOnClick(true)
                            .setItem(
                                    new ItemStack(Material.NAME_TAG),
                                    noItalic(FancyText.colorText(type, Emoji.CHECK + " Finish", colors))
                            ), 4, 6);
        };

        for (int index = 0; index < numColors; index++) {
            colorUpdater.accept(index, current != null && index < current.colors.length ? current.colors[index] : NamedTextColor.WHITE);

            int cachedIndex = index;
            page.addChild(getColorSelectPage(e -> {
                colorUpdater.accept(cachedIndex, e.getCurrentItem().getItemMeta().displayName().color());
                page.open();
            }, index));
        }

        if (numColors == 2) {
            page.addComponent(new ButtonGuiComponent()
                    .setAction(e -> {
                        TextColor temp = colors[0];
                        colorUpdater.accept(0, colors[1]);
                        colorUpdater.accept(1, temp);
                        page.open();
                    })
                    .setItem(
                            new ItemStack(Material.REPEATER),
                            noItalic(Component.text(Emoji.LEFT_RIGHT + " Swap Colors"))
                    ), 4, 2);
        }

        return page;
    }

    private static GuiPage selectColorTypeMenu(Player player) {
        BasicGuiPage page = new BasicGuiPage()
                .setSize(5)
                .setName("Select Color Type")
                .addComponent(new BackGuiComponent()
                        .setItem(
                                new ItemStack(Material.ENDER_PEARL),
                                noItalic(Component.text(Emoji.LEFT + " Back"))
                        ), 4, 4);

        int index = 0;
        for (FancyText.ColorType type : FancyText.ColorType.values()) {
            Component hasAccess = type.hasAccess(player);

            int cachedIndex = index;
            page.addComponent(new ButtonGuiComponent()
                    .setAction(e -> {
                        if (hasAccess == null) {
                            if (type.minColors == 0) {
                                NameColor.setNameColor(player, type, true);
                                return;
                            }

                            if (e.getClickedInventory().getHolder() instanceof BasicGuiPage p) {
                                p.getChild(cachedIndex).open();
                            }
                        }
                    })
                    .setCloseOnClick(type.minColors == 0)
                    .setItem(
                            type.exampleItem(),
                            null,
                            hasAccess == null ? null : List.of(noItalic(hasAccess.colorIfAbsent(Command.ERROR_COLOR)))
                    ), 1 + (index / 4), 1 + (index % 4) * 2);
            page.addChild(selectColorsMenu(
                    type,
                    NameColor.getNameColor(player),
                    type.minColors,
                    colors -> NameColor.setNameColor(player, type, true, colors)
            ));

            index++;
        }

        return page;
    }

    public static void openMenu(Player player) {
        boolean hasNicknamePerm = player.hasPermission(Permission.NICKNAME.toString());
        TextColor loreColor = TextColor.color(133, 194, 201);
        Bukkit.getPluginManager().registerEvents(
                new Gui()
                        .setRootPage(new BasicGuiPage()
                                .setSize(5)
                                .setName("Cosmetics Settings")
                                .addComponent(new OpenPageGuiComponent()
                                        .setChildIndex(0)
                                        .setItem(
                                                new ItemStack(Material.NAME_TAG),
                                                noItalic(Component.text(Emoji.RIGHT + " Name Color", TextColor.color(238, 69, 255)))
                                        ), 1, 2)
                                .addComponent(new OpenPageGuiComponent()
                                        .setChildIndex(1)
                                        .setItem(
                                                new ItemStack(hasNicknamePerm ? Material.OAK_SIGN : Material.BARRIER),
                                                noItalic(Component.text((hasNicknamePerm ? Emoji.RIGHT : Emoji.X) + " Nickname", TextColor.color(33, 245, 169))),
                                                hasNicknamePerm ? null : List.of(SPONSOR_LOCK_MESSAGE)
                                        ), 1, 6)
                                .addComponent(new BackGuiComponent()
                                        .setItem(
                                                new ItemStack(Material.ENDER_PEARL),
                                                noItalic(Component.text(Emoji.LEFT + " Exit"))
                                        ), 4, 4)
                                .addChild(selectColorTypeMenu(player))
                                .addChild(new TextInputGuiPage()
                                        .setItem(
                                                new ItemStack(Material.OAK_SIGN),
                                                noItalic(player.name()),
                                                List.of(
                                                        noItalic(Component.text("Requirements:", loreColor).decorate(TextDecoration.UNDERLINED)),
                                                        noItalic(Component.text(" - 3 to 16 characters in length", loreColor)),
                                                        noItalic(Component.text(" - No spaces" + (player.hasPermission(Permission.NICKNAME_SPECIAL_CHARS.toString()) ? "" : " or special characters"), loreColor)),
                                                        noItalic(Component.text(" - Cannot be the same as an existing player name or nickname", loreColor))
                                                )
                                        )
                                        .setWindowName("Edit Nickname")
                                        .setAction(e -> Nickname.setNickname((Player) e.getWhoClicked(), Util.getTextContent(e.getCurrentItem().getItemMeta().displayName()), true))
                                        .setCloseOnClick(true)
                                        .setResultGenerator(anvil -> {
                                            String text = anvil.getRenameText();
                                            if (text == null || Nickname.validateNickname(player, text) != null) return null;
                                            ItemStack item = new ItemStack(Material.OAK_SIGN);
                                            ItemMeta meta = item.getItemMeta();
                                            meta.displayName(noItalic(Component.text(text)));
                                            item.setItemMeta(meta);
                                            return item;
                                        })
                                )
                        )
                        .display(player), NautilusManager.INSTANCE);
    }
}
