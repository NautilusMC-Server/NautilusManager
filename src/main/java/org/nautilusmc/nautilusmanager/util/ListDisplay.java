package org.nautilusmc.nautilusmanager.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.nautilusmc.nautilusmanager.commands.Command;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ListDisplay<T> {
    public static final Component INVALID_PAGE_NUMBER_ERROR = Component.text("Invalid page number!", Command.ERROR_COLOR);

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final Component DEFAULT_EMPTY_MESSAGE = Component.text("The list is empty.", Command.INFO_COLOR);

    private String title;
    private int pageSize;
    private Function<T, Component> formatter;
    private Component emptyMessage;
    private List<T> list;

    public ListDisplay(String title) {
        this(title, DEFAULT_PAGE_SIZE);
    }

    public ListDisplay(String title, int pageSize) {
        this.title = title;
        this.pageSize = pageSize;
        this.formatter = null;
        this.emptyMessage = null;
        this.list = null;
    }

    public String getTitle() {
        return title;
    }

    public ListDisplay<T> setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public ListDisplay<T> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Function<T, Component> getFormatter() {
        return formatter;
    }

    public ListDisplay<T> setFormatter(Function<T, Component> formatter) {
        this.formatter = formatter;
        return this;
    }

    public Component getEmptyMessage() {
        return emptyMessage;
    }

    public ListDisplay<T> setEmptyMessage(Component emptyMessage) {
        this.emptyMessage = emptyMessage;
        return this;
    }

    public List<T> getList() {
        return list;
    }

    public ListDisplay<T> setList(List<T> list) {
        this.list = list;
        return this;
    }

    public int getPageCount() {
        return Math.max((int) Math.ceil((double) list.size() / pageSize), 1);
    }

    public Component fetchPageContent(String pageArg) {
        try {
            return fetchPageContent(pageArg == null ? 1 : Integer.parseInt(pageArg));
        } catch (NumberFormatException e) {
            return INVALID_PAGE_NUMBER_ERROR;
        }
    }

    public Component fetchPageContent(int page) {
        if (list == null || list.isEmpty()) {
            return Objects.requireNonNullElse(emptyMessage, DEFAULT_EMPTY_MESSAGE).colorIfAbsent(Command.INFO_COLOR);
        }

        int pageCount = getPageCount();
        page = Math.max(Math.min(page, pageCount), 1);

        Component content = Component.text("----- ")
                .append(Component.text(title, Command.INFO_ACCENT_COLOR, TextDecoration.BOLD))
                .append(Component.text(" (page " + page + " of " + pageCount + ") -----"))
                .color(Command.INFO_COLOR);

        for (int row = 0; row < pageSize; row++) {
            int index = (page - 1) * pageSize + row;
            if (index >= list.size()) break;

            content = content.appendNewline()
                    .append(formatter == null ? Component.text(list.get(index).toString()) : formatter.apply(list.get(index)))
                    .colorIfAbsent(Command.INFO_COLOR);
        }

        return content;
    }
}
