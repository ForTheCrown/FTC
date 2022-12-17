package net.forthecrown.commands.arguments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.selectors.EntitySelector;
import net.forthecrown.royalgrenadier.types.selector.EntityArgumentImpl;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;

public class UserParseResult implements ParseResult<User> {

  private final User user;
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
      throws CommandSyntaxException {
    User result = isSelectorUsed() ? Users.get(selector.getPlayer(source)) : user;

    if (validate
        && result.get(Properties.VANISHED)
        && !isOfflineAllowed()
        && !source.hasPermission(Permissions.VANISH_SEE)
    ) {
      throw Exceptions.notOnline(user);
    }

    return result;
  }

  public List<User> getUsers(CommandSource source, boolean checkVanished)
      throws CommandSyntaxException {
    if (selector == null) {
      if (!offlineAllowed
          && checkVanished
          && !source.hasPermission(Permissions.VANISH_SEE)
          && user.get(Properties.VANISHED)
      ) {
        throw EntityArgumentImpl.NO_ENTITIES_FOUND.create();
      }

      return new ArrayList<>(Collections.singletonList(user));
    }

    List<User> users = selector.getPlayers(source)
        .stream()
        .map(Users::get)

        // Optionally filter out vanished users
        .filter(u -> {
          return offlineAllowed
              || !checkVanished
              || source.hasPermission(Permissions.VANISH_SEE);
        })
        .collect(Collectors.toList());

    if (users.isEmpty()) {
      throw EntityArgumentImpl.NO_ENTITIES_FOUND.create();
    }

    return users;
  }

  public boolean isSelectorUsed() {
    return selector != null;
  }
}