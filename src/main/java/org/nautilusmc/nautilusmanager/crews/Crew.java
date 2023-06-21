package org.nautilusmc.nautilusmanager.crews;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class Crew {
    public static final int MIN_NAME_LENGTH = 1;
    public static final int MAX_NAME_LENGTH = 24;
    public static final int MIN_PREFIX_LENGTH = 1;
    public static final int MAX_PREFIX_LENGTH = 6;

    private UUID uuid;
    private Team team;
    private OfflinePlayer captain;
    private List<OfflinePlayer> members;
    private String name;
    private boolean open;
    //private boolean pvp;
    private List<War> wars;
    private String prefix;

    public Crew(OfflinePlayer captain, String name) {
        this.captain = captain;
        this.name = name;
        open = false;
        //pvp = false;
        members = new ArrayList<>();
        wars = new ArrayList<>();
        members.add(captain);
        prefix = "";
        uuid = UUID.randomUUID();

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        team = board.getTeam(name);
        if (team == null) team = board.registerNewTeam(name);
        addMemberToTeam(captain);

        setPrefix(prefix);
    }

    //Constructor used in updateSQL()
    public Crew(UUID uuid, OfflinePlayer captain, String name, boolean open, String prefix) {
        this.uuid = uuid;
        this.captain = captain;
        this.name = name;
        this.open = open;
        this.prefix = prefix;
        wars = new ArrayList<>();
        members = new ArrayList<>();

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        if (board.getTeam(name) == null) {
            team = board.registerNewTeam(name);
            updatePrefix();
            addMemberToTeam(captain);
        }
        team = board.getTeam(name);
        if (!team.hasPlayer(captain)) {
            addMemberToTeam(captain);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        CrewHandler.crewDatabase.setSQL(uuid.toString(), Map.of("name", name));
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
        CrewHandler.crewDatabase.setSQL(uuid.toString(), Map.of("open", open));
    }


    public OfflinePlayer getCaptain() {
        return captain;
    }

    public void setCaptain(OfflinePlayer captain) {
        this.captain = captain;
        CrewHandler.crewDatabase.setSQL(uuid.toString(), Map.of("captain", captain.getUniqueId().toString()));
    }

    public List<OfflinePlayer> getMembers() {
        return members;
    }

    public List<OfflinePlayer> getMembers(boolean excludeCaptain) {
        return members.stream().filter(member -> !(excludeCaptain && captain.equals(member))).toList();
    }

    public List<War> getWars() {
        return wars;
    }

    public void setWars(List<War> wars) {
        this.wars = wars;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isAtWarWith(Crew other) {
        return wars.contains(new War(this, other));
    }

    public void setMembers(List<OfflinePlayer> members) {
        this.members = members;
        members.forEach(this::addMemberToTeam);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(@NotNull String prefix) {
        this.prefix = prefix;
        CrewHandler.crewDatabase.setSQL(uuid.toString(), Map.of("prefix", prefix));
        updatePrefix();
    }

    private void updatePrefix() {
        if (prefix.equals("")) {
            team.prefix(Component.empty());
        } else {
            team.prefix(Component.text("[")
                    .append(Component.text(prefix).color(Command.INFO_ACCENT_COLOR))
                    .append(Component.text("]"))
                    .color(NamedTextColor.GRAY));
        }
        this.members.stream()
                .filter(OfflinePlayer::isOnline)
                .map(OfflinePlayer::getPlayer)
                .forEach(player -> Util.updateNameTag(player, player.displayName(), Bukkit.getOnlinePlayers()));
    }

/*
    public boolean allowsPvp() {
        return pvp;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }
*/

    public void addMember(OfflinePlayer player) {
        members.add(player);
        if (!team.hasPlayer(player)) {
            addMemberToTeam(player);
        }
        CrewHandler.playerCrewDatabase.setSQL(player.getUniqueId().toString(), Map.of("crew", uuid.toString()));
    }

    public void removeMember(OfflinePlayer player) {
        members.remove(player);
        team.removePlayer(player);
        CrewHandler.playerCrewDatabase.deleteSQL(player.getUniqueId().toString());
    }

    public void removeAllMembers() {
        for (OfflinePlayer player : members) {
            CrewHandler.playerCrewDatabase.deleteSQL(player.getUniqueId().toString());
        }
        members.clear();
        clearTeam();
    }

    public void addWar(War war) {
        wars.add(war);
    }

    public void removeWar(War war) {
        wars.remove(war);
    }

    public War getWar(Crew other) {
        for (War war : wars) {
            if (war.getAttacker().equals(other) || war.getDefender().equals(other)) {
                return war;
            }
        }
        Bukkit.getLogger().log(Level.WARNING, "No war found between " + name + " and " + other.getName());
        return null;
    }

    public Boolean containsPlayer(OfflinePlayer player) {
        return members.contains(player);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Crew crew)) return false;
        return Objects.equals(uuid, crew.uuid);
    }

    public Component toComponent() {
        Component out = Component.text("--------------------------------").color(Command.INFO_COLOR);
        out = out.appendNewline();
        out = out.append(Component.text("Name: "))
                .append(Component.text(name).color(Command.INFO_ACCENT_COLOR))
                .appendNewline()
                .append(Component.text("Captain: "))
                .append(Component.text(Util.getName(captain)).color(Command.INFO_ACCENT_COLOR))
                .appendNewline()
                .append(Component.text("Members: "))
                .color(Command.INFO_COLOR);
        OfflinePlayer member;
        List<OfflinePlayer> membersNoCaptain = getMembers(true);
        for (int i = 0; i < membersNoCaptain.size(); i++) {
            member = membersNoCaptain.get(i);
            out = out.append(Component.text(Util.getName(member)).color(Command.INFO_ACCENT_COLOR));
            if (i < membersNoCaptain.size() - 1) {
                out = out.append(Component.text(", ").color(Command.INFO_ACCENT_COLOR));
            }
        }
        out = out.appendNewline();
        out = out.append(Component.text("Status: ").color(Command.INFO_COLOR))
                .append(Component.text(isOpen() ? "Open" : "Closed for invitation only").color(Command.INFO_ACCENT_COLOR));
        /*out = out.appendNewline();
        out = out.append(Component.text("PVP: ").color(Command.INFO_COLOR))
                .append(Component.text(allowsPvp() ? "Allowed" : "Not Allowed").color(Command.INFO_ACCENT_COLOR));*/
        out = out.appendNewline();
        out = out.append(Component.text("Wars: ").color(Command.INFO_COLOR))
                .append(Component.text(wars.isEmpty() ? "None" : warsToString()).color(Command.INFO_ACCENT_COLOR));
        out = out.appendNewline();
        out = out.append(Component.text("--------------------------------").color(Command.INFO_COLOR));
        return out;
    }

    public void sendMessageToMembers(Component component) {
        sendMessageToMembers(component, false);
    }

    public void sendMessageToMembers(Component component, boolean excludeCaptain) {
        for (OfflinePlayer member : members) {
            if (member instanceof Player onlineMember && !(excludeCaptain && member.equals(captain))) {
                onlineMember.sendMessage(component);
            }
        }
    }

    private void addMemberToTeam(OfflinePlayer member) {
        team.addPlayer(member);
        if (member instanceof Player onlineMember) {
            Util.updateNameTag(onlineMember, onlineMember.displayName(), Bukkit.getOnlinePlayers());
        }
    }

    public void deleteTeam() {
        team.unregister();
    }

    public void clearTeam() {
        for (String entry : team.getEntries()) {
            team.removeEntry(entry);
        }
    }

    public List<String> warsAsStrings() {
        return wars.stream().map(war -> war.getAttacker().equals(this) ? war.getDefender().getName() : war.getAttacker().getName()).toList();
    }

    private String warsToString() {
        return String.join(", ", warsAsStrings());
    }
}
