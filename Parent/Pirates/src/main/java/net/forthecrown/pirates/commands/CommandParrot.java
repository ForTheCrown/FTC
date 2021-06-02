package net.forthecrown.pirates.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.arguments.PetType;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.enums.Branch;
import net.forthecrown.emperor.user.enums.Pet;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.pirates.Pirates;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandParrot extends CrownCommandBuilder {

    public CommandParrot(){
        super("parrot", Pirates.inst);

        setPermission(Permissions.DEFAULT);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    removeOldParrot(user.getPlayer(), (Parrot) user.getPlayer().getShoulderEntityLeft());
                    return 0;
                })
                .then(argument("parrot", PetType.PET)
                        .executes(c -> setParrot(c, false))
                        .then(argument("silent").executes(c -> setParrot(c, true)))
                );
    }

    private int setParrot(CommandContext<CommandSource> c, boolean silent) throws CommandSyntaxException {
        CrownUser user = getUserSender(c);
        if(user.getBranch() != Branch.PIRATES) throw FtcExceptionProvider.create("Only Pirates can use this");

        List<Pet> pets = user.getPets();
        Pet pet = PetType.getPetIfOwned(c, "parrot");

        if(!pets.contains(pet)){
            user.sendMessage(
                    Component.text("You need to buy a ")
                            .color(NamedTextColor.GRAY)
                            .append(pet.getName())
                            .append(Component.text(" first"))
            );
            return 0;
        }

        makeParrot(pet, user.getPlayer(), silent);
        return 0;
    }

    private void makeParrot(Pet pet, Player player, boolean silent) {
        Parrot parrot = player.getWorld().spawn(player.getLocation(), Parrot.class);
        parrot.setVariant(pet.getVariant());
        parrot.setSilent(silent);

        Pirates.inst.events.parrots.put(parrot.getUniqueId(), player.getUniqueId());

        player.setShoulderEntityLeft(parrot);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);

        player.sendMessage(
                Component.text("Equipped ")
                        .color(NamedTextColor.GRAY)
                        .append(pet.getName())
        );
    }

    private void removeOldParrot(Player player, Parrot parrot) {
        player.setShoulderEntityLeft(null);
        if (parrot != null) {
            Pirates.inst.events.parrots.remove(parrot.getUniqueId());
            parrot.remove();
        }
    }
}