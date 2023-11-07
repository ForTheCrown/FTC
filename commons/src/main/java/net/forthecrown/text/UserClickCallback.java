package net.forthecrown.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.Exceptions;
import net.forthecrown.user.User;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;
import org.jetbrains.annotations.NotNull;

public interface UserClickCallback extends ClickCallback<Audience> {

  @Override
  default void accept(@NotNull Audience audience) {
    User user = Audiences.getUser(audience);

    if (user == null) {
      return;
    }

    try {
      accept(user);
    } catch (CommandSyntaxException exc) {
      Exceptions.handleSyntaxException(user, exc);
    }
  }

  void accept(User user) throws CommandSyntaxException;
}
