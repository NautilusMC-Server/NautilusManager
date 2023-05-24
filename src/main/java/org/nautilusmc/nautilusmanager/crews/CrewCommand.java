package org.nautilusmc.nautilusmanager.crews;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.util.ConfirmationMessage;
import org.nautilusmc.nautilusmanager.util.Util;


import java.util.List;

public class CrewCommand extends NautilusCommand {
    private static final String CAPTAIN_PERM_MESSAGE = "You must be a captain to use this command!";
    private static final String CREW_PERM_MESSAGE = "You must be part of a crew to use this command!";

    private static final String[] subcommands = {
            "create",
            "listplayers",
            "join",
            "leave",
            "info",
            "delete", //captains
            "kick", //captains
            "makecaptain", //captains
            "open", //captains
            "close", //captains
            "invite" //captains if crew closed
    };
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            return false;
        }
        Player player = (Player) commandSender;
        if (strings.length == 0) {
            player.sendMessage(Component.text(help()).color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
            return true;
        }
        switch (strings[0]) {
            case "create" -> createCrew(player, strings);
            case "delete" -> deleteCrew(player);
            case "listplayers" -> listPlayers(player);
            case "join" -> joinCrew(player, strings);
            case "leave" -> leaveCrew(player);
            case "kick" -> kickMember(player, strings[1]);
            case "makecaptain" -> makeCaptain(player, strings[1]);
            case "open" -> openCrew(player);
            case "close" -> closeCrew(player);
            case "invite" -> invite(player, strings[1]);
            case "info" -> crewInfo(player, strings);
            default -> player.sendMessage(Component.text(help()).color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }

    public static void createCrew(Player player, String[] strings) {
        if (!(player.hasPermission(CREATE_CREW_PERM))) {
            error(player, DEFAULT_PERM_MESSAGE);
            return;
        }
        String name = getFormattedArgs(strings, 1);
        if (name.length() > 15) {
            error(player, "Crew name must be under 16 characters!");
            return;
        }
        for (Crew crew : CrewHandler.getCrews()) {
            if (crew.getName().equals(name)) {
                error(player, "Crew name already taken!");
                return;
            }
        }
        CrewHandler.registerCrew(new Crew(player, name));
        player.sendMessage(Component.text("Crew \"" + name + "\" created!").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
        //TODO: Add player to crew perm group
    }

    public static void deleteCrew(Player player) {
        if (!(player.hasPermission(DELETE_CREW_PERM))) {
            error(player, CAPTAIN_PERM_MESSAGE);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, "You are not part of any crew!");
            return;
        }
        ConfirmationMessage.sendConfirmationMessage(player, "delete your crew", new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(Component.text("Crew \"" + crew.getName() + "\" deleted!").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
                CrewHandler.deleteCrew(crew);
                CrewHandler.updateSQL();
            }
        });
        //TODO: Remove players from crew and captain perm groups
    }

    public static void listPlayers(Player player) {
        if (!(player.hasPermission(LIST_CREW_PERM))) {
            error(player, CREW_PERM_MESSAGE);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, "You are not part of any crew!");
            return;
        }
        player.sendMessage(Component.text(crew.toString()).color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
    }

    public static void joinCrew(Player player, String[] strings) {
        if (!(player.hasPermission(JOIN_CREW_PERM))) {
            error(player, DEFAULT_PERM_MESSAGE);
            return;
        }

        if (CrewHandler.getCrew(player) != null) {
            error(player, "You are already part of a crew!");
            error(player, "\"/crew leave\" to leave your crew");
            return;
        }

        for (Crew crew : CrewHandler.getCrews()) {
            if (crew.getName().equals(getFormattedArgs(strings, 1))) {
                if (!crew.isOpen()) {
                    error(player, "Crew is closed to invitations only!");
                }
                crew.addMember(player);
                CrewHandler.updateSQL();
                return;
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
            error(player, "You are not part of any crew!");
            return;
        }
        //TODO: add confirmation message later
        player.sendMessage(Component.text("You left \"" + crew.getName() + "\"").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
        crew.getMembers().remove(player);
        CrewHandler.updateSQL();
        //TODO: Remove player from crew perm group
    }

    public static void kickMember(Player player, String name) {
        if (!(player.hasPermission(KICK_CREW_PERM))) {
            error(player, CAPTAIN_PERM_MESSAGE);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, "You are not part of any crew!");
            return;
        }
        Player kicked;
        if (NautilusManager.INSTANCE.getServer().getPlayer(name) != null && crew.containsPlayer(NautilusManager.INSTANCE.getServer().getPlayer(name))) {
            kicked = NautilusManager.INSTANCE.getServer().getPlayer(name);
        } else if (Util.getOnlinePlayer(name) != null && crew.containsPlayer(Util.getOnlinePlayer(name))) {
            kicked = Util.getOnlinePlayer(name);
        } else {
            error(player, "Player \"" + name + "\" is not in your crew");
            return;
        }
        //TODO: add confirmation message later
        player.sendMessage(Component.text(name + " was kicked from your crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
        if (kicked.isOnline()) {
            kicked.sendMessage(Component.text("You were kicked from your crew!").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
        }
        crew.getMembers().remove(kicked);
        CrewHandler.updateSQL();
        //TODO: Remove player from crew perm group
    }
    public static void makeCaptain(Player player, String name) {
        if (!(player.hasPermission(MAKECAPTAIN_CREW_PERM))) {
            error(player, CAPTAIN_PERM_MESSAGE);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, "You are not part of any crew!");
            return;
        }
        Player newCaptain;
        if (NautilusManager.INSTANCE.getServer().getPlayer(name) != null && crew.containsPlayer(NautilusManager.INSTANCE.getServer().getPlayer(name))) {
            newCaptain = NautilusManager.INSTANCE.getServer().getPlayer(name);
        } else if (Util.getOnlinePlayer(name) != null && crew.containsPlayer(Util.getOnlinePlayer(name))) {
            newCaptain = Util.getOnlinePlayer(name);
        } else {
            error(player, "Player \"" + name + "\" is not in your crew");
            return;
        }
        //TODO: add confirmation message later
        player.sendMessage(Component.text(name + " is now the captain of your crew").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
        if (newCaptain.isOnline()) {
            newCaptain.sendMessage(Component.text("You are now the captain of your crew!").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
        }
        crew.setCaptain(newCaptain);
        CrewHandler.updateSQL();
        //TODO: Change player perm groups
    }
    public static String help() {
        String out = "--------------------------------\n";;
        for (String subcommand : subcommands) {
            out += getSubcommandUsage(subcommand) + "\n";
        }
        out += "--------------------------------";
        return out;
    }

    public static void openCrew(Player player) {
        if (!(player.hasPermission(OPEN_CREW_PERM))) {
            error(player, CAPTAIN_PERM_MESSAGE);
            return;
        }
        Crew crew = CrewHandler.getCrew(player);
        if (crew == null) {
            error(player, "You are not part of any crew!");
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
            error(player, "You are not part of any crew!");
            return;
        }
        crew.setOpen(false);
        player.sendMessage(Component.text("Crew set to closed!").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
        CrewHandler.updateSQL();
    }

    public static void invite(Player player, String name) {
        //TODO: All of this
    }

    public static void crewInfo(Player player, String[] args) {
        if (!(player.hasPermission(CREW_INFO_PERM))) {
            error(player, DEFAULT_PERM_MESSAGE);
            return;
        }
        if (args.length == 1) {
            Crew crew = CrewHandler.getCrew(player);
            if (crew == null) {
                error(player, "You are not part of any crew!");
                return;
            }
            player.sendMessage(Component.text(crew.toString()).color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
        } else {
            String name = getFormattedArgs(args, 1);
            Crew crew = CrewHandler.getCrew(name);
            if (crew == null) {
                error(player, "Crew \"" + name + "\" does not exist!");
                return;
            }
            player.sendMessage(Component.text(crew.toString()).color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
        }
    }

    private static String getSubcommandUsage(String subcommand) {
        switch (subcommand) {
            case "create" : return "/crew create <name> - Creates a new crew with specified name";
            case "delete" : return "/crew delete - Deletes your crew (Captain Only)";
            case "listplayers" : return  "/crew list - Lists all members of your crew";
            case "join" : return "/crew join <name> - Joins a crew";
            case "leave" : return "/crew leave <name> - Leaves your crew";
            case "kick" : return  "/crew kick <player> - Kicks a player from your crew (Captains Only)";
            case "makecaptain" : return "/crew makecaptain <player> - Makes specified player captian of your crew (Captians Only)";
            case "open" : return "/crew open - Opens crew for anyone to join (Captains Only)";
            case "close" : return "/crew close - Closes crew for invitation only (Captains Only)";
            case "invite" : return "/crew invite - Invites a player to your crew";
            case "info" : return "/crew info <name> - Sends crew information";
            default : return "You shouldn't see this";
        }
    }

}
