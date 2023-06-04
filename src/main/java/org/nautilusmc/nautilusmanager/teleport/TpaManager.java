package org.nautilusmc.nautilusmanager.teleport;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand.Default;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand.ErrorMessage;
import org.nautilusmc.nautilusmanager.events.TeleportHandler;
import org.nautilusmc.nautilusmanager.sql.SQLSyncedPerPlayerList;
import org.nautilusmc.nautilusmanager.teleport.commands.homes.HomeCommand;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.*;

public class TpaManager implements Listener {
    public static int MAX_TRUSTED = 99; // the SQL can have 2 digits

    private static final Map<UUID, Map.Entry<UUID, TpRequestType>> requests = new HashMap<>(); // from Request Maker to Request Receiver, Request Type
    private static final Map<UUID, UUID> lastRequests = new HashMap<>(); // from Request Maker to Request Receiver

    public static final int TP_REQUEST_TIMEOUT_SECONDS = 5 * 60;

    // requester : (recipient, request type)
    private static final Map<UUID, Map.Entry<UUID, RequestType>> REQUESTS = new HashMap<>();
    // recipient : requester
    private static final Map<UUID, UUID> LAST_REQUESTS = new HashMap<>();

    private static final SQLSyncedPerPlayerList<UUID, String> trusted = new SQLSyncedPerPlayerList<>(String.class, UUID::toString, UUID::fromString, "trusted", MAX_TRUSTED);

    public static void init() {
        trusted.initSQL("tpa_trustlist");
    }

    public static boolean isTrusted(Player truster, OfflinePlayer player) {
        return trusted.contains(truster.getUniqueId(), player.getUniqueId());
    }

    public static List<UUID> getTrusted(Player truster) {
        return new ArrayList<>(trusted.getOrDefault(truster.getUniqueId(), List.of()));
    }

    /**
     * @return Whether the player is now trusted
     */
    public static boolean toggleTrust(Player truster, OfflinePlayer player) {
        if (isTrusted(truster, player)) {
            trusted.remove(truster.getUniqueId(), player.getUniqueId());
            return false;
        } else {
            return trusted.add(truster.getUniqueId(), player.getUniqueId());
        }
    }

