package org.nautilusmc.nautilusmanager.crews;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.sql.SQLHandler;
import org.nautilusmc.nautilusmanager.util.PermsUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;


public class CrewHandler implements Listener {
    private static ArrayList<Crew> crews;
    private static ArrayList<War> wars;
    public static SQLHandler CREW_HANDLER;
    public static SQLHandler PLAYER_CREW_HANDLER;
    public static SQLHandler WAR_HANDLER;
    public static void init() {
        crews = new ArrayList<>();
        wars = new ArrayList<>();
        CREW_HANDLER = new SQLHandler("crews") {
            @Override
            public void updateSQL(ResultSet results) throws SQLException {
                //Bukkit.getLogger().log(Level.INFO, "Updating \"crews\"");
                crews.clear();
                while(results.next()) {
                    crews.add(new Crew(
                            UUID.fromString(results.getString("uuid")),
                            Bukkit.getOfflinePlayer(UUID.fromString(results.getString("captain"))),
                            results.getString("name"),
                            results.getBoolean("open")));
                }
            }
        };

        Bukkit.getScheduler().runTaskLater(NautilusManager.INSTANCE, (Runnable) () -> PLAYER_CREW_HANDLER = new SQLHandler("player_crews") {
            @Override
            public void updateSQL(ResultSet results) throws SQLException {
                //Bukkit.getLogger().log(Level.INFO, "Updating \"player_crews\"");
                crews.forEach(crew -> crew.getMembers().clear());
                crews.forEach(Crew::clearTeam);
                while (results.next()) {
                    UUID uuid = UUID.fromString(results.getString("uuid"));
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                    if (getCrew(UUID.fromString(results.getString("crew"))) == null) {
                        Bukkit.getLogger().log(Level.WARNING, "Crew " + results.getString("crew") + " not found!");
                        continue;
                    }
                    Crew crew = getCrew(UUID.fromString(results.getString("crew")));
                    crew.addMember(player);
                }
                //If crews are empty -> delete them
                List<Crew> emptyCrews =  crews.stream().filter(crew -> crew.getMembers().isEmpty()).toList();
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
                crews.forEach(crew -> crew.getWars().clear());
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
                }
            }
        }, 0);
    }

    public static void registerCrew(Crew crew) {
        crews.add(crew);
        CREW_HANDLER.setSQL(crew.getUuid().toString(), Map.of(
                "name", crew.getName(),
                "captain",crew.getCaptain().getUniqueId().toString(),
                "open", crew.isOpen()));
        PLAYER_CREW_HANDLER.setSQL(crew.getCaptain().getUniqueId().toString(), Map.of("crew", crew.getUuid().toString()));
    }

    public static void deleteCrew(Crew crew) {
        crews.remove(crew);
        crew.removeAllMembers();
        for (int i = 0; i < wars.size(); i++) {
            War war = wars.get(i);
            if (war.getAttacker().equals(crew)) {
                war.getDefender().removeWar(war);
                wars.remove(war);
                i--;
            }
            if (war.getDefender().equals(crew)) {
                war.getAttacker().removeWar(war);
                wars.remove(war);
                i--;
            }
        }
        crew.deleteTeam();
        CREW_HANDLER.deleteSQL(crew.getUuid().toString());
    }

    public static ArrayList<Crew> getCrews() {
        return crews;
    }
    public static ArrayList<War> getWars() {
        return wars;
    }
    public static Crew getCrew(OfflinePlayer player) {
        Crew returned = null;
        for (int i = 0; i < crews.size(); i++) {
            if (crews.get(i).containsPlayer(player)) {
                returned = crews.get(i);
            }
        }
        return returned;
    }

    public static Crew getCrew(String name) {
        Crew returned = null;
        for (int i = 0; i < crews.size(); i++) {
            if (crews.get(i).getName().equals(name)) {
                returned = crews.get(i);
            }
        }
        return returned;
    }

    public static Crew getCrew(UUID uuid) {
        Crew returned = null;
        for (Crew crew : crews) {
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
        WAR_HANDLER.setSQL(war.getUuid(), Map.of(
                "attacker", war.getAttacker().getUuid().toString(),
                "defender", war.getDefender().getUuid().toString()
        ));
    }

    public static void endWar(War war) {
        wars.remove(war);
        war.getAttacker().removeWar(war);
        war.getDefender().removeWar(war);
        WAR_HANDLER.deleteSQL(war.getUuid());
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
        if (getCrew(player) == null) {
            PermsUtil.removeGroup(player, "captain");
            PermsUtil.removeGroup(player, "crewmember");
            player.sendMessage(Component.text("You are no longer a part of your crew").color(NautilusCommand.MAIN_COLOR));
            return;
        }
        if (getCrew(player).getCaptain().equals(player)) {
            if (PermsUtil.removeGroup(player, "crewmember"))  {
                player.sendMessage(Component.text("You were made captain of your crew!").color(NautilusCommand.MAIN_COLOR));
            }
            PermsUtil.addGroup(player, "captain");
        } else {
            PermsUtil.removeGroup(player, "captain");
            PermsUtil.addGroup(player, "crewmember");
        }
    }
}
