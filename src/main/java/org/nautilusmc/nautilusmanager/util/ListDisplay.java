package org.nautilusmc.nautilusmanager.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.nautilusmc.nautilusmanager.commands.Command;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class ListDisplay<T> {
    public enum Prefix {
        NONE(""),
        HYPHEN(" - "),
        NUMBER_DOT("%d. "::formatted),
        NUMBER_COLON("%d: "::formatted);

        private final Function<Integer, String> generator;

        Prefix(String content) {
            this(number -> content);
        }

        Prefix(Function<Integer, String> generator) {
            this.generator = generator;
        }

        public String generate(int index) {
            return generator.apply(index + 1);
        }
    }

    public static final Component INVALID_PAGE_NUMBER_ERROR = Component.text("Invalid page number!", Command.ERROR_COLOR);

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final Component DEFAULT_EMPTY_MESSAGE = Component.text("The list is empty.", Command.INFO_COLOR);

    private String title;
    private int pageSize;
    private Function<T, Component> formatter = null;
    private Prefix prefix = null;
    private Component emptyMessage = null;
    private List<T> list = null;

    public ListDisplay(String title) {
        this(title, DEFAULT_PAGE_SIZE);
    }

    public ListDisplay(String title, int pageSize) {
        this.title = title;
        this.pageSize = pageSize;
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

    public Prefix getPrefix() {
        return prefix;
    }

    public ListDisplay<T> setPrefix(Prefix prefix) {
        this.prefix = prefix;
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

    public Component fetchPageContent(int pageNumber) {
        if (list == null || list.isEmpty()) {
            return Objects.requireNonNullElse(emptyMessage, DEFAULT_EMPTY_MESSAGE).colorIfAbsent(Command.INFO_COLOR);
        }

        final int pageCount = getPageCount();
        final int page = Math.max(Math.min(pageNumber, pageCount), 1);

        Component content = Component.empty()
                .append(page == 1 ? Component.text("]----- ") : Component.empty()
                        .append(Component.text(Emoji.LEFT + "----- ", Command.INFO_ACCENT_COLOR))
                        .hoverEvent(HoverEvent.showText(Component.text("Previous Page")))
                        .clickEvent(ClickEvent.callback(clicker -> clicker.sendMessage(fetchPageContent(page - 1)))))
                .append(Component.text(title, Command.INFO_ACCENT_COLOR, TextDecoration.BOLD))
                .append(Component.text(" (page " + page + " of " + pageCount + ")"))
                .append(page == pageCount ? Component.text(" -----[") : Component.empty()
                        .append(Component.text(" -----" + Emoji.RIGHT, Command.INFO_ACCENT_COLOR))
                        .hoverEvent(HoverEvent.showText(Component.text("Next Page")))
                        .clickEvent(ClickEvent.callback(clicker -> clicker.sendMessage(fetchPageContent(page + 1)))));

        for (int row = 0; row < pageSize; row++) {
            int index = (page - 1) * pageSize + row;
            if (index >= list.size()) break;

            content = content.appendNewline()
                    .append(Component.text(Objects.requireNonNullElse(prefix, Prefix.HYPHEN).generate(index)))
                    .append(formatter == null ? Component.text(list.get(index).toString()) : formatter.apply(list.get(index)));
        }

        return content.colorIfAbsent(Command.INFO_COLOR);
    }
}
