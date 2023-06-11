package net.forthecrown.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.Permissions;
import net.forthecrown.grenadier.AbstractCommand;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;

public abstract class FtcCommand extends AbstractCommand {

  public static final String DEFAULT_DESCRIPTION       = "An FTC command";
  public static final String DEFAULT_PERMISSION_FORMAT = "ftc.commands.{command}";

  @Getter
  private boolean simpleUsages = false;

  public FtcCommand(String name) {
    super(name);

    String perm = DEFAULT_PERMISSION_FORMAT.replace("{command}", name);

    setPermission(Permissions.register(perm));
    getCommand().withDescription(DEFAULT_DESCRIPTION);
  }

  public String getHelpListName() {
    return getName();
  }

  public void simpleUsages() {
    simpleUsages = true;
  }

  public void populateUsages(UsageFactory factory) {

  }

  protected static User getUserSender(CommandContext<CommandSource> context)
      throws CommandSyntaxException
  {
    return Users.get(context.getSource().asPlayer());
  }
}