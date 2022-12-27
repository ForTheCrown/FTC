package net.forthecrown.guilds.unlockables;

import static net.forthecrown.commands.guild.GuildCommandNode.testPermission;
import static net.forthecrown.guilds.GuildDiscord.isArchived;
import static net.forthecrown.guilds.GuildSettings.GUILD_CHANNEL;
import static net.forthecrown.guilds.menu.GuildMenus.GUILD;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.FTC;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildConfig;
import net.forthecrown.guilds.GuildDiscord;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;

public class UnlockableTextChannel implements Unlockable {
  public static final UnlockableTextChannel CHANNEL
      = new UnlockableTextChannel();

  private UnlockableTextChannel() {
  }

  @Override
  public GuildPermission getPerm() {
    return GuildPermission.DISCORD;
  }

  @Override
  public String getKey() {
    return "discord/guild_chat";
  }

  @Override
  public Component getName() {
    return Component.text("Guild Text Channel");
  }

  @Override
  public int getSlot() {
    return Slot.toIndex(6, 2);
  }

  @Override
  public int getExpRequired() {
    return 0;
  }

  @Override
  public boolean isUnlocked(Guild guild) {
    return guild.getSettings()
        .hasFlags(GUILD_CHANNEL);
  }

  @Override
  public MenuNode toInvOption() {
    return MenuNode.builder()
        .setItem((user, context) -> {
          var guild = context.getOrThrow(GUILD);

          var builder = ItemStacks.builder(Material.WRITABLE_BOOK)
              .setName("&eGuild Text Channel")
              .addLore("&7Creates a private Discord channel for the guild!")
              .addLore("");

          if (isUnlocked(guild)) {
            guild.getDiscord().getChannel().ifPresentOrElse(channel -> {
              if (isArchived(channel)) {
                builder
                    .addLore("&cArchived!")
                    .addLore("&7Click to unarchive");

                return;
              }

              builder
                  .addLore("&aActive!")
                  .addLore("&7Shift-Click to archive")
                  .addLore("&8Text channels cannot be deleted, only archived")

                  .addEnchant(Enchantment.BINDING_CURSE, 1)
                  .addFlags(ItemFlag.HIDE_ENCHANTS);
            }, () -> {
              builder
                  .addLore("&cNot active!")
                  .addLore("&7Click to activate!");
            });
          } else {
            builder
                .addLore("&cNot yet unlocked!")
                .addLore("&7Requires any Guild member to buy the Guild Chat")
                .addLore("&7package from the webstore.");
          }

          return builder.build();
        })

        .setRunnable((user, context, click) -> {
          var guild = context.getOrThrow(GUILD);

          testPermission(user, guild, getPerm(), Exceptions.NO_PERMISSION);

          if (!isUnlocked(guild)) {
            throw Exceptions.format(
                "Requires any Guild member to buy the Guild Chat"
            );
          }

          var channelOpt = guild.getDiscord().getChannel();

          long lastUpdate = guild.getDiscord().getLastChannelUpdate();
          long nextChange = GuildConfig.roleUpdateInterval + lastUpdate;

          if (!FTC.inDebugMode() && !Time.isPast(nextChange)) {
            throw Exceptions.format(
                "This action can be performed in {0, time, -timestamp}",
                nextChange
            );
          }

          click.shouldReloadMenu(true);

          boolean archived = channelOpt.map(GuildDiscord::isArchived)
              .orElse(false);

          if (channelOpt.isEmpty() || archived) {
            if (archived) {
              guild.getDiscord().unarchiveChannel();

              guild.sendMessage(
                  Text.format(
                      "&e{0, user}&r unarchived the guild's Text channel",
                      NamedTextColor.GOLD,
                      user
                  )
              );

              return;
            }

            guild.getDiscord().createChannel();
            guild.sendMessage(
                Text.format(
                    "&e{0, user}&r created a Text channel for the guild",
                    NamedTextColor.GOLD,
                    user
                )
            );

            return;
          }

          if (click.getClickType() != ClickType.SHIFT_LEFT) {
            return;
          }

          guild.getDiscord().archiveChannel("");
          guild.sendMessage(
              Text.format(
                  "&e{0, user}&r archived the guild's text channel",
                  NamedTextColor.GOLD,
                  user
              )
          );
        })

        .build();
  }
}