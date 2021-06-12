package net.forthecrown.emperor.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.utils.CrownUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.permissions.Permission;

public class CommandSelfOrUser extends FtcCommand {
    private final CommandFunction function;
    private CommandSelfOrUser(String name, Permission perm, CommandFunction function, String... aliases){
        super(name, CrownCore.inst());

        this.permission = perm;
        this.aliases = aliases;
        this.function = function;

        EntityPlayer player;

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> function.run(getUserSender(c), c.getSource(), true))

                .then(argument("user", UserType.onlineUser())
                        .executes(c -> {
                            CrownUser user = UserType.getUser(c, "user");

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
                    user.getPlayer().setFoodLevel(20);

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
                    user.getPlayer().getActivePotionEffects().clear();

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
                Permissions.CORE_ADMIN,
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
                Permissions.CORE_ADMIN,
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
                    if(CrownUtils.isItemEmpty(item)) throw FtcExceptionProvider.mustHoldItem();

                    if(!(item.getItemMeta() instanceof Damageable)) throw FtcExceptionProvider.create("Given is not repairable");
                    Damageable meta = (Damageable) item.getItemMeta();

                    meta.setDamage(item.getType().getMaxDurability());

                    source.sendAdmin(
                            Component.text("Repaired item held by")
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
