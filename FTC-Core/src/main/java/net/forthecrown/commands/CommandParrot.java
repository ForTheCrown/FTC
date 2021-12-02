package net.forthecrown.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.PetArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.Pet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandParrot extends FtcCommand {

    public CommandParrot(){
        super("parrot");

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
                .then(argument("parrot", PetArgument.PET)
                        .executes(c -> setParrot(c, false))
                        .then(literal("silent").executes(c -> setParrot(c, true)))
                );
    }

    private int setParrot(CommandContext<CommandSource> c, boolean silent) throws CommandSyntaxException {
        CrownUser user = getUserSender(c);

        List<Pet> pets = user.getPets();
        Pet pet = PetArgument.getPetIfOwned(c, "parrot");

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
        parrot.setOwner(player);
        parrot.setSilent(silent);

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
            parrot.remove();
        }
    }
}