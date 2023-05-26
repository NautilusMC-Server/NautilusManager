package org.nautilusmc.nautilusmanager.crews.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.crews.Crew;
import org.nautilusmc.nautilusmanager.crews.CrewHandler;
import org.nautilusmc.nautilusmanager.crews.Invite;
import org.nautilusmc.nautilusmanager.util.ConfirmationMessage;
import org.nautilusmc.nautilusmanager.util.PermsUtil;
import org.nautilusmc.nautilusmanager.util.Util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CrewCommand extends NautilusCommand {
    private static final String CAPTAIN_PERM_MESSAGE = "You must be a captain to use this command!";
    private static final String CREW_PERM_MESSAGE = "You must be part of a crew to use this command!";
    private static final String ALREADY_IN_CREW_MESSAGE = "You are already in a crew!";
    private static final String[] DEFAULT_COMMANDS = {
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
            "delete", //captains
            "kick", //captains
            "makecaptain", //captains
            "open", //captains
            "close", //captains
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
            case "delete" -> deleteCrew(player);
            case "join" -> joinCrew(player, strings);
            case "leave" -> leaveCrew(player);
            case "kick" -> kickMember(player, strings);
            case "makecaptain" -> makeCaptain(player, strings);
            case "open" -> openCrew(player);
            case "close" -> closeCrew(player);
            case "invite" -> invite(player, strings);
            case "info" -> crewInfo(player, strings);
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
                case "join" -> tabCompletions.addAll(CrewHandler.getCrews().stream().map(Crew::getName).toList());
                case "kick" -> tabCompletions.addAll(CrewHandler.getCrew(player).getMembers().stream().map(Util::getName).toList());
                case "makecaptain" -> tabCompletions.addAll(CrewHandler.getCrew(player).getMembers().stream().map(Util::getName).toList());
                case "invite" -> tabCompletions.addAll(getOnlineNames());
                case "info" -> tabCompletions.addAll(CrewHandler.getCrews().stream().map(Crew::getName).toList());
            }
        }
        return tabCompletions;


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
        if (name.length() > 15) {
            error(player, "Crew name must be under 16 characters!");
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

    public static void deleteCrew(Player player) {
        if (!(player.hasPermission(DELETE_CREW_PERM))) {
            error(player, CAPTAIN_PERM_MESSAGE);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, CREW_PERM_MESSAGE);
            return;
        }
        ConfirmationMessage.sendConfirmationMessage(player, Component.text("delete your crew").color(NautilusCommand.MAIN_COLOR), new BukkitRunnable() {
            @Override
            public void run() {
                //remove perms
                PermsUtil.removeGroup(crew.getCaptain(), "captain");
                for (Player p : crew.getMembers()) {
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
                    crew.sendMessageToMembers(player.displayName()
                            .append(Component.text(" joined the crew!").color(NautilusCommand.MAIN_COLOR)));
                    PermsUtil.addGroup(player, "crewmember");
                    CrewHandler.updateSQL();
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
                        Player newCaptain = crew.getMembers().get(1);
                        if (newCaptain.equals(player)) {
                            newCaptain = crew.getMembers().get(2);
                        }
                        crew.setCaptain(newCaptain);
                        crew.getMembers().remove(player);
                        crew.sendMessageToMembers(player.displayName()
                                .append(Component.text(" left the crew").color(MAIN_COLOR)));
                        crew.sendMessageToMembers(newCaptain.displayName()
                                .append(Component.text(" is now the captain of your crew").color(MAIN_COLOR)), true);
                        if (newCaptain.isOnline()) {
                            newCaptain.sendMessage(Component.text("You are now the captain of your crew!").color(MAIN_COLOR));
                        }
                        CrewHandler.updateSQL();
                        PermsUtil.removeGroup(player, "captain");
                        PermsUtil.removeGroup(newCaptain, "crewmember");
                        PermsUtil.addGroup(newCaptain, "captain");
                    }
                } else {
                    crew.getMembers().remove(player);
                    PermsUtil.removeGroup(player, "crewmember");
                    crew.sendMessageToMembers(player.displayName()
                            .append(Component.text(" left the crew!").color(NautilusCommand.MAIN_COLOR)));
                    CrewHandler.updateSQL();
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
        Player kicked;
        if (NautilusManager.INSTANCE.getServer().getPlayer(name) != null && crew.containsPlayer(NautilusManager.INSTANCE.getServer().getPlayer(name))) {
            kicked = NautilusManager.INSTANCE.getServer().getPlayer(name);
        } else if (Util.getOnlinePlayer(name) != null && crew.containsPlayer(Util.getOnlinePlayer(name))) {
            kicked = Util.getOnlinePlayer(name);
        } else {
            error(player, "Player \"" + name + "\" is not in your crew");
            return;
        }
        if (kicked.equals(player)) {
            error(player, "You can't kick yourself out of the crew!");
            return;
        }
        ConfirmationMessage.sendConfirmationMessage(player, Component.text("kick ").color(NautilusCommand.MAIN_COLOR)
                .append(kicked.isOnline() ? kicked.displayName() : Component.text(kicked.getName()).color(ACCENT_COLOR))
                .append(Component.text(" out of your crew")).color(NautilusCommand.MAIN_COLOR) , new BukkitRunnable() {
            @Override
            public void run() {
                crew.sendMessageToMembers(kicked.displayName()
                        .append(Component.text(" was kicked from the crew!").color(NautilusCommand.MAIN_COLOR)));
                if (kicked.isOnline()) {
                    kicked.sendMessage(Component.text("You were kicked from ").color(MAIN_COLOR)
                            .append(Component.text(crew.getName()).color(ACCENT_COLOR)));
                }
                crew.getMembers().remove(kicked);
                PermsUtil.removeGroup(kicked, "crewmember");
                CrewHandler.updateSQL();
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
        Player newCaptain;
        if (NautilusManager.INSTANCE.getServer().getPlayer(name) != null && crew.containsPlayer(NautilusManager.INSTANCE.getServer().getPlayer(name))) {
            newCaptain = NautilusManager.INSTANCE.getServer().getPlayer(name);
        } else if (Util.getOnlinePlayer(name) != null && crew.containsPlayer(Util.getOnlinePlayer(name))) {
            newCaptain = Util.getOnlinePlayer(name);
        } else {
            error(player, "Player \"" + name + "\" is not in your crew");
            return;
        }

        ConfirmationMessage.sendConfirmationMessage(player, Component.text("make ").color(MAIN_COLOR)
                .append(newCaptain.displayName())
                .append(Component.text("the captian of you crew").color(ACCENT_COLOR)), new BukkitRunnable() {
            @Override
            public void run() {
                crew.sendMessageToMembers(newCaptain.displayName()
                        .append(Component.text(" is now the captain of your crew").color(MAIN_COLOR)), true);
                if (newCaptain.isOnline()) {
                    newCaptain.sendMessage(Component.text("You are now the captain of your crew!").color(MAIN_COLOR));
                }
                crew.setCaptain(newCaptain);
                CrewHandler.updateSQL();
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
        CrewHandler.updateSQL();
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
        CrewHandler.updateSQL();
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
                .append(invited.displayName())
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
            default -> Component.text("You shouldn't see this").color(ERROR_COLOR);
        };
    }

}
