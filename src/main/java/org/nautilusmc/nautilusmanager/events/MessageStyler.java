package org.nautilusmc.nautilusmanager.events;

import com.deepl.api.DeepLException;
import com.deepl.api.TextResult;
import com.deepl.api.Translator;
import com.google.common.collect.EvictingQueue;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
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
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.commands.SuicideCommand;
import org.nautilusmc.nautilusmanager.cosmetics.MuteManager;
import org.nautilusmc.nautilusmanager.discord.DiscordBot;
import org.nautilusmc.nautilusmanager.gui.page.GuiPage;
import org.nautilusmc.nautilusmanager.util.Emoji;
import org.nautilusmc.nautilusmanager.util.FancyText;
import org.nautilusmc.nautilusmanager.util.Util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("UnstableApiUsage")
public class MessageStyler implements Listener {

    public static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)");
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public static EvictingQueue<Component> runningMessages = EvictingQueue.create(50);
    public static HashMap<Player, String> languages = new HashMap<>(); //player, language code

    public static Translator translator = new Translator("4e426403-9add-46cc-5fba-daa5bc78e602:fx");

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
                .append(Component.text(Emoji.SKULL.getRaw())
                        .append(Component.text(" Death").decorate(TextDecoration.BOLD))
                        .color(TextColor.color(46, 230, 255)))
                .append(Component.text(" | ")
                        .color(TextColor.color(87, 87, 87)))
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

        Bukkit.getScheduler().runTaskLater(NautilusManager.INSTANCE, () -> {
            nms.connection.send(new ClientboundPlayerCombatKillPacket(nms.getCombatTracker(), PaperAdventure.asVanilla(styledDeathMessage)), PacketSendListener.exceptionallySend(() -> {
                // TODO: do something with this? currently just copying nms
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

    @EventHandler
    public void onTryLogin(AsyncPlayerPreLoginEvent e) {
        BanEntry banEntry = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(e.getName());
        if (banEntry == null) return;

        Date expiry = banEntry.getExpiration();
        if (expiry != null && new Date().after(expiry)) {
            Bukkit.getBanList(BanList.Type.NAME).pardon(e.getName());
        } else {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, getBanMessage(banEntry));
        }
    }

    public static Component getBanMessage(BanEntry entry) {
        return Component.empty()
                .append(Component.text("You are banned from NautilusMC. If this is a mistake, please contact a staff member on Discord.")
                        .color(TextColor.color(199, 9, 22))
                        .decorate(TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Reason: ")
                        .color(NautilusCommand.MAIN_COLOR))
                .append(Component.text(entry.getReason()))
                .append(Component.newline())
                .append(Component.text("Expires: ")
                        .color(NautilusCommand.MAIN_COLOR))
                .append(Component.text(entry.getExpiration() == null ? "Never" : DATE_FORMAT.format(entry.getExpiration())))
                .color(NautilusCommand.ERROR_COLOR);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        if (e.joinMessage() == null) return;

        // send the packets to the joining player for all the online players' name tags
        for (Player p : Bukkit.getOnlinePlayers()) {
            Util.updateNameTag(p, p.displayName(), List.of(e.getPlayer()));
        }
        Util.updateNameTag(e.getPlayer(),e.getPlayer().displayName(), Bukkit.getOnlinePlayers());

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

        languages.put(e.getPlayer(), "en-US");
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (e.quitMessage() == null) return;

        e.quitMessage(Component.empty()
                .append(Component.text("Quit").color(TextColor.color(255, 58, 30)))
                .append(Component.text(" | ").color(TextColor.color(87, 87, 87)))
                .append(e.getPlayer().displayName()));

        runningMessages.add(e.quitMessage());
        languages.remove(e.getPlayer());
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
        DiscordBot.onMessage(player, message);

        sendMessageAsUser(
                player.displayName(),
                player.hasPermission(NautilusCommand.CHAT_FORMATTING_PERM),
                message,
                Bukkit.getOnlinePlayers().stream().filter(p->!MuteManager.isMuted(p, player)).toList(),
                player);
    }

    public static void sendMessageAsUser(Component displayName, boolean withChatFormatting, Component message, Collection<? extends Player> recipients, Player ... sender) {
        HashMap<String, Component> translations = new HashMap<>(); //language code, translated message

        if(sender != null && !languages.get(sender[0]).equalsIgnoreCase("en-US")) { //if sender is not using English
            translations.put("en-US", compileUserMessage(translate(Util.getTextContent(message), "en-US"), displayName, withChatFormatting));
        }
        else translations.put("en-US", compileUserMessage(Util.getTextContent(message), displayName, withChatFormatting));

        for(Player p : recipients) {
            String code = languages.get(p);
            if(code.equalsIgnoreCase("en")) return; //already translated or in English
            if(translations.containsKey(code)) return; //already translated
            translations.put(code, compileUserMessage(translate(Util.getTextContent(message), code), displayName, withChatFormatting));
        }
        Bukkit.getLogger().info(Util.getTextContent(translations.get("en")));

        recipients.forEach(r->r.sendMessage(translations.get(languages.get(r))));
        runningMessages.add(translations.get("en-US"));
    }

    public static Component compileUserMessage(String message, Component displayName, boolean withChatFormatting) {

        return Component.empty()
                .append(getTimeStamp())
                .append(Component.text(" "))
                .append(displayName)
                .append(Component.text(" » ").color(TextColor.color(150, 150, 150)))
                .append(formatUserMessage(withChatFormatting, Component.text(message)).color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR))
                .clickEvent(ClickEvent.copyToClipboard(message));
    }

    public static Component formatUserMessage(CommandSender player, Component message) {
        return formatUserMessage(player.hasPermission(NautilusCommand.CHAT_FORMATTING_PERM), message);
    }

    public static Component formatUserMessage(boolean withChatFormatting, Component message) {
        // convert emojis
        message = Component.text(Emoji.parseText(Util.getTextContent(message)));
        // apply formatting tags
        if (withChatFormatting) {
            message = FancyText.parseChatFormatting(Util.getTextContent(message));
        }
        // detect URLs
        return styleURL(message);
    }

    public static Component styleURL(Component component, String url) {
        return component.clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text("Go to " + url)))
                .color(TextColor.color(57, 195, 255))
                .decorate(TextDecoration.UNDERLINED);
    }

    public static Component styleURL(Component component) {
        Matcher matcher = URL_PATTERN.matcher(Util.getTextContent(component));
        Map<Map.Entry<Integer, Integer>, String> urls = new HashMap<>();
        while (matcher.find()) {
            urls.put(Map.entry(matcher.start(), matcher.end()), matcher.group());
        }
        return styleURL(component, new MutableInt(0), urls);
    }

    private static Component styleURL(Component component, @NotNull MutableInt index, @NotNull Map<Map.Entry<Integer, Integer>, String> urls) {
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
            linkChildren.removeIf(child -> child.content().isEmpty());
        }

        children.replaceAll(child -> styleURL(child, index, urls));

        children.addAll(0, linkChildren);
        return component.children(children);
    }

    public static void setLanguage(Player player, String code) {
        languages.put(player, code);
    }

    public static String translate(String message, String to) {
        TextResult result;
        try {
            result = translator.translateText(message, null, to);
        } catch (DeepLException | InterruptedException e) {
            e.printStackTrace();
            return message;
        }
        return result.getText();
    }

    public static Component getTimeStamp() {
        Calendar calendar = Util.getCalendar();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return Component.text("%2d:%02d".formatted(hour, minute))
                .color(TextColor.color(34, 150, 155));
    }
}