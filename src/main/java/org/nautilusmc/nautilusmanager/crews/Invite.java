package org.nautilusmc.nautilusmanager.crews;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.util.PermsUtil;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand.Default;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand.ErrorMessage;
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
        inviteReceiver.sendMessage(Component.empty().append(inviteSender.displayName()).color(Default.INFO_ACCENT_COLOR)
                .append(Component.text(" sent you an invite to their crew!").color(Default.INFO_COLOR)));

        inviteReceiver.sendMessage(Util.clickableCommand("/invite accept ", true).color(Default.INFO_ACCENT_COLOR)
                .append(Component.text("to accept").color(Default.INFO_COLOR)));

        inviteReceiver.sendMessage(Util.clickableCommand("/invite decline ", true).color(Default.INFO_ACCENT_COLOR)
                .append(Component.text("to decline").color(Default.INFO_COLOR)));

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
            player.sendMessage(ErrorMessage.NO_PENDING_INVITES);
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
        PermsUtil.addGroup(invite.getReceiver(), "crewmember");

        player.sendMessage(Component.text("Crew ").color(NautilusCommand.MAIN_COLOR)
                .append(Component.text(crew.getName()).color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text(" joined!").color(NautilusCommand.MAIN_COLOR)));
        crew.sendMessageToMembers(Component.text(Util.getName(invite.getReceiver())).color(NautilusCommand.ACCENT_COLOR)
                .append(Component.text(" joined the crew!").color(NautilusCommand.MAIN_COLOR)));
    }

    public static void deny(Player player) {
        UUID uuid = player.getUniqueId();
        if (!PENDING.containsKey(uuid)) {
            player.sendMessage(Component.text("No pending invites!").color(Default.ERROR_COLOR));
            return;
        }
        Stack<Invite> stack =  PENDING.get(uuid);
        Invite invite = stack.pop();
        if (stack.isEmpty()) {
            PENDING.remove(uuid);
        }
        player.sendMessage(Component.text("Invitation declined!").color(NautilusCommand.ERROR_COLOR));
        if (invite.getSender().isOnline()) {
            Player inviteSender = invite.getSender().getPlayer();
            inviteSender.sendMessage(player.displayName()
                    .append(Component.text(" declined your invite!").color(NautilusCommand.ERROR_COLOR)));
        }
    }
}
