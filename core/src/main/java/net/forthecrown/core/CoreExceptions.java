package net.forthecrown.core;

import static net.forthecrown.command.Exceptions.create;
import static net.forthecrown.command.Exceptions.format;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.user.User;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;

public interface CoreExceptions {

  CommandSyntaxException CANNOT_IGNORE_SELF = create("You cannot ignore yourself... lol");

  CommandSyntaxException NO_RETURN = create("No location to return to.");

  CommandSyntaxException NO_NEARBY_PLAYERS = create("No nearby players.");

  CommandSyntaxException ALREADY_YOUR_NICK = create("This is already your nickname");

  CommandSyntaxException ALREADY_THEIR_NICK = create("This is already their nickname");

  CommandSyntaxException NO_REPLY_TARGETS = create("No one to reply to.");

  /**
   * Exception stating that a given item is not repairable
   */
  CommandSyntaxException NOT_REPAIRABLE = create("Given item is not repairable");


  CommandSyntaxException ENCH_MUST_BE_BETTER = create(
      "Enchantment must be higher level than already existing one"
  );

  CommandSyntaxException ITEM_CANNOT_HAVE_META = create(
      "Item cannot have name/enchantments/lore"
  );

  CommandSyntaxException NO_LORE = create("Item has no lore");

  CommandSyntaxException NO_ATTR_MODS = create("No attribute modifiers to remove");

  static CommandSyntaxException enchantNotFound(Enchantment enchantment) {
    return format("Held item does not have '{0}' enchantment",
        enchantment.displayName(1)
    );
  }

  static CommandSyntaxException profilePrivate(User user) {
    return format("{0, user}'s profile is not public.", user);
  }

  static CommandSyntaxException notSign(Location l) {
    return format("{0, location, -c -w} is not a sign", l);
  }
}
