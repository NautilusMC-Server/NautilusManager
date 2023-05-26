package org.nautilusmc.nautilusmanager.crews;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.C;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
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
    public ArrayList<Player> getMembers(boolean excludeCaptain) {
        ArrayList<Player> returned = members;
        if (excludeCaptain) {
            returned.remove(captain);
        }
        return returned;
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

    public Component toComponent() {
        Component out  = Component.text("Name: ").color(NautilusCommand.MAIN_COLOR)
                .append(Component.text(name).color(NautilusCommand.ACCENT_COLOR))
                .appendNewline()
                .append(Component.text("Captain: ").color(NautilusCommand.MAIN_COLOR))
                .append(Component.text(captain.getName()).color(NautilusCommand.ACCENT_COLOR))
                .appendNewline()
                .append(Component.text("Members: ").color(NautilusCommand.MAIN_COLOR));
        Player member;
        ArrayList<Player> membersNoCaptain = getMembers(true);
        for (int i = 0; i < membersNoCaptain.size(); i++) {
            member = membersNoCaptain.get(i);
            out = out.append(Component.text(Util.getTextContent(member.displayName())).color(NautilusCommand.ACCENT_COLOR));
            if (i < membersNoCaptain.size() - 1) {
                out = out.append(Component.text(", ").color(NautilusCommand.ACCENT_COLOR));
            }
        }
        out = out.append(Component.text("Status: ").color(NautilusCommand.MAIN_COLOR))
                .append(Component.text(isOpen() ? "open" : "closed").color(NautilusCommand.ACCENT_COLOR));
        return out;
    }

    public void sendMessageToMembers(Component component) {
        for (Player player : members) {
            if (player.isOnline()) {
                player.sendMessage(component);
            }
        }
    }
    public void sendMessageToMembers(Component component, boolean excludeCaptain) {
        for (Player player : members) {
            if (player.isOnline() && (!player.equals(captain) || !excludeCaptain)) {
                player.sendMessage(component);
            }
        }
    }
}
