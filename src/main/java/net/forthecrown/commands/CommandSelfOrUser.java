package net.forthecrown.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;

public class CommandSelfOrUser extends FtcCommand {

  private final CommandFunction function;
  private final String usageText;

  private CommandSelfOrUser(String name,
                            String desc,
                            String usageText,
                            Permission perm,
                            CommandFunction function,
                            String... aliases
  ) {
    super(name);

    this.function = function;
    this.usageText = usageText;

    setPermission(perm);
    setAliases(aliases);
    setDescription(desc);

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("")
        .addInfo(usageText, "yourself");

    factory.usage("<player>")
        .addInfo(usageText, "a <player>");
  }

  @Override
  protected void createCommand(BrigadierCommand command) {
    command
        .executes(c -> function.run(getUserSender(c), c.getSource(), true))

        .then(argument("user", Arguments.ONLINE_USER)
            .executes(c -> {
              User user = Arguments.getUser(c, "user");

              return function.run(
                  user,
                  c.getSource(),
                  c.getSource().textName().equalsIgnoreCase(user.getName())
              );
            })
        );
  }

  public interface CommandFunction {

    int run(User user, CommandSource source, boolean self) throws CommandSyntaxException;
  }

  public static void createCommands() {
    new CommandSelfOrUser("feed",
        "Feeds yourself or a player",
        "Feeds %s",
        Permissions.FEED,
        (user, source, self) -> {
          Player player = user.getPlayer();
          player.setFoodLevel(20);
          player.setExhaustion(0f);
          player.setSaturation(10f);

          if (!self) {
            user.sendMessage(Messages.FED);
          }

          source.sendAdmin(Messages.feeding(user));
          return 0;
        }
    );

    new CommandSelfOrUser("heal",
        "Heals yourself or another player",
        "Heals %s",
        Permissions.HEAL,
        (user, source, self) -> {
          var player = user.getPlayer();
          player.setHealth(user.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
          player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));

          if (!self) {
            player.sendMessage(Messages.HEALED);
          }

          source.sendMessage(Messages.healing(user));
          return 0;
        }
    );

    new CommandSelfOrUser("disposal",
        "Opens a menu to dispose of items for yourself or another player",
        "Opens a menu to dispose of items for %s",
        Permissions.DISPOSAL,
        (user, source, self) -> {
          Inventory inv = Bukkit.createInventory(null, InventoryType.DISPENSER, Messages.DISPOSAL);
          user.getPlayer().openInventory(inv);

          return 0;
        },
        "bin"
    );

    new CommandSelfOrUser("repair",
        "Repairs the item you're holding",
        "Repairs an item held by %s",
        Permissions.REPAIR,
        (user, source, self) -> {
          ItemStack item = user.getPlayer().getInventory().getItemInMainHand();

          if (ItemStacks.isEmpty(item)) {
            throw Exceptions.MUST_HOLD_ITEM;
          }

          if (!(item.getItemMeta() instanceof Damageable)) {
            throw Exceptions.NOT_REPAIRABLE;
          }

          ItemMeta meta = item.getItemMeta();

          Damageable damageable = (Damageable) meta;
          damageable.setDamage(0);

          item.setItemMeta(meta);

          source.sendAdmin(Messages.repairedItem(user));
          return 0;
        }
    );

    new CommandSelfOrUser("staffchattoggle",
        "Toggles all chat messages going to staff chat",
        "Toggles all chat messages going to staff chat for %s",
        Permissions.STAFF_CHAT,
        new CommandFunction() {
          @Override
          public int run(User user, CommandSource source, boolean self) {
            var set = StaffChat.toggledPlayers;
            var id = user.getUniqueId();
            boolean state;

            if (set.contains(id)) {
              state = !set.remove(id);
            } else {
              state = set.add(id);
            }

            sendMessages(user, source, self, state);
            return 0;
          }

          private void sendMessages(User user, CommandSource source, boolean self, boolean state) {
            if (self) {
              user.sendMessage(Messages.toggleStaffChatSelf(state));
            } else {
              source.sendAdmin(Messages.toggleStaffChatOther(user, state));
            }
          }
        },
        "sct", "sctoggle"
    );
  }
}