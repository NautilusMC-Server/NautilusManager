package org.nautilusmc.nautilusmanager.cosmetics;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.nautilusmc.nautilusmanager.sql.SQLSyncedPerPlayerList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MuteManager {

    public static int MAX_MUTED = 99; // the SQL can have 2 digits

    private static SQLSyncedPerPlayerList<UUID, String> muted = new SQLSyncedPerPlayerList<>(String.class, UUID::toString, UUID::fromString, "muted", MAX_MUTED);

    public static void init() {
        muted.initSQL("muted");
    }

    public static boolean isMuted(Player muter, OfflinePlayer player) {
        return muted.contains(muter.getUniqueId(), player.getUniqueId());
    }

    public static List<UUID> getMuted(Player muter) {
        return muted.getOrDefault(muter.getUniqueId(), new ArrayList<>());
    }

    public static boolean toggleMute(Player muter, OfflinePlayer player) {
        if (isMuted(muter, player)) {
            muted.remove(muter.getUniqueId(), player.getUniqueId());
            return false;
        } else {
            return muted.add(muter.getUniqueId(), player.getUniqueId());
        }
    }
}
