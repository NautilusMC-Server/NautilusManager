package org.nautilusmc.nautilusmanager.teleport;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.events.TeleportHandler;
import org.nautilusmc.nautilusmanager.sql.SQLSyncedPerPlayerList;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.*;

public class TpaManager implements Listener {
    public static final Component NO_OUTGOING_TP_REQUEST_ERROR = Component.text("You don't have an outgoing request!").color(Command.ERROR_COLOR);
    public static final Component PENDING_TP_REQUEST_ERROR = Component.text("You already have a pending request!").color(Command.ERROR_COLOR);
    public static final Component NO_PENDING_TP_REQUEST_ERROR = Component.text("No pending request found!").color(Command.ERROR_COLOR);

    public enum TeleportRequest {
        REQUESTER_TO_RECIPIENT("to teleport to you"),
        RECIPIENT_TO_REQUESTER("that you teleport to them");

        public final String intent;

        TeleportRequest(String intent) {
            this.intent = intent;
        }
    }

    public static final Component CANNOT_TP_TO_SELF_ERROR = Component.text("You can't teleport to yourself!").color(Command.ERROR_COLOR);
    public static final int MAX_TRUSTED = 99; // the SQL can have 2 digits

    public static final int TP_REQUEST_TIMEOUT_SECONDS = 5 * 60;

    // requester : (recipient, request type)
    private static final Map<UUID, Map.Entry<UUID, TeleportRequest>> REQUESTS = new HashMap<>();
    // recipient : requester
    private static final Map<UUID, UUID> LAST_REQUESTS = new HashMap<>();

    private static final SQLSyncedPerPlayerList<UUID, String> TRUST_LISTS = new SQLSyncedPerPlayerList<>(String.class, UUID::toString, UUID::fromString, "trusted", MAX_TRUSTED);

    public static void init() {
        TRUST_LISTS.initSQL("tpa_trustlist");
    }

    public static boolean isTrusted(Player truster, OfflinePlayer player) {
        return TRUST_LISTS.contains(truster.getUniqueId(), player.getUniqueId());
    }

    public static List<UUID> getTrusted(Player truster) {
        return new ArrayList<>(TRUST_LISTS.getOrDefault(truster.getUniqueId(), List.of()));
    }

    /**
     * @return Whether the player is now trusted
     */
    public static boolean toggleTrust(Player truster, OfflinePlayer player) {
        if (isTrusted(truster, player)) {
            TRUST_LISTS.remove(truster.getUniqueId(), player.getUniqueId());
            return false;
        } else {
            return TRUST_LISTS.add(truster.getUniqueId(), player.getUniqueId());
        }
    }

