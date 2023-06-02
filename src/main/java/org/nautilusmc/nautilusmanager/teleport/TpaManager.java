package org.nautilusmc.nautilusmanager.teleport;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.events.TeleportHandler;
import org.nautilusmc.nautilusmanager.sql.SQLSyncedPerPlayerList;
import org.nautilusmc.nautilusmanager.teleport.commands.homes.HomeCommand;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.*;

public class TpaManager implements Listener {

    public static int MAX_TRUSTED = 99; // the SQL can have 2 digits

    private static final Map<UUID, Map.Entry<UUID, TpRequestType>> requests = new HashMap<>(); // from Request Maker to Request Receiver, Request Type
    private static final Map<UUID, UUID> lastRequests = new HashMap<>(); // from Request Maker to Request Receiver


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

        Map.Entry<UUID, TpRequestType> entry = Map.entry(requested.getUniqueId(), type);
        requests.put(requester.getUniqueId(), entry);
        lastRequests.put(requested.getUniqueId(), requester.getUniqueId());

        requester.sendMessage(Component.text("Sent a request to ")
                .append(Component.empty().color(NautilusCommand.ACCENT_COLOR).append(requested.displayName()))
                .color(NautilusCommand.MAIN_COLOR));
        requester.sendMessage(Util.clickableCommand("/tpcancel", true).color(NautilusCommand.ACCENT_COLOR)
                .append(Component.text(" to cancel").color(NautilusCommand.MAIN_COLOR)));

        requested.sendMessage(Component.empty().color(NautilusCommand.ACCENT_COLOR).append(requester.displayName())
                .append(Component.text(" has requested " + type.message))
                .color(NautilusCommand.MAIN_COLOR));
        requested.sendMessage(Util.clickableCommand("/tpaccept", true).color(NautilusCommand.ACCENT_COLOR)
                .append(Component.text(" to accept or ").color(NautilusCommand.MAIN_COLOR))
                .append(Util.clickableCommand("/tpdeny", true).color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text(" to deny").color(NautilusCommand.MAIN_COLOR)));

        Bukkit.getScheduler().runTaskLater(NautilusManager.INSTANCE, () -> {
            if (requests.get(requester.getUniqueId()) == entry) {
                requested.sendMessage(Component.empty().color(NautilusCommand.ACCENT_COLOR).append(requester.displayName())
                        .append(Component.text("'s request timed out"))
                        .color(NautilusCommand.MAIN_COLOR));

                requester.sendMessage(Component.text("Request timed out").color(NautilusCommand.MAIN_COLOR));

                removeRequest(requester, requested);
            }
        }, 5 * 60 * 20L);
    }

    public static List<UUID> incomingRequests(Player player) {
        return requests.entrySet().stream()
                .filter(entry -> entry.getValue().getKey().equals(player.getUniqueId()))
                .map(Map.Entry::getKey)
                .toList();
    }

    public static UUID outgoingRequest(Player player) {
        return requests.containsKey(player.getUniqueId()) ? requests.get(player.getUniqueId()).getKey() : null;
    }

    public static void removeRequest(Player requester, Player requested) {
        requests.remove(requester.getUniqueId());
        if (lastRequests.get(requested.getUniqueId()) == requester.getUniqueId()) {
            lastRequests.remove(requested.getUniqueId());
        }
    }

    private static Player getRequester(Player requested, String requester) {
        Player requesterPlayer;
        if (requester == null) {
            if (!lastRequests.containsKey(requested.getUniqueId())) {
                requested.sendMessage(Component.text("No pending requests").color(HomeCommand.ERROR_COLOR));
                return null;
            }

            requesterPlayer = Bukkit.getPlayer(lastRequests.get(requested.getUniqueId()));
        } else {
            requesterPlayer = Util.getOnlinePlayer(requester);
            if (requesterPlayer == null) {
                requested.sendMessage(Component.text("Player not found").color(HomeCommand.ERROR_COLOR));
                return null;
            }
        }

        if (!requested.getUniqueId().equals(outgoingRequest(requesterPlayer))) {
            requested.sendMessage(Component.text("No pending request from that player").color(HomeCommand.ERROR_COLOR));
            return null;
        }

        return requesterPlayer;
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

    public static void acceptRequest(Player requested, String requester) {
        Player requesterPlayer = getRequester(requested, requester);
        if (requesterPlayer == null) return;

        TpRequestType type = requests.get(requesterPlayer.getUniqueId()).getValue();
        performTp(requested, requesterPlayer, type);


        requested.sendMessage(Component.text("Accepted ")
                .append(Component.empty().color(NautilusCommand.ACCENT_COLOR).append(requesterPlayer.displayName()))
                .append(Component.text("'s request"))
                .color(NautilusCommand.MAIN_COLOR));
        requesterPlayer.getPlayer().sendMessage(Component.empty().color(NautilusCommand.ACCENT_COLOR).append(requested.displayName())
                .append(Component.text(" has accepted the request"))
                .color(NautilusCommand.MAIN_COLOR));

        removeRequest(requesterPlayer, requested);
    }

    public static void denyRequest(Player requested, String requester) {
        Player requesterPlayer = getRequester(requested, requester);
        if (requesterPlayer == null) return;

        requested.sendMessage(Component.text("Denied ")
                .append(Component.empty().color(NautilusCommand.ACCENT_COLOR).append(requesterPlayer.displayName()))
                .append(Component.text("'s request"))
                .color(NautilusCommand.MAIN_COLOR));

        requesterPlayer.sendMessage(Component.empty().color(NautilusCommand.ACCENT_COLOR).append(requested.displayName())
                .append(Component.text(" has denied the request"))
                .color(NautilusCommand.MAIN_COLOR));

        removeRequest(requesterPlayer, requested);
    }

    public static void cancelRequest(Player requester) {
        UUID requestedUUID = outgoingRequest(requester);
        if (requestedUUID == null) {
            requester.sendMessage(Component.text("No outgoing request found").color(HomeCommand.ERROR_COLOR));
            return;
        }
        Player requested = Bukkit.getPlayer(requestedUUID);

        requester.sendMessage(Component.text("Canceled your request to ")
                .append(Component.empty().color(NautilusCommand.ACCENT_COLOR).append(requested.displayName()))
                .color(NautilusCommand.MAIN_COLOR));

        requested.sendMessage(Component.empty().color(NautilusCommand.ACCENT_COLOR).append(requester.displayName())
                .append(Component.text(" has canceled the request"))
                .color(NautilusCommand.MAIN_COLOR));

        removeRequest(requester, requested);
    }

    public enum TpRequestType {
        TP_TO("to teleport to you"),
        TP_HERE("that you teleport to them");

        public final String message;

        TpRequestType(String message) {
            this.message = message;
        }
    }
}
