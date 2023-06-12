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
    private static List<Crew> CREWS;
    private static List<War> wars;

    public static SQLHandler CREW_HANDLER;
    public static SQLHandler PLAYER_CREW_HANDLER;
    public static SQLHandler WAR_HANDLER;

    public static void init() {
        CREWS = new ArrayList<>();
        wars = new ArrayList<>();
        CREW_HANDLER = new SQLHandler("crews") {
            @Override
            public void updateSQL(ResultSet results) throws SQLException {
                //Bukkit.getLogger().log(Level.INFO, "Updating \"crews\"");
                CREWS.clear();
                while(results.next()) {
                    if (!Arrays.stream(Bukkit.getOfflinePlayers()).toList().contains(Bukkit.getOfflinePlayer(UUID.fromString(results.getString("captain"))))) {
                        continue;
                    }
                    CREWS.add(new Crew(
                            UUID.fromString(results.getString("uuid")),
                            Bukkit.getOfflinePlayer(UUID.fromString(results.getString("captain"))),
                            results.getString("name"),
                            results.getBoolean("open"),
                            results.getString("prefix")));
                }
            }
        };

        Bukkit.getScheduler().runTaskLater(NautilusManager.INSTANCE, (Runnable) () -> PLAYER_CREW_HANDLER = new SQLHandler("player_crews") {
            @Override
            public void updateSQL(ResultSet results) throws SQLException {
                //Bukkit.getLogger().log(Level.INFO, "Updating \"player_crews\"");
                CREWS.forEach(crew -> crew.getMembers().clear());
                CREWS.forEach(Crew::clearTeam);
                while (results.next()) {
                    UUID uuid = UUID.fromString(results.getString("uuid"));

                    if (Bukkit.getOfflinePlayer(uuid).getName() == null) {
                        Bukkit.getLogger().log(Level.WARNING, "Player with uuid " + results.getString("uuid") + " not found");
                        continue;
                    }
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

                    if (getCrew(UUID.fromString(results.getString("crew"))) == null) {
                        Bukkit.getLogger().log(Level.WARNING, "Crew " + results.getString("crew") + " not found!");
                        continue;
                    }
                    Crew crew = getCrew(UUID.fromString(results.getString("crew")));
                    crew.addMember(player);
                }
                //If crews are empty -> delete them
                List<Crew> emptyCrews =  CREWS.stream().filter(crew -> crew.getMembers().isEmpty()).toList();
                for (Crew crew : emptyCrews) {
                    deleteCrew(crew);
                }
            }
        },0);
        Bukkit.getScheduler().runTaskLater(NautilusManager.INSTANCE, (Runnable) () -> WAR_HANDLER = new SQLHandler("wars") {
            @Override
            public void updateSQL(ResultSet results) throws SQLException {
                //Bukkit.getLogger().log(Level.INFO, "Updating \"wars\"");
                wars.clear();
                CREWS.forEach(crew -> crew.getWars().clear());
                while (results.next()) {
                    UUID uuid = UUID.fromString(results.getString("uuid"));
                    if (getWar(uuid) != null) {
                        continue;
                    }
                    //make sure attacker exists
                    if (getCrew(UUID.fromString(results.getString("attacker"))) == null) {
                        this.deleteSQL(uuid.toString());
                        continue;
                    };
                    Crew attacker = getCrew(UUID.fromString(results.getString("attacker")));

                    if (getCrew(UUID.fromString(results.getString("defender"))) == null) {
                        this.deleteSQL(uuid.toString());
                        continue;
                    };
                    Crew defender = getCrew(UUID.fromString(results.getString("defender")));

                    War war = new War(attacker, defender);
                    attacker.getWars().add(war);
                    defender.getWars().add(war);
                    wars.add(war);
                }
            }
        }, 0);
    }

    public static void registerCrew(Crew crew) {
        CREWS.add(crew);
        CREW_HANDLER.setSQL(crew.getUuid().toString(), Map.of(
                "name", crew.getName(),
                "captain",crew.getCaptain().getUniqueId().toString(),
                "open", crew.isOpen(),
                "prefix", crew.getPrefix()));
        PLAYER_CREW_HANDLER.setSQL(crew.getCaptain().getUniqueId().toString(), Map.of("crew", crew.getUuid().toString()));
    }

    public static void deleteCrew(Crew crew) {
        CREWS.remove(crew);
        crew.removeAllMembers();
        List<War> endedWars = crew.getWars();
        endedWars.forEach(CrewHandler::endWar);

        crew.deleteTeam();
        CREW_HANDLER.deleteSQL(crew.getUuid().toString());
    }

    public static List<Crew> getCrews() {
        return CREWS;
    }
    public static List<War> getWars() {
        return wars;
    }
    public static Crew getCrew(OfflinePlayer player) {
        Crew returned = null;
        for (int i = 0; i < CREWS.size(); i++) {
            if (CREWS.get(i).containsPlayer(player)) {
                returned = CREWS.get(i);
            }
        }
        return returned;
    }

    public static Crew getCrew(String name) {
        Crew returned = null;
        for (int i = 0; i < CREWS.size(); i++) {
            if (CREWS.get(i).getName().equals(name)) {
                returned = CREWS.get(i);
            }
        }
        return returned;
    }

    public static Crew getCrew(UUID uuid) {
        Crew returned = null;
        for (Crew crew : CREWS) {
            if (crew.getUuid().equals(uuid)) {
                returned = crew;
            }
        }
        return returned;
    }

    public static void registerWar(War war) {
        wars.add(war);
        war.getAttacker().addWar(war);
        war.getDefender().addWar(war);
        WAR_HANDLER.setSQL(war.getUuid().toString(), Map.of(
                "attacker", war.getAttacker().getUuid().toString(),
                "defender", war.getDefender().getUuid().toString()
        ));
    }

    public static void endWar(War war) {
        wars.remove(war);
        war.getAttacker().removeWar(war);
        war.getDefender().removeWar(war);
        WAR_HANDLER.deleteSQL(war.getUuid().toString());
    }

    public static War getWar(UUID uuid) {
        War returned = null;
        for (int i = 0; i < wars.size(); i++) {
            if (wars.get(i).getUuid().equals(uuid)) {
                returned = wars.get(i);
            }
        }
        return returned;
    }

    //Updates player perms if crew was deleted or if they were kicked/made captain
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (getCrew(player) == null && (player.hasPermission("group.captain") || player.hasPermission("group.crewmember"))) {
            Permission.removeGroup(player, "captain");
            Permission.removeGroup(player, "crewmember");
            player.sendMessage(Component.text("You are no longer a part of your crew.").color(Command.INFO_COLOR));
            return;
        }
        if (getCrew(player).getCaptain().equals(player) && !player.hasPermission("group.captain")) {
            if (Permission.removeGroup(player, "crewmember"))  {
                player.sendMessage(Component.text("You were promoted to captain of your crew.").color(Command.INFO_COLOR));
            }
            Permission.addGroup(player, "captain");
        }
    }
}
