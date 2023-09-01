package net.forthecrown.mail;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.Exceptions;

public interface MailExceptions {

  CommandSyntaxException CLAIM_NOT_ALLOWED = Exceptions.create("Not allowed to claim mail");

  CommandSyntaxException ALREADY_CLAIMED = Exceptions.create("Mail items already claimed");

}
