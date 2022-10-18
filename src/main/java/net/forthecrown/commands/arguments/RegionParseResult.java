package net.forthecrown.commands.arguments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;

public class RegionParseResult {
    private final UserParseResult userParse;
    private PopulationRegion region;

    public RegionParseResult(UserParseResult userParse) {
        this.userParse = userParse;
        this.region = null;
    }

    public RegionParseResult(PopulationRegion region) {
        this.region = region;
        this.userParse = null;
    }

    /**
     * Gets whether the parsed input is a player's name or target selector
     * @return Read the above line
     */
    public boolean isUserRegion() {
        return userParse != null;
    }

    /**
     * Gets a region
     * @param source The source getting the region
     * @param checkInvite Whether this should check if the source was invited to the region
     * @return The parsed region
     * @throws CommandSyntaxException If {@link RegionParseResult#isUserRegion()} is true, then it will
     * be thrown either by the user not having a home region, or if checkInvite is true, then it will
     * be thrown because the source is not invited to the parsed region
     */
    public PopulationRegion getRegion(CommandSource source, boolean checkInvite) throws CommandSyntaxException {
        //If the given input is a player's name
        if (!isUserRegion()) {
            return region;
        }

        User user = userParse.getUser(source, true);
        boolean self = user.getName().equals(source.textName());

        //Get the region cords of their home,
        //If they don't have, throw exception
        RegionPos homeCords = user.getHomes().getHomeRegion();
        if(homeCords == null) {
            if (self) {
                throw Exceptions.NO_HOME_REGION;
            } else {
                throw Exceptions.noHomeRegion(user);
            }
        }

        region = RegionManager.get().get(homeCords);

        if (region.hasName()) {
            return region;
        }

        if (self) {
            return region;
        }

        //Check if source can access the region
        if(source.isPlayer() && checkInvite && !source.hasPermission(Permissions.REGIONS_ADMIN)) {
            User sourceUser = Users.get(source.asPlayer());

            //If source is not allowed to access the region
            if(!sourceUser.getInteractions().hasBeenInvited(user.getUniqueId())) {
                throw Exceptions.notInvited(user);
            }
        }

        return region;
    }
}