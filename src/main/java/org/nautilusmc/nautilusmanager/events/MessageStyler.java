package org.nautilusmc.nautilusmanager.events;

import com.google.common.collect.EvictingQueue;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;
import org.apache.commons.lang3.mutable.MutableInt;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.commands.SuicideCommand;
import org.nautilusmc.nautilusmanager.gui.page.GuiPage;
import org.nautilusmc.nautilusmanager.util.FancyText;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("UnstableApiUsage")
public class MessageStyler implements Listener {

    public static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)");
    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("CST");

    public static EvictingQueue<Component> runningMessages = EvictingQueue.create(50);

    private static TranslatableComponent styleMessage(TranslatableComponent message) {
        List<Component> args = new ArrayList<>(message.args());

        for (int i = 0; i < args.size(); i++) {
            if (!(args.get(i) instanceof TextComponent component)) continue;

            Player player = Bukkit.getPlayerExact(component.content());

            if (player != null) {
                args.set(i, player.displayName()
                        .clickEvent(component.clickEvent())
                        .hoverEvent(component.hoverEvent()));
            }
        }

        return message.args(args);
    }

    private static Component replace(Component component, String target, String replace) {
        if (component instanceof TextComponent text) text.content(text.content().replace(target, replace));

        List<Component> children = component.children();
        for (int i = 0; i < children.size(); i++) {
            children.set(i, replace(children.get(i), target, replace));
        }
        component.children(children);

        return component;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (e.deathMessage() == null) return;

        Component styledDeathMessage;
        if (e.deathMessage() instanceof TranslatableComponent t && t.key().equals(SuicideCommand.SUICIDE_TRANSLATION_KEY)) {
            styledDeathMessage = Component.empty()
                    .append(e.getPlayer().displayName())
                    .append(Component.text(" took the easy way out"));
        } else {
            styledDeathMessage = styleMessage((TranslatableComponent) e.deathMessage());
        }

        Component deathMessage = Component.empty()
                .append(Component.empty()
                        .append(Component.text("Death"))
                        .append(Component.text(" ☠"))
                        .color(TextColor.color(46, 230, 255))
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" | ").color(TextColor.color(87, 87, 87)))
                .append(styledDeathMessage);

        ServerPlayer nms = ((CraftPlayer) e.getPlayer()).getHandle();
        net.minecraft.network.chat.Component nmsMessage = PaperAdventure.asVanilla(deathMessage);

        Team team = nms.getTeam();
        if (team != null && team.getDeathMessageVisibility() != Team.Visibility.ALWAYS) {
            if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                nms.server.getPlayerList().broadcastSystemToTeam(nms, nmsMessage);
            } else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                nms.server.getPlayerList().broadcastSystemToAllExceptTeam(nms, nmsMessage);
            }
        } else {
            nms.server.getPlayerList().broadcastSystemMessage(nmsMessage, false);
        }

        // processedDisconnect will make it ignore the death packet so that we can do our own
        // this is a little bit hacky, but it works
        nms.connection.processedDisconnect = true;
        Bukkit.getScheduler().runTaskLater(NautilusManager.INSTANCE, () -> {
            nms.connection.processedDisconnect = false;
            nms.connection.send(new ClientboundPlayerCombatKillPacket(nms.getCombatTracker(), PaperAdventure.asVanilla(styledDeathMessage)), PacketSendListener.exceptionallySend(() -> {
                // TODO: do something with this?
                String s = nmsMessage.getString(256);
                MutableComponent hover = net.minecraft.network.chat.Component.translatable("death.attack.message_too_long", net.minecraft.network.chat.Component.literal(s).withStyle(ChatFormatting.YELLOW));
                MutableComponent newComp = net.minecraft.network.chat.Component.translatable("death.attack.even_more_magic", PaperAdventure.asVanilla(e.getPlayer().displayName())).withStyle((chatmodifier) -> chatmodifier.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
                return new ClientboundPlayerCombatKillPacket(nms.getCombatTracker(), newComp);
            }));
        }, 1);

        runningMessages.add(deathMessage);

        e.deathMessage(null);
    }

    // e.message() is null sooo idk what to do
