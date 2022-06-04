package net.forthecrown.user.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserMail;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.Struct;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class MailAddAction implements UserAction, Struct {
    @Getter private final Component text;
    @Getter private final CrownUser user;
    @Getter private final UserMail mail;

    @Getter private final UUID sender;
    @Setter private boolean validateSender;

    @Setter
    private boolean informSender = true;

    @Getter @Setter
    private UserMail.MailAttachment attachment;

    public MailAddAction(Component text, CrownUser user, UUID sender) {
        this.text = text;
        this.user = user;
        this.sender = sender;

        mail = user.getMail();

        setValidateSender(hasSender());
    }

    public boolean hasSender() {
        return sender != null;
    }

    public boolean shouldValidateSender() {
        return validateSender;
    }

    public boolean informSender() {
        return informSender;
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