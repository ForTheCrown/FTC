package net.forthecrown.commands.click;

import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public interface PromptCreator {
    Component prompt(CrownUser user);

    static FormattedPromptCreator formatted(FormattedPromptCreator creator) {
        return creator;
    }

    interface FormattedPromptCreator extends PromptCreator {
        Component unformatted(CrownUser user);

        @Override
        default Component prompt(CrownUser user) {
            Component unformatted = unformatted(user);
            if(unformatted == null) return null;

            return Component.text("[")
                    .color(NamedTextColor.AQUA)
                    .append(unformatted)
                    .append(Component.text("]"));
        }
    }
}
