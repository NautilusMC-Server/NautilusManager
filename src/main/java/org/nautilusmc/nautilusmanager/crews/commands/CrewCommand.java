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
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.crews.Crew;
import org.nautilusmc.nautilusmanager.crews.CrewHandler;
import org.nautilusmc.nautilusmanager.crews.Invite;
import org.nautilusmc.nautilusmanager.crews.WarDeclaration;
import org.nautilusmc.nautilusmanager.util.ConfirmationMessage;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CrewCommand extends Command {
    public static final Component NOT_IN_CREW_ERROR = Component.text("You must be part of a crew to use that command!", ERROR_COLOR);
    public static final Component NOT_CAPTAIN_ERROR = Component.text("You must be a captain to use that command!", ERROR_COLOR);
    public static final Component ALREADY_IN_CREW_ERROR = Component.text("You are already in a crew!", ERROR_COLOR);
    
    public static final String[] DEFAULT_COMMANDS = {
            "help",
            "info",
    };
    private static final String[] NO_CREW_COMMANDS = {
            "create",
            "join",
    };
    private static final String[] CREW_MEMBER_COMMANDS = {
            "leave",
            "invite", //captains if crew closed
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
            "endwar",
    };

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(NOT_PLAYER_ERROR);
            return true;
        }

        if (args.length < 1 || !switch (args[0]) {
            case "create" -> createCrew(player, args);
            case "delete" -> deleteCrew(player, args);
            case "join" -> joinCrew(player, args);
            case "leave" -> leaveCrew(player);
            case "kick" -> kickMember(player, args);
            case "makecaptain" -> makeCaptain(player, args);
            case "open" -> openCrew(player);
            case "close" -> closeCrew(player);
            case "invite" -> invite(player, args);
            case "info" -> crewInfo(player, args);
            case "declarewar" -> declareWar(player, args);
            case "prefix" -> setPrefix(player, args);
            case "clearprefix" -> clearPrefix(player);
            case "endwar" -> endWar(player, args);
            default -> sendHelpMessage(player);
        }) {
            sendHelpMessage(player);
        }

        return true;
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        ArrayList<String> out = new ArrayList<>();

        if (!(sender instanceof Player player)) return out;

        if (args.length == 1) {
            out.addAll(Arrays.asList(DEFAULT_COMMANDS));
            if (CrewHandler.isCrewMember(player)) {
                out.addAll(Arrays.asList(CREW_MEMBER_COMMANDS));
            } else {
                out.addAll(Arrays.asList(NO_CREW_COMMANDS));
            }
            if (CrewHandler.isCaptain(player)) {
                out.addAll(Arrays.asList(CAPTAIN_COMMANDS));
            }
        } else if (args.length == 2 && !CrewHandler.getCrews().isEmpty()) {
            switch (args[0]) {
                case "join", "info" -> out.addAll(CrewHandler.getCrews().stream().map(Crew::getName).toList());
                case "kick", "makecaptain" -> {
                    if (CrewHandler.getCrew(player) != null) {
                        out.addAll(CrewHandler.getCrew(player).getMembers().stream().map(Util::getName).toList());
                    }
                }
                case "invite" -> out.addAll(getOnlineNames());
                case "delete" -> {
                    if (player.hasPermission(Permission.DELETE_OTHER_CREWS.toString())) {
                        out.addAll(CrewHandler.getCrews().stream().map(Crew::getName).toList());
                    }
                }
                case "declarewar" -> {
                    out.addAll(CrewHandler.getCrews().stream().map(Crew::getName).toList());
                    if (CrewHandler.getCrew(player) != null) {
                        out.remove(CrewHandler.getCrew(player).getName());
                    }
                }
                case "endwar" -> {
                    if (CrewHandler.getCrew(player) != null) {
                        out.addAll(CrewHandler.getCrew(player).warsAsStrings());
                    }
                }
            }
        }

        return out;
    }

    public static boolean createCrew(Player player, String[] args) {
        if (!player.hasPermission(Permission.CREATE_CREW.toString())) {
            player.sendMessage(NO_PERMISSION_ERROR);
            return true;
        }

        if (CrewHandler.getCrew(player) != null) {
            player.sendMessage(ALREADY_IN_CREW_ERROR);
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        String name = getMessageFromArgs(args, 1);
        if (name.length() > Crew.MAX_NAME_LENGTH) {
            player.sendMessage(Component.text("That crew name is too long! Maximum length is " + Crew.MAX_NAME_LENGTH + ".").color(ERROR_COLOR));
            return true;
        }
        if (name.length() < Crew.MIN_NAME_LENGTH) {
            player.sendMessage(Component.text("That crew name is too short! Minimum length is " + Crew.MIN_NAME_LENGTH + ".").color(ERROR_COLOR));
            return true;
        }
        for (Crew crew : CrewHandler.getCrews()) {
            if (crew.getName().equals(name)) {
                player.sendMessage(Component.text("That crew name is already taken!").color(ERROR_COLOR));
                return true;
            }
        }
        CrewHandler.registerCrew(new Crew(player, name));
        Permission.removeGroup(player, "crewmember");
        Permission.addGroup(player, "captain");
        player.sendMessage(Component.text("Crew created successfully! You are now the captain of ")
                .append(Component.text(name, INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(INFO_COLOR));

        return true;
    }

    public static boolean deleteCrew(Player player, String[] args) {
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(NOT_IN_CREW_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.DELETE_CREW.toString())) {
            player.sendMessage(NOT_CAPTAIN_ERROR);
            return true;
        }

        if (args.length < 2) {
            ConfirmationMessage.sendConfirmationMessage(player, Component.text("delete your crew").color(INFO_COLOR), new BukkitRunnable() {
                @Override
                public void run() {
                    //remove perms
                    Permission.removeGroup(crew.getCaptain(), "captain");
                    for (OfflinePlayer member : crew.getMembers()) {
                        if (!crew.getCaptain().equals(member)) {
                            Permission.removeGroup(member, "crewmember");
                        }
                    }
                    //delete crew
                    crew.sendMessageToMembers(Component.text(crew.getName()).color(INFO_ACCENT_COLOR)
                            .append(Component.text(" was deleted.").color(INFO_COLOR)));
                    CrewHandler.deleteCrew(crew);
                }
            });
        } else {
            if (!player.hasPermission(Permission.DELETE_OTHER_CREWS.toString())) {
                player.sendMessage(NO_PERMISSION_ERROR);
                return true;
            }

            String name = getMessageFromArgs(args, 1);
            Crew otherCrew = CrewHandler.getCrew(name);
            if (otherCrew == null) {
                player.sendMessage(Component.text("Crew does not exist!").color(ERROR_COLOR));
                return true;
            }

            ConfirmationMessage.sendConfirmationMessage(player, Component.text("delete ").color(INFO_COLOR)
                    .append(Component.text("\"" + otherCrew.getName() + "\"").color(INFO_ACCENT_COLOR)), new BukkitRunnable() {
                @Override
                public void run() {
                    //remove perms
                    Permission.removeGroup(crew.getCaptain(), "captain");
                    for (OfflinePlayer member : crew.getMembers()) {
                        if (!crew.getCaptain().equals(member)) {
                            Permission.removeGroup(member, "crewmember");
                        }
                    }
                    //delete crew
                    crew.sendMessageToMembers(Component.text(crew.getName()).color(INFO_ACCENT_COLOR)
                            .append(Component.text(" was deleted").color(INFO_COLOR)));
                    CrewHandler.deleteCrew(crew);
                }
            });
        }

        return true;
    }

    public static boolean joinCrew(Player player, String[] args) {
        if (!player.hasPermission(Permission.JOIN_CREW.toString())) {
            player.sendMessage(NO_PERMISSION_ERROR);
            return true;
        }

        if (CrewHandler.getCrew(player) != null) {
            player.sendMessage(ALREADY_IN_CREW_ERROR);
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        if (CrewHandler.getCrew(player) != null) {
            player.sendMessage(Component.text("You are already part of a crew!").color(ERROR_COLOR));
            player.sendMessage(Component.text("/crew leave").color(ERROR_ACCENT_COLOR)
                    .append(Component.text(" to leave your crew").color(ERROR_COLOR)));
            return true;
        }
        String crewName = getMessageFromArgs(args, 1);
        for (Crew crew : CrewHandler.getCrews()) {
            if (crew.getName().equals(crewName)) {
                if (!crew.isOpen()) {
                    player.sendMessage(Component.text("This crew can only be joined by invitation!").color(ERROR_COLOR));
                    return true;
                }
                crew.addMember(player);
                crew.sendMessageToMembers(Component.empty().append(player.displayName()).color(INFO_ACCENT_COLOR)
                        .append(Component.text(" joined the crew!").color(INFO_COLOR)));
                Permission.addGroup(player, "crewmember");
                return true;
            }
        }
        player.sendMessage(Component.text("Crew \"" + crewName + "\" does not exist!", ERROR_COLOR));

        return true;
    }

    public static boolean leaveCrew(Player player) {
        if (!player.hasPermission(Permission.LEAVE_CREW.toString())) {
            player.sendMessage(NOT_IN_CREW_ERROR);
            return true;
        }

        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(NOT_IN_CREW_ERROR);
            return true;
        }

        ConfirmationMessage.sendConfirmationMessage(player, Component.text("leave your crew").color(INFO_COLOR), new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(Component.text("You left ").color(INFO_COLOR)
                        .append(Component.text("\"" + crew.getName() + "\"").color(INFO_ACCENT_COLOR)));
                if (crew.getCaptain().equals(player)) {
                    if (crew.getMembers().size() == 1) {
                        // they were the last member of the crew, so clean up after them
                        CrewHandler.deleteCrew(crew);
                        Permission.removeGroup(player, "captain");
                    } else {
                        // find a new captain to take their place
                        OfflinePlayer newCaptain = crew.getMembers().get(0);
                        if (newCaptain.equals(player)) {
                            newCaptain = crew.getMembers().get(1);
                        }
                        crew.setCaptain(newCaptain);
                        crew.removeMember(player);
                        crew.sendMessageToMembers(Component.empty().append(player.displayName()).color(INFO_ACCENT_COLOR)
                                .append(Component.text(" left the crew.").color(INFO_COLOR)));
                        crew.sendMessageToMembers(Component.text(Util.getName(newCaptain)).color(INFO_ACCENT_COLOR)
                                .append(Component.text(" is now the captain of your crew.").color(INFO_COLOR)), true);
                        if (newCaptain.isOnline()) {
                            newCaptain.getPlayer().sendMessage(Component.text("You are now the captain of your crew!").color(INFO_COLOR));
                        }
                        Permission.removeGroup(player, "captain");
                        Permission.removeGroup(newCaptain, "crewmember");
                        Permission.addGroup(newCaptain, "captain");
                        crew.setCaptain(newCaptain);
                    }
                } else {
                    crew.removeMember(player);
                    Permission.removeGroup(player, "crewmember");
                    crew.sendMessageToMembers(Component.empty().append(player.displayName()).color(INFO_ACCENT_COLOR)
                            .append(Component.text(" left the crew.").color(INFO_COLOR)));
                }
            }
        });

        return true;
    }

    public static boolean kickMember(Player player, String[] args) {
        if (!player.hasPermission(Permission.KICK_CREWMATES.toString())) {
            player.sendMessage(NOT_CAPTAIN_ERROR);
            return true;
        }

        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(NOT_IN_CREW_ERROR);
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        String name = args[1];
        OfflinePlayer kicked;
        if (Nickname.getPlayerFromNickname(name) != null && crew.containsPlayer(Nickname.getPlayerFromNickname(name))) {
            kicked = Nickname.getPlayerFromNickname(name);
        } else if (crew.containsPlayer(Bukkit.getOfflinePlayer(name))) {
            kicked = Bukkit.getOfflinePlayer(name);
        } else {
            player.sendMessage(Component.text("Player \"" + name + "\" is not in your crew!").color(ERROR_COLOR));
            return true;
        }
        if (Objects.equals(kicked, player)) {
            player.sendMessage(Component.text("You can't kick yourself out of the crew!").color(ERROR_COLOR));
            return true;
        }
        ConfirmationMessage.sendConfirmationMessage(player, Component.text("kick ").color(INFO_COLOR)
                .append(Component.empty().append(Component.text(Util.getName(kicked))).color(INFO_ACCENT_COLOR))
                .append(Component.text(" out of your crew")).color(INFO_COLOR) , new BukkitRunnable() {
            @Override
            public void run() {
                crew.sendMessageToMembers(Component.text(Util.getName(kicked)).color(INFO_ACCENT_COLOR)
                        .append(Component.text(" was kicked from the crew!").color(INFO_COLOR)));
                if (kicked.isOnline()) {
                    kicked.getPlayer().sendMessage(Component.text("You were kicked from ").color(INFO_COLOR)
                            .append(Component.text(crew.getName()).color(INFO_ACCENT_COLOR)));
                }
                crew.removeMember(kicked);
                Permission.removeGroup(kicked, "crewmember");
            }
        });

        return true;
    }

    public static boolean makeCaptain(Player player, String[] args) {
        if (!player.hasPermission(Permission.MAKE_CAPTAIN.toString())) {
            player.sendMessage(NOT_CAPTAIN_ERROR);
            return true;
        }

        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(NOT_IN_CREW_ERROR);
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        String name = args[1];
        OfflinePlayer newCaptain;
        if (Nickname.getPlayerFromNickname(name) != null && crew.containsPlayer(Nickname.getPlayerFromNickname(name))) {
            newCaptain = Nickname.getPlayerFromNickname(name);
        } else if (crew.containsPlayer(Bukkit.getOfflinePlayer(name))) {
            newCaptain = Bukkit.getOfflinePlayer(name);
        } else {
            player.sendMessage(Component.text("Player \"" + name + "\" is not in your crew").color(ERROR_COLOR));
            return true;
        }
        if (player.equals(newCaptain)) {
            player.sendMessage(Component.text("You're already the captain of this crew!", ERROR_COLOR));
            return true;
        }
        ConfirmationMessage.sendConfirmationMessage(player, Component.text("make ").color(INFO_COLOR)
                .append(Component.text(Util.getName(newCaptain)).color(INFO_ACCENT_COLOR))
                .append(Component.text(" the captain of you crew").color(INFO_COLOR)), new BukkitRunnable() {
            @Override
            public void run() {
                crew.setCaptain(newCaptain);
                crew.sendMessageToMembers(Component.text(Util.getName(newCaptain)).color(INFO_ACCENT_COLOR)
                        .append(Component.text(" is now the captain of your crew").color(INFO_COLOR)), true);
                if (newCaptain.isOnline()) {
                    newCaptain.getPlayer().sendMessage(Component.text("You are now the captain of your crew!").color(INFO_COLOR));
                }
                Permission.removeGroup(player, "captain");
                Permission.addGroup(player, "crewmember");
                Permission.removeGroup(newCaptain, "crewmember");
                Permission.addGroup(newCaptain, "captain");
                crew.setCaptain(newCaptain);
            }
        });

        return true;
    }

    public static boolean openCrew(Player player) {
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(NOT_IN_CREW_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.OPEN_CREW.toString())) {
            player.sendMessage(NOT_CAPTAIN_ERROR);
            return true;
        }

        crew.setOpen(true);
        player.sendMessage(Component.text("Players will now need an invitation to join your crew.", INFO_COLOR));

        return true;
    }

    public static boolean closeCrew(Player player) {
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(NOT_IN_CREW_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.CLOSE_CREW.toString())) {
            player.sendMessage(NOT_CAPTAIN_ERROR);
            return true;
        }

        crew.setOpen(false);
        player.sendMessage(Component.text("Players can now join your crew without an invitation.", INFO_COLOR));

        return true;
    }

    public static boolean invite(Player player, String[] args) {
        if (!player.hasPermission(Permission.INVITE_TO_CREW.toString())) {
            player.sendMessage(NO_PERMISSION_ERROR);
            return true;
        }

        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(NOT_IN_CREW_ERROR);
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        String name = args[1];
        if (!crew.isOpen() && !player.hasPermission(Permission.CLOSED_INVITE_TO_CREW.toString())) {
            player.sendMessage(NOT_CAPTAIN_ERROR);
            return true;
        }
        Player invited = Util.getOnlinePlayer(name);
        if (invited == null) {
            invited = NautilusManager.INSTANCE.getServer().getPlayer(name);
        }
        if (invited == null) {
            player.sendMessage(Component.text("Player not online!").color(ERROR_COLOR));
            return true;
        }
        if (CrewHandler.getCrew(invited) != null) {
            player.sendMessage(Component.text("Player already in a crew!").color(ERROR_COLOR));
            return true;
        }
        player.sendMessage(Component.text("Invited ").color(INFO_COLOR)
                .append(Component.empty().append(invited.displayName()).color(INFO_ACCENT_COLOR))
                .append(Component.text(" to your crew").color(INFO_COLOR)));
        Invite.sendInvite(player, invited);
        return true;
    }

    public static boolean crewInfo(Player player, String[] args) {
        if (!player.hasPermission(Permission.CREW_INFO.toString())) {
            player.sendMessage(NO_PERMISSION_ERROR);
            return true;
        }

        if (args.length == 1) {
            Crew crew = CrewHandler.getCrew(player);
            if (crew == null) {
                player.sendMessage(NOT_IN_CREW_ERROR);
                player.sendMessage(Component.text("To get information about a specific crew, try ")
                        .append(Util.clickableCommand("/crew info <name>", false).color(ERROR_ACCENT_COLOR))
                        .append(Component.text("."))
                        .color(ERROR_COLOR));
                return true;
            }
            player.sendMessage(crew.toComponent());
        } else {
            String crewName = getMessageFromArgs(args, 1);
            Crew crew = CrewHandler.getCrew(crewName);
            if (crew == null) {
                player.sendMessage(Component.text("Crew \"" + crewName + "\" does not exist!").color(ERROR_COLOR));
                return true;
            }
            player.sendMessage(crew.toComponent());
        }
        
        return true;
    }

    public static boolean declareWar(Player player, String[] args) {
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(NOT_IN_CREW_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.DECLARE_WAR.toString())) {
            player.sendMessage(NOT_CAPTAIN_ERROR);
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        String crewName = getMessageFromArgs(args, 1);
        if (CrewHandler.getCrew(crewName) == null) {
            player.sendMessage(Component.text("Crew \"" + crewName + "\" does not exist!", ERROR_COLOR));
            return true;
        }

        Crew other = CrewHandler.getCrew(crewName);
        if (crew.equals(other)) {
            player.sendMessage(Component.text("You cannot declare war on yourself!", ERROR_COLOR));
            return true;
        }
        if (!other.getCaptain().isOnline()) {
            player.sendMessage(Component.text("The captain of \"" + crewName + "\" is not online!", ERROR_COLOR));
            return true;
        }
        if (crew.isAtWarWith(other)) {
            player.sendMessage(Component.text("You are already at war with \"" + crewName + "\"!", ERROR_COLOR));
            return true;
        }
        WarDeclaration.sendWarDeclaration(crew, other);
        player.sendMessage(Component.text("You declared war on ").color(INFO_COLOR)
                .append(Component.text("\"" + crewName + "\"").color(INFO_ACCENT_COLOR)));
        return true;
    }

