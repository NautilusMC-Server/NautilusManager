package org.nautilusmc.nautilusmanager.discord;

import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.JDAImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.cosmetics.commands.MsgCommand;
import org.nautilusmc.nautilusmanager.events.MessageStyler;
import org.nautilusmc.nautilusmanager.util.CaseInsensitiveString;
import org.nautilusmc.nautilusmanager.util.Util;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DiscordBot implements Listener {

    private static final TextColor DISCORD_COLOR = TextColor.color(85,98,234);
    private static final TextColor DISCORD_OUTER_COLOR = TextColor.color(138, 143, 255);

    private static final String PLAYER_LIST_BUTTON_ID = "playerlist";

    private static JDA jda;

    public static void init() {
        try {
            jda = JDABuilder
                    .createDefault(NautilusManager.INSTANCE.getConfig().getString("discord.token"), GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
                    .setActivity(Activity.playing("play.nautilusmc.org"))
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .disableCache(CacheFlag.VOICE_STATE, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                    .setEventManager(new AnnotatedEventManager())
                    .addEventListeners(new DiscordBot())
                    .build();
        } catch (InvalidTokenException | IllegalArgumentException e) {
            NautilusManager.INSTANCE.getLogger().warning("Invalid Discord token! Discord integration will be disabled");
            jda = null;

            return;
        }

        jda.updateCommands().addCommands(
                Commands.slash("msg", "Send a player a private message")
                        .addOption(OptionType.STRING, "player", "The player to send the message to", true, true)
                        .addOption(OptionType.STRING, "message", "The message to send", true)
                        .setGuildOnly(true)
        ).queue();

        Bukkit.getScheduler().runTaskTimer(NautilusManager.INSTANCE, DiscordBot::updateStatusMessage, 0, NautilusManager.INSTANCE.getConfig().getLong("discord.status_update_interval") * 20);
    }

    public static void unload() {
        if (jda == null) return;

        jda.shutdownNow();
    }

    private static void updateStatusMessage() {
        TextChannel statusChannel = jda.getTextChannelById(NautilusManager.INSTANCE.getConfig().getLong("discord.status_channel"));

        MessageEmbed embed = new EmbedBuilder()
                .setColor(new Color(27, 143, 169, 255))
                .setTitle("Welcome to NautilusMC")
                .setThumbnail("https://cdn.discordapp.com/icons/1100956444494401646/b255f5dea85238b811f8734fb4149302.webp")
                .setDescription("A Minecraft server for everyone!")
                .addField("Player Count", Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers(), true)
                .addField("Version", "Survival " + Bukkit.getMinecraftVersion(), true)
                .setFooter("play.nautilusmc.org")
                .setTimestamp(Instant.now())
                .build();

        MessageHistory.getHistoryFromBeginning(statusChannel).queue(history-> {
            Optional<Message> message = history.getRetrievedHistory()
                    .stream()
                    .filter(m->m.getAuthor() == jda.getSelfUser()).findFirst();

            if (message.isPresent()) {
                message.get().editMessageEmbeds(embed).queue();
            } else {

                statusChannel.sendMessage(new MessageCreateBuilder()
                        .addEmbeds(embed)
                        .addComponents(ActionRow.of(
                                Button.primary(PLAYER_LIST_BUTTON_ID, "Get Online Players")))
                        .build()).queue();
            }
        });
    }

    @SubscribeEvent
    public void onButton(ButtonInteractionEvent e) {
        if (e.getComponentId().equals(PLAYER_LIST_BUTTON_ID)) {
            e.replyEmbeds(new EmbedBuilder()
                        .setTitle("Online Players")
                        .setColor(new Color(17, 90, 107))
                        .setDescription(Bukkit.getOnlinePlayers().isEmpty() ? "No one is online :cry:" :
                            Bukkit.getOnlinePlayers().stream()
                            .map(p-> {
                                String nick = Util.getName(p);
                                return nick + (!nick.equals(p.getName()) ? " *(" + p.getName() + ")*" : "");
                            })
                            .sorted()
                            .collect(Collectors.joining("\n")))
                        .build())
                    .setEphemeral(true)
                    .queue();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        updateStatusMessage();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        updateStatusMessage();
    }

    public static Component discordToBukkit(Member member, String message) {
        if (!member.getRoles().contains(getSponsorRole())) {
            return Component.text(message);
        }

        Component component = Component.empty();
        TextComponent building = Component.empty();

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            char prev = i > 0 ? message.charAt(i-1) : 0;
            char next = i < message.length()-1 ? message.charAt(i+1) : 0;

            TextDecoration decoration = null;
            if (c == '*') {
                decoration = TextDecoration.ITALIC;
                i++;

                if (next == '*') {
                    decoration = TextDecoration.BOLD;
                    i++;
                }
            } else if (c == '_' && next == '_') {
                decoration = TextDecoration.UNDERLINED;
                i += 2;
            } else if (c == '~' && next == '~') {
                decoration = TextDecoration.STRIKETHROUGH;
                i += 2;
            }

            if (decoration != null) {
                if (prev == '\\') {
                    building = building.content(building.content().substring(0, building.content().length()-1)+c);
                    continue;
                }
                component = component.append(building);
                building = (TextComponent) Util.toggleDecoration(Component.empty().style(building.style()), decoration);
            } else {
                building = building.content(building.content()+c);
            }
        }

        return component.append(building);
    }

    private static void parseBukkitMsg(Component component, List<Component> parents, List<Map.Entry<Character, List<TextDecoration>>> output) {
        TextDecoration[] possibleDecorations = new TextDecoration[] {
                TextDecoration.BOLD,
                TextDecoration.ITALIC,
                TextDecoration.UNDERLINED,
                TextDecoration.STRIKETHROUGH
        };

        if (component instanceof TextComponent text) {
            List<TextDecoration> decorations = Arrays.stream(possibleDecorations)
                    .filter(t->text.hasDecoration(t) || parents.stream().anyMatch(c->c.hasDecoration(t)))
                    .toList();

            for (char c : text.content().toCharArray()) {
                output.add(Map.entry(c, decorations));
            }
        }

        for (Component child : component.children()) {
            List<Component> newParents = new ArrayList<>(parents);
            newParents.add(component);

            parseBukkitMsg(child, newParents, output);
        }
    }

    private static void formatBukkitMentions(List<Map.Entry<Character, List<TextDecoration>>> message) {
        Guild guild = jda.getGuildById(NautilusManager.INSTANCE.getConfig().getLong("discord.guild"));

        Map<CaseInsensitiveString, Member> members = guild.getMemberCache()
                .stream()
                .collect(Collectors.toMap(m->new CaseInsensitiveString(m.getEffectiveName()), m->m, (a,b)->a));
        Map<CaseInsensitiveString, Role> roles = guild.getRoles().stream()
                .collect(Collectors.toMap(r->new CaseInsensitiveString(r.getName()), r->r, (a,b)->a));
        Map<CaseInsensitiveString, GuildChannel> channels = guild.getChannels().stream()
                .collect(Collectors.toMap(c->new CaseInsensitiveString(c.getName()), c->c, (a,b)->a));

        int maxMemberLength = members.keySet().stream()
                .mapToInt(s->s.string.length())
                .max()
                .orElse(0);
        int maxRoleLength = roles.keySet().stream()
                .mapToInt(s->s.string.length())
                .max()
                .orElse(0);
        int maxAtLength = Math.max(maxMemberLength, maxRoleLength);

        int maxChannelLength = channels.keySet().stream()
                .mapToInt(s->s.string.length())
                .max()
                .orElse(0);

        String messageString = message.stream()
                .map(Map.Entry::getKey)
                .map(String::valueOf)
                .collect(Collectors.joining());

        // TODO: make this better w/ less copied code
        for (int i = 0; i < message.size(); i++) {
            char c = message.get(i).getKey();

            if (c == '@') {
                for (int k = 1; k < maxAtLength && i+k < messageString.length(); k++) {
                    CaseInsensitiveString name = new CaseInsensitiveString(messageString.substring(i+1, i+k+1));

                    if (members.containsKey(name)) {
                        for (int j = 0; j <= k; j++) message.remove(i);
                        String mention = members.get(name).getAsMention();

                        for (int x = 0; x < mention.length(); x++) {
                            message.add(i+x, Map.entry(mention.charAt(x), List.of()));
                        }

                        messageString = messageString.substring(0, i) + mention + messageString.substring(i+k+1);

                        i += mention.length()-1;
                        break;
                    } else if (roles.containsKey(name)) {
                        for (int j = 0; j <= k; j++) message.remove(i);
                        String mention = roles.get(name).getAsMention();

                        for (int x = 0; x < mention.length(); x++) {
                            message.add(i+x, Map.entry(mention.charAt(x), List.of()));
                        }

                        messageString = messageString.substring(0, i) + mention + messageString.substring(i+k+1);

                        i += mention.length()-1;
                        break;
                    }
                }
            } else if (c == '#') {
                for (int k = 1; k < maxChannelLength && i+k < messageString.length(); k++) {
                    CaseInsensitiveString name = new CaseInsensitiveString(messageString.substring(i+1, i+k+1));

                    if (channels.containsKey(name)) {
                        for (int j = 0; j <= k; j++) message.remove(i);
                        String mention = channels.get(name).getAsMention();

                        for (int x = 0; x < mention.length(); x++) {
                            message.add(i+x, Map.entry(mention.charAt(x), List.of()));
                        }

                        messageString = messageString.substring(0, i) + mention + messageString.substring(i+k+1);

                        i += mention.length()-1;
                        break;
                    }
                }
            }
        }
    }

    public static String bukkitToDiscord(Component component) {
        List<Map.Entry<Character, List<TextDecoration>>> msg = new ArrayList<>();
        parseBukkitMsg(component, List.of(), msg);
        formatBukkitMentions(msg);

        String building = "";
        List<TextDecoration> activeDecorations = new ArrayList<>();

        for (Map.Entry<Character, List<TextDecoration>> entry : msg) {
            char c = entry.getKey();
            List<TextDecoration> decorations = entry.getValue();

            List<TextDecoration> toRemove = activeDecorations.stream().filter(d->!decorations.contains(d)).toList();
            List<TextDecoration> toAdd = decorations.stream().filter(d->!activeDecorations.contains(d)).toList();

            activeDecorations.removeAll(toRemove);
            activeDecorations.addAll(toAdd);

            // TODO: deal with "``boldh``reset``italici" case
            building += getFormatting(toRemove, false);
            building += getFormatting(toAdd, true);
            building += c;
        }

        return building + getFormatting(activeDecorations, false);
    }

    private static String getFormatting(List<TextDecoration> decorations, boolean forward) {
        StringBuilder out = new StringBuilder();

        for (int i = forward ? 0 : decorations.size()-1; forward ? i < decorations.size() : i >= 0; i += forward ? 1 : -1) {
            switch (decorations.get(i)) {
                case BOLD -> out.append("**");
                case ITALIC -> out.append("*");
                case UNDERLINED -> out.append("__");
                case STRIKETHROUGH -> out.append("~~");
            }

        }

        return out.toString();
    }

    public static Role getSponsorRole() {
        return jda.getRoleById(NautilusManager.INSTANCE.getConfig().getLong("discord.sponsor_role"));
    }

    private static boolean inChannelAndGuild(Channel channel) {
        return channel.getIdLong() == NautilusManager.INSTANCE.getConfig().getLong("discord.synced_channel") &&
                channel instanceof GuildChannel guild && guild.getGuild().getIdLong() == NautilusManager.INSTANCE.getConfig().getLong("discord.guild");
    }

    private static Component getDisplayName(Member user) {
        return Component.empty()
                .append(Component.text("[").color(DISCORD_OUTER_COLOR))
                .append(Component.text("Discord").color(DISCORD_COLOR))
                .append(Component.text("] ").color(DISCORD_OUTER_COLOR))
                .append(Component.text(user.getEffectiveName()).color(TextColor.color(user.getColorRaw())));
    }

    @SubscribeEvent
    public void onMessage(MessageReceivedEvent e) {
        if (inChannelAndGuild(e.getChannel()) && e.getAuthor().getIdLong() != jda.getSelfUser().getIdLong()) {
            boolean hasPermission = e.getMember().getRoles().contains(getSponsorRole());
            Component bukkitMessage = discordToBukkit(e.getMember(), e.getMessage().getContentDisplay());
            Component displayName = getDisplayName(e.getMember());

            // TODO: maybe have a way for ppl to block discord users too but for now just send to all players
            MessageStyler.sendMessageAsUser(displayName, hasPermission, bukkitMessage, Bukkit.getOnlinePlayers());
            e.getMessage().delete().queue(ignore->sendMinecraftMsgInDiscord(displayName, bukkitMessage));
        }
    }

    @SubscribeEvent
    public void onAutoComplete(CommandAutoCompleteInteractionEvent e) {
        if (inChannelAndGuild(e.getChannel())) {
            if (e.getName().equals("msg")) {
                if (e.getFocusedOption().getName().equals("player")) {
                    String value = e.getFocusedOption().getValue();
                    e.replyChoiceStrings(Bukkit.getOnlinePlayers().stream()
                            .map(Util::getName)
                            .filter(s->s.toLowerCase().startsWith(value.toLowerCase()))
                            .toList()
                    ).queue();
                }
            }
        } else {
            e.replyChoices().queue();
        }
    }

    @SubscribeEvent
    public void onSlashCommand(SlashCommandInteractionEvent e) {
        if (inChannelAndGuild(e.getChannel())) {
            if (e.getName().equals("msg")) {
                String player = e.getOption("player").getAsString();
                String message = e.getOption("message").getAsString();

                Component msg = discordToBukkit(e.getMember(), message)
                        .decorate(TextDecoration.ITALIC);

                Player bukkitPlayer = Util.getOnlinePlayer(player);
                if (bukkitPlayer != null) {
                    Map.Entry<Component, Component> messages = MsgCommand.styleWhisper(
                            getDisplayName(e.getMember()),
                            bukkitPlayer.displayName(),
                            msg);
                    bukkitPlayer.sendMessage(messages.getValue());
                    e.reply("*You whispered to **%s** »* %s".formatted(
                            Util.getName(bukkitPlayer), bukkitToDiscord(msg))).setEphemeral(true).queue();
                } else {
                    e.reply("Player not found").setEphemeral(true).queue();
                }
            }
        } else {
            e.reply("This command can only be used in <#%d>"
                    .formatted(NautilusManager.INSTANCE.getConfig().getLong("discord.synced_channel")))
                    .setEphemeral(true).queue();
        }
    }

    public static void onMessage(Player player, Component message) {
        if (jda == null) return;

        sendMinecraftMsgInDiscord(player, message);
    }

    private static void sendMinecraftMsgInDiscord(Player player, Component message) {
        sendMinecraftMsgInDiscord(player.displayName(), MessageStyler.formatUserMessage(player, message));
    }

    private static void sendMinecraftMsgInDiscord(Component displayName, Component message) {
        message = Component.empty()
                .append(displayName.decorate(TextDecoration.BOLD))
                .append(Component.text(" » "))
                .append(message);

        jda.getTextChannelById(NautilusManager.INSTANCE.getConfig().getLong("discord.synced_channel"))
                .sendMessage(bukkitToDiscord(message)).queue();
    }
}
