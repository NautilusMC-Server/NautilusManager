package org.nautilusmc.nautilusmanager.crews;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.crews.commands.CrewCommand;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.*;

public class WarDeclaration {
    private Crew sender;
    private Crew receiver;
    private static HashMap<Crew, Stack<WarDeclaration>> PENDING = new HashMap<>();
    public Crew getSender() {
        return sender;
    }

    public void setSender(Crew sender) {
        this.sender = sender;
    }

    public Crew getReceiver() {
        return receiver;
    }

    public void setReceiver(Crew receiver) {
        this.receiver = receiver;
    }

    private WarDeclaration(Crew sender, Crew receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public static void sendWarDeclaration(Crew warDeclarer, Crew warReceiver) {
        Player captain = warReceiver.getCaptain();
        captain.sendMessage(Component.text("Crew ").color(NautilusCommand.MAIN_COLOR)
                .append(Component.text("\"" + warDeclarer.getName() + "\"").color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text(" has declared war on your crew!").color(NautilusCommand.MAIN_COLOR)));
        captain.sendMessage(Util.clickableCommand("/war accept ", true).color(NautilusCommand.ACCENT_COLOR)
                .append(Component.text("to accept").color(NautilusCommand.MAIN_COLOR)));

        captain.sendMessage(Util.clickableCommand("/war decline ", true).color(NautilusCommand.ACCENT_COLOR)
                .append(Component.text("to decline").color(NautilusCommand.MAIN_COLOR)));

        if (!PENDING.containsKey(warReceiver)) {
            PENDING.put(warReceiver, new Stack<>());
        }
        Stack<WarDeclaration> stack = PENDING.get(warReceiver);
        if (stack.size() > 10) {
            stack.remove(stack.firstElement());
        }
        stack.push(new WarDeclaration(warDeclarer, warReceiver));
    }

    public static void accept(Player player) {
        if (CrewHandler.getCrew(player) == null || !CrewHandler.getCrew(player).getCaptain().equals(player)) {
            player.sendMessage(Component.text(CrewCommand.CAPTAIN_PERM_MESSAGE).color(NautilusCommand.ERROR_COLOR));
        }
        Crew crew = CrewHandler.getCrew(player);
        if (!PENDING.containsKey(crew)) {
            player.sendMessage(Component.text("No pending war declarations!").color(NautilusCommand.ERROR_COLOR));
            return;
        }
        Stack<WarDeclaration> stack =  PENDING.get(crew);
        WarDeclaration warDeclaration = stack.pop();
        if (stack.isEmpty()) {
            PENDING.remove(crew);
        }
        warDeclaration.getSender().getAtWarWith().add(warDeclaration.getReceiver());
        warDeclaration.getReceiver().getAtWarWith().add(warDeclaration.getSender());
        warDeclaration.getReceiver().sendMessageToMembers(Component.text("Your crew is now at war with ").color(NautilusCommand.MAIN_COLOR)
                .append(Component.text("\"" + warDeclaration.getSender().getName() + "\"").color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text("!").color(NautilusCommand.MAIN_COLOR)));
        warDeclaration.getSender().sendMessageToMembers(Component.text("Your crew is now at war with ").color(NautilusCommand.MAIN_COLOR)
                .append(Component.text("\"" + warDeclaration.getReceiver().getName() + "\"").color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text("!").color(NautilusCommand.MAIN_COLOR)));
    }
    public static void deny(Player player) {
        if (CrewHandler.getCrew(player) == null || !CrewHandler.getCrew(player).getCaptain().equals(player)) {
            player.sendMessage(Component.text(CrewCommand.CAPTAIN_PERM_MESSAGE).color(NautilusCommand.ERROR_COLOR));
        }
        Crew crew = CrewHandler.getCrew(player);
        if (!PENDING.containsKey(crew)) {
            player.sendMessage(Component.text("No pending war declarations!").color(NautilusCommand.ERROR_COLOR));
            return;
        }
        Stack<WarDeclaration> stack =  PENDING.get(crew);
        WarDeclaration warDeclaration = stack.pop();
        if (stack.isEmpty()) {
            PENDING.remove(crew);
        }
        warDeclaration.getSender().getCaptain().sendMessage(Component.text(warDeclaration.getReceiver().getName()).color(NautilusCommand.ACCENT_COLOR)
                .append(Component.text(" declined your war declaration!").color(NautilusCommand.MAIN_COLOR)));
        player.sendMessage(Component.text("War declaration declined!").color(NautilusCommand.MAIN_COLOR));
    }

}
