package net.forthecrown.pirates.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.custom.PetType;
import net.forthecrown.core.enums.Pet;
import net.forthecrown.pirates.Pirates;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Sound;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandParrot extends CrownCommandBuilder {

    public CommandParrot(){
        super("parrot", Pirates.inst);

        setPermission(null);
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    removeOldParrot(user.getPlayer(), (Parrot) user.getPlayer().getShoulderEntityLeft());
                    return 0;
                })
                .then(argument("parrot", PetType.pet())
                        .suggests(PetType::suggestUserAware)
                        .executes(c -> setParrot(c, false))
                        .then(argument("silent").executes(c -> setParrot(c, true)))
                );
    }

    private int setParrot(CommandContext<CommandListenerWrapper> c, boolean silent) throws CommandSyntaxException {
        CrownUser user = getUserSender(c);
        List<Pet> pets = user.getPets();
        Pet pet = PetType.getPet(c, "parrot");

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