package org.nautilusmc.nautilusmanager.crews;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand.Default;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand.ErrorMessage;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.*;

public class WarDeclaration {
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
        captain.sendMessage(Component.text("Crew \"").color(Default.INFO_COLOR)
                .append(Component.text(warDeclarer.getName()).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text("\" has declared war on your crew!").color(Default.INFO_COLOR)));
        captain.sendMessage(Util.clickableCommand("/war accept", true).color(Default.INFO_ACCENT_COLOR)
                .append(Component.text(" to accept").color(Default.INFO_COLOR)));

        captain.sendMessage(Util.clickableCommand("/war decline", true).color(Default.INFO_ACCENT_COLOR)
                .append(Component.text(" to decline").color(Default.INFO_COLOR)));

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
            player.sendMessage(ErrorMessage.NOT_CAPTAIN);
        }
        Crew crew = CrewHandler.getCrew(player);
        if (!PENDING.containsKey(crew)) {
            player.sendMessage(ErrorMessage.NO_PENDING_WARS);
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
                .append(Component.text(warDeclaration.getSender().getName()).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text("\"!"))
                .color(Default.INFO_COLOR));

        warDeclaration.getSender().sendMessageToMembers(Component.text("Your crew is now at war with \"")
                .append(Component.text(warDeclaration.getReceiver().getName()).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text("\"!"))
                .color(Default.INFO_COLOR));
    }

    public static void deny(Player player) {
        if (CrewHandler.getCrew(player) == null || !CrewHandler.getCrew(player).getCaptain().equals(player)) {
            player.sendMessage(ErrorMessage.NOT_CAPTAIN);
        }
        Crew crew = CrewHandler.getCrew(player);
        if (!PENDING.containsKey(crew)) {
            player.sendMessage(ErrorMessage.NO_PENDING_WARS);
            return;
        }
        Stack<WarDeclaration> stack = PENDING.get(crew);
        WarDeclaration warDeclaration = stack.pop();
        if (stack.isEmpty()) {
            PENDING.remove(crew);
        }

       if (warDeclaration.getSender().getCaptain().isOnline()) {
            Player senderCaptain = warDeclaration.getSender().getCaptain().getPlayer();
            senderCaptain.sendMessage(Component.text(warDeclaration.getReceiver().getName()).color(Default.INFO_ACCENT_COLOR)
                    .append(Component.text(" declined your war declaration!").color(Default.INFO_COLOR)));
        }
        player.sendMessage(Component.text("War declaration declined!").color(Default.INFO_COLOR));
    }
}
