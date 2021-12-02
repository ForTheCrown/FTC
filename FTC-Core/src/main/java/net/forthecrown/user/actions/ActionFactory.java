package net.forthecrown.user.actions;

import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public abstract class ActionFactory {
    private ActionFactory() {}

    public static void addMail(CrownUser user, Component text) {
        addMail(user, text, null);
    }

    public static void addMail(CrownUser user, Component text, UUID sender) {
        UserActionHandler.handleAction(new MailAddAction(text, user, sender));
    }

    public static void visitRegion(CrownUser user, PopulationRegion region) {
        UserActionHandler.handleAction(new RegionVisit(user, region));
    }
}
