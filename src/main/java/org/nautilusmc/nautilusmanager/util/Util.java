package org.nautilusmc.nautilusmanager.util;

import com.mojang.authlib.GameProfile;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;

import java.util.*;

public class Util {
    public static @NotNull String getTextContent(@NotNull Component component) {
        Objects.requireNonNull(component, "Util.getTextContent() encountered a null component!");
        StringBuilder out = new StringBuilder();

        if (component instanceof TextComponent text) {
            out.append(text.content());
        }
        for (Component child : component.children()) {
            out.append(getTextContent(child));
        }

        return out.toString();
    }

    public static Calendar getCalendar() {
        return Calendar.getInstance(TimeZone.getTimeZone("CST"));
    }

    public static Player getOnlinePlayer(String nickname) {
        OfflinePlayer player = getOfflinePlayerIfCachedByNick(nickname);
        return player == null ? null : player.getPlayer();
    }

    public static String getName(OfflinePlayer player) {
        String nickname = Nickname.getNickname(player);
        return nickname == null ? player.getName() : nickname;
    }

    public static Component clickableCommand(String command, boolean run) {
        return Component.text(command)
                .clickEvent(run ? ClickEvent.runCommand(command) : ClickEvent.suggestCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text("Run " + command)));
    }

    public static Component nmsFormat(Component component, ChatFormatting formatting) {
        if (formatting.isColor()) return component.color(PaperAdventure.asAdventure(formatting));
        else if (formatting.isFormat()) return component.decorate(formatting == ChatFormatting.UNDERLINE ? TextDecoration.UNDERLINED : TextDecoration.valueOf(formatting.name()));
        else return component;
    }

    private static void setNameTagName(Player player, String name, Collection<? extends Player> players) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        GameProfile oldProfile = nmsPlayer.gameProfile;
        nmsPlayer.lastSave = MinecraftServer.currentTick;
        nmsPlayer.gameProfile = new GameProfile(player.getUniqueId(), name);
        nmsPlayer.gameProfile.getProperties().putAll(oldProfile.getProperties());

        ClientboundPlayerInfoRemovePacket remove = new ClientboundPlayerInfoRemovePacket(List.of(player.getUniqueId()));
        ClientboundPlayerInfoUpdatePacket update = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(nmsPlayer));
        nmsPlayer.gameProfile = oldProfile;

        for (Player p : players) {
            if (p == player) continue;
            ServerPlayer nms = ((CraftPlayer) p).getHandle();

            nms.connection.send(remove);
            nms.connection.send(update);

            if (nmsPlayer.tracker != null) {
                nmsPlayer.tracker.serverEntity.removePairing(nms);
                nmsPlayer.tracker.serverEntity.addPairing(nms);
            }
        }
    }

    public static void updateNameTag(Player player, Component name, Collection<? extends Player> players) {
        String text = "%08x".formatted(player.getEntityId()).replaceAll("(.)", "§$1");

        players = new ArrayList<>(players);
        players.removeIf(p -> !p.canSee(player) || p == player);
        if (players.isEmpty()) return;

        setNameTagName(player, text, players);

        Team existingTeam = player.getScoreboard().getEntryTeam(player.getName());

        PlayerTeam team = new PlayerTeam(new Scoreboard(), player.getName());
        team.setPlayerPrefix(PaperAdventure.asVanilla((existingTeam != null ? Component.empty().append(existingTeam.prefix().append(Component.space())) : Component.empty()).append(name)));

        ClientboundSetPlayerTeamPacket addTeam = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
        ClientboundSetPlayerTeamPacket addPlayer = ClientboundSetPlayerTeamPacket.createPlayerPacket(team, text, ClientboundSetPlayerTeamPacket.Action.ADD);

        for (Player p : Bukkit.getOnlinePlayers()) {
            ServerPlayer nms = ((CraftPlayer) p).getHandle();

            nms.connection.send(addTeam);
            nms.connection.send(addPlayer);
        }
    }

    public static OfflinePlayer getOfflinePlayerIfCached(String name) {
        if (name == null) return null;
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(p -> name.equalsIgnoreCase(p.getName()))
                .findFirst().orElse(null);
    }

    public static OfflinePlayer getOfflinePlayerIfCachedByNick(String name) {
        OfflinePlayer player = Nickname.getPlayerFromNickname(name);
        return player == null ? getOfflinePlayerIfCached(name) : player;
    }

    public static Component toggleDecoration(Component component, TextDecoration decoration) {
        return component.decoration(decoration, !component.hasDecoration(decoration));
    }

    public static Map<String, Object> locationAsMap(Location loc) {
        return Map.of(
                "world", loc.getWorld().getUID().toString(),
                "x", loc.getX(),
                "y", loc.getY(),
                "z", loc.getZ(),
                "pitch", loc.getPitch(),
                "yaw", loc.getYaw()
        );
    }

    public static ItemStack addActionLore(ItemStack item, Component actionText) {
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        List<Component> lore = meta.lore();

        if (lore == null) {
            lore = new ArrayList<>();
        } else if (!lore.isEmpty()) {
            lore.add(0, Component.empty());
        }
        lore.add(0, actionText.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        meta.lore(lore);
        item.setItemMeta(meta);

        return item;
    }

    public static final int TICKS_PER_SECOND = 20;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MINUTES_PER_HOUR = 60;

    public record TimeAmount(int ticks) implements Comparable<TimeAmount> {
        @Override
        public int compareTo(@NotNull Util.TimeAmount other) {
            return Integer.compare(ticks, other.ticks);
        }

        public String toHoursMinutes() {
            int minutes = ticks / (TICKS_PER_SECOND * SECONDS_PER_MINUTE);
            int hours = minutes / MINUTES_PER_HOUR;
            minutes %= MINUTES_PER_HOUR;
            return "%dh %dm".formatted(hours, minutes);
        }
    }
}
