package net.forthecrown.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.commands.ToggleCommand;
import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.user.User;
import net.forthecrown.utils.book.BookBuilder;
import net.forthecrown.utils.book.BookSetting;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public final class SettingsBook {
    private SettingsBook() {}

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
        user.openBook(BookBuilder.createSettings(user, text("Settings"), OPTIONS));
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
            return BookSetting.createButton(state, currentState, "", null);
        });

        return node;
    }

    @Getter
    @RequiredArgsConstructor
    private static class ToggleOption implements BookSetting<User> {
        private final ToggleCommand command;
        private final ClickableTextNode denyNode, acceptNode;

        public Component displayName() {
            return text(command.getDisplayName())
                    .hoverEvent(text(command.getDescription()));
        }

        @Override
        public Component createButtons(User user) {
            return text()
                    .append(acceptNode.prompt(user))
                    .append(Component.space())
                    .append(denyNode.prompt(user))
                    .build();
        }

        @Override
        public boolean shouldInclude(User user) {
            if (!user.hasPermission(command.getPermission())) {
                return false;
            }

            return command.allowedInBook(user);
        }
    }
}