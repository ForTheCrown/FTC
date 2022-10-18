package net.forthecrown.book;

import lombok.Getter;
import net.forthecrown.book.builder.BookBuilder;
import net.forthecrown.book.builder.TextInfo;
import net.forthecrown.commands.ToggleCommand;
import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.user.User;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;

import static net.forthecrown.text.Messages.BUTTON_ACCEPT_TICK;
import static net.forthecrown.text.Messages.BUTTON_DENY_CROSS;

public final class SettingsBook {
    private SettingsBook() {}

    public static final int BUTTONS_LENGTH = TextInfo.getPxWidth(BUTTON_DENY_CROSS.content() + " " + BUTTON_ACCEPT_TICK.content());

    private static final ClickableTextNode ROOT_NODE = ClickableTexts.register(new ClickableTextNode("settings"));
    private static final List<ToggleOption> OPTIONS = new ArrayList<>();

    public static void addOption(ToggleCommand cmd) {
        var acceptNode = commandNode(cmd, true);
        var denyNode = commandNode(cmd, false);

        var option = new ToggleOption(cmd, denyNode, acceptNode);
        OPTIONS.add(option);

        ROOT_NODE.addNode(acceptNode);
        ROOT_NODE.addNode(denyNode);
    }

    public static void open(User user) {
        user.openBook(createBook(user));
    }

    private static Book createBook(User user) {
        BookBuilder builder = new BookBuilder()
                .setAuthor(user.getName())
                .setTitle("Settings")
                .addText(Component.text("Settings:"))
                .addEmptyLine();

        for (var option : OPTIONS) {
            var cmd = option.getCommand();

            if (!cmd.test(user.getCommandSource(cmd))) {
                continue;
            }

            if (!cmd.allowedInBook(user)) {
                continue;
            }

            if (builder.getLineCount() + 1 > BookBuilder.MAX_LINES) {
                builder.newPage();
            }

            Component header = option.displayName().append(Component.text(":"));
            Component options = option.buttonComponent(user);
            Component filler = option.getAligner();

            builder.addText(
                    Component.text()
                            .append(header)
                            .append(filler)
                            .append(options)
                            .build()
            );
        }

        return builder.build();
    }

    private static String createNodeId(ToggleCommand command, boolean state) {
        return command.getName() + "_" + state;
    }

    private static ClickableTextNode commandNode(ToggleCommand command, boolean state) {
        ClickableTextNode node = new ClickableTextNode(createNodeId(command, state));

        // We give the node an executor which sets the user's state
        node.setExecutor(user -> {
            command.setState(user, state);
            SettingsBook.open(user); // reopen book
        });

        node.setPrompt(user -> {
            boolean currentState = user.get(command.getProperty());

            var text = (state ? BUTTON_ACCEPT_TICK : BUTTON_DENY_CROSS).toBuilder();

            if (currentState == state) {
                text.color(NamedTextColor.DARK_AQUA)
                        .hoverEvent(null);
            } else {
                text.color(NamedTextColor.GRAY);
            }

            return text.build();
        });

        return node;
    }

    @Getter
    private static class ToggleOption {
        private final ToggleCommand command;
        private final ClickableTextNode denyNode, acceptNode;

        /**
         * The amount of dots used to align the
         * option name and buttons
         */
        private final Component aligner;

        public ToggleOption(ToggleCommand command, ClickableTextNode denyNode, ClickableTextNode acceptNode) {
            this.command = command;
            this.denyNode = denyNode;
            this.acceptNode = acceptNode;

            var name = command.getDisplayName() + ":";
            int occupied = TextInfo.getPxWidth(name) + BUTTONS_LENGTH;
            int remaining = BookBuilder.PIXELS_PER_LINE - occupied;

            this.aligner = Component.text(TextInfo.getFiller(remaining), NamedTextColor.WHITE);
        }

        public Component displayName() {
            return Component.text(command.getDisplayName())
                    .hoverEvent(Component.text(command.getDescription()));
        }

        public Component buttonComponent(User user) {
            return Component.text()
                    .append(acceptNode.prompt(user))
                    .append(Component.space())
                    .append(denyNode.prompt(user))
                    .build();
        }
    }
}