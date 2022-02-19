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
                .addText(Component.text("Settings:"))
                .addEmptyLine()
                .addText(Component.text(2))
                .addText(Component.text(3))
                .addText(Component.text(4))
                .addText(Component.text(5))
                .addText(Component.text(6))
                .addText(Component.text(7))
                .addText(Component.text(8))
                .addText(Component.text(9))
                .addText(Component.text(10))
                .addText(Component.text(11))
                .addText(Component.text(12))
                .addText(Component.text(13))
                .addText(Component.text(14))
                .addText(Component.text(15))
                .addText(Component.text(16))
                .addText(Component.text(17))
                .newPage()
                .addText(Component.text("This is my really long text that should increase the counter by a few lines idk how many we'll see lol"))
                .addAmountOfLines()
                .build();
                /*.addText(Component.text("Option: ")
                        .append(Component.text("[findpole]")
                                .hoverEvent(HoverEvent.showText(Component.text("First option")))
                                .clickEvent(ClickEvent.runCommand("/findpole"))
                                .color(NamedTextColor.YELLOW))
                        .append(Component.text("[spawn]")
                                .hoverEvent(HoverEvent.showText(Component.text("Second option")))
                                .clickEvent(ClickEvent.runCommand("/spawn"))
                                .color(NamedTextColor.YELLOW))
                )*/
    }
}
