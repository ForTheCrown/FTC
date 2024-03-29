package net.forthecrown.guilds.unlockables;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

import java.time.Duration;
import java.time.Instant;
import net.forthecrown.Loggers;
import net.forthecrown.command.Exceptions;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.guilds.menu.GuildMenus;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Slot;
import net.forthecrown.text.Text;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;

public class UnlockableDiscordRole implements Unlockable {

  private MenuNode node;

  UnlockableDiscordRole() {
  }

  @Override
  public GuildPermission getPerm() {
    return GuildPermission.DISCORD;
  }

  @Override
  public String getKey() {
    return "discord/role";
  }

  @Override
  public Component getName() {
    return Component.text("Discord role");
  }

  @Override
  public int getSlot() {
    return Slot.toIndex(2, 2);
  }

  @Override
  public int getExpRequired() {
    return 4000;
  }

  @Override
  public MenuNode toInvOption() {
    if (node != null) {
      return node;
    }

    return node = MenuNode.builder()
        .setItem((user, context) -> {
          var guild = context.getOrThrow(GUILD);

          var builder = ItemStacks.builder(
              isUnlocked(guild) ? Material.NAME_TAG : Material.DRAGON_HEAD
          );

          builder.setName("&eDiscord role");

          if (Loggers.isDebugEnabled()) {
            builder.addLore("roleId: " + guild.getDiscord().getRoleId());
          }

          if (isUnlocked(guild)) {
            guild.getDiscord().getRole().ifPresentOrElse(role -> {
              builder
                  .addLore("&aCurrently active!")
                  .addLore("&7Shift-Click to delete role")
                  .addEnchant(Enchantment.BINDING_CURSE, 1)
                  .addFlags(ItemFlag.HIDE_ENCHANTS);
            }, () -> {
              builder
                  .addLore("&cNo Discord role active")
                  .addLore("&eClick to create a role");
            });
          } else {
            builder
                .addLore(getProgressComponent(guild))
                .addLoreRaw(empty())
                .addLore(getClickComponent())
                .addLore(getShiftClickComponent());
          }

          return builder.build();
        })

        .setRunnable((user, context, click) -> {
          onClick(user, click, context, () -> {
            var guild = context.getOrThrow(GUILD);

            Instant lastUpdate = guild.getDiscord().getLastRoleUpdate();
            Instant now = Instant.now();

            Duration interval = Guilds.getConfig().roleUpdateInterval();
            Instant nextPossible = lastUpdate.plus(interval);

            if (now.isBefore(nextPossible)) {
              throw Exceptions.format("Can disable/enable discord role in {0, time, -timestamp}",
                  nextPossible
              );
            }

            var roleOpt = guild.getDiscord().getRole();
            var page = GuildMenus.MAIN_MENU
                .getUpgradesMenu()
                .getDiscordMenu();

            if (roleOpt.isEmpty()) {
              guild.getDiscord().createRole().whenComplete((role, throwable) -> {
                if (throwable != null) {
                  guild.sendMessage(text(
                      "Failed to create role, internal error!",
                      NamedTextColor.RED
                  ));

                  return;
                }

                guild.sendMessage(Text.format(
                    "&e{0, user}&r created Discord role for the Guild!",
                    NamedTextColor.GOLD,
                    user
                ));

                click.reloadMenu();
              });
              return;
            }

            if (click.getClickType() != ClickType.SHIFT_LEFT) {
              return;
            }

            guild.getDiscord().deleteRole().whenComplete((unused, error) -> {
              if (error != null) {
                guild.sendMessage(
                    Component.text("Failed to delete role, internal error",
                        NamedTextColor.RED
                    )
                );

                return;
              }

              guild.sendMessage(
                  Text.format(
                      "&e{0, user}&r deleted the guild's Discord role",
                      NamedTextColor.GRAY,
                      user
                  )
              );

              click.reloadMenu();
            });
          });
        })

        .build();
  }
}