package net.forthecrown.book;

import net.forthecrown.book.builder.BookBuilder;
import net.forthecrown.book.builder.BuiltBook;
import net.forthecrown.book.builder.TextInfo;
import net.forthecrown.commands.StateChangeCommand;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;

public class SettingsBook {

    public SettingsBook() {}

    private static final List<StateChangeCommand> cmds = new ArrayList<>();
    public static void addCmdToBook(StateChangeCommand cmd) { cmds.add(cmd); }

    public static void open(CrownUser user) {
        BuiltBook book = initBook(user);
        user.getPlayer().openBook(book.getBookItem());
    }

    private static BuiltBook initBook(CrownUser user) {
        BookBuilder builder = new BookBuilder()
                .setAuthor(user.getName())
                .setTitle("Settings")
                .addText(Component.text("Settings:"))
                .addEmptyLine();

        for (StateChangeCommand cmd : cmds) getLine(builder, cmd, user);
        return builder.build();
    }

    private static void getLine(BookBuilder builder, StateChangeCommand cmd, CrownUser user) {
        if(builder.getLineCount() + 1 > BookBuilder.MAX_LINES) {
            builder.newPage();
        }

        Component header = cmd.getDisplayName().append(Component.text(":")).hoverEvent(Component.text(cmd.getDescription()));
        Component options = cmd.getButtonComponent(user); // get from StateChangeCommand?

        int headerLength = TextInfo.getPxLength(ChatUtils.plainText(header));
        int optionsLength = TextInfo.getPxLength(ChatUtils.plainText(options));

        Component filler = getFiller(BookBuilder.PIXELS_PER_LINE - (headerLength + optionsLength));

        builder.addText(
                Component.text()
                        .append(header)
                        .append(filler)
                        .append(options)
                        .build()
        );

        /*String headerText = ChatUtils.plainText(header); // ?
        String optionsText = ChatUtils.plainText(header); // 68
        int pxLength = TextInfo.getPxLength(headerText) + TextInfo.getPxLength(optionsText);

        StringBuilder indent = new StringBuilder();
        int pxLengthIndent = BookBuilder.PIXELS_PER_LINE - pxLength; // length of 'blank' space between options and header
        while (pxLengthIndent > 5) {
            indent.append(' ');
            pxLengthIndent -= 4;
        }
        TextComponent indentAdjuster = Component.empty();
        switch (pxLengthIndent) {
            case 5 -> indentAdjuster = getFillerChar('f'); // add white f
            case 4 -> indent.append(' '); // add space
            case 3 -> indentAdjuster = getFillerChar('l'); // add white l
            case 2 -> indentAdjuster = getFillerChar(':'); // add white :
            default -> indentAdjuster = Component.empty(); // uhhhh shouldn't get here, right?? let's hope so
        }
        TextComponent filler = Component.text(indent.toString()).append(indentAdjuster);
        return Component.text().append(header).append(filler).append(options).build();*/
    }

    private static Component getFiller(int amount) {
        int half = amount >> 1;

        // All hail the single pixel I
        return Component.text(".".repeat(half)).color(NamedTextColor.WHITE);
    }

    private static TextComponent getFillerChar(char c) {
        return Component.text(c).color(NamedTextColor.WHITE);
    }

}
