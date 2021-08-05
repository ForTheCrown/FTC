package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.ComponentTagVisitor;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;

public class CommandTestCore extends FtcCommand {

    public CommandTestCore(){
        super("coretest", ForTheCrown.inst());

        setAliases("testcore");
        setPermission(Permissions.FTC_ADMIN);
        register();
    }

    @Override
    public boolean test(CommandSource sender) { //test method used by Brigadier to determine who can use the command, from Predicate interface
        return sender.asBukkit().isOp() && testPermissionSilent(sender.asBukkit());
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser u = getUserSender(c);
            u.sendMessage("-Beginning test-");
            //Use this command to test things lol
            //This is as close as I currently know how to get to actual automatic test

            CompoundTag tag = ((CraftEntity) u.getPlayer()).getHandle().saveWithoutId(new CompoundTag());
            ListTag tags = tag.getList("Inventory", Tag.TAG_COMPOUND);

            ComponentTagVisitor visitor = new ComponentTagVisitor(true);
            TextComponent text = visitor.visit(tags);

            u.sendMessage(text);
            u.sendMessage("-Test finished-");
            return 0;
        });
    }
}