//    @EventHandler
//    public void onAdvancement(PlayerAdvancementDoneEvent e) {
//        e.message(styleMessage((TranslatableComponent) e.message()));
//        runningMessages.add(e.message());
//    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        e.getPlayer().sendMessage(Component.text("Your coordinates: (" + Math.round(e.getPlayer().getLocation().getX()) + ", " +
                Math.round(e.getPlayer().getLocation().getZ()) + ")").color(TextColor.color(200, 200, 200)).decorate(TextDecoration.BOLD));
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        if (e.joinMessage() == null) return;

        // send the packets to the joining player for all the online players' name tags
        for (Player p : Bukkit.getOnlinePlayers()) {
            Util.updateNameTag(p, p.displayName(), List.of(e.getPlayer()));
        }

        e.joinMessage(Component.empty()
                        .append(Component.text("Join").color(TextColor.color(83, 255, 126)))
                        .append(Component.text(" | ").color(TextColor.color(87, 87, 87)))
                        .append(e.getPlayer().displayName()));

        runningMessages.forEach(c->e.getPlayer().sendMessage(c));
        runningMessages.add(e.joinMessage());

        Component obfuscation = Component.text("x").decorate(TextDecoration.OBFUSCATED).color(TextColor.color(47, 250, 255));
        Component welcomeMessage = Component.empty()
                .append(obfuscation)
                .append(Component.text(" Feel welcome at ").color(TextColor.color(170, 170, 170)))
                .append(Component.text("Nautilus").color(TextColor.color(34, 150, 155)))
                .append(Component.text("MC").color(TextColor.color(47, 250, 255)))
                .append(Component.text(", ").color(TextColor.color(170, 170, 170)))
                .append(e.getPlayer().displayName())
                .append(Component.text("! ").color(TextColor.color(170, 170, 170)))
                .append(obfuscation);
        Bukkit.getScheduler().runTaskLater(NautilusManager.INSTANCE, () -> e.getPlayer().sendMessage(welcomeMessage), 1);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (e.quitMessage() == null) return;

        e.quitMessage(Component.empty()
                .append(Component.text("Quit").color(TextColor.color(255, 58, 30)))
                .append(Component.text(" | ").color(TextColor.color(87, 87, 87)))
                .append(e.getPlayer().displayName()));

        runningMessages.add(e.quitMessage());
    }

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent e) {
        if (e.getInventory().getHolder() instanceof GuiPage) return;

        String renameText = e.getInventory().getRenameText();
        ItemStack result = e.getResult();

        if (e.getView().getPlayer().hasPermission(NautilusCommand.CHAT_FORMATTING_PERM) && renameText != null && !renameText.isEmpty() && result != null) {
            ItemMeta meta = result.getItemMeta();
            meta.displayName(FancyText.parseChatFormatting(renameText));
            result.setItemMeta(meta);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMessage(AsyncChatEvent e) {
        e.setCancelled(true);

        sendMessageAsUser(e.getPlayer(), e.message());
    }

    public static void sendMessageAsUser(Player player, Component message) {
        Component m = Component.empty()
                .append(getTimeStamp())
                .append(player.displayName())
                .append(Component.text(" » ").color(TextColor.color(150, 150, 150)))
                .append(formatUserMessage(player, message).color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));

        Bukkit.broadcast(m);
        runningMessages.add(m);
    }

    public static Component formatUserMessage(CommandSender player, Component message) {
        if (player.hasPermission(NautilusCommand.CHAT_FORMATTING_PERM)) {
            message = FancyText.parseChatFormatting(Util.getTextContent(message));
        }

        return styleURL(message, null, null);
    }

    public static Component styleURL(Component component, String url) {
        return component.clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text("Go to "+url)))
                .color(TextColor.color(57, 195, 255))
                .decorate(TextDecoration.UNDERLINED);
    }

    // call as styleURL(message, null, null)
    public static Component styleURL(Component component, MutableInt index, Map<Map.Entry<Integer, Integer>, String> urls) {
        if (index == null) index = new MutableInt(0);
        if (urls == null) {
            Matcher matcher = URL_PATTERN.matcher(Util.getTextContent(component));
            urls = new HashMap<>();
            while (matcher.find()) {
                urls.put(Map.entry(matcher.start(), matcher.end()), matcher.group());
            }
        }

        List<Component> children = new ArrayList<>(component.children());
        List<TextComponent> linkChildren = new ArrayList<>();

        if (component instanceof TextComponent text) {
            component = text.content("");
            String contents = text.content();
            TextComponent building = Component.empty();
            boolean inUrl = false;

            for (char c : contents.toCharArray()) {
                for (Map.Entry<Map.Entry<Integer, Integer>, String> entry : urls.entrySet()) {
                    if (entry.getKey().getKey() <= index.getValue() && index.getValue() < entry.getKey().getValue()) {
                        if (!inUrl) {
                            linkChildren.add(building);
                            building = (TextComponent) styleURL(Component.empty(), entry.getValue());
                            inUrl = true;
                        }
                    } else if (inUrl) {
                        linkChildren.add(building);
                        building = Component.empty();
                        inUrl = false;
                    }

                    if (entry.getKey().getValue() >= index.getValue()) {
                        break;
                    }
                }

                building = building.content(building.content() + c);

                index.increment();
            }


            linkChildren.add(building);
            linkChildren.removeIf(c -> c.content().isEmpty());
        }

        for (int i = 0; i < children.size(); i++) {
            children.set(i, styleURL(children.get(i), index, urls));
        }


        children.addAll(0, linkChildren);
        return component.children(children);
    }

    public static Component getTimeStamp() {
        Calendar c = GregorianCalendar.getInstance(TIME_ZONE);

        return Component.text("%2d:%02d".formatted(c.get(Calendar.HOUR), c.get(Calendar.MINUTE))+" ").color(TextColor.color(34, 150, 155));
    }
}