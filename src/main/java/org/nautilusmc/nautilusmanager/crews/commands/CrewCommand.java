package org.nautilusmc.nautilusmanager.crews.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.crews.Crew;
import org.nautilusmc.nautilusmanager.crews.CrewHandler;
import org.nautilusmc.nautilusmanager.crews.Invite;
import org.nautilusmc.nautilusmanager.crews.WarDeclaration;
import org.nautilusmc.nautilusmanager.util.ConfirmationMessage;
import org.nautilusmc.nautilusmanager.util.Util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CrewCommand extends Command {
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
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Command.NOT_PLAYER_ERROR);
            return true;
        }

        if (strings.length < 1) {
            player.sendMessage(getHelpMessage());
            return true;
        }

        return switch (strings[0]) {
            case "create" -> createCrew(player, strings);
            case "delete" -> deleteCrew(player, strings);
            case "join" -> joinCrew(player, strings);
            case "leave" -> { leaveCrew(player); yield true; }
            case "kick" -> kickMember(player, strings);
            case "makecaptain" -> makeCaptain(player, strings);
            case "open" -> { openCrew(player); yield true; }
            case "close" -> { closeCrew(player); yield true; }
            case "invite" -> invite(player, strings);
            case "info" -> crewInfo(player, strings);
            case "declarewar" -> declareWar(player, strings);
            case "prefix" -> setPrefix(player, strings);
            case "clearprefix" -> { clearPrefix(player); yield true; }
            case "endwar" -> endWar(player, strings);
            default -> { player.sendMessage(getHelpMessage()); yield true; }
        };
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> tabCompletions = new ArrayList<>();

        if (!(commandSender instanceof Player player)) return tabCompletions;

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
        } else if (strings.length == 2 && !CrewHandler.CREWS.isEmpty()) {
            switch (strings[0]) {
                case "join", "info" -> tabCompletions.addAll(CrewHandler.CREWS.stream().map(Crew::getName).toList());
                case "kick", "makecaptain" -> {
                    if (CrewHandler.getCrew(player) != null) tabCompletions.addAll(CrewHandler.getCrew(player).getMembers().stream().map(Util::getName).toList());
                }
                case "invite" -> tabCompletions.addAll(getOnlineNames());
                case "delete" -> {
                    if (player.hasPermission(Command.Permission.DELETE_OTHER_CREWS)) tabCompletions.addAll(CrewHandler.CREWS.stream().map(Crew::getName).toList());
                }
                case "declarewar" -> {
                    tabCompletions.addAll(CrewHandler.CREWS.stream().map(Crew::getName).toList());
                    if (CrewHandler.getCrew(player) != null) tabCompletions.remove(CrewHandler.getCrew(player).getName());
                }
                case "endwar" -> {
                    if (CrewHandler.getCrew(player) != null) tabCompletions.addAll(CrewHandler.getCrew(player).warsAsStrings());
                }
            }
        }

        return tabCompletions.stream().filter(str->str.toLowerCase().startsWith(strings[strings.length-1].toLowerCase())).toList();
    }

    public static boolean createCrew(Player player, String[] strings) {
        if (!(player.hasPermission(Command.Permission.CREATE_CREW))) {
            player.sendMessage(Command.NO_PERMISSION_ERROR);
            return true;
        }
        if (CrewHandler.getCrew(player) != null) {
            player.sendMessage(Command.ALREADY_IN_CREW_ERROR);
            return true;
        }
        String name = getMessageFromArgs(strings, 1);
        if (name.length() > Crew.MAX_NAME_LENGTH) {
            player.sendMessage(Component.text("That crew name is too long! Maximum length is " + Crew.MAX_NAME_LENGTH + ".").color(Default.ERROR_COLOR));
            return true;
        }
        if (name.length() < Crew.MIN_NAME_LENGTH) {
            player.sendMessage(Component.text("That crew name is too short! Minimum length is " + Crew.MIN_NAME_LENGTH + ".").color(Default.ERROR_COLOR));
            return true;
        }
        if (!CrewHandler.CREWS.isEmpty()) {
            for (Crew crew : CrewHandler.CREWS) {
                if (crew.getName().equals(name)) {
                    player.sendMessage(Component.text("That crew name is already taken!").color(Default.ERROR_COLOR));
                    return true;
                }
            }
        }
        CrewHandler.registerCrew(new Crew(player, name));
        player.sendMessage(Component.text("Crew ").color(Default.INFO_COLOR)
                .append(Component.text("\"" + name + "\"").color(Default.INFO_ACCENT_COLOR))
                .append(Component.text(" created!").color(Default.INFO_COLOR)));
        org.nautilusmc.nautilusmanager.util.Permission.addGroup(player, "captain");
        return true;
    }

    public static boolean deleteCrew(Player player, String[] strings) {
        if (!(player.hasPermission(Command.Permission.DELETE_CREW))) {
            player.sendMessage(Command.NOT_CAPTAIN_ERROR);
            return true;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(Command.NOT_IN_CREW_ERROR);
            return true;
        }
        if (strings.length < 2) {
            ConfirmationMessage.sendConfirmationMessage(player, Component.text("delete your crew").color(Command.Default.INFO_COLOR), new BukkitRunnable() {
                @Override
                public void run() {
                    //remove perms
                    org.nautilusmc.nautilusmanager.util.Permission.removeGroup(crew.getCaptain(), "captain");
                    for (OfflinePlayer p : crew.getMembers()) {
                        if (!crew.getCaptain().equals(p)) {
                            org.nautilusmc.nautilusmanager.util.Permission.removeGroup(p, "crewmember");
                        }
                    }
                    //delete crew
                    crew.sendMessageToMembers(Component.text(crew.getName()).color(Default.INFO_ACCENT_COLOR)
                            .append(Component.text(" was deleted").color(Default.INFO_COLOR)));
                    CrewHandler.deleteCrew(crew);
                }
            });
        } else {
            if (!player.hasPermission(Command.Permission.DELETE_OTHER_CREWS)) {
                player.sendMessage(Command.NO_PERMISSION_ERROR);
                return true;
            }
            String name = getMessageFromArgs(strings, 1);
            Crew otherCrew = CrewHandler.getCrew(name);
            if (otherCrew == null) {
                player.sendMessage(Component.text("Crew does not exist!").color(Default.ERROR_COLOR));
                return true;
            }
            ConfirmationMessage.sendConfirmationMessage(player, Component.text("delete ").color(Command.Default.INFO_COLOR)
                    .append(Component.text("\"" + otherCrew.getName() + "\"").color(Default.INFO_ACCENT_COLOR)), new BukkitRunnable() {
                @Override
                public void run() {
                    //remove perms
                    org.nautilusmc.nautilusmanager.util.Permission.removeGroup(crew.getCaptain(), "captain");
                    for (OfflinePlayer member : crew.getMembers()) {
                        if (!crew.getCaptain().equals(member)) {
                            org.nautilusmc.nautilusmanager.util.Permission.removeGroup(member, "crewmember");
                        }
                    }
                    //delete crew
                    crew.sendMessageToMembers(Component.text(crew.getName()).color(Default.INFO_ACCENT_COLOR)
                            .append(Component.text(" was deleted").color(Default.INFO_COLOR)));
                    CrewHandler.deleteCrew(crew);
                }
            });
        }
        return true;
    }

    public static boolean joinCrew(Player player, String[] strings) {
        if (!(player.hasPermission(Command.Permission.JOIN_CREW))) {
            player.sendMessage(Command.NO_PERMISSION_ERROR);
            return true;
        }
        if (CrewHandler.getCrew(player) != null) {
            player.sendMessage(Command.ALREADY_IN_CREW_ERROR);
            return true;
        }
        if (strings.length < 2) {
            return false;
        }
        if (CrewHandler.getCrew(player) != null) {
            player.sendMessage(Component.text("You are already part of a crew!").color(Default.ERROR_COLOR));
            player.sendMessage(Component.text("/crew leave").color(Default.ERROR_ACCENT_COLOR)
                    .append(Component.text(" to leave your crew").color(Default.ERROR_COLOR)));
            return true;
        }
        if (!CrewHandler.CREWS.isEmpty()) {
            for (Crew crew : CrewHandler.CREWS) {
                if (crew.getName().equals(getMessageFromArgs(strings, 1))) {
                    if (!crew.isOpen()) {
                        player.sendMessage(Component.text("This crew can only be joined by invitation!").color(Default.ERROR_COLOR));
                        return true;
                    }
                    crew.addMember(player);
                    crew.sendMessageToMembers(Component.empty().append(player.displayName()).color(Default.INFO_ACCENT_COLOR)
                            .append(Component.text(" joined the crew!").color(Command.Default.INFO_COLOR)));
                    org.nautilusmc.nautilusmanager.util.Permission.addGroup(player, "crewmember");
                    return true;
                }
            }
        }
        player.sendMessage(Component.text("Crew \"" + getMessageFromArgs(strings, 1) + "\" does not exist!").color(Default.ERROR_COLOR));
        return true;
    }

    public static void leaveCrew(Player player) {
        if (!(player.hasPermission(Command.Permission.LEAVE_CREW))) {
            player.sendMessage(Command.NOT_IN_CREW_ERROR);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(Command.NOT_IN_CREW_ERROR);
            return;
        }
        ConfirmationMessage.sendConfirmationMessage(player, Component.text("leave your crew").color(Command.Default.INFO_COLOR), new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(Component.text("You left ").color(Default.INFO_COLOR)
                        .append(Component.text("\"" + crew.getName() + "\"").color(Default.INFO_ACCENT_COLOR)));
                if (crew.getCaptain().equals(player)) {
                    //If Player is only member of crew
                    if (crew.getMembers().size() == 1) {
                        CrewHandler.deleteCrew(crew);
                        org.nautilusmc.nautilusmanager.util.Permission.removeGroup(player, "captain");
                    }
                    //If Player is captain -> make new captain
                    else {
                        OfflinePlayer newCaptain = crew.getMembers().get(1);
                        if (newCaptain.equals(player)) {
                            newCaptain = crew.getMembers().get(2);
                        }
                        crew.setCaptain(newCaptain);
                        crew.removeMember(player);
                        crew.sendMessageToMembers(Component.empty().append(player.displayName()).color(Default.INFO_ACCENT_COLOR)
                                .append(Component.text(" left the crew").color(Default.INFO_COLOR)));
                        crew.sendMessageToMembers(Component.text(Util.getName(newCaptain)).color(Default.INFO_ACCENT_COLOR)
                                .append(Component.text(" is now the captain of your crew").color(Default.INFO_COLOR)), true);
                        if (newCaptain.isOnline()) {
                            newCaptain.getPlayer().sendMessage(Component.text("You are now the captain of your crew!").color(Default.INFO_COLOR));
                        }
                        org.nautilusmc.nautilusmanager.util.Permission.removeGroup(player, "captain");
                        org.nautilusmc.nautilusmanager.util.Permission.removeGroup(newCaptain, "crewmember");
                        org.nautilusmc.nautilusmanager.util.Permission.addGroup(newCaptain, "captain");
                    }
                } else {
                    crew.removeMember(player);
                    org.nautilusmc.nautilusmanager.util.Permission.removeGroup(player, "crewmember");
                    crew.sendMessageToMembers(Component.empty().append(player.displayName()).color(Default.INFO_ACCENT_COLOR)
                            .append(Component.text(" left the crew!").color(Command.Default.INFO_COLOR)));
                }

            }
        });
    }

    public static boolean kickMember(Player player, String[] strings) {
        if (!(player.hasPermission(Command.Permission.KICK_CREWMATES))) {
            player.sendMessage(Command.NOT_CAPTAIN_ERROR);
            return true;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(Command.NOT_IN_CREW_ERROR);
            return true;
        }
        if (strings.length < 2) {
            return false;
        }
        String name = strings[1];
        OfflinePlayer kicked;
        if (Nickname.getPlayerFromNickname(name) != null && crew.containsPlayer(Nickname.getPlayerFromNickname(name))) {
            kicked = Nickname.getPlayerFromNickname(name);
        } else if (crew.containsPlayer(Bukkit.getOfflinePlayer(name))) {
            kicked = Bukkit.getOfflinePlayer(name);
        } else {
            player.sendMessage(Component.text("Player \"" + name + "\" is not in your crew!").color(Default.ERROR_COLOR));
            return true;
        }
        if (Objects.equals(kicked, player)) {
            player.sendMessage(Component.text("You can't kick yourself out of the crew!").color(Default.ERROR_COLOR));
            return true;
        }
        ConfirmationMessage.sendConfirmationMessage(player, Component.text("kick ").color(Command.Default.INFO_COLOR)
                .append(Component.empty().append(Util.getName(kicked)).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text(" out of your crew")).color(Command.Default.INFO_COLOR) , new BukkitRunnable() {
            @Override
            public void run() {
                crew.sendMessageToMembers(Component.text(Util.getName(kicked)).color(Default.INFO_ACCENT_COLOR)
                        .append(Component.text(" was kicked from the crew!").color(Command.Default.INFO_COLOR)));
                if (kicked.isOnline()) {
                    kicked.getPlayer().sendMessage(Component.text("You were kicked from ").color(Default.INFO_COLOR)
                            .append(Component.text(crew.getName()).color(Default.INFO_ACCENT_COLOR)));
                }
                crew.removeMember(kicked);
                org.nautilusmc.nautilusmanager.util.Permission.removeGroup(kicked, "crewmember");
            }
        });
        return true;
    }

    public static boolean makeCaptain(Player player, String[] strings) {
        if (!(player.hasPermission(Command.Permission.MAKE_CAPTAIN))) {
            player.sendMessage(Command.NOT_CAPTAIN_ERROR);
            return true;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(Command.NOT_IN_CREW_ERROR);
            return true;
        }
        if (strings.length < 2) {
            return false;
        }
        String name = strings[1];
        OfflinePlayer newCaptain;
        if (Nickname.getPlayerFromNickname(name) != null && crew.containsPlayer(Nickname.getPlayerFromNickname(name))) {
            newCaptain = Nickname.getPlayerFromNickname(name);
        } else if (crew.containsPlayer(Bukkit.getOfflinePlayer(name))) {
            newCaptain = Bukkit.getOfflinePlayer(name);
        } else {
            player.sendMessage(Component.text("Player \"" + name + "\" is not in your crew").color(Default.ERROR_COLOR));
            return true;
        }
        if (newCaptain.equals(player)) {
            error(player, "You're already the captain of this crew!");
            return;
        }
        ConfirmationMessage.sendConfirmationMessage(player, Component.text("make ").color(Default.INFO_COLOR)
                .append(Component.text(Util.getName(newCaptain)).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text(" the captain of you crew").color(Default.INFO_COLOR)), new BukkitRunnable() {
            @Override
            public void run() {
                crew.setCaptain(newCaptain);
                crew.sendMessageToMembers(Component.text(Util.getName(newCaptain)).color(Default.INFO_ACCENT_COLOR)
                        .append(Component.text(" is now the captain of your crew").color(Default.INFO_COLOR)), true);
                if (newCaptain.isOnline()) {
                    newCaptain.getPlayer().sendMessage(Component.text("You are now the captain of your crew!").color(Default.INFO_COLOR));
                }
                org.nautilusmc.nautilusmanager.util.Permission.removeGroup(player, "captain");
                org.nautilusmc.nautilusmanager.util.Permission.addGroup(player, "crewmember");
                org.nautilusmc.nautilusmanager.util.Permission.removeGroup(newCaptain, "crewmember");
                org.nautilusmc.nautilusmanager.util.Permission.addGroup(newCaptain, "captain");
            }
        });
        return true;
    }

    public static void openCrew(Player player) {
        if (!(player.hasPermission(Command.Permission.OPEN_CREW))) {
            player.sendMessage(Command.NOT_CAPTAIN_ERROR);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(Command.NOT_IN_CREW_ERROR);
            return;
        }
        crew.setOpen(true);
        player.sendMessage(Component.text("Crew set to open!").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
    }

    public static void closeCrew(Player player) {
        if (!(player.hasPermission(Command.Permission.CLOSE_CREW))) {
            player.sendMessage(Command.NOT_CAPTAIN_ERROR);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(Command.NOT_IN_CREW_ERROR);
            return;
        }
        crew.setOpen(false);
        player.sendMessage(Component.text("Crew set to closed!").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
    }

    public static boolean invite(Player player, String[] strings) {
        if (!(player.hasPermission(Command.Permission.INVITE_TO_CREW))) {
            player.sendMessage(Command.NO_PERMISSION_ERROR);
            return true;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(Command.NOT_IN_CREW_ERROR);
            return true;
        }
        if (strings.length < 2) {
            return false;
        }
        String name = strings[1];
        if (!crew.isOpen() && !(player.hasPermission(Command.Permission.CLOSED_INVITE_TO_CREW))) {
            player.sendMessage(Command.NOT_CAPTAIN_ERROR);
            return true;
        }
        Player invited = Util.getOnlinePlayer(name);
        if (invited == null) {
            invited = NautilusManager.INSTANCE.getServer().getPlayer(name);
        }
        if (invited == null) {
            player.sendMessage(Component.text("Player not online!").color(Default.ERROR_COLOR));
            return true;
        }
        if (CrewHandler.getCrew(invited) != null) {
            player.sendMessage(Component.text("Player already in crew!").color(Default.ERROR_COLOR));
            return true;
        }
        player.sendMessage(Component.text("Invited ").color(Default.INFO_COLOR)
                .append(Component.empty().append(invited.displayName()).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text(" to your crew").color(Default.INFO_COLOR)));
        Invite.sendInvite(player, invited);
        return true;
    }

    public static boolean crewInfo(Player player, String[] args) {
        if (!(player.hasPermission(Command.Permission.CREW_INFO))) {
            player.sendMessage(Command.NO_PERMISSION_ERROR);
            return true;
        }
        if (args.length == 1) {
            Crew crew = CrewHandler.getCrew(player);
            if (crew == null) {
                player.sendMessage(Command.NOT_IN_CREW_ERROR);
                return true;
            }
            player.sendMessage(crew.toComponent());
        } else {
            String name = getMessageFromArgs(args, 1);
            Crew crew = CrewHandler.getCrew(name);
            if (crew == null) {
                player.sendMessage(Component.text("Crew \"" + name + "\" does not exist!").color(Default.ERROR_COLOR));
                return true;
            }
            player.sendMessage(crew.toComponent());
        }
        return true;
    }

    public static boolean declareWar(Player player, String[] strings) {
        if (!(player.hasPermission(Command.Permission.DECLARE_WAR))) {
            player.sendMessage(Command.NOT_CAPTAIN_ERROR);
            return true;
        }
        if (strings.length < 2) {
            return false;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(Command.NOT_IN_CREW_ERROR);
            return true;
        }
        String name = getMessageFromArgs(strings, 1);
        if (CrewHandler.getCrew(name) == null) {
            player.sendMessage(Component.text("Crew \"" + name + "\" does not exist!").color(Default.ERROR_COLOR));
            return true;
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
        player.sendMessage(Component.text("You declared war on ").color(Default.INFO_COLOR)
                .append(Component.text("\"" + name + "\"").color(Default.INFO_ACCENT_COLOR)));
        return true;
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
        String name = getMessageFromArgs(strings, 1);
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

    public static boolean setPrefix(Player player, String[] strings) {
        if (!(player.hasPermission(Command.Permission.SET_CREW_PREFIX))) {
            player.sendMessage(Command.NOT_CAPTAIN_ERROR);
            return true;
        }
        if (strings.length < 2) {
            return false;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(Command.NOT_IN_CREW_ERROR);
            return true;
        }
        String prefix = getMessageFromArgs(strings, 1);
        if (prefix.length() < Crew.MIN_PREFIX_LENGTH || Crew.MAX_PREFIX_LENGTH < prefix.length()) {
            player.sendMessage(Component.text("Prefixes must be from " + Crew.MIN_PREFIX_LENGTH + "-" + Crew.MAX_PREFIX_LENGTH + " characters long!").color(Default.ERROR_COLOR));
            return true;
        }
        crew.sendMessageToMembers(Component.text("Prefix changed to ")
                .append(Component.text(prefix).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(Default.INFO_COLOR));
        crew.setPrefix(prefix);
        return true;
    }

    public static void clearPrefix(Player player) {
        if (!(player.hasPermission(Command.Permission.CLEAR_CREW_PREFIX))) {
            player.sendMessage(Command.NOT_CAPTAIN_ERROR);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(Command.NOT_IN_CREW_ERROR);
            return;
        }
        crew.sendMessageToMembers(Component.text("Prefix cleared.").color(Default.INFO_COLOR));
        crew.setPrefix("");
    }

    public static Component getHelpMessage() {
        ArrayList<String> subcommands = new ArrayList<>();
        subcommands.addAll(Arrays.asList(DEFAULT_COMMANDS));
        subcommands.addAll(Arrays.asList(NO_CREW_COMMANDS));
        subcommands.addAll(Arrays.asList(CREW_MEMBER_COMMANDS));
        subcommands.addAll(Arrays.asList(CAPTAIN_COMMANDS));
        Component out = Component.text("--------------------------------").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR).appendNewline();
        for (String subcommand : subcommands) {
            out = out.append(getSubcommandUsage(subcommand)).appendNewline();
        }
        out = out.append(Component.text("--------------------------------").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
        return out;
    }

    private static Component getSubcommandUsage(String subcommand) {
        return switch (subcommand) {
            case "create" -> Component.text("/crew create <name>").color(Default.INFO_ACCENT_COLOR)
                    .append(Component.text(" - Creates a new crew with specified name").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "delete" -> Component.text("/crew delete").color(Default.INFO_ACCENT_COLOR)
                    .append(Component.text(" - Deletes your crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "join" -> Component.text("/crew join <name>").color(Default.INFO_ACCENT_COLOR)
                    .append(Component.text(" - Joins a crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "leave" -> Component.text("/crew leave <name>").color(Default.INFO_ACCENT_COLOR)
                    .append(Component.text(" - Leaves your crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "kick" -> Component.text("/crew kick <player>").color(Default.INFO_ACCENT_COLOR)
                    .append(Component.text(" - Kicks a player from your crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "makecaptain" -> Component.text("/crew makecaptain <player>").color(Default.INFO_ACCENT_COLOR)
                    .append(Component.text(" - Makes specified player captian of your crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "open" -> Component.text("/crew open").color(Default.INFO_ACCENT_COLOR)
                    .append(Component.text(" Opens crew for anyone to join").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "close" -> Component.text("/crew close").color(Default.INFO_ACCENT_COLOR)
                    .append(Component.text(" Closes crew to invite only").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "invite" -> Component.text("/crew invite").color(Default.INFO_ACCENT_COLOR)
                    .append(Component.text(" - Invites a player to your crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "info" -> Component.text("/crew info <name>").color(Default.INFO_ACCENT_COLOR)
                    .append(Component.text(" - Lists crew information").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "declarewar" -> Component.text("/crew declarewar <crew>").color(Default.INFO_ACCENT_COLOR)
                    .append(Component.text(" - Sends a war declaration to another crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "endwar" -> Component.text("/crew endwar <crew>").color(ACCENT_COLOR)
                    .append(Component.text(" - Ends a war with another crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "prefix" -> Component.text("/crew setprefix <prefix>").color(Default.INFO_ACCENT_COLOR)
                    .append(Component.text(" - Sets a prefix for your crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            case "clearprefix" -> Component.text("/crew clearprefix").color(Default.INFO_ACCENT_COLOR)
                    .append(Component.text(" - Clears your crew's prefix").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            default -> Component.text("You shouldn't see this!").color(Default.ERROR_COLOR);
        };
    }

}
