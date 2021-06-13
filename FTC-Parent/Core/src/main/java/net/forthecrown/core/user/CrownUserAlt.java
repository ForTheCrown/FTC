package net.forthecrown.core.user;

import java.util.UUID;

/**
 * An alt user, for when a player is considered an alt, it uses their main accounts data
 */
public interface CrownUserAlt extends CrownUser {
    UUID getMainUniqueID();
    CrownUser getMain();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}