package org.nautilusmc.nautilusmanager.util;

import com.google.common.collect.ImmutableMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.HSVLike;
import net.minecraft.ChatFormatting;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.cosmetics.CosmeticsMenu;

import java.util.Map;

public class FancyText {
    public enum ColorType {
        RESET(0, Material.PAPER, NamedTextColor.WHITE),
        SOLID(1, Material.SLIME_BALL, TextColor.color(168, 131, 255)),
        GRADIENT(2, Material.MAGMA_CREAM, TextColor.color(255, 156, 253), TextColor.color(86, 171, 255)),
        ALTERNATING(2, Material.SWEET_BERRIES, TextColor.color(27, 255, 197), TextColor.color(40, 181, 146)),
        RAINBOW(0, "end/dragon_egg", Material.TORCHFLOWER);

        public final int minColors;
        public final Advancement requiredAdvancement;

        private final ItemStack example;

        ColorType(int minColors, Material material, TextColor... colors) {
            this(minColors, null, material, colors);
        }

        ColorType(int minColors, String requiredAdvancement, Material example, TextColor... colors) {
            this.minColors = minColors;
            this.requiredAdvancement = requiredAdvancement == null ? null : Bukkit.getAdvancement(NamespacedKey.fromString(requiredAdvancement));

            this.example = new ItemStack(example);
            Bukkit.getScheduler().runTaskLater(NautilusManager.INSTANCE, () -> {
                ItemMeta meta = this.example.getItemMeta();

                meta.displayName(FancyText.colorText(this, WordUtils.capitalizeFully(name().replace("_", " ")), colors)
                        .decoration(TextDecoration.ITALIC, false));

                this.example.setItemMeta(meta);
            }, 1);
        }

        /**
         * Returns `null` if `sender` has access, otherwise returns the error string
         */
        public Component hasAccess(CommandSender sender) {
            if (this == ColorType.RESET) return null;

            if (sender instanceof Player player && requiredAdvancement != null) {
                if (!player.getAdvancementProgress(requiredAdvancement).isDone()) {
                    return Component.text("Complete ")
                            .append(requiredAdvancement.displayName())
                            .append(Component.text(" to unlock!"));
                }

                return null;
            }

            if (!sender.hasPermission("nautiluscosmetics.color." + name().toLowerCase())) {
                return CosmeticsMenu.SPONSOR_LOCK_MESSAGE;
            }

            return null;
        }

        public ItemStack exampleItem() {
            return example.clone();
        }
    }

    public static final Map<Material, TextColor> DYE_COLORS = ImmutableMap.ofEntries(
            Map.entry(Material.WHITE_DYE, TextColor.color(252, 252, 252)),
            Map.entry(Material.LIGHT_GRAY_DYE, TextColor.color(214, 214, 214)),
            Map.entry(Material.GRAY_DYE, TextColor.color(130, 130, 130)),
            Map.entry(Material.BLACK_DYE, TextColor.color(39, 39, 50)),
            Map.entry(Material.BROWN_DYE, TextColor.color(151, 92, 50)),
            Map.entry(Material.RED_DYE, TextColor.color(207, 67, 62)),
            Map.entry(Material.ORANGE_DYE, TextColor.color(227, 156, 51)),
            Map.entry(Material.YELLOW_DYE, TextColor.color(228, 228, 41)),
            Map.entry(Material.LIME_DYE, TextColor.color(129, 209, 28)),
            Map.entry(Material.GREEN_DYE, TextColor.color(73, 106, 24)),
            Map.entry(Material.CYAN_DYE, TextColor.color(44, 123, 155)),
            Map.entry(Material.LIGHT_BLUE_DYE, TextColor.color(141, 183, 241)),
            Map.entry(Material.BLUE_DYE, TextColor.color(51, 93, 193)),
            Map.entry(Material.PURPLE_DYE, TextColor.color(162, 82, 204)),
            Map.entry(Material.MAGENTA_DYE, TextColor.color(201, 104, 195)),
            Map.entry(Material.PINK_DYE, TextColor.color(234, 165, 201))
    );

