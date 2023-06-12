package org.nautilusmc.nautilusmanager.crews;

import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.*;

public class Invite {
    private OfflinePlayer sender;
    private OfflinePlayer receiver;

    public OfflinePlayer getSender() {
        return sender;
    }

    public void setSender(OfflinePlayer sender) {
        this.sender = sender;
    }

    public OfflinePlayer getReceiver() {
        return receiver;
    }

    public void setReceiver(OfflinePlayer receiver) {
        this.receiver = receiver;
    }

    private static HashMap<UUID, Stack<Invite>> PENDING = new HashMap<>();

    private Invite(OfflinePlayer sender, OfflinePlayer receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public static void sendInvite(Player inviteSender, Player inviteReceiver) {
        Crew senderCrew = CrewHandler.getCrew(inviteSender);
        if (senderCrew == null) {
            return;
        }
        inviteReceiver.sendMessage(Component.empty().append(inviteSender.displayName()).color(Command.INFO_ACCENT_COLOR)
                .append(Component.text(" sent you an invite to their crew!").color(Command.INFO_COLOR)));

        inviteReceiver.sendMessage(Util.clickableCommand("/invite accept", true).color(Command.INFO_ACCENT_COLOR)
                .append(Component.text(" to accept").color(Command.INFO_COLOR)));

        inviteReceiver.sendMessage(Util.clickableCommand("/invite decline", true).color(Command.INFO_ACCENT_COLOR)
                .append(Component.text(" to decline").color(Command.INFO_COLOR)));

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
            player.sendMessage(Command.NO_PENDING_INVITES_ERROR);
            return;
        }
        Stack<Invite> stack =  PENDING.get(uuid);
        Invite invite = stack.pop();
        if (stack.isEmpty()) {
            PENDING.remove(uuid);
        }
        Crew crew = CrewHandler.getCrew(invite.getSender());
        if (crew == null) {
            return;
        }
        crew.addMember(invite.getReceiver());
        Permission.addGroup(invite.getReceiver(), "crewmember");

        player.sendMessage(Component.text("You joined \"").color(Command.INFO_COLOR)
                .append(Component.text(crew.getName()).color(Command.INFO_ACCENT_COLOR))
                .append(Component.text("\"!").color(Command.INFO_COLOR)));
        crew.sendMessageToMembers(Component.text(Util.getName(invite.getReceiver())).color(Command.INFO_ACCENT_COLOR)
                .append(Component.text(" joined the crew!").color(Command.INFO_COLOR)));
    }

    public static void deny(Player player) {
        UUID uuid = player.getUniqueId();
        if (!PENDING.containsKey(uuid)) {
            player.sendMessage(Command.NO_PENDING_INVITES_ERROR);
            return;
        }
        Stack<Invite> stack =  PENDING.get(uuid);
        Invite invite = stack.pop();
        if (stack.isEmpty()) {
            PENDING.remove(uuid);
        }
        player.sendMessage(Component.text("Invitation declined.").color(Command.ERROR_COLOR));
        if (invite.getSender().isOnline()) {
            Player inviteSender = invite.getSender().getPlayer();
            inviteSender.sendMessage(player.displayName()
                    .append(Component.text(" declined your invite!").color(Command.ERROR_COLOR)));
        }
    }
}
