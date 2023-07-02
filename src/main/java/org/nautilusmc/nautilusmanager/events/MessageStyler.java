package org.nautilusmc.nautilusmanager.events;

import com.deepl.api.DeepLException;
import com.deepl.api.Translator;
import com.google.common.collect.EvictingQueue;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;
import org.apache.commons.lang3.mutable.MutableInt;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
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
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.commands.SuicideCommand;
import org.nautilusmc.nautilusmanager.cosmetics.MuteManager;
import org.nautilusmc.nautilusmanager.discord.DiscordBot;
import org.nautilusmc.nautilusmanager.gui.page.GuiPage;
import org.nautilusmc.nautilusmanager.util.Emoji;
import org.nautilusmc.nautilusmanager.util.FancyText;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("UnstableApiUsage")
public class MessageStyler implements Listener {
    public static final TextColor DEFAULT_COLOR = TextColor.color(200, 200, 200);
    public static final TextColor DARKER_COLOR = TextColor.color(150, 150, 150);
    public static final TextColor URL_COLOR = TextColor.color(57, 195, 255);

    public static final Component STATUS_SEPARATOR = Component.text(" | ", DARKER_COLOR);
    public static final Component MESSAGE_SEPARATOR = Component.text(" » ", DARKER_COLOR);

    public static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)");
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public static EvictingQueue<Component> runningMessages = EvictingQueue.create(50);
    public static HashMap<Player, String> languages = new HashMap<>(); //player, language code

    public static Translator translator = new Translator("4e426403-9add-46cc-5fba-daa5bc78e602:fx");

    private static TranslatableComponent styleTranslatable(TranslatableComponent translatable) {
        List<Component> args = new ArrayList<>(translatable.args());

        for (int i = 0; i < args.size(); i++) {
            if (!(args.get(i) instanceof TextComponent component)) continue;

            Player player = Bukkit.getPlayerExact(component.content());

            if (player != null) {
                args.set(i, player.displayName()
                        .clickEvent(component.clickEvent())
                        .hoverEvent(component.hoverEvent()));
            }
        }

        return translatable.args(args);
    }

    private static Component replace(Component component, String target, String replace) {
        if (component instanceof TextComponent text) {
            return text.content(text.content().replace(target, replace));
        } else {
            return component.children(component.children().stream().map(child -> replace(child, target, replace)).toList());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        if (!(e.deathMessage() instanceof TranslatableComponent originalMessage)) return;

        Component deathMessageContent = originalMessage.key().equals(SuicideCommand.SUICIDE_TRANSLATION_KEY)
                ? Component.empty()
                    .append(e.getPlayer().displayName())
                    .append(Component.text(" took the easy way out"))
                : styleTranslatable(originalMessage);

        Component deathMessage = Component.empty()
                .append(getTimeStamp())
                .appendSpace()
                .append(Component.text(Emoji.SKULL + " ")
                        .append(Component.text("Death").decorate(TextDecoration.BOLD))
                        .color(NamedTextColor.AQUA))
                .append(STATUS_SEPARATOR)
                .append(deathMessageContent);

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

        Bukkit.getScheduler().runTaskLater(NautilusManager.getPlugin(), () -> {
            nms.connection.send(new ClientboundPlayerCombatKillPacket(nms.getId(), PaperAdventure.asVanilla(deathMessageContent)), PacketSendListener.exceptionallySend(() -> {
                // TODO: do something with this? currently just copying nms
                String s = nmsMessage.getString(256);
                MutableComponent hover = net.minecraft.network.chat.Component.translatable("death.attack.message_too_long", net.minecraft.network.chat.Component.literal(s).withStyle(ChatFormatting.YELLOW));
                MutableComponent newComp = net.minecraft.network.chat.Component.translatable("death.attack.even_more_magic", PaperAdventure.asVanilla(e.getPlayer().displayName())).withStyle((chatmodifier) -> chatmodifier.withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, hover)));
                return new ClientboundPlayerCombatKillPacket(nms.getId(), newComp);
            }));
        }, 1);

        runningMessages.add(deathMessage);
        e.deathMessage(null);
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent e) {
        if (e.message() == null) return;

        e.message(Component.empty()
                .append(getTimeStamp())
                .appendSpace()
                .append(Component.text(Emoji.CHECK + " ")
                        .append(Component.text("Advancement")/*.decorate(TextDecoration.BOLD)*/)
                        .color(NamedTextColor.GREEN))
                .append(STATUS_SEPARATOR)
                .append(styleTranslatable((TranslatableComponent) e.message())));
        runningMessages.add(e.message());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Location deathLocation = e.getPlayer().getLocation();
        e.getPlayer().sendMessage(Component.text(
                "Your death coordinates: (%d, %d)"
                        .formatted(Math.round(deathLocation.getX()), Math.round(deathLocation.getZ())),
                DARKER_COLOR,
                TextDecoration.BOLD
        ));
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
                .append(Component.text("You are banned from NautilusMC. If this is a mistake, please contact a staff member on Discord.", Command.ERROR_COLOR, TextDecoration.BOLD))
                .appendNewline()
                .appendNewline()
                .append(Component.text("Reason: ", Command.INFO_COLOR))
                .append(Component.text(Objects.requireNonNullElse(entry.getReason(), "None given")))
                .appendNewline()
                .append(Component.text("Expires: ", Command.INFO_COLOR))
                .append(Component.text(entry.getExpiration() == null ? "Never" : DATE_FORMAT.format(entry.getExpiration())))
                .color(Command.ERROR_COLOR);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        if (e.joinMessage() == null) return;

        // send the packets to the joining player for all the online players' name tags
        for (Player p : Bukkit.getOnlinePlayers()) {
            Util.updateNameTag(p, p.displayName(), List.of(e.getPlayer()));
        }
        Util.updateNameTag(e.getPlayer(),e.getPlayer().displayName(), Bukkit.getOnlinePlayers());

        e.joinMessage(Component.empty()
                .append(getTimeStamp())
                .appendSpace()
                .append(Component.text("Join", NamedTextColor.GREEN))
                .append(STATUS_SEPARATOR)
                .append(e.getPlayer().displayName()));

        runningMessages.forEach(message -> e.getPlayer().sendMessage(message));
        runningMessages.add(e.joinMessage());

        Component obfuscation = Component.text("x", NautilusManager.WORDMARK_LIGHT_COLOR, TextDecoration.OBFUSCATED);
        Component welcomeMessage = Component.empty()
                .append(obfuscation)
                .append(Component.text(" Feel welcome at ", DEFAULT_COLOR))
                .append(NautilusManager.WORDMARK)
                .append(Component.text(", ", DEFAULT_COLOR))
                .append(e.getPlayer().displayName())
                .append(Component.text("! ", DEFAULT_COLOR))
                .append(obfuscation);
        Bukkit.getScheduler().runTaskLater(NautilusManager.getPlugin(), () -> e.getPlayer().sendMessage(welcomeMessage), 1);

        languages.put(e.getPlayer(), "en-US");
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (e.quitMessage() == null) return;

        e.quitMessage(Component.empty()
                .append(getTimeStamp())
                .appendSpace()
                .append(Component.text("Quit", NamedTextColor.RED))
                .append(STATUS_SEPARATOR)
                .append(e.getPlayer().displayName()));

        runningMessages.add(e.quitMessage());
        languages.remove(e.getPlayer());
    }

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent e) {
        if (e.getInventory().getHolder() instanceof GuiPage) return;

        String renameText = e.getInventory().getRenameText();
        ItemStack result = e.getResult();

        if (e.getView().getPlayer().hasPermission(Permission.USE_CHAT_FORMATTING.toString()) && renameText != null && !renameText.isEmpty() && result != null) {
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

    public static void sendMessageAsUser(Player sender, Component message) {
        DiscordBot.onMessage(sender, message);

        sendMessageAsUser(sender, message, Bukkit.getOnlinePlayers().stream()
                        .filter(recipient -> !MuteManager.isMuted(recipient, sender))
                        .toList());
    }

    public static void sendMessageAsUser(Player sender, Component message, Collection<? extends Player> recipients) {
        sendMessageAsUser(sender.displayName(), sender.hasPermission(Permission.USE_CHAT_FORMATTING.toString()), message, recipients, languages.get(sender));
    }

    public static void sendMessageAsUser(Component senderName, boolean applyFormatting, Component message, Collection<? extends Player> recipients) {
        sendMessageAsUser(senderName, applyFormatting, message, recipients, null);
    }

    public static void sendMessageAsUser(Component senderName, boolean applyFormatting, Component message, Collection<? extends Player> recipients, String language) {
        // language : translated message
        Map<String, Component> translations = new HashMap<>();

        if (language != null && !language.equalsIgnoreCase("en-US")) {
            translations.put("en-US", compileUserMessage(senderName, translate(Util.getTextContent(message), "en-US"), applyFormatting));
        } else {
            translations.put("en-US", compileUserMessage(senderName, Util.getTextContent(message), applyFormatting));
        }

        for (Player recipient : recipients) {
            String recipientLanguage = languages.get(recipient);
            if (recipientLanguage.equalsIgnoreCase("en-US") || translations.containsKey(recipientLanguage)) continue;
            // FIXME: i am certain this won't work with formatting codes
            translations.put(recipientLanguage, compileUserMessage(senderName, translate(Emoji.parseText(Util.getTextContent(message)), recipientLanguage), applyFormatting));
        }

        Bukkit.getServer().getConsoleSender().sendMessage(translations.get("en-US"));
        for (Player recipient : recipients) {
            recipient.sendMessage(translations.get(languages.get(recipient)));
        }
        runningMessages.add(translations.get("en-US"));
    }

    public static Component compileUserMessage(Component displayName, String message, boolean applyFormatting) {
        return Component.empty()
                .append(getTimeStamp())
                .appendSpace()
                .append(displayName)
                .append(MESSAGE_SEPARATOR)
                .append(formatUserMessage(Component.text(message), applyFormatting).colorIfAbsent(DEFAULT_COLOR))
                .clickEvent(ClickEvent.copyToClipboard(message));
    }

    public static Component formatUserMessage(CommandSender sender, Component message) {
        return formatUserMessage(message, sender.hasPermission(Permission.USE_CHAT_FORMATTING.toString()));
    }

    public static Component formatUserMessage(Component message, boolean withChatFormatting) {
        // convert emojis
        message = Component.text(Emoji.parseText(Util.getTextContent(message)));
        // apply formatting tags
        if (withChatFormatting) {
            message = FancyText.parseChatFormatting(Util.getTextContent(message));
        }
        // detect URLs
        return styleURL(message);
    }

    public static Component styleOutgoingWhisper(Component recipientName, Component message) {
        return Component.empty()
                .append(getTimeStamp())
                .appendSpace()
                .append(Component.text("You whispered to ", DARKER_COLOR))
                .append(recipientName)
                .append(MESSAGE_SEPARATOR)
                .append(message)
                .decorate(TextDecoration.ITALIC);
    }

    public static Component styleIncomingWhisper(Component senderName, Component message) {
        return Component.empty()
                .append(getTimeStamp())
                .appendSpace()
                .append(senderName)
                .append(Component.text(" whispered to you", DARKER_COLOR))
                .append(MESSAGE_SEPARATOR)
                .append(message)
                .decorate(TextDecoration.ITALIC);
    }

    public static Component styleURL(Component component, String url) {
        return component.clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(HoverEvent.showText(Component.text("Go to ")
                        .append(Component.text(url, URL_COLOR, TextDecoration.UNDERLINED))))
                .color(URL_COLOR)
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

    public static String getLanguage(Player player) {
        return languages.get(player);
    }

    public static void setLanguage(Player player, String key) {
        languages.put(player, key);
    }

    public static String translate(String message, String to) {
        try {
            return translator.translateText(message, null, to).getText();
        } catch (DeepLException | InterruptedException e) {
            e.printStackTrace();
            return message;
        }
    }

    public static Component getTimeStamp() {
        Calendar calendar = Util.getCalendar();
        int hour = Math.floorMod(calendar.get(Calendar.HOUR) - 1, 12) + 1;
        int minute = calendar.get(Calendar.MINUTE);

        return Component.text("%2d:%02d".formatted(hour, minute), NamedTextColor.DARK_AQUA)
                .decoration(TextDecoration.BOLD, false)
                .decoration(TextDecoration.ITALIC, false);
    }
}