    public static void tpRequest(Player requester, Player recipient, TeleportRequest request) {
        if (REQUESTS.containsKey(requester.getUniqueId())) {
            requester.sendMessage(PENDING_TP_REQUEST_ERROR);
            return;
        }

        Map.Entry<UUID, TeleportRequest> entry = Map.entry(recipient.getUniqueId(), request);
        REQUESTS.put(requester.getUniqueId(), entry);
        LAST_REQUESTS.put(recipient.getUniqueId(), requester.getUniqueId());

        requester.sendMessage(Component.text("Sent a teleport request to ")
                .append(Component.empty().append(recipient.displayName()).color(Command.INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(Command.INFO_COLOR));
        requester.sendMessage(Component.text("To cancel your request, use ")
                .append(Util.clickableCommand("/tpcancel", true).color(Command.INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(Command.INFO_COLOR));

        recipient.sendMessage(Component.empty()
                .append(Component.empty().append(requester.displayName()).color(Command.INFO_ACCENT_COLOR))
                .append(Component.text(" has requested " + request.intent + "."))
                .color(Command.INFO_COLOR));
        recipient.sendMessage(Component.empty()
                .append(Util.clickableCommand("/tpaccept", true).color(Command.INFO_ACCENT_COLOR))
                .append(Component.text(" to accept or "))
                .append(Util.clickableCommand("/tpdeny", true).color(Command.INFO_ACCENT_COLOR))
                .append(Component.text(" to deny."))
                .color(Command.INFO_COLOR));

        Bukkit.getScheduler().runTaskLater(NautilusManager.INSTANCE, () -> {
            if (REQUESTS.get(requester.getUniqueId()) == entry) {
                recipient.sendMessage(Component.text("The teleport request from ")
                        .append(Component.empty().append(requester.displayName()).color(Command.INFO_ACCENT_COLOR))
                        .append(Component.text(" timed out."))
                        .color(Command.INFO_COLOR));

                requester.sendMessage(Component.text("Your teleport request timed out.").color(Command.INFO_COLOR));

                removeRequest(requester, recipient);
            }
        }, TP_REQUEST_TIMEOUT_SECONDS * 20L);
    }

    public static List<UUID> incomingRequests(Player player) {
        return REQUESTS.entrySet().stream()
                .filter(entry -> entry.getValue().getKey().equals(player.getUniqueId()))
                .map(Map.Entry::getKey)
                .toList();
    }

    public static UUID outgoingRequest(Player player) {
        return REQUESTS.containsKey(player.getUniqueId()) ? REQUESTS.get(player.getUniqueId()).getKey() : null;
    }

    public static void removeRequest(Player requester, Player recipient) {
        REQUESTS.remove(requester.getUniqueId());
        if (LAST_REQUESTS.get(recipient.getUniqueId()) == requester.getUniqueId()) {
            LAST_REQUESTS.remove(recipient.getUniqueId());
        }
    }

    private static Player getRequester(Player recipient, String requesterName) {
        Player requester;
        if (requesterName == null) {
            if (!LAST_REQUESTS.containsKey(recipient.getUniqueId())) {
                recipient.sendMessage(NO_PENDING_TP_REQUEST_ERROR);
                return null;
            }

            requester = Bukkit.getPlayer(LAST_REQUESTS.get(recipient.getUniqueId()));
        } else {
            requester = Util.getOnlinePlayer(requesterName);
        }

        if (requester == null) {
            recipient.sendMessage(Command.INVALID_PLAYER_ERROR);
            return null;
        }

        if (!recipient.getUniqueId().equals(outgoingRequest(requester))) {
            recipient.sendMessage(NO_PENDING_TP_REQUEST_ERROR);
            return null;
        }

        return requester;
    }

    public static void performTeleport(Player recipient, Player requester, TeleportRequest request) {
        if (request == TeleportRequest.REQUESTER_TO_RECIPIENT) {
            TeleportHandler.teleportAfterDelay(
                    requester,
                    recipient::getLocation,
                    TeleportHandler.DEFAULT_TELEPORT_DELAY_TICKS,
                    () -> {
                        recipient.sendMessage(Component.text("Teleport canceled; ")
                                .append(requester.displayName())
                                .append(Component.text(" moved!"))
                                .color(Command.ERROR_COLOR));
                    }
            );
        } else if (request == TeleportRequest.RECIPIENT_TO_REQUESTER) {
            TeleportHandler.teleportAfterDelay(
                    recipient,
                    requester::getLocation,
                    TeleportHandler.DEFAULT_TELEPORT_DELAY_TICKS,
                    () -> {
                        requester.sendMessage(Component.text("Teleport canceled; ")
                                .append(recipient.displayName())
                                .append(Component.text(" moved!"))
                                .color(Command.ERROR_COLOR));
                    }
            );
        }
    }

    public static void acceptRequest(Player recipient, String requesterName) {
        Player requester = getRequester(recipient, requesterName);
        if (requester == null) return;

        recipient.sendMessage(Component.text("Accepted the teleport request from ")
                .append(requester.displayName().color(Command.INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(Command.INFO_COLOR));

        requester.sendMessage(recipient.displayName().color(Command.INFO_ACCENT_COLOR)
                .append(Component.text(" accepted your teleport request."))
                .color(Command.INFO_COLOR));

        performTeleport(recipient, requester, REQUESTS.get(requester.getUniqueId()).getValue());

        removeRequest(requester, recipient);
    }

    public static void denyRequest(Player recipient, String requesterName) {
        Player requester = getRequester(recipient, requesterName);
        if (requester == null) return;

        recipient.sendMessage(Component.text("Denied the teleport request from ")
                .append(requester.displayName().color(Command.INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(Command.INFO_COLOR));

        requester.sendMessage(recipient.displayName().color(Command.INFO_ACCENT_COLOR)
                .append(Component.text(" denied your teleport request."))
                .color(Command.INFO_COLOR));

        removeRequest(requester, recipient);
    }

    public static void cancelRequest(Player requester) {
        UUID recipientID = outgoingRequest(requester);
        if (recipientID == null) {
            requester.sendMessage(NO_OUTGOING_TP_REQUEST_ERROR);
            return;
        }
        Player recipient = Bukkit.getPlayer(recipientID);
        if (recipient == null) {
            requester.sendMessage(Command.INVALID_PLAYER_ERROR);
            return;
        }

        requester.sendMessage(Component.text("Canceled your teleport request to ")
                .append(Component.empty().append(recipient.displayName()).color(Command.INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(Command.INFO_COLOR));

        recipient.sendMessage(Component.empty().color(Command.INFO_ACCENT_COLOR).append(requester.displayName())
                .append(Component.text(" canceled their teleport request."))
                .color(Command.INFO_COLOR));

        removeRequest(requester, recipient);
    }
}
