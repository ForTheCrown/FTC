package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Crown;
import net.forthecrown.core.admin.JailCell;
import net.forthecrown.core.npc.SimpleNpc;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.dungeons.boss.KeyedBoss;
import net.forthecrown.economy.sell.SellShopMenu;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.Structures;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.useables.UsageType;
import net.forthecrown.user.data.TimeField;
import net.forthecrown.user.data.UserTimeTracker;

import java.util.concurrent.CompletableFuture;

public class RegistryArguments<T> implements ArgumentType<Holder<T>>, VanillaMappedArgument {

    public static final RegistryArguments<BlockStructure> STRUCTURE = new RegistryArguments<>(Structures.get().getRegistry(), "Structure");

    public static final RegistryArguments<KeyedBoss> DUNGEON_BOSS =  new RegistryArguments<>(Registries.DUNGEON_BOSSES, "Dungeon boss");

    public static final RegistryArguments<JailCell> JAIL_CELL = new RegistryArguments<>(Registries.JAILS, "Jail Cell");

    public static final RegistryArguments<SimpleNpc> NPC = new RegistryArguments<>(Registries.NPCS, "NPC");

    public static final RegistryArguments<SellShopMenu> SELLS_SHOP = new RegistryArguments<>(Crown.getEconomy().getSellShop().getMenus(), "Menu");

    public static final RegistryArguments<TimeField> TIME_FIELDS = new RegistryArguments<>(UserTimeTracker.TIME_FIELDS, "TimeStamp");

    public static final RegistryArguments<UsageType<UsageAction>> USAGE_ACTION =  new RegistryArguments<>(
            ((Registry) Registries.USAGE_ACTIONS), "Usage action"
    );
    public static final RegistryArguments<UsageType<UsageTest>>   USAGE_CHECK  =   new RegistryArguments<>(
            ((Registry) Registries.USAGE_CHECKS), "Usage check"
    );

    private final Registry<T> registry;
    private final String unknown;

    public RegistryArguments(Registry<T> registry, String unknownMessage) {
        this.registry = registry;
        this.unknown = unknownMessage;
    }

    @Override
    public Holder<T> parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String key = Arguments.FTC_KEY.parse(reader);

        return registry.getHolder(key).orElseThrow(() -> {
            return Exceptions.unknown(unknown,
                    GrenadierUtils.correctReader(reader, cursor),
                    key
            );
        });
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CompletionProvider.suggestMatching(builder, registry.keys());
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return Arguments.FTC_KEY.getVanillaArgumentType();
    }
}