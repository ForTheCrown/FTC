package net.forthecrown.commands.click;

import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;

public interface PromptCreator {
    Component prompt(CrownUser user);
}
