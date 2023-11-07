package net.forthecrown.core.commands.home;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.command.arguments.ParseResult;
import net.forthecrown.core.CoreExceptions;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.core.user.UserHomes;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.user.User;
import net.forthecrown.user.UserLookup.LookupEntry;
import net.forthecrown.user.Users;
import org.bukkit.Location;

@Getter
public class HomeParseResult implements ParseResult<Home> {

  public static final HomeParseResult DEFAULT
      = new HomeParseResult(Readers.EMPTY, UserHomes.DEFAULT);

  private final ImmutableStringReader reader;
  private final LookupEntry user;
  private final String name;
  private final boolean defaultHome;

  public HomeParseResult(ImmutableStringReader reader, LookupEntry user, String name) {
    this.reader = reader;
    this.user = user;
    this.name = name;
    this.defaultHome = UserHomes.DEFAULT.equals(name);
  }

  public HomeParseResult(ImmutableStringReader reader, String name) {
    this(reader, null, name);
  }

  public Home get(CommandSource source, boolean validate)
      throws CommandSyntaxException
  {
    if (user != null) {
      if (validate
          && source.isPlayer()
          && !source.hasPermission(CorePermissions.HOME_OTHERS)
      ) {
        throw exception();
      }

      User u = Users.get(user);
      Location l = u.getComponent(UserHomes.class).get(name);

      if (l == null) {
        throw exception();
      }

      return new Home(name, l);
    }

    User sUser = Users.get(source.asPlayer());
    Location l = sUser.getComponent(UserHomes.class).get(name);

    if (l == null) {
      if (isDefaultHome()) {
        throw CoreExceptions.NO_DEF_HOME;
      } else {
        throw exception();
      }
    }

    return new Home(name, l);
  }

  private CommandSyntaxException exception() {
    return CoreExceptions.unknownHome(reader, reader.getRemaining());
  }
}