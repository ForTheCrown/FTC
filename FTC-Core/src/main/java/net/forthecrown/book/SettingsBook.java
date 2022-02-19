package net.forthecrown.book;

import net.forthecrown.book.builder.BookBuilder;
import net.forthecrown.book.builder.BuiltBook;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class SettingsBook {

    public SettingsBook() {}

    public static void open(CrownUser user) {
        BuiltBook book = initBook(user);
        user.getPlayer().openBook(book.getBookItem());
    }

    private static BuiltBook initBook(CrownUser user) {
        return new BookBuilder()
                .setAuthor(user.getName())
                .setTitle("My test book")
                .addToPage(Component.text("Hello, do these work?"))
                .addToPage(Component.text("My name: " + user.getName()))
                .addToPage(Component.text("Option: ")
                        .append(Component.text("[findpole]")
                                .hoverEvent(HoverEvent.showText(Component.text("First option")))
                                .clickEvent(ClickEvent.runCommand("/findpole"))
                                .color(NamedTextColor.YELLOW))
                        .append(Component.text("[spawn]")
                                .hoverEvent(HoverEvent.showText(Component.text("Second option")))
                                .clickEvent(ClickEvent.runCommand("/spawn"))
                                .color(NamedTextColor.YELLOW))
                )
                .build();
    }
}
