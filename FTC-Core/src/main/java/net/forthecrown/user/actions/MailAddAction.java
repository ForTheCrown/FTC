package net.forthecrown.user.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserMail;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.Struct;
import net.kyori.adventure.text.Component;

import javax.annotation.Nullable;
import java.util.UUID;

public class MailAddAction implements UserAction, Struct {
    private final Component text;
    private final CrownUser user;
    private final UserMail mail;
    private final UUID sender;
    private boolean validateSender;

    public MailAddAction(Component text, CrownUser user, UUID sender) {
        this.text = text;
        this.user = user;
        this.sender = sender;

        mail = user.getMail();
    }

    public MailAddAction(Component text, CrownUser user) {
        this(text, user, null);
    }

    public @Nullable UUID getSender() {
        return sender;
    }

    public boolean hasSender() {
        return sender != null;
    }

    public UserMail getMail() {
        return mail;
    }

    public CrownUser getUser() {
        return user;
    }

    public Component getText() {
        return text;
    }

    public boolean shouldValidateSender() {
        return validateSender;
    }

    public void setValidateSender(boolean validateSender) {
        this.validateSender = validateSender;
    }

    @Override
    public void handle(UserActionHandler handler) {
        try {
            handler.handleMailAdd(this);
        } catch (CommandSyntaxException e) {
            //Only thrown when there is a sender
            FtcUtils.handleSyntaxException(UserManager.getUser(getSender()), e);
        }
    }
}
