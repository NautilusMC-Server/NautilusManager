package org.nautilusmc.nautilusmanager.crews;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.sql.SQLHandler;
import org.nautilusmc.nautilusmanager.util.Permission;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class CrewHandler implements Listener {
    private static final List<Crew> CREWS = new ArrayList<>();
    private static final List<War> WARS = new ArrayList<>();

    public static SQLHandler crewDatabase;
    public static SQLHandler playerCrewDatabase;
    public static SQLHandler warDatabase;

    public static void init() {
        crewDatabase = new SQLHandler("crews") {
            @Override
            public void update(ResultSet results) throws SQLException {
                CREWS.clear();
                while (results.next()) {
                    if (!Arrays.asList(Bukkit.getOfflinePlayers())
                            .contains(Bukkit.getOfflinePlayer(UUID.fromString(results.getString("captain"))))) {
                        continue;
                    }
                    CREWS.add(new Crew(
                            UUID.fromString(results.getString("uuid")),
                            Bukkit.getOfflinePlayer(UUID.fromString(results.getString("captain"))),
                            results.getString("name"),
                            results.getBoolean("open"),
                            results.getString("prefix")
                    ));
                }
            }
        };

        Bukkit.getScheduler().runTaskLater(NautilusManager.INSTANCE, () -> playerCrewDatabase = new SQLHandler("player_crews") {
            @Override
            public void update(ResultSet results) throws SQLException {
                CREWS.forEach(crew -> crew.getMembers().clear());
                CREWS.forEach(Crew::clearTeam);
                while (results.next()) {
                    UUID uuid = UUID.fromString(results.getString("uuid"));

                    if (Bukkit.getOfflinePlayer(uuid).getName() == null) {
                        Bukkit.getLogger().log(Level.WARNING, "Player with uuid " + results.getString("uuid") + " not found");
                        continue;
                    }
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

                    Crew crew = getCrew(UUID.fromString(results.getString("crew")));
                    if (crew == null) {
                        Bukkit.getLogger().log(Level.WARNING, "Crew " + results.getString("crew") + " not found!");
                        continue;
                    }
                    crew.addMember(player);
                }
                // Clean up empty crews if they exist
                for (Crew crew : CREWS) {
                    if (crew.getMembers().isEmpty()) {
                        deleteCrew(crew);
                    }
                }
            }
        },0);
        Bukkit.getScheduler().runTaskLater(NautilusManager.INSTANCE, () -> warDatabase = new SQLHandler("wars") {
            @Override
            public void update(ResultSet results) throws SQLException {
                WARS.clear();
                CREWS.forEach(crew -> crew.getWars().clear());
                while (results.next()) {
                    UUID uuid = UUID.fromString(results.getString("uuid"));
                    if (getWar(uuid) != null) {
                        continue;
                    }
                    Crew attacker = getCrew(UUID.fromString(results.getString("attacker")));
                    if (attacker == null) {
                        this.deleteEntry(uuid.toString());
                        continue;
                    }
                    Crew defender = getCrew(UUID.fromString(results.getString("defender")));
                    if (defender == null) {
                        this.deleteEntry(uuid.toString());
                        continue;
                    }

                    War war = new War(attacker, defender);
                    attacker.getWars().add(war);
                    defender.getWars().add(war);
                    WARS.add(war);
                }
            }
        }, 0);
    }

    public static void registerCrew(Crew crew) {
        CREWS.add(crew);
        crewDatabase.setValues(crew.getUuid().toString(), Map.of(
                "name", crew.getName(),
                "captain",crew.getCaptain().getUniqueId().toString(),
                "open", crew.isOpen(),
                "prefix", crew.getPrefix()));
        playerCrewDatabase.setValues(crew.getCaptain().getUniqueId().toString(), Map.of("crew", crew.getUuid().toString()));
    }

    public static void deleteCrew(Crew crew) {
        CREWS.remove(crew);
        crew.removeAllMembers();
        List<War> endedWars = crew.getWars();
        endedWars.forEach(CrewHandler::endWar);

        crew.deleteTeam();
        crewDatabase.deleteEntry(crew.getUuid().toString());
    }

    public static List<Crew> getCrews() {
        return CREWS;
    }

    public static List<War> getWars() {
        return WARS;
    }

    public static boolean isCrewMember(OfflinePlayer player) {
        return Permission.hasGroup(player, "crewmember");
    }

    public static boolean isCaptain(OfflinePlayer player) {
        return Permission.hasGroup(player, "captain");
    }

    public static Crew getCrew(OfflinePlayer player) {
        for (Crew crew : CREWS) {
            if (crew.containsPlayer(player)) {
                return crew;
            }
        }
        return null;
    }

    public static Crew getCrew(String name) {
        for (Crew crew : CREWS) {
            if (crew.getName().equals(name)) {
                return crew;
            }
        }
        return null;
    }

    public static Crew getCrew(UUID uuid) {
        for (Crew crew : CREWS) {
            if (crew.getUuid().equals(uuid)) {
                return crew;
            }
        }
        return null;
    }

    public static void registerWar(War war) {
        WARS.add(war);
        war.getAttacker().addWar(war);
        war.getDefender().addWar(war);
        warDatabase.setValues(war.getUuid().toString(), Map.of(
                "attacker", war.getAttacker().getUuid().toString(),
                "defender", war.getDefender().getUuid().toString()
        ));
    }

    public static void endWar(War war) {
        WARS.remove(war);
        war.getAttacker().removeWar(war);
        war.getDefender().removeWar(war);
        warDatabase.deleteEntry(war.getUuid().toString());
    }

    public static War getWar(UUID uuid) {
        for (War war : WARS) {
            if (war.getUuid().equals(uuid)) {
                return war;
            }
        }
        return null;
    }

    // Updates player perms if crew was deleted or if they were kicked/made captain
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Crew crew = getCrew(player);

        if (crew == null && (isCrewMember(player) || isCaptain(player))) {
            Permission.removeGroup(player, "captain");
            Permission.removeGroup(player, "crewmember");
            player.sendMessage(Component.text("You are no longer a part of your crew.", Command.INFO_COLOR));
        }
        else if (crew != null && crew.getCaptain().equals(player) && !isCaptain(player)) {
            if (Permission.removeGroup(player, "crewmember"))  {
                player.sendMessage(Component.text("You were promoted to captain of your crew.", Command.INFO_COLOR));
            }
            Permission.addGroup(player, "captain");
        }
    }
}
