package org.nautilusmc.nautilusmanager.crews;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public class CrewHandler {
    private static ArrayList<Crew> crews;

    public static void init() {
        //I'll deal with SQL stuff later
        crews = new ArrayList<>();
    }
    public static void registerCrew(Crew crew) {
        crews.add(crew);
        updateSQL();
    }
    public static void deleteCrew(Crew crew) {
        crews.remove(crew);
        updateSQL();
    }
    public static void updateSQL() {
        //SQL stuff
    }

    public static ArrayList<Crew> getCrews() {
        return crews;
    }
    public static Crew getCrew(Player player) {
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
}
