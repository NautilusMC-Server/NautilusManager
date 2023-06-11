package org.nautilusmc.nautilusmanager.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.nautilusmc.nautilusmanager.commands.Command;

import java.util.List;
import java.util.function.Function;

public class ListDisplay<T> {
    public static final Component INVALID_PAGE_NUMBER_ERROR = Component.text("Invalid page number!").color(Command.ERROR_COLOR);
    
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
    
    public int getPageCount() {
        return Math.max((int) Math.ceil((double) list.size() / pageSize), 1);
    }

    public void sendPageTo(String pageArg, CommandSender recipient) {
        try {
            int pageCount = getPageCount();
            int page = Math.max(Math.min(pageArg == null ? 1 : Integer.parseInt(pageArg), pageCount), 1);

            recipient.sendMessage(Component.text("----- ")
                    .append(Component.text(title).color(Command.INFO_ACCENT_COLOR).decorate(TextDecoration.BOLD))
                    .append(Component.text(" (page " + page + " of " + pageCount + ") -----"))
                    .color(Command.INFO_COLOR));

            for (int row = 0; row < pageSize; row++) {
                int index = (page - 1) * pageSize + row;
                if (index >= list.size()) break;
                recipient.sendMessage(Component.text(" - ")
                        .append(formatter.apply(list.get(index)))
                        .color(Command.INFO_COLOR));
            }
        } catch (NumberFormatException e) {
            recipient.sendMessage(INVALID_PAGE_NUMBER_ERROR);
        }
    }
}
