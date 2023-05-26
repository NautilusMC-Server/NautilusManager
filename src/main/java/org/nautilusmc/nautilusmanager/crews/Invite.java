package org.nautilusmc.nautilusmanager.crews;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.*;

public class Invite {
    private Player sender;
    private Player receiver;

    public Player getSender() {
        return sender;
    }

    public void setSender(Player sender) {
        this.sender = sender;
    }

    public Player getReceiver() {
        return receiver;
    }

    public void setReceiver(Player receiver) {
        this.receiver = receiver;
    }

    private static HashMap<UUID, Stack<Invite>> PENDING = new HashMap<>();

    private Invite(Player sender, Player receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public static void sendInvite(Player inviteSender, Player inviteReceiver) {
        Crew senderCrew = CrewHandler.getCrew(inviteSender);
        if (senderCrew == null) {
            return;
        }
        inviteReceiver.sendMessage(inviteSender.displayName().append(Component.text(" sent you an invite to their crew!").color(NautilusCommand.MAIN_COLOR)));

        inviteReceiver.sendMessage(Util.clickableCommand("/invite accept", true).color(NautilusCommand.ACCENT_COLOR)
                .append(Component.text("to accept").color(NautilusCommand.ACCENT_COLOR)));

        inviteReceiver.sendMessage(Util.clickableCommand("/invite deny", true).color(NautilusCommand.ACCENT_COLOR)
                .append(Component.text("to deny").color(NautilusCommand.ACCENT_COLOR)));

        UUID uuid = inviteReceiver.getUniqueId();
        if (!PENDING.containsKey(uuid)) {
            PENDING.put(uuid, new Stack<>());
        }
        Stack<Invite> stack = PENDING.get(uuid);
        if (stack.size() > 10) {
            stack.remove(stack.firstElement());
        }
        PENDING.get(uuid).push(new Invite(inviteSender, inviteReceiver));
    }

    public static void accept(Player player) {
        UUID uuid = player.getUniqueId();
        if (!PENDING.containsKey(uuid)) {
            player.sendMessage(Component.text("No pending invites!").color(NautilusCommand.ERROR_COLOR));
            return;
        }
        Stack<Invite> stack =  PENDING.get(uuid);
        Invite invite = stack.pop();
        if (stack.isEmpty()) {
            PENDING.remove(uuid);
        }
        Crew crew = CrewHandler.getCrew(invite.getSender());
        crew.getMembers().add(invite.getReceiver());
        invite.getReceiver().sendMessage(Component.text("Crew ").color(NautilusCommand.MAIN_COLOR)
                .append(Component.text(crew.getName()).color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text(" joined!").color(NautilusCommand.MAIN_COLOR)));
        crew.sendMessageToMembers(invite.getReceiver().displayName()
                .append(Component.text(" joined the crew!").color(NautilusCommand.MAIN_COLOR)));
    }
    public static void deny(Player player) {
        UUID uuid = player.getUniqueId();
        if (!PENDING.containsKey(uuid)) {
            player.sendMessage(Component.text("No pending invites!").color(NautilusCommand.ERROR_COLOR));
            return;
        }
        Stack<Invite> stack =  PENDING.get(uuid);
        if (stack.isEmpty()) {
            PENDING.remove(uuid);
        }
        stack.pop();
        player.sendMessage(Component.text("Invitation declined!").color(NautilusCommand.ERROR_COLOR));
    }

}
