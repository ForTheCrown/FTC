package net.forthecrown.user.actions;

import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.user.CrownUser;

/**
 * Struct for a user vising a region
 */
public class RegionVisit implements UserAction {
    private final CrownUser visitor;
    private final PopulationRegion region;

    public RegionVisit(CrownUser visitor, PopulationRegion region) {
        this.visitor = visitor;
        this.region = region;
    }

    /**
     * Gets the region that's being visited
     * @return The region that's being visited
     */
    public PopulationRegion getRegion() {
        return region;
    }

    /**
     * Gets the user that's visiting the region
     * @return The user that's visiting B)
     */
    public CrownUser getVisitor() {
        return visitor;
    }

    @Override
    public void handle(UserActionHandler handler) {
        handler.handleVisit(this);
    }
}
