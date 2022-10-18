package net.forthecrown.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.text.Messages;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
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
    private CommandSelfOrUser(String name, Permission perm, CommandFunction function, String... aliases){
        super(name);

        this.permission = perm;
        this.aliases = aliases;
        this.function = function;

        register();
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

    public interface CommandFunction{
        int run(User user, CommandSource source, boolean self) throws CommandSyntaxException;
    }

    public static void createCommands(){
        new CommandSelfOrUser("feed",
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
                Permissions.DISPOSAL,
                (user, source, self) -> {
                    Inventory inv = Bukkit.createInventory(null, InventoryType.DISPENSER, Messages.DISPOSAL);
                    user.getPlayer().openInventory(inv);

                    return 0;
                },
                "bin"
        );

        new CommandSelfOrUser("repair",
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
                            state = !set.add(id);
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