    public static void tpRequest(Player requester, Player requested, TpRequestType type) {
        if (requests.containsKey(requester.getUniqueId())) {
            requester.sendMessage(Component.text("You already have a pending request!").color(HomeCommand.ERROR_COLOR));
            return;
        }

        Map.Entry<UUID, RequestType> entry = Map.entry(recipient.getUniqueId(), type);
        REQUESTS.put(requester.getUniqueId(), entry);
        LAST_REQUESTS.put(recipient.getUniqueId(), requester.getUniqueId());

        requester.sendMessage(Component.text("Sent a teleport request to ")
                .append(Component.empty().append(recipient.displayName()).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(Default.INFO_COLOR));
        requester.sendMessage(Component.text("To cancel your request, use ")
                .append(Util.clickableCommand("/tpcancel", true).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(Default.INFO_COLOR));

        recipient.sendMessage(Component.empty()
                .append(Component.empty().append(requester.displayName()).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text(" has requested " + type.message + "."))
                .color(Default.INFO_COLOR));
        recipient.sendMessage(Component.empty()
                .append(Util.clickableCommand("/tpaccept", true).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text(" to accept or "))
                .append(Util.clickableCommand("/tpdeny", true).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text(" to deny."))
                .color(Default.INFO_COLOR));

        Bukkit.getScheduler().runTaskLater(NautilusManager.INSTANCE, () -> {
            if (REQUESTS.get(requester.getUniqueId()) == entry) {
                recipient.sendMessage(Component.text("The teleport request from ")
                        .append(Component.empty().append(requester.displayName()).color(Default.INFO_ACCENT_COLOR))
                        .append(Component.text(" timed out."))
                        .color(Default.INFO_COLOR));

                requester.sendMessage(Component.text("Your teleport request timed out.").color(Default.INFO_COLOR));

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

    public static void removeRequest(Player requester, Player requested) {
        REQUESTS.remove(requester.getUniqueId());
        if (LAST_REQUESTS.get(requested.getUniqueId()) == requester.getUniqueId()) {
            LAST_REQUESTS.remove(requested.getUniqueId());
        }
    }

    private static Player getRequester(Player recipient, String requesterName) {
        Player requester;
        if (requesterName == null) {
            if (!LAST_REQUESTS.containsKey(recipient.getUniqueId())) {
                recipient.sendMessage(ErrorMessage.NO_PENDING_TP_REQUEST);
                return null;
            }

            requester = Bukkit.getPlayer(LAST_REQUESTS.get(recipient.getUniqueId()));
        } else {
            requester = Util.getOnlinePlayer(requesterName);
        }

        if (requester == null) {
            recipient.sendMessage(ErrorMessage.INVALID_PLAYER);
            return null;
        }

        if (!recipient.getUniqueId().equals(outgoingRequest(requester))) {
            recipient.sendMessage(ErrorMessage.NO_PENDING_TP_REQUEST);
            return null;
        }

        return requester;
    }

    public static void performTp(Player requested, Player requester, TpRequestType type) {
        TeleportHandler.teleportAfterDelay(type == TpRequestType.TP_TO ? requester : requested,
                (type == TpRequestType.TP_TO ? requested : requester)::getLocation,
                5 * 20, () -> {
                    (type == TpRequestType.TP_TO ? requested : requester).sendMessage(Component.text("Teleport canceled, ")
                            .append((type == TpRequestType.TP_TO ? requester : requested).displayName())
                            .append(Component.text(" moved"))
                            .color(NautilusCommand.ERROR_COLOR));
                });
    }

    public static void acceptRequest(Player recipient, String requesterName) {
        Player requester = getRequester(recipient, requesterName);
        if (requester == null) return;

        TpRequestType type = requests.get(requesterPlayer.getUniqueId()).getValue();
        performTp(requested, requesterPlayer, type);


        recipient.sendMessage(Component.text("Accepted the teleport request from ")
                .append(requester.displayName().color(Default.INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(Default.INFO_COLOR));

        requester.sendMessage(recipient.displayName().color(Default.INFO_ACCENT_COLOR)
                .append(Component.text(" accepted your teleport request."))
                .color(Default.INFO_COLOR));

        removeRequest(requester, recipient);
    }

    public static void denyRequest(Player recipient, String requesterName) {
        Player requester = getRequester(recipient, requesterName);
        if (requester == null) return;

        recipient.sendMessage(Component.text("Denied the teleport request from ")
                .append(requester.displayName().color(Default.INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(Default.INFO_COLOR));

        requester.sendMessage(recipient.displayName().color(Default.INFO_ACCENT_COLOR)
                .append(Component.text(" denied your teleport request."))
                .color(Default.INFO_COLOR));

        removeRequest(requester, recipient);
    }

    public static void cancelRequest(Player requester) {
        UUID recipientID = outgoingRequest(requester);
        if (recipientID == null) {
            requester.sendMessage(ErrorMessage.NO_OUTGOING_TP_REQUEST);
            return;
        }
        Player requested = Bukkit.getPlayer(recipientID);
        if (requested == null) {
            requester.sendMessage(ErrorMessage.INVALID_PLAYER);
            return;
        }

        requester.sendMessage(Component.text("Canceled your teleport request to ")
                .append(Component.text(Util.getName(requested)).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(Default.INFO_COLOR));

        requested.sendMessage(Component.empty().color(Default.INFO_ACCENT_COLOR).append(requester.displayName())
                .append(Component.text(" canceled their teleport request."))
                .color(Default.INFO_COLOR));

        removeRequest(requester, requested);
    }
}
