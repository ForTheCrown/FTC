package net.forthecrown.core.types.interactable.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtGetter;
import net.forthecrown.core.types.interactable.InteractionCheck;
import net.forthecrown.core.utils.InterUtils;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.v1_16_R3.MojangsonParser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CheckHasItem implements InteractionCheck {
    private ItemStack item;

    @Override
    public void parse(CommandContext<CommandSource> c, StringReader reader) throws CommandSyntaxException {
        item = InterUtils.parseGivenItem(c, reader);
    }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        try {
            item = NbtGetter.itemFromNBT(NBT.of(MojangsonParser.parse(json.getAsString())));
        } catch (RuntimeException e) {
            item = null;
            e.printStackTrace();
        }
    }

    @Override
    public String getRegistrationName() {
        return "has_item";
    }

    @Override
    public String asString() {
        return getClass().getSimpleName() + "{item=" + item + "}";
    }

    @Override
    public Component getFailMessage() {
        return Component.text("You don't have the required item").color(NamedTextColor.GRAY);
    }

    @Override
    public boolean test(Player player) {
        return player.getInventory().containsAtLeast(item, item.getAmount());
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(NbtGetter.ofItem(item).serialize());
    }
}
