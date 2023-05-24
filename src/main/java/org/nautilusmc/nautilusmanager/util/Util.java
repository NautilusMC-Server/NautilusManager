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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.cosmetics.NameColor;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Util {

    public static String getTextContent(Component component) {
        String out = "";

        if (component instanceof TextComponent text) out += text.content();
        for (Component child : component.children()) out += getTextContent(child);

        return out;
    }

    public static Player getOnlinePlayer(String nickname) {
        OfflinePlayer p = Nickname.getPlayerFromNickname(nickname);
        if (p == null) p = Bukkit.getOfflinePlayer(nickname);

        return p.isOnline() ? p.getPlayer() : null;
    }

    public static String getName(Player player) {
        String nickname = Nickname.getNickname(player);
        return nickname == null ? player.getName() : nickname;
    }

    public static Component clickableCommand(String command, boolean run) {
        return Component.text(command)
                .clickEvent(run ? ClickEvent.runCommand(command) : ClickEvent.suggestCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text("Run "+command)));
    }

    public static Component nmsFormat(Component component, ChatFormatting formatting) {
        if (formatting.isColor()) return component.color(PaperAdventure.asAdventure(formatting));
        else if (formatting.isFormat()) return component.decorate(formatting == ChatFormatting.UNDERLINE ? TextDecoration.UNDERLINED : TextDecoration.valueOf(formatting.name()));
        else return component;
    }

    private static void setNameTagName(Player player, String name, Collection<? extends Player> players) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        GameProfile oldProfile = nmsPlayer.gameProfile;
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

        setNameTagName(player, text, players);

        PlayerTeam team = new PlayerTeam(new Scoreboard(), player.getName());
        team.setPlayerPrefix(PaperAdventure.asVanilla(name));

        ClientboundSetPlayerTeamPacket addTeam = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
        ClientboundSetPlayerTeamPacket addPlayer = ClientboundSetPlayerTeamPacket.createPlayerPacket(team, text, ClientboundSetPlayerTeamPacket.Action.ADD);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == player) continue;
            ServerPlayer nms = ((CraftPlayer) p).getHandle();

            nms.connection.send(addTeam);
            nms.connection.send(addPlayer);
        }
    }
    public static ArrayList<String> getOnlineNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Player player : NautilusManager.INSTANCE.getServer().getOnlinePlayers()) {
            names.add(player.getName());
            names.add(Nickname.getNickname(player));
        }
        return names;
    }
}
