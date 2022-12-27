package net.forthecrown.guilds.unlockables;

import static net.forthecrown.commands.guild.GuildCommandNode.testPermission;
import static net.forthecrown.guilds.GuildSettings.ROLE_COLOR;
import static net.forthecrown.guilds.menu.GuildMenus.GUILD;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.text.Component;

public class UnlockableRoleColor implements Unlockable {
  public static final UnlockableRoleColor COLOR = new UnlockableRoleColor();

  private UnlockableRoleColor() {
  }

  @Override
  public GuildPermission getPerm() {
    return GuildPermission.DISCORD;
  }

  @Override
  public String getKey() {
    return "discord/role_color";
  }

  @Override
  public Component getName() {
    return Component.text("Role color");
  }

  @Override
  public int getSlot() {
    return Slot.toIndex(4, 2);
  }

  @Override
  public int getExpRequired() {
    return 0;
  }

  @Override
  public boolean isUnlocked(Guild guild) {
    return guild.getSettings().hasFlags(ROLE_COLOR)
        && UnlockableDiscordRole.ROLE.isUnlocked(guild);
  }

  @Override
  public MenuNode toInvOption() {
    return MenuNode.builder()
        .setItem((user, context) -> {
          var guild = context.getOrThrow(GUILD);

          var builder = ItemStacks.builder(
              guild.getSettings().getPrimaryColor().toWool()
          );

          builder.setName("&eRole color")
              .addLore("&7Set Discord role color to guild's primary color")
              .addLoreRaw(Component.empty());

          if (isUnlocked(guild)) {
            builder
                .addLore("&aUnlocked!")
                .addLore("&8Click this option to update the discord role")
                .addLore("&8if it hasn't updated already");
          } else {
            builder
                .addLore("&cNot yet unlocked")
                .addLore("&7Requires a member of the guild to have")
                .addLore("&7donated in the webstore");
          }

          return builder.build();
        })

        // Runnable just updates the role if it has the wrong color
        .setRunnable((user, context, click) -> {
          if (Cooldown.containsOrAdd(user, 5 * 20)) {
            return;
          }

          var guild = context.getOrThrow(GUILD);
          testPermission(user, guild, getPerm(), Exceptions.NO_PERMISSION);

          if (!isUnlocked(guild)) {
            throw Exceptions.format("Not yet unlocked");
          }

          guild.getDiscord().getRole().ifPresent(role -> {
            int colorValue = guild.getSettings()
                .getPrimaryColor()
                .getTextColor()
                .value();

            if (role.getColorRaw() == colorValue) {
              return;
            }

            role.getManager()
                .setColor(colorValue)
                .submit();
          });
        })

        .build();
  }
}