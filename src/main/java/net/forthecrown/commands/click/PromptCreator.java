package net.forthecrown.commands.click;

import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public interface PromptCreator {
    Component prompt(User user);

    static Formatted formatted(Formatted creator) {
        return creator;
    }

    interface Formatted extends PromptCreator {
        Component unformatted(User user);

        @Override
        default Component prompt(User user) {
            Component unformatted = unformatted(user);

            if (unformatted == null) {
                return null;
            }

            return Component.text("[")
                    .color(NamedTextColor.AQUA)
                    .append(unformatted)
                    .append(Component.text("]"));
        }
    }
}