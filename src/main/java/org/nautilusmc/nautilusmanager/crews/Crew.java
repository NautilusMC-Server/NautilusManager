package org.nautilusmc.nautilusmanager.crews;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.util.Util;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class Crew {
    private UUID uuid;
    private Team team;
    private OfflinePlayer captain;
    private ArrayList<OfflinePlayer> members;
    private String name;
    private boolean open;
    //private boolean pvp;
    private ArrayList<War> wars;
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
        CrewHandler.CREW_HANDLER.setSQL(uuid.toString(), Map.of("name", name));
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
        CrewHandler.CREW_HANDLER.setSQL(uuid.toString(), Map.of("open", open));
    }


    public OfflinePlayer getCaptain() {
        return captain;
    }

    public void setCaptain(OfflinePlayer captain) {
        this.captain = captain;
        CrewHandler.CREW_HANDLER.setSQL(uuid.toString(), Map.of("captain", captain.getUniqueId().toString()));
    }

    public ArrayList<OfflinePlayer> getMembers() {
        return members;
    }

    public ArrayList<OfflinePlayer> getMembers(boolean excludeCaptain) {
        ArrayList<OfflinePlayer> returned = members;
        if (excludeCaptain) {
            returned.remove(captain);
        }
        return returned;
    }
    public ArrayList<War> getWars() {
        return wars;
    }

    public void setWars(ArrayList<War> wars) {
        this.wars = wars;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isAtWarWith(Crew other) {
        return wars.contains(new War(this, other));
    }

    public void setMembers(ArrayList<OfflinePlayer> members) {
        this.members = members;
        members.forEach(this::addMemberToTeam);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(@NotNull String prefix) {
        this.prefix = prefix;
        CrewHandler.CREW_HANDLER.setSQL(uuid.toString(), Map.of("prefix", prefix));
        updatePrefix();
    }

    private void updatePrefix() {
        if (prefix.equals("")) {
            team.prefix(Component.empty());
        } else {
            team.prefix(Component.text("[")
                    .append(Component.text(prefix).color(NautilusCommand.ACCENT_COLOR))
                    .append(Component.text("]"))
                    .color(NamedTextColor.GRAY));
        }
        this.members.stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).
                forEach(p->Util.updateNameTag(p, p.displayName(), Bukkit.getOnlinePlayers()));
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
        CrewHandler.PLAYER_CREW_HANDLER.setSQL(player.getUniqueId().toString(), Map.of("crew", uuid.toString()));
    }
    public void removeMember(OfflinePlayer player) {
        members.remove(player);
        team.removePlayer(player);
        CrewHandler.PLAYER_CREW_HANDLER.deleteSQL(player.getUniqueId().toString());
    }
    public void removeAllMembers() {
        for (OfflinePlayer player : members) {
            CrewHandler.PLAYER_CREW_HANDLER.deleteSQL(player.getUniqueId().toString());
        }
        members.clear();
        clearTeam();
    }
    public void addWar(War war) {
        wars.add(war);
    }
    public void removeWar(War war) {
        boolean ended = wars.remove(war);
    }
    public War getWar(Crew other) {
        War out = null;
        for (War war : wars) {
            if (war.equals(new War(this, other))) {
                out = war;

            }
        }
        if (out == null) {
            Bukkit.getLogger().log(Level.WARNING, "No war found with between " + name + " and " + other.getName());
        }
        return out;
    }
    public Boolean containsPlayer(OfflinePlayer player) {
        return members.contains(player);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Crew crew = (Crew) o;
        return Objects.equals(uuid, crew.uuid);
    }

    public Component toComponent() {
        Component out = Component.text("--------------------------------").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR);
        out = out.appendNewline();
        out = out.append(Component.text("Name: ").color(NautilusCommand.MAIN_COLOR)
                .append(Component.text(name).color(NautilusCommand.ACCENT_COLOR))
                .appendNewline()
                .append(Component.text("Captain: ").color(NautilusCommand.MAIN_COLOR))
                .append(Component.text(Util.getName(captain)).color(NautilusCommand.ACCENT_COLOR))
                .appendNewline()
                .append(Component.text("Members: ").color(NautilusCommand.MAIN_COLOR)));
        OfflinePlayer member;
        ArrayList<OfflinePlayer> membersNoCaptain = getMembers(false);
        for (int i = 0; i < membersNoCaptain.size(); i++) {
            member = membersNoCaptain.get(i);
            out = out.append(Component.text(Util.getName(member)).color(NautilusCommand.ACCENT_COLOR));
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
        out = out.appendNewline();
        out = out.append(Component.text("Wars: ").color(NautilusCommand.MAIN_COLOR))
                .append(Component.text(wars.isEmpty() ? "None" : warsToString()).color(NautilusCommand.ACCENT_COLOR));
        out = out.appendNewline();
        out = out.append(Component.text("--------------------------------").color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR));
        return out;
    }

    public void sendMessageToMembers(Component component) {
        for (OfflinePlayer player : members) {
            if (player.isOnline()) {
                player.getPlayer().sendMessage(component);
            }
        }
    }
    public void sendMessageToMembers(Component component, boolean excludeCaptain) {
        for (OfflinePlayer player : members) {
            if (player.isOnline() && (!player.equals(captain) || !excludeCaptain)) {
                player.getPlayer().sendMessage(component);
            }
        }
    }

    private void addMemberToTeam(OfflinePlayer offlinePlayer) {
        team.addPlayer(offlinePlayer);
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            Util.updateNameTag(player, player.displayName(), Bukkit.getOnlinePlayers());
        }
    }

    public void deleteTeam() {
        team.unregister();
    }

    public void clearTeam() {
        for (OfflinePlayer player : team.getPlayers()) {
            team.removePlayer(player);
        }
    }
    public ArrayList<String> warsAsStrings() {
        ArrayList<String> out = new ArrayList<>();
        for (War war : wars) {
            out.add(war.getAttacker().equals(this) ? war.getDefender().getName() : war.getAttacker().getName());
        }
        return out;
    }
    private String warsToString() {
        String out = "";
        for (int i = 0; i < wars.size(); i++) {
            War war = wars.get(i);
            out += war.getAttacker().equals(this) ? war.getDefender().getName() : war.getAttacker().getName();
            if (i != wars.size() - 1) {
                out += ", ";
            }
        }
        return out;
    }
}
