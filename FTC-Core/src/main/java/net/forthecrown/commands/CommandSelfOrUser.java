package net.forthecrown.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        super(name, Crown.inst());

        this.permission = perm;
        this.aliases = aliases;
        this.function = function;

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> function.run(getUserSender(c), c.getSource(), true))

                .then(argument("user", UserArgument.onlineUser())
                        .executes(c -> {
                            CrownUser user = UserArgument.getUser(c, "user");

                            return function.run(
                                    user,
                                    c.getSource(),
                                    c.getSource().textName().equalsIgnoreCase(user.getName())
                            );
                        })
                );
    }

    public interface CommandFunction{
        int run(CrownUser user, CommandSource source, boolean self) throws CommandSyntaxException;
    }

    public static void init(){
        new CommandSelfOrUser("feed",
                Permissions.FEED,
                (user, source, self) -> {
                    Player player = user.getPlayer();
                    player.setFoodLevel(20);
                    player.setExhaustion(0f);
                    player.setSaturation(10f);

                    source.sendAdmin(Component.text("Satiated the appetite of ")
                            .color(NamedTextColor.YELLOW)
                            .append(user.nickDisplayName().color(NamedTextColor.GOLD))
                    );
                    return 0;
                }
        );

        new CommandSelfOrUser("heal",
                Permissions.HEAL,
                (user, source, self) -> {
                    user.getPlayer().setHealth(user.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                    FtcUtils.clearEffects(user.getPlayer());

                    source.sendAdmin(Component.text("Healing ")
                            .color(NamedTextColor.YELLOW)
                            .append(user.nickDisplayName().color(NamedTextColor.GOLD))
                    );
                    return 0;
                }
        );

        new CommandSelfOrUser("disposal",
                Permissions.DISPOSAL,
                (user, source, self) -> {
                    Inventory inv = Bukkit.createInventory(null, InventoryType.DISPENSER, Component.text("Disposal"));
                    user.getPlayer().openInventory(inv);

                    return 0;
                },
                "bin"
        );

        new CommandSelfOrUser("fly",
                Permissions.ADMIN,
                (user, source, self) -> {
                    boolean flying = !user.isFlying();
                    user.setFlying(flying);

                    source.sendAdmin(
                            Component.text("Flying " + (flying ? "enabled" : "disabled") + " for ")
                                    .color(NamedTextColor.YELLOW)
                                    .append(user.nickDisplayName().color(NamedTextColor.GOLD))
                    );
                    return 0;
                }
        );

        new CommandSelfOrUser("godmode",
                Permissions.ADMIN,
                (user, source, self) -> {
                    boolean inv = !user.godMode();
                    user.setGodMode(inv);

                    source.sendAdmin(
                            Component.text("Set godmode " + (inv ? "enabled" : "disabled") + " for ")
                                    .color(NamedTextColor.YELLOW)
                                    .append(user.nickDisplayName().color(NamedTextColor.GOLD))
                    );
                    return 0;
                }, "god"
        );

        new CommandSelfOrUser("repair",
                Permissions.REPAIR,
                (user, source, self) -> {
                    ItemStack item = user.getPlayer().getInventory().getItemInMainHand();
                    if(ItemStacks.isEmpty(item)) throw FtcExceptionProvider.mustHoldItem();

                    if(!(item.getItemMeta() instanceof Damageable)) throw FtcExceptionProvider.create("Given item is not repairable");
                    ItemMeta meta = item.getItemMeta();

                    Damageable damageable = (Damageable) meta;
                    damageable.setDamage(0);

                    item.setItemMeta(meta);

                    source.sendAdmin(
                            Component.text("Repaired item held by ")
                                    .color(NamedTextColor.YELLOW)
                                    .append(user.nickDisplayName().color(NamedTextColor.GOLD))
                    );
                    return 0;
                }
        );

        new CommandSelfOrUser("eavesdrop",
                Permissions.EAVESDROP,
                (user, source, self) -> {
                    boolean ed = user.isEavesDropping();
                    ed = !ed;

                    user.setEavesDropping(ed);
                    source.sendAdmin(
                            Component.text() //This shit retarded
                                    .append(self ? Component.text("N") : user.displayName().append(Component.text(" is n")))
                                    .append(Component.text((ed ? "ow" : "o longer") + " eavesdropping"))
                                    .color(NamedTextColor.GRAY)
                                    .build()
                    );
                    return 0;
                }
        );
    }
}
