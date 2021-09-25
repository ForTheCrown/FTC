package net.forthecrown.user.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserMail;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.Struct;

public class MailQuery implements UserAction, Struct {
    private final CommandSource source;
    private final CrownUser mailUser;
    private final UserMail mail;
    private final boolean onlyUnread;
    private final boolean self;
    private final int page;

    public MailQuery(CommandSource source, CrownUser user, boolean onlyUnread, int page) {
        this.source = source;
        this.onlyUnread = onlyUnread;
        this.page = page;

        self = user.getName().equalsIgnoreCase(source.textName());
        mailUser = user;
        mail = user.getMail();
    }

    public int getPage() {
        return page;
    }

    public boolean isSelfQuery() {
        return self;
    }

    public boolean onlyUnread() {
        return onlyUnread;
    }

    public CommandSource getSource() {
        return source;
    }

    public CrownUser getUser() {
        return mailUser;
    }

    public UserMail getMail() {
        return mail;
    }

    @Override
    public void handle(UserActionHandler handler) {
        try {
            handler.handleMailQuery(this);
        } catch (CommandSyntaxException e) {
            FtcUtils.handleSyntaxException(source.asBukkit(), e);
        }
    }
}
