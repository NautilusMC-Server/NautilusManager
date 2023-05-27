package org.nautilusmc.nautilusmanager.crews;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.Objects;

public class Crew {

    private Team team;
    private Player captain;
    private ArrayList<Player> members;
    private String name;
    private boolean open;
    //private boolean pvp;
    private ArrayList<Crew> atWarWith;
    private String prefix;

    public Crew(Player captain, String name) {
        this.captain = captain;
        this.name = name;
        open = false;
        //pvp = false;
        members = new ArrayList<>();
        atWarWith = new ArrayList<>();
        members.add(captain);
        prefix = "";

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        team = board.getTeam(name);
        if (team == null) team = board.registerNewTeam(name);
        addMemberToTeam(captain);

        setPrefix(prefix);
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
    public ArrayList<Crew> getAtWarWith() {
        return atWarWith;
    }

    public void setAtWarWith(ArrayList<Crew> atWarWith) {
        this.atWarWith = atWarWith;
    }

    public void setMembers(ArrayList<Player> members) {
        this.members = members;
        members.forEach(this::addMemberToTeam);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(@NotNull String prefix) {
        this.prefix = prefix;
        if (prefix.equals("")) {
            team.prefix(Component.empty());
            return;
        }

        team.prefix(Component.text("[")
                .append(Component.text(prefix).color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text("]"))
                .color(NautilusCommand.MAIN_COLOR));

        this.members.forEach(p->Util.updateNameTag(p, p.displayName(), Bukkit.getOnlinePlayers()));
    }

/*
    public boolean allowsPvp() {
        return pvp;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }
*/

    public void addMember(Player player) {
        members.add(player);
        addMemberToTeam(player);
    }
    public void removeMember(Player player) {
        members.remove(player);
        team.removePlayer(player);
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
        ArrayList<Player> membersNoCaptain = getMembers(false);
        for (int i = 0; i < membersNoCaptain.size(); i++) {
            member = membersNoCaptain.get(i);
            out = out.append(Component.text(Util.getTextContent(member.displayName())).color(NautilusCommand.ACCENT_COLOR));
            if (i < membersNoCaptain.size() - 1) {
                out = out.append(Component.text(", ").color(NautilusCommand.ACCENT_COLOR));
            }
        }
        out = out.appendNewline();
        out = out.append(Component.text("Status: ").color(NautilusCommand.MAIN_COLOR))
                .append(Component.text(isOpen() ? "Open" : "Closed for invitation only").color(NautilusCommand.ACCENT_COLOR));
        /*out = out.appendNewline();
        out = out.append(Component.text("PVP: ").color(NautilusCommand.MAIN_COLOR))
                .append(Component.text(allowsPvp() ? "Allowed" : "Not Allowed").color(NautilusCommand.ACCENT_COLOR));*/
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

    private void addMemberToTeam(Player player) {
        team.addPlayer(player);
        Util.updateNameTag(player, player.displayName(), Bukkit.getOnlinePlayers());
    }

    public void deleteTeam() {
        team.unregister();
    }
}
