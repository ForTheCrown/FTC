package net.forthecrown.useables.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class SimpleCheckType implements UsageCheck<UsageCheckInstance> {

    private final Supplier<UsageCheckInstance> supplier;
    private final Key key;

    public SimpleCheckType(Supplier<UsageCheckInstance> supplier, Key key) {
        this.supplier = supplier;
        this.key = key;
    }

    @Override
    public UsageCheckInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return supplier.get();
    }

    @Override
    public UsageCheckInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return supplier.get();
    }

    @Override
    public JsonElement serialize(UsageCheckInstance value) {
        return JsonNull.INSTANCE;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
