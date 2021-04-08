package net.forthecrown.core.exceptions;

import net.forthecrown.core.api.CrownUser;

/**
 * An exception that's thrown when a method attempts to use an online only method
 * in the CrownUser class while the user is not online
 */
public class UserNotOnlineException extends RuntimeException{
    public UserNotOnlineException(CrownUser user){
        super(user.getName() + " is not online");
    }
}
