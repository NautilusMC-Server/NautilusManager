package org.nautilusmc.nautilusmanager.cosmetics.commands;

import com.google.common.collect.ImmutableMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.ChatFormatting;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.cosmetics.CosmeticsMenu;
import org.nautilusmc.nautilusmanager.cosmetics.NameColor;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.util.FancyText;
import org.nautilusmc.nautilusmanager.util.Permission;

import java.util.*;

public class CosmeticsCommand extends Command {
    public static final Component INVALID_COLOR_ERROR = Component.text("Invalid color!", ERROR_COLOR);
    
    private static final Map<String, Integer> SETTINGS_ARG_COUNTS = ImmutableMap.of(
            "color", 2,
            "nickname", 1
    );

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(NOT_PLAYER_ERROR);
            return true;
        }

        if (args.length < 1) {
            CosmeticsMenu.openMenu(player);
            return true;
        }

        switch (args[0]) {
            case "menu" -> CosmeticsMenu.openMenu(player);
            case "set" -> setCosmetic(player, args);
            case "clear" -> clearCosmetic(player, args);
            default -> player.sendMessage(getUsageMessage(args));
        }

        return true;
    }

    public Component getUsageMessage(String[] args) {
        StringBuilder out = new StringBuilder();

        if (args.length < 1) {
            out.append("""
                    Usage:
                    -----------------------------------------------
                    /cosmetics menu - Open the cosmetics menu
                    /cosmetics set [...] - Modify a cosmetic option
                    /cosmetics clear - Reset all cosmetic options
                    -----------------------------------------------""");
        } else {
            out.append("Usage: /cosmetics ");
            switch (args[0]) {
                case "set" -> {
                    if (args.length < 2) {
                        out.append("set <");
                        out.append(String.join("|", SETTINGS_ARG_COUNTS.keySet()));
                        out.append("> [...]");
                    } else {
                        switch (args[1]) {
                            case "color" -> {
                                out.append("set color <");
                                out.append(String.join("|", Arrays.stream(FancyText.ColorType.values())
                                        .map(type -> type.name().toLowerCase())
                                        .toList()));
                                out.append("> <color> [color 2]");
                            }
                            case "nickname" -> out.append("set nickname <nickname>");
                        }
                    }
                }
                case "menu" -> out.append("menu");
                case "clear" -> {
                    out.append("clear <");
                    out.append(String.join("|", SETTINGS_ARG_COUNTS.keySet()));
                    out.append(">");
                }
                default -> out.append("<menu|set|clear> [...]");
            }
        }


        return Component.text(out.toString(), INFO_COLOR);
    }

    private void setCosmetic(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getUsageMessage(args));
            return;
        };

        Player player = null;
        int playerIndex = SETTINGS_ARG_COUNTS.containsKey(args[1]) ? SETTINGS_ARG_COUNTS.get(args[1]) + 3 : -1;

        Optional<FancyText.ColorType> colorType = Optional.empty();
        if (playerIndex >= 0 && args.length > 2 && args[1].equalsIgnoreCase("color")) {
            if ((colorType = Arrays.stream(FancyText.ColorType.values())
                    .filter(t -> t.name().equalsIgnoreCase(args[2]))
                    .findFirst())
                    .isEmpty()) {
                sender.sendMessage(getUsageMessage(args));
                return;
            }
            playerIndex += colorType.get().minColors - 1;
        }

        if (playerIndex != -1 && args.length >= playerIndex) {
            if (!sender.hasPermission(Permission.MODIFY_OTHER_PLAYERS.toString())) {
                sender.sendMessage(NO_PERMISSION_ERROR);
                return;
            }

            if ((player = Bukkit.getPlayer(args[playerIndex - 1])) == null) {
                sender.sendMessage(INVALID_PLAYER_ERROR);
                return;
            }
        }

        if (player == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(NOT_PLAYER_ERROR);
                return;
            }
            player = (Player) sender;
        }

        if (args[1].equalsIgnoreCase("color")) {
            if (colorType.isEmpty() || args.length < 3 + colorType.get().minColors) {
                sender.sendMessage(getUsageMessage(args));
                return;
            }

            Component hasAccess = colorType.get().hasAccess(sender);
            if (hasAccess != null) {
                sender.sendMessage(hasAccess.color(ERROR_COLOR));
                return;
            }

            net.minecraft.network.chat.TextColor[] colors = new net.minecraft.network.chat.TextColor[colorType.get().minColors];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = net.minecraft.network.chat.TextColor.parseColor(args[3+i].toLowerCase().replace(' ', '_'));
            }

            if (Arrays.stream(colors).anyMatch(Objects::isNull)) {
                sender.sendMessage(INVALID_COLOR_ERROR);
                return;
            }

            NameColor.setNameColor(player, colorType.get(), true, Arrays.stream(colors).map(c -> TextColor.color(c.getValue())).toArray(TextColor[]::new));

            return;
        } else if (args[1].equalsIgnoreCase("nickname")) {
            if (!sender.hasPermission(Permission.NICKNAME.toString())) {
                sender.sendMessage(NOT_SPONSOR_ERROR);
                return;
            }

            if (args.length < 3) {
                sender.sendMessage(getUsageMessage(args));
                return;
            }

            String error = Nickname.validateNickname(player, args[2]);
            if (error != null) {
                sender.sendMessage(Component.text(error, ERROR_COLOR));
                return;
            }

            Nickname.setNickname(player, args[2], true);

            return;
        }

        sender.sendMessage(getUsageMessage(args));
    }

    private void clearCosmetic(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getUsageMessage(args));
            return;
        }

        Player player = null;
        if (args[0].equalsIgnoreCase("clear")) {
            if (args.length > 2) {
                if (!sender.hasPermission(Permission.MODIFY_OTHER_PLAYERS.toString())) {
                    sender.sendMessage(NO_PERMISSION_ERROR);
                    return;
                }

                if ((player = Bukkit.getPlayer(args[2])) == null) {
                    sender.sendMessage(INVALID_PLAYER_ERROR);
                    return;
                }
            }
        }

        if (player == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(NOT_PLAYER_ERROR);
                return;
            }
            player = (Player) sender;
        }

        if (args[1].equalsIgnoreCase("color")) {
            NameColor.setNameColor(player, true, NameColor.UNSET_COLOR);
            return;
        } else if (args[1].equalsIgnoreCase("nickname")) {
            Nickname.setNickname(player, player.getName(), true);
            return;
        }

        sender.sendMessage(getUsageMessage(args));
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> out = new ArrayList<>();

        if (args.length == 1) {
            out.add("menu");
            out.add("set");
            out.add("clear");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("clear")) {
                out.addAll(SETTINGS_ARG_COUNTS.keySet());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("color")) {
                for (FancyText.ColorType type : FancyText.ColorType.values()) {
                    if (type.hasAccess(sender) == null) out.add(type.name().toLowerCase());
                }
                out.addAll(Arrays.stream(FancyText.ColorType.values())
                        .filter(type -> type.hasAccess(sender) == null)
                        .map(type -> type.name().toLowerCase())
                        .toList());
            }
        }

        int idx = args.length >= 2 && SETTINGS_ARG_COUNTS.containsKey(args[1]) ? SETTINGS_ARG_COUNTS.get(args[1])+3 : -1;

        Optional<FancyText.ColorType> type;
        if (idx >= 0 && args.length > 2 && args[1].equalsIgnoreCase("color") &&
                (type = Arrays.stream(FancyText.ColorType.values())
                        .filter(t -> t.name().equalsIgnoreCase(args[2]))
                        .findFirst())
                        .isPresent()) {
            idx += type.get().minColors - 1;
            if (args.length - 3 <= type.get().minColors) {
                out.addAll(Arrays.stream(ChatFormatting.values())
                        .filter(ChatFormatting::isColor)
                        .map(ChatFormatting::getName)
                        .toList());
                out.add("#ffffff");
            }
        }

        if (idx >= 0 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("clear")) && args.length == idx &&
                sender.hasPermission(Permission.MODIFY_OTHER_PLAYERS.toString())) {
            out.addAll(getOnlineNames());
        }

        return out;
    }
}
