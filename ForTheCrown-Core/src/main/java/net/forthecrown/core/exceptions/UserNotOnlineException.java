package net.forthecrown.core.exceptions;

import net.forthecrown.core.api.CrownUser;

public class UserNotOnlineException extends RuntimeException{
    public UserNotOnlineException(CrownUser user){
        super(user.getName() + " is not online");
    }
}