public static boolean endWar(Player player, String[] args) {
        if (!player.hasPermission(Permission.END_WAR.toString())) {
            player.sendMessage(NOT_CAPTAIN_ERROR);
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(NOT_IN_CREW_ERROR);
            return true;
        }
        String name = getMessageFromArgs(args, 1);
        if (CrewHandler.getCrew(name) == null) {
            player.sendMessage(Component.text("Crew \"" + name + "\" does not exist!", ERROR_COLOR));
            return true;
        }
        Crew other = CrewHandler.getCrew(name);
        if (!crew.isAtWarWith(other)) {
            player.sendMessage(Component.text("You are not at war with \"" + name + "\"!", ERROR_COLOR));
            return true;
        }
        ConfirmationMessage.sendConfirmationMessage(player, Component.text("end your war with ").color(INFO_COLOR)
                .append(Component.text("\"" + name + "\"").color(INFO_ACCENT_COLOR)), new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(Component.text("You ended your war with ").color(INFO_COLOR)
                        .append(Component.text("\"" + name + "\"").color(INFO_ACCENT_COLOR)));
                crew.sendMessageToMembers(Component.text("Your crew is no longer at war with ").color(INFO_COLOR)
                        .append(Component.text("\"" + name + "\"").color(INFO_ACCENT_COLOR)), true);
                other.sendMessageToMembers(Component.text("\"" + name + "\"").color(INFO_ACCENT_COLOR)
                        .append(Component.text(" has ended the war with your crew!").color(INFO_COLOR)), false);
                CrewHandler.endWar(crew.getWar(other));
            }
        });
        return true;
    }

    public static boolean setPrefix(Player player, String[] args) {
        if (!player.hasPermission(Permission.SET_CREW_PREFIX.toString())) {
            player.sendMessage(NOT_CAPTAIN_ERROR);
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(NOT_IN_CREW_ERROR);
            return true;
        }
        String prefix = getMessageFromArgs(args, 1);
        if (prefix.length() < Crew.MIN_PREFIX_LENGTH || Crew.MAX_PREFIX_LENGTH < prefix.length()) {
            player.sendMessage(Component.text("Prefixes must be from " + Crew.MIN_PREFIX_LENGTH + "-" + Crew.MAX_PREFIX_LENGTH + " characters long!").color(ERROR_COLOR));
            return true;
        }
        crew.sendMessageToMembers(Component.text("Prefix changed to ")
                .append(Component.text(prefix).color(INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(INFO_COLOR));
        crew.setPrefix(prefix);

        return true;
    }

    public static boolean clearPrefix(Player player) {
        if (!player.hasPermission(Permission.CLEAR_CREW_PREFIX.toString())) {
            player.sendMessage(NOT_CAPTAIN_ERROR);
            return true;
        }

        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            player.sendMessage(NOT_IN_CREW_ERROR);
            return true;
        }
        crew.sendMessageToMembers(Component.text("Prefix cleared.").color(INFO_COLOR));
        crew.setPrefix("");

        return true;
    }

    public static boolean sendHelpMessage(Player player) {
        player.sendMessage(getHelpMessage());

        return true;
    }

    public static Component getHelpMessage() {
        ArrayList<String> subcommands = new ArrayList<>();
        subcommands.addAll(Arrays.asList(DEFAULT_COMMANDS));
        subcommands.addAll(Arrays.asList(NO_CREW_COMMANDS));
        subcommands.addAll(Arrays.asList(CREW_MEMBER_COMMANDS));
        subcommands.addAll(Arrays.asList(CAPTAIN_COMMANDS));
        Component out = Component.text("--------------------------------", INFO_COLOR)
                .appendNewline();
        for (String subcommand : subcommands) {
            out = out.append(getSubcommandUsage(subcommand))
                    .appendNewline();
        }
        out = out.append(Component.text("/crews", INFO_ACCENT_COLOR))
                .append(Component.text(" - List all crews on the server", INFO_COLOR))
                .appendNewline()
                .append(Component.text("--------------------------------", INFO_COLOR));

        return out;
    }

    private static Component getSubcommandUsage(String subcommand) {
        return switch (subcommand) {
            case "help" -> Component.text("/crew help", INFO_ACCENT_COLOR)
                    .append(Component.text(" - Show this message", INFO_COLOR));
            case "create" -> Component.text("/crew create <name>", INFO_ACCENT_COLOR)
                    .append(Component.text(" - Create a new crew and become the captain", INFO_COLOR));
            case "join" -> Component.text("/crew join <name>", INFO_ACCENT_COLOR)
                    .append(Component.text(" - Join an existing crew", INFO_COLOR));
            case "delete" -> Component.text("/crew delete", INFO_ACCENT_COLOR)
                    .append(Component.text(" - Delete your crew (Captain only)", INFO_COLOR));
            case "leave" -> Component.text("/crew leave", INFO_ACCENT_COLOR)
                    .append(Component.text(" - Leave your crew", INFO_COLOR));
            case "kick" -> Component.text("/crew kick <player>", INFO_ACCENT_COLOR)
                    .append(Component.text(" - Kick a player from your crew (Captain only)", INFO_COLOR));
            case "makecaptain" -> Component.text("/crew makecaptain <player>", INFO_ACCENT_COLOR)
                    .append(Component.text(" - Transfer ownership of your crew (Captain only)", INFO_COLOR));
            case "open" -> Component.text("/crew open", INFO_ACCENT_COLOR)
                    .append(Component.text(" - Allow anyone to join your crew (Captain only)", INFO_COLOR));
            case "close" -> Component.text("/crew close", INFO_ACCENT_COLOR)
                    .append(Component.text(" - Require players to be invited to join (Captain only)", INFO_COLOR));
            case "invite" -> Component.text("/crew invite", INFO_ACCENT_COLOR)
                    .append(Component.text(" - Invite a player to your crew", INFO_COLOR));
            case "info" -> Component.text("/crew info [name]", INFO_ACCENT_COLOR)
                    .append(Component.text(" - See information about your crew or another crew", INFO_COLOR));
            case "declarewar" -> Component.text("/crew declarewar <name>", INFO_ACCENT_COLOR)
                    .append(Component.text(" - Send a declaration of war to another crew (Captain only)", INFO_COLOR));
            case "endwar" -> Component.text("/crew endwar <name>", INFO_ACCENT_COLOR)
                    .append(Component.text(" - End a war with another crew (Captain only)", INFO_COLOR));
            case "prefix" -> Component.text("/crew setprefix <prefix>", INFO_ACCENT_COLOR)
                    .append(Component.text(" - Set a prefix for your crew (Captain only)", INFO_COLOR));
            case "clearprefix" -> Component.text("/crew clearprefix", INFO_ACCENT_COLOR)
                    .append(Component.text(" - Clear your crew's prefix (Captain only)", INFO_COLOR));
            default -> Component.text("You shouldn't see this! Please contact an administrator.", ERROR_COLOR);
        };
    }

}
