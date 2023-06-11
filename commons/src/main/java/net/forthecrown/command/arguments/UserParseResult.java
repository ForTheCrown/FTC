package net.forthecrown.command.arguments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import net.forthecrown.Permissions;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.types.EntitySelector;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;

public class UserParseResult implements ParseResult<User> {

  private final User user;

  @Getter
  private final EntitySelector selector;

  @Getter
  private final boolean offlineAllowed;

  UserParseResult(User user, boolean offlineAllowed) {
    this.user = user;
    this.selector = null;
    this.offlineAllowed = offlineAllowed;
  }

  UserParseResult(EntitySelector selector, boolean offlineAllowed) {
    this.selector = selector;
    this.user = null;
    this.offlineAllowed = offlineAllowed;
  }

  public User get(CommandSource source, boolean validate)
      throws CommandSyntaxException
  {
    User result = selector != null
        ? Users.get(selector.findPlayer(source))
        : user;

    assert result != null : "Result is null???";

    if (!offlineAllowed && !result.isOnline()) {
      throw Exceptions.notOnline(result);
    }

    if (validate
        && result.get(Properties.VANISHED)
        && !source.hasPermission(Permissions.VANISH_SEE)
        && !offlineAllowed
    ) {
      throw Exceptions.notOnline(result);
    }

    return result;
  }

  public List<User> getUsers(CommandSource source, boolean checkVanished)
      throws CommandSyntaxException
  {
    if (user != null) {
      if (!offlineAllowed
          && checkVanished
          && !source.hasPermission(Permissions.VANISH_SEE)
          && user.get(Properties.VANISHED)
      ) {
        throw Grenadier.exceptions().noPlayerFound();
      }

      return new ArrayList<>(Collections.singletonList(user));
    }

    assert selector != null;

    List<User> users = selector.findPlayers(source)
        .stream()
        .map(Users::get)

        // Optionally filter out vanished users
        .filter(u -> {
          if (checkVanished
              && !source.hasPermission(Permissions.VANISH_SEE)
              && u.get(Properties.VANISHED)
          ) {
            return false;
          }

          if (offlineAllowed) {
            return true;
          }

          return u.isOnline();
        })
        .collect(Collectors.toList());

    if (users.isEmpty()) {
      throw Grenadier.exceptions().noPlayerFound();
    }

    return users;
  }

  public boolean isSelectorUsed() {
    return selector != null;
  }
}