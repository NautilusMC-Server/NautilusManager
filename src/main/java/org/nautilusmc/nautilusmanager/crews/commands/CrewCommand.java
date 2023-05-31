package org.nautilusmc.nautilusmanager.crews.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.crews.Crew;
import org.nautilusmc.nautilusmanager.crews.CrewHandler;
import org.nautilusmc.nautilusmanager.crews.Invite;
import org.nautilusmc.nautilusmanager.crews.WarDeclaration;
import org.nautilusmc.nautilusmanager.util.ConfirmationMessage;
import org.nautilusmc.nautilusmanager.util.PermsUtil;
import org.nautilusmc.nautilusmanager.util.Util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CrewCommand extends NautilusCommand {
    public static final String CAPTAIN_PERM_MESSAGE = "You must be a captain to use this command!";
    public static final String CREW_PERM_MESSAGE = "You must be part of a crew to use this command!";
    public static final String ALREADY_IN_CREW_MESSAGE = "You are already in a crew!";
    public static final String[] DEFAULT_COMMANDS = {
            "info",
    };
    private static final String[] NO_CREW_COMMANDS = {
            "create",
            "join",
    };
    private static final String[] CREW_MEMBER_COMMANDS = {
            "leave",
            "invite" //captains if crew closed
    };
    private static final String[] CAPTAIN_COMMANDS = {
            "delete",
            "kick",
            "makecaptain",
            "open",
            "close",
            "declarewar",
            "prefix",
            "clearprefix",
            "endwar"
    };

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            return false;
        }
        Player player = (Player) commandSender;
        if (strings.length == 0) {
            player.sendMessage(help());
            return true;
        }
        switch (strings[0]) {
            case "create" -> createCrew(player, strings);
            case "delete" -> deleteCrew(player, strings);
            case "join" -> joinCrew(player, strings);
            case "leave" -> leaveCrew(player);
            case "kick" -> kickMember(player, strings);
            case "makecaptain" -> makeCaptain(player, strings);
            case "open" -> openCrew(player);
            case "close" -> closeCrew(player);
            case "invite" -> invite(player, strings);
            case "info" -> crewInfo(player, strings);
            case "declarewar" -> declareWar(player, strings);
            case "prefix" -> setPrefix(player, strings);
            case "clearprefix" -> clearPrefix(player);
            case "endwar" -> endWar(player, strings);
            default -> player.sendMessage(help());
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;
        ArrayList<String> tabCompletions = new ArrayList<>();
        if (strings.length == 1) {
            tabCompletions.addAll(Arrays.asList(DEFAULT_COMMANDS));
            tabCompletions.add("help");
            if (player.hasPermission("group.crewmember")) {
                tabCompletions.addAll(Arrays.asList(CREW_MEMBER_COMMANDS));
            } else {
                tabCompletions.addAll(Arrays.asList(NO_CREW_COMMANDS));
            }
            if (player.hasPermission("group.captain")) {
                tabCompletions.addAll(Arrays.asList(CAPTAIN_COMMANDS));
            }
        }
        if (strings.length == 2 && !CrewHandler.getCrews().isEmpty()) {
            switch (strings[0]) {
                case "join", "info" -> tabCompletions.addAll(CrewHandler.getCrews().stream().map(Crew::getName).toList());
                case "kick", "makecaptain" -> {
                    if (CrewHandler.getCrew(player) != null) tabCompletions.addAll(CrewHandler.getCrew(player).getMembers().stream().map(Util::getName).toList());
                }
                case "invite" -> tabCompletions.addAll(getOnlineNames());
                case "delete" -> {
                    if (player.hasPermission(DELETE_OTHER_CREW_PERM)) tabCompletions.addAll(CrewHandler.getCrews().stream().map(Crew::getName).toList());
                }
                case "declarewar" -> {
                    tabCompletions.addAll(CrewHandler.getCrews().stream().map(Crew::getName).toList());
                    if (CrewHandler.getCrew(player) != null) tabCompletions.remove(CrewHandler.getCrew(player).getName());
                }
                case "endwar" -> {
                    if (CrewHandler.getCrew(player) != null) tabCompletions.addAll(CrewHandler.getCrew(player).warsAsStrings());
                }
            }
        }
        return tabCompletions.stream().filter(str->str.toLowerCase().startsWith(strings[strings.length-1].toLowerCase())).toList();
    }

    public static void createCrew(Player player, String[] strings) {
        if (!(player.hasPermission(CREATE_CREW_PERM))) {
            error(player, DEFAULT_PERM_MESSAGE);
            return;
        }
        if (CrewHandler.getCrew(player) != null) {
            error(player, ALREADY_IN_CREW_MESSAGE);
            return;
        }
        String name = getFormattedArgs(strings, 1);
        if (name.length() > 24) {
            error(player, "Crew name must be 24 characters or less!");
            return;
        }
        if (name.length() == 0) {
            error(player, "Crew name must be at least one character!");
            return;
        }
        if (!CrewHandler.getCrews().isEmpty()) {
            for (Crew crew : CrewHandler.getCrews()) {
                if (crew.getName().equals(name)) {
                    error(player, "Crew name already taken!");
                    return;
                }
            }
        }
        CrewHandler.registerCrew(new Crew(player, name));
        player.sendMessage(Component.text("Crew ").color(MAIN_COLOR)
                .append(Component.text("\"" + name + "\"").color(ACCENT_COLOR))
                .append(Component.text(" created!").color(MAIN_COLOR)));
        PermsUtil.addGroup(player, "captain");
    }

    public static void deleteCrew(Player player,String[] strings) {
        if (!(player.hasPermission(DELETE_CREW_PERM))) {
            error(player, CAPTAIN_PERM_MESSAGE);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, CREW_PERM_MESSAGE);
            return;
        }
        if (strings.length == 1) {
            ConfirmationMessage.sendConfirmationMessage(player, Component.text("delete your crew").color(NautilusCommand.MAIN_COLOR), new BukkitRunnable() {
                @Override
                public void run() {
                    //remove perms
                    PermsUtil.removeGroup(crew.getCaptain(), "captain");
                    for (OfflinePlayer p : crew.getMembers()) {
                        if (!crew.getCaptain().equals(p)) {
                            PermsUtil.removeGroup(p, "crewmember");
                        }
                    }
                    //delete crew
                    crew.sendMessageToMembers(Component.text(crew.getName()).color(ACCENT_COLOR)
                            .append(Component.text(" was deleted").color(MAIN_COLOR)));
                    CrewHandler.deleteCrew(crew);
                }
            });
        } else /*delete other*/ {
            if (!player.hasPermission(DELETE_OTHER_CREW_PERM)) {
                error(player, DEFAULT_PERM_MESSAGE);
                return;
            }
            String name = getFormattedArgs(strings, 1);
            Crew otherCrew = CrewHandler.getCrew(name);
            if (otherCrew == null) {
                error(player, "Crew does not exist!");
                return;
            }
            ConfirmationMessage.sendConfirmationMessage(player, Component.text("delete ").color(NautilusCommand.MAIN_COLOR)
                    .append(Component.text("\"" + otherCrew.getName() + "\"").color(ACCENT_COLOR)), new BukkitRunnable() {
                @Override
                public void run() {
                    //remove perms
                    PermsUtil.removeGroup(crew.getCaptain(), "captain");
                    for (OfflinePlayer p : crew.getMembers()) {
                        if (!crew.getCaptain().equals(p)) {
                            PermsUtil.removeGroup(p, "crewmember");
                        }
                    }
                    //delete crew
                    crew.sendMessageToMembers(Component.text(crew.getName()).color(ACCENT_COLOR)
                            .append(Component.text(" was deleted").color(MAIN_COLOR)));
                    CrewHandler.deleteCrew(crew);
                }
            });
        }

    }

    public static void joinCrew(Player player, String[] strings) {
        if (!(player.hasPermission(JOIN_CREW_PERM))) {
            error(player, DEFAULT_PERM_MESSAGE);
            return;
        }
        if (CrewHandler.getCrew(player) != null) {
            error(player, ALREADY_IN_CREW_MESSAGE);
            return;
        }
        if (CrewHandler.getCrew(player) != null) {
            error(player, "You are already part of a crew!");
            error(player, "\"/crew leave\" to leave your crew");
            return;
        }
        if (!CrewHandler.getCrews().isEmpty()) {
            for (Crew crew : CrewHandler.getCrews()) {
                if (crew.getName().equals(getFormattedArgs(strings, 1))) {
                    if (!crew.isOpen()) {
                        error(player, "Crew is closed to invitations only!");
                        return;
                    }
                    crew.addMember(player);
                    crew.sendMessageToMembers(Component.empty().append(player.displayName()).color(ACCENT_COLOR)
                            .append(Component.text(" joined the crew!").color(NautilusCommand.MAIN_COLOR)));
                    PermsUtil.addGroup(player, "crewmember");
                    return;
                }
            }
        }
        error(player, "Crew \"" + getFormattedArgs(strings, 1) + "\" does not exist!");
    }

    public static void leaveCrew(Player player) {
        if (!(player.hasPermission(LEAVE_CREW_PERM))) {
            error(player, CREW_PERM_MESSAGE);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, CREW_PERM_MESSAGE);
            return;
        }
        ConfirmationMessage.sendConfirmationMessage(player, Component.text("leave your crew").color(NautilusCommand.MAIN_COLOR), new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(Component.text("You left ").color(MAIN_COLOR)
                        .append(Component.text("\"" + crew.getName() + "\"").color(ACCENT_COLOR)));
                if (crew.getCaptain().equals(player)) {
                    //If Player is only member of crew
                    if (crew.getMembers().size() == 1) {
                        CrewHandler.deleteCrew(crew);
                        PermsUtil.removeGroup(player, "captain");
                    }
                    //If Player is captain -> make new captain
                    else {
                        OfflinePlayer newCaptain = crew.getMembers().get(1);
                        if (newCaptain.equals(player)) {
                            newCaptain = crew.getMembers().get(2);
                        }
                        crew.setCaptain(newCaptain);
                        crew.removeMember(player);
                        crew.sendMessageToMembers(Component.empty().append(player.displayName()).color(ACCENT_COLOR)
                                .append(Component.text(" left the crew").color(MAIN_COLOR)));
                        crew.sendMessageToMembers(Component.text(Util.getName(newCaptain)).color(ACCENT_COLOR)
                                .append(Component.text(" is now the captain of your crew").color(MAIN_COLOR)), true);
                        if (newCaptain.isOnline()) {
                            newCaptain.getPlayer().sendMessage(Component.text("You are now the captain of your crew!").color(MAIN_COLOR));
                        }
                        PermsUtil.removeGroup(player, "captain");
                        PermsUtil.removeGroup(newCaptain, "crewmember");
                        PermsUtil.addGroup(newCaptain, "captain");
                    }
                } else {
                    crew.removeMember(player);
                    PermsUtil.removeGroup(player, "crewmember");
                    crew.sendMessageToMembers(Component.empty().append(player.displayName()).color(ACCENT_COLOR)
                            .append(Component.text(" left the crew!").color(NautilusCommand.MAIN_COLOR)));
                }

            }
        });
    }

    public static void kickMember(Player player, String[] strings) {
        if (!(player.hasPermission(KICK_CREW_PERM))) {
            error(player, CAPTAIN_PERM_MESSAGE);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, CREW_PERM_MESSAGE);
            return;
        }
        if (strings.length == 1) {
            error(player, "Please provide player to kick");
            return;
        }
        String name = strings[1];
        OfflinePlayer kicked;
        if (Nickname.getPlayerFromNickname(name) != null && crew.containsPlayer(Nickname.getPlayerFromNickname(name))) {
            kicked = Nickname.getPlayerFromNickname(name);
        } else if (crew.containsPlayer(Bukkit.getOfflinePlayer(name))) {
            kicked = Bukkit.getOfflinePlayer(name);
        } else {
            error(player, "Player \"" + name + "\" is not in your crew");
            return;
        }
        if (kicked.equals(player)) {
            error(player, "You can't kick yourself out of the crew!");
            return;
        }
        ConfirmationMessage.sendConfirmationMessage(player, Component.text("kick ").color(NautilusCommand.MAIN_COLOR)
                .append(Component.text(Util.getName(kicked)).color(ACCENT_COLOR))
                .append(Component.text(" out of your crew")).color(NautilusCommand.MAIN_COLOR) , new BukkitRunnable() {
            @Override
            public void run() {
                crew.sendMessageToMembers(Component.text(Util.getName(kicked)).color(ACCENT_COLOR)
                        .append(Component.text(" was kicked from the crew!").color(NautilusCommand.MAIN_COLOR)));
                if (kicked.isOnline()) {
                    kicked.getPlayer().sendMessage(Component.text("You were kicked from ").color(MAIN_COLOR)
                            .append(Component.text(crew.getName()).color(ACCENT_COLOR)));
                }
                crew.removeMember(kicked);
                PermsUtil.removeGroup(kicked, "crewmember");
            }
        });
    }
    public static void makeCaptain(Player player, String[] strings) {
        if (!(player.hasPermission(MAKECAPTAIN_CREW_PERM))) {
            error(player, CAPTAIN_PERM_MESSAGE);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, CREW_PERM_MESSAGE);
            return;
        }
        if (strings.length == 1) {
            error(player, "Please provide player to make captain");
            return;
        }
        String name = strings[1];
        OfflinePlayer newCaptain;
        if (Nickname.getPlayerFromNickname(name) != null && crew.containsPlayer(Nickname.getPlayerFromNickname(name))) {
            newCaptain = Nickname.getPlayerFromNickname(name);
        } else if (crew.containsPlayer(Bukkit.getOfflinePlayer(name))) {
            newCaptain = Bukkit.getOfflinePlayer(name);
        } else {
            error(player, "Player \"" + name + "\" is not in your crew");
            return;
        }
        if (newCaptain.equals(player)) {
            error(player, "You're already the captain of this crew!");
            return;
        }
        ConfirmationMessage.sendConfirmationMessage(player, Component.text("make ").color(MAIN_COLOR)
                .append(Component.text(Util.getName(newCaptain)).color(ACCENT_COLOR))
                .append(Component.text(" the captain of you crew").color(MAIN_COLOR)), new BukkitRunnable() {
            @Override
            public void run() {
                crew.setCaptain(newCaptain);
                crew.sendMessageToMembers(Component.text(Util.getName(newCaptain)).color(ACCENT_COLOR)
                        .append(Component.text(" is now the captain of your crew").color(MAIN_COLOR)), true);
                if (newCaptain.isOnline()) {
                    newCaptain.getPlayer().sendMessage(Component.text("You are now the captain of your crew!").color(MAIN_COLOR));
                }
                PermsUtil.removeGroup(player, "captain");
                PermsUtil.addGroup(player, "crewmember");
                PermsUtil.removeGroup(newCaptain, "crewmember");
                PermsUtil.addGroup(newCaptain, "captain");
            }
        });
    }


    public static void openCrew(Player player) {
        if (!(player.hasPermission(OPEN_CREW_PERM))) {
            error(player, CAPTAIN_PERM_MESSAGE);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, CREW_PERM_MESSAGE);
            return;
        }
        crew.setOpen(true);
        player.sendMessage(Component.text("Crew set to open!").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
    }

    public static void closeCrew(Player player) {
        if (!(player.hasPermission(CLOSE_CREW_PERM))) {
            error(player, CAPTAIN_PERM_MESSAGE);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, CREW_PERM_MESSAGE);
            return;
        }
        crew.setOpen(false);
        player.sendMessage(Component.text("Crew set to closed!").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
    }

    public static void invite(Player player, String[] strings) {
        if (!(player.hasPermission(CREW_INVITE_PERM))) {
            error(player, CREW_PERM_MESSAGE);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, CREW_PERM_MESSAGE);
            return;
        }
        if (strings.length == 1) {
            error(player, "Please provide player to invite");
            return;
        }
        String name = strings[1];
        if (!crew.isOpen() && !(player.hasPermission(CREW_CLOSED_INVITE_PERM))) {
            error(player, CAPTAIN_PERM_MESSAGE);
            return;
        }
        Player invited = Util.getOnlinePlayer(name);
        if (invited == null) {
            invited = NautilusManager.INSTANCE.getServer().getPlayer(name);
        }
        if (invited == null) {
            error(player, "Player not online!");
            return;
        }
        if (CrewHandler.getCrew(invited) != null) {
            error(player, "Player already in crew!");
            return;
        }
        player.sendMessage(Component.text("Invited ").color(MAIN_COLOR)
                .append(Component.empty().append(invited.displayName()).color(ACCENT_COLOR))
                .append(Component.text(" to your crew").color(MAIN_COLOR)));
        Invite.sendInvite(player, invited);
    }

    public static void crewInfo(Player player, String[] args) {
        if (!(player.hasPermission(CREW_INFO_PERM))) {
            error(player, DEFAULT_PERM_MESSAGE);
            return;
        }
        if (args.length == 1) {
            Crew crew = CrewHandler.getCrew(player);
            if (crew == null) {
                error(player, CREW_PERM_MESSAGE);
                return;
            }
            player.sendMessage(crew.toComponent());
        } else {
            String name = getFormattedArgs(args, 1);
            Crew crew = CrewHandler.getCrew(name);
            if (crew == null) {
                error(player, "Crew \"" + name + "\" does not exist!");
                return;
            }
            player.sendMessage(crew.toComponent());
        }
    }
    public static void declareWar(Player player, String[] strings) {
        if (!(player.hasPermission(DECLARE_WAR_PERM))) {
            error(player, CAPTAIN_PERM_MESSAGE);
            return;
        }
        if (strings.length == 1) {
            error(player, "Please specify a crew to declare war on");
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, CREW_PERM_MESSAGE);
            return;
        }
        String name = getFormattedArgs(strings, 1);
        if (CrewHandler.getCrew(name) == null) {
            error(player, "Crew \"" + name + "\" does not exist!");
            return;
        }
        Crew other = CrewHandler.getCrew(name);
        /*if (crew.equals(other)) {
            error(player, "You cannot declare war on yourself!");
            return;
        }*/
        if (!other.getCaptain().isOnline()) {
            error(player, "The captain of \"" + name + "\" is not online!");
            return;
        }
        if (crew.isAtWarWith(other)) {
            error(player, "You are already at war with \"" + name + "\"!");
            return;
        }
        player.sendMessage(Component.text("You declared war on ").color(MAIN_COLOR)
                .append(Component.text("\"" + name + "\"").color(ACCENT_COLOR)));
        WarDeclaration.sendWarDeclaration(crew, other);
    }
    public static void endWar(Player player, String[] strings) {
        if (!(player.hasPermission(END_WAR_PERM))) {
            error(player, CAPTAIN_PERM_MESSAGE);
            return;
        }
        if (strings.length == 1) {
            error(player, "Please specify a crew to declare war on");
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, CREW_PERM_MESSAGE);
            return;
        }
        String name = getFormattedArgs(strings, 1);
        if (CrewHandler.getCrew(name) == null) {
            error(player, "Crew \"" + name + "\" does not exist!");
            return;
        }
        Crew other = CrewHandler.getCrew(name);
        if (!crew.isAtWarWith(other)) {
            error(player, "You are not at war with \"" + name + "\"!");
            return;
        }
        player.sendMessage(Component.text("You ended your war with ").color(MAIN_COLOR)
                        .append(Component.text("\"" + name + "\"").color(ACCENT_COLOR)));
        crew.sendMessageToMembers(Component.text("Your crew is no longer at war with ").color(MAIN_COLOR)
                .append(Component.text("\"" + name + "\"").color(ACCENT_COLOR)), true);
        crew.sendMessageToMembers(Component.text("\"" + name + "\"").color(ACCENT_COLOR)
                .append(Component.text(" has ended the war with your crew!").color(MAIN_COLOR)), false);
        CrewHandler.endWar(crew.getWar(other));
    }

    public static void setPrefix(Player player, String[] strings) {
        if (!(player.hasPermission(SET_PREFIX_PERM))) {
            error(player, CAPTAIN_PERM_MESSAGE);
            return;
        }
        if (strings.length == 1) {
            error(player, "Please specify a prefix to set");
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, CREW_PERM_MESSAGE);
            return;
        }
        String prefix = getFormattedArgs(strings, 1);
        if (prefix.length() < 2 || prefix.length() > 6) {
            error(player, "Prefixes must be between 2 and 6 characters");
            return;
        }
        crew.sendMessageToMembers(Component.text("Prefix changed to ").color(MAIN_COLOR)
                .append(Component.text(prefix).color(ACCENT_COLOR)));
        crew.setPrefix(prefix);
    }
    public static void clearPrefix(Player player) {
        if (!(player.hasPermission(CLEAR_PREFIX_PERM))) {
            error(player, CAPTAIN_PERM_MESSAGE);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, CREW_PERM_MESSAGE);
            return;
        }
        crew.sendMessageToMembers(Component.text("Prefix cleared").color(MAIN_COLOR));
        crew.setPrefix("");
    }
    public static Component help() {
        ArrayList<String> subcommands = new ArrayList<>();
        subcommands.addAll(Arrays.asList(DEFAULT_COMMANDS));
        subcommands.addAll(Arrays.asList(NO_CREW_COMMANDS));
        subcommands.addAll(Arrays.asList(CREW_MEMBER_COMMANDS));
        subcommands.addAll(Arrays.asList(CAPTAIN_COMMANDS));
        Component out = Component.text("--------------------------------").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR).appendNewline();
        for (String subcommand : subcommands) {
            out = out.append(getSubcommandUsage(subcommand)).appendNewline() ;
        }
        out = out.append(Component.text("--------------------------------").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
        return out;
    }
    private static Component getSubcommandUsage(String subcommand) {
        return switch (subcommand) {
            case "create" -> Component.text("/crew create <name>").color(ACCENT_COLOR)
                    .append(Component.text(" - Creates a new crew with specified name").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "delete" -> Component.text("/crew delete").color(ACCENT_COLOR)
                    .append(Component.text(" - Deletes your crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "join" -> Component.text("/crew join <name>").color(ACCENT_COLOR)
                    .append(Component.text(" - Joins a crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "leave" -> Component.text("/crew leave <name>").color(ACCENT_COLOR)
                    .append(Component.text(" - Leaves your crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "kick" -> Component.text("/crew kick <player>").color(ACCENT_COLOR)
                    .append(Component.text(" - Kicks a player from your crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "makecaptain" -> Component.text("/crew makecaptain <player>").color(ACCENT_COLOR)
                    .append(Component.text(" - Makes specified player captian of your crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "open" -> Component.text("/crew open").color(ACCENT_COLOR)
                    .append(Component.text(" Opens crew for anyone to join").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "close" -> Component.text("/crew close").color(ACCENT_COLOR)
                    .append(Component.text(" Closes crew to invite only").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "invite" -> Component.text("/crew invite").color(ACCENT_COLOR)
                    .append(Component.text(" - Invites a player to your crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "info" -> Component.text("/crew info <name>").color(ACCENT_COLOR)
                    .append(Component.text(" - Lists crew information").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "declarewar" -> Component.text("/crew declarewar <crew>").color(ACCENT_COLOR)
                    .append(Component.text(" - Sends a war declaration to another crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "endwar" -> Component.text("/crew endwar <crew>").color(ACCENT_COLOR)
                    .append(Component.text(" - Ends a war with another crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "prefix" -> Component.text("/crew setprefix <prefix>").color(ACCENT_COLOR)
                    .append(Component.text(" - Sets a prefix for your crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "clearprefix" -> Component.text("/crew clearprefix").color(ACCENT_COLOR)
                    .append(Component.text(" - Clears your crew's prefix").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            default -> Component.text("You shouldn't see this").color(ERROR_COLOR);
        };
    }

}
