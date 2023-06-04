package org.nautilusmc.nautilusmanager.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class ListDisplay<T> {
    private String title;
    private int pageSize;
    private List<T> list;
    private Function<T, Component> formatter;

    public ListDisplay(String title, int pageSize, List<T> list, Function<T, Component> formatter) {
        this.title = title;
        this.pageSize = pageSize;
        this.list = list;
        this.formatter = formatter;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Function<T, Component> getFormatter() {
        return formatter;
    }

    public void setFormatter(Function<T, Component> formatter) {
        this.formatter = formatter;
    }

    public boolean sendPageTo(int page, Player player) {
        try {
            int onlinePlayerCount = Bukkit.getOnlinePlayers().size();
            int pageCount = (int) Math.ceil((double) onlinePlayerCount / PAGE_SIZE);
            int page = Math.max(Math.min(strings.length > 1 ? Integer.parseInt(strings[1]) : 1, pageCount), 1);

            commandSender.sendMessage(Component.text("----- Nicknames (Page " + page + "/" + pageCount + ") -----").color(NautilusCommand.Default.INFO_COLOR).decorate(TextDecoration.BOLD));

            List<? extends Player> playerList = Bukkit.getOnlinePlayers().stream().sorted(Comparator.comparing(Player::getName)).toList();
            for (int i = 0; i < PAGE_SIZE; i++) {
                int playerIndex = (page - 1) * PAGE_SIZE + i;
                if (playerIndex >= onlinePlayerCount) break;
                commandSender.sendMessage(Component.text(" - ").append(generateNicknameListing.apply(playerList.get(playerIndex))).color(NautilusCommand.Default.INFO_COLOR));
            }
        } catch (NumberFormatException e) {
            commandSender.sendMessage(NautilusCommand.ErrorMessage.INVALID_PAGE_NUMBER);
        }
    }
}
