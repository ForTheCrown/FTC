package net.forthecrown.commands.manager;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.commands.help.FtcHelpMap;
import net.forthecrown.commands.help.UsageFactory;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.AbstractCommand;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import org.jetbrains.annotations.NotNull;

public abstract class FtcCommand extends AbstractCommand {

  public static final String DEFAULT_DESCRIPTION = "An FTC command";

  @Getter
  private boolean simpleUsages = false;

  protected FtcCommand(@NotNull String name) {
    super(name);

    setPermission(Permissions.registerCmd(getName()));
    setDescription(DEFAULT_DESCRIPTION);

    FtcHelpMap.getInstance().addCommand(this);
  }

  public String getHelpListName() {
    return getName();
  }

  protected static User getUserSender(CommandContext<CommandSource> c)
      throws CommandSyntaxException
  {
    return Users.get(c.getSource().asPlayer());
  }

  public void populateUsages(UsageFactory factory) {
  }

  public void simpleUsages() {
    simpleUsages = true;
  }
}