    public static Material getClosestDye(TextColor color) {
        Material closest = Material.WHITE_DYE;
        // since we don't need the value of the distance, we can skip the square root and compare distance^2
        int smallestSquaredDist = Integer.MAX_VALUE;

        for (Map.Entry<Material, TextColor> entry : DYE_COLORS.entrySet()) {
            int redDist = color.red() - entry.getValue().red();
            int greenDist = color.green() - entry.getValue().green();
            int blueDist = color.blue() - entry.getValue().blue();
            int squaredDist = redDist * redDist + greenDist * greenDist + blueDist * blueDist;

            if (squaredDist < smallestSquaredDist) {
                closest = entry.getKey();
                smallestSquaredDist = squaredDist;
            }
        }

        return closest;
    }

    public static Component colorText(ColorType type, String text, TextColor... colors) {
        if (type == null || colors.length < type.minColors) {
            return Component.text(text);
        }

        switch (type) {
            default -> {
                return Component.text(text);
            }
            case SOLID -> {
                return Component.text(text, colors[0]);
            }
            case GRADIENT -> {
                Component out = Component.empty();

                int sectionLen = text.length() / (colors.length - 1);
                for (int i = 0; i < text.length(); i++) {
                    int section = i / sectionLen;
                    out = out.append(Component.text(text.charAt(i), TextColor.lerp((i % sectionLen) / (float) sectionLen, colors[section], colors[section + 1])));
                }

                return out;
            }
            case ALTERNATING -> {
                Component out = Component.empty();

                for (int i = 0; i < text.length(); i++) {
                    out = out.append(Component.text(text.charAt(i), colors[i % colors.length]));
                }

                return out;
            }
            case RAINBOW -> {
                Component out = Component.empty();

                for (int i = 0; i < text.length(); i++) {
                    out = out.append(Component.text(text.charAt(i), TextColor.color(HSVLike.hsvLike(0.9f / text.length() * i, 0.725f, 1))));
                }

                return out;
            }
        }
    }

    public static Component parseChatFormatting(String message) {
        TextComponent component = Component.empty();
        TextComponent building = Component.empty();

        for (int i = 0; i < message.length(); i++) {
            boolean consumed = false;
            boolean canceled = i != 0 && message.charAt(i-1) == '\\';

            if (message.charAt(i) == '`' && i < message.length()-1) {
                if (message.charAt(i + 1) == 'x') {
                    try {
                        if (!canceled) {
                            int hex = Integer.parseInt(message.substring(i + 2, i + 8), 16);
                            component = component.append(building);
                            building = Component.empty().style(building.style()).color(TextColor.color(hex));
                            i += 7;
                        }
                        consumed = true;
                    } catch (NumberFormatException ignored) {}
                } else {
                    ChatFormatting formatting = ChatFormatting.getByCode(message.charAt(i+1));

                    if (message.charAt(i + 1) == '`') {
                        for (ChatFormatting f : ChatFormatting.values()) {
                            if (message.substring(i+2).toUpperCase().startsWith(f.name())) {
                                formatting = f;
                                if (!canceled) i += f.name().length();
                                consumed = true;
                            }
                        }
                    }

                    if (formatting != null) {
                        if (!canceled) {
                            component = component.append(building);
                            building = (TextComponent) Util.nmsFormat(Component.empty().style(formatting != ChatFormatting.RESET ? building.style() : Style.empty()), formatting);

                            i++;
                        }
                        consumed = true;
                    }
                }
            }

            if (consumed && canceled) {
                building = building.content(building.content().substring(0, building.content().length() - 1) + message.charAt(i));
                continue;
            }

            if (!consumed) {
                building = building.content(building.content() + message.charAt(i));
            }
        }

        return component.append(building);
    }
}
