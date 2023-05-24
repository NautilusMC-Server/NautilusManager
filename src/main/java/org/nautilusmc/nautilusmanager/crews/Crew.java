package org.nautilusmc.nautilusmanager.crews;

import org.bukkit.entity.Player;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.Objects;

public class Crew {
    private Player captain;
    private ArrayList<Player> members;
    private String name;
    private boolean open;

    public Crew() {
        members = new ArrayList<>();
        name = null;
        captain = null;
        open = false;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }


    public Crew(Player captain, String name) {
        this.captain = captain;
        this.name = name;
        open = false;
        members = new ArrayList<>();
        members.add(captain);
    }

    public Player getCaptain() {
        return captain;
    }

    public void setCaptain(Player captain) {
        this.captain = captain;
    }

    public ArrayList<Player> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<Player> members) {
        this.members = members;
    }
    public void addMember(Player player) {
        members.add(player);
    }

    public Boolean containsPlayer(Player player) {
        return members.contains(player);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Crew crew = (Crew) o;
        return Objects.equals(captain, crew.captain) && Objects.equals(members, crew.members) && Objects.equals(name, crew.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(captain, members, name);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Name: " + name + "\nCaptain: " + Util.getName(captain) + "\n");
        Player member;
        for (int i = 0; i < members.size(); i++) {
            member = members.get(i);
            if (!(member.equals(captain))) {
                stringBuilder.append(Util.getName(member));
            }
            if (i < members.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }
}
