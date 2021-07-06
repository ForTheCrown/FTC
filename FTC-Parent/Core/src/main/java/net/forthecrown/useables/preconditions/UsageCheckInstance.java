package net.forthecrown.useables.preconditions;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface UsageCheckInstance extends Predicate<Player> {
    String asString();
    Component failMessage();

    default Consumer<Player> onSuccess(){
        return null;
    }

    default Component personalizedMessage(Player player){
        return failMessage();
    }

    @NotNull Key typeKey();
}