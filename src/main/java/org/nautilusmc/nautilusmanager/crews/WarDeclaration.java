package org.nautilusmc.nautilusmanager.crews;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.crews.commands.CrewCommand;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.*;

public class WarDeclaration {
    public static final Component NO_PENDING_WARS_ERROR = Component.text("No pending war declarations!").color(Command.ERROR_COLOR);
    private static final HashMap<Crew, Stack<WarDeclaration>> PENDING = new HashMap<>();

    private Crew sender;
    private Crew receiver;

    private WarDeclaration(Crew sender, Crew receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

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

    public static void sendWarDeclaration(Crew warDeclarer, Crew warReceiver) {
        if (!warReceiver.getCaptain().isOnline()) {
            return;
        }
        Player captain = warReceiver.getCaptain().getPlayer();
        captain.sendMessage(Component.text("Crew \"").color(Command.INFO_COLOR)
                .append(Component.text(warDeclarer.getName()).color(Command.INFO_ACCENT_COLOR))
                .append(Component.text("\" has declared war on your crew!").color(Command.INFO_COLOR)));
        captain.sendMessage(Util.clickableCommand("/war accept", true).color(Command.INFO_ACCENT_COLOR)
                .append(Component.text(" to accept").color(Command.INFO_COLOR)));

        captain.sendMessage(Util.clickableCommand("/war decline", true).color(Command.INFO_ACCENT_COLOR)
                .append(Component.text(" to decline").color(Command.INFO_COLOR)));

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
            player.sendMessage(CrewCommand.NOT_CAPTAIN_ERROR);
        }
        Crew crew = CrewHandler.getCrew(player);
        if (!PENDING.containsKey(crew)) {
            player.sendMessage(NO_PENDING_WARS_ERROR);
            return;
        }
        Stack<WarDeclaration> stack = PENDING.get(crew);
        WarDeclaration warDeclaration = stack.pop();
        if (stack.isEmpty()) {
            PENDING.remove(crew);
        }
        if (warDeclaration.getSender() == null) {
            return;
        }
        War war = new War(warDeclaration.getSender(), warDeclaration.getReceiver());
        CrewHandler.registerWar(war);

        warDeclaration.getReceiver().sendMessageToMembers(Component.text("Your crew is now at war with \"")
                .append(Component.text(warDeclaration.getSender().getName()).color(Command.INFO_ACCENT_COLOR))
                .append(Component.text("\"!"))
                .color(Command.INFO_COLOR));

        warDeclaration.getSender().sendMessageToMembers(Component.text("Your crew is now at war with \"")
                .append(Component.text(warDeclaration.getReceiver().getName()).color(Command.INFO_ACCENT_COLOR))
                .append(Component.text("\"!"))
                .color(Command.INFO_COLOR));
    }

    public static void deny(Player player) {
        if (CrewHandler.getCrew(player) == null || !CrewHandler.getCrew(player).getCaptain().equals(player)) {
            player.sendMessage(CrewCommand.NOT_CAPTAIN_ERROR);
        }
        Crew crew = CrewHandler.getCrew(player);
        if (!PENDING.containsKey(crew)) {
            player.sendMessage(NO_PENDING_WARS_ERROR);
            return;
        }
        Stack<WarDeclaration> stack = PENDING.get(crew);
        WarDeclaration warDeclaration = stack.pop();
        if (stack.isEmpty()) {
            PENDING.remove(crew);
        }

       if (warDeclaration.getSender().getCaptain().isOnline()) {
            Player senderCaptain = warDeclaration.getSender().getCaptain().getPlayer();
            senderCaptain.sendMessage(Component.text(warDeclaration.getReceiver().getName()).color(Command.INFO_ACCENT_COLOR)
                    .append(Component.text(" declined your war declaration!").color(Command.INFO_COLOR)));
        }
        player.sendMessage(Component.text("War declaration declined!").color(Command.INFO_COLOR));
    }
}
