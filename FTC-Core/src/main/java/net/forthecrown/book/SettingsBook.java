package net.forthecrown.book;

import net.forthecrown.book.builder.BookBuilder;
import net.forthecrown.book.builder.BuiltBook;
import net.forthecrown.book.builder.TextInfo;
import net.forthecrown.commands.StateChangeCommand;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashSet;
import java.util.Set;

public class SettingsBook {

    public SettingsBook() {}

    private static final Set<StateChangeCommand> cmds = new HashSet<>();
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

        for (StateChangeCommand cmd : cmds) builder.addText(getLine(cmd, user));
        return builder.build();
    }

    private static TextComponent getLine(StateChangeCommand cmd, CrownUser user) {
        TextComponent header = Component.text(cmd.getDisplayName() + ":").hoverEvent(HoverEvent.showText(Component.text(cmd.getDescription())));
        Component options = cmd.getButtonComponent(user); // get from StateChangeCommand?

        String headerText = ChatUtils.plainText(header); // ?
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
        return Component.text().append(header).append(filler).append(options).build();
    }

    private static TextComponent getFillerChar(char c) {
        return Component.text(c).color(NamedTextColor.WHITE);
    }

}
