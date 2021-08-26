package net.forthecrown.economy.shops.template;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.KeyArgument;
import net.forthecrown.serializer.JsonBuf;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class GenericShopTemplateType implements ShopTemplateType<GenericShopTemplate> {
    public static final Key KEY = Crown.coreKey("generic_shop_template");
    private static final EnumArgument<EditCategory> CATEGORY_PARSER = EnumArgument.of(EditCategory.class);
    private static final IntegerArgumentType ITEM_AMOUNT_PARSER = IntegerArgumentType.integer(1, 64);

    @Override
    public GenericShopTemplate parse(Key key, StringReader reader, CommandSource source) throws CommandSyntaxException {
        int price = reader.readInt();
        checkCanRead(reader);
        int amount = ITEM_AMOUNT_PARSER.parse(reader);
        checkCanRead(reader);

        int cursor = reader.getCursor();
        NamespacedKey matKey = KeyArgument.minecraft().parse(reader);
        Material material = Material.matchMaterial(matKey.asString());

        if(material == null) throw FtcExceptionProvider.createWithContext("Unknown material: '" + matKey.asString() + "'", reader.getString(), cursor);
        if(material.getMaxStackSize() < amount) throw FtcExceptionProvider.create("Invalid amount (" + amount + ") for item " + matKey.asString());

        return new GenericShopTemplate((byte) amount, price, material, key);
    }

    private static void checkCanRead(StringReader reader) throws CommandSyntaxException {
        if(!reader.canRead()) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().createWithContext(reader);

        reader.skipWhitespace();
    }

    @Override
    public void edit(GenericShopTemplate value, StringReader reader, CommandSource source) throws CommandSyntaxException {
        EditCategory category = CATEGORY_PARSER.parse(reader);
        checkCanRead(reader);

        category.edit(value, reader);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        String remaining = builder.getRemainingLowerCase();
        String[] args = remaining.split(" ");

        if(args.length < 2) return CATEGORY_PARSER.listSuggestions(context, builder);
        else {
            StringReader reader = new StringReader(args[1]);
            EditCategory category = CATEGORY_PARSER.parse(reader);

            return category.getSuggestions(context, builder.createOffset(builder.getInput().lastIndexOf(' ')));
        }
    }

    @Override
    public GenericShopTemplate deserialize(JsonElement element, Key key) {
        JsonBuf json = JsonBuf.of(element.getAsJsonObject());

        return new GenericShopTemplate(
                json.getByte("amount"),
                json.getInt("price"),
                Material.matchMaterial(json.getString("material")),
                key
        );
    }

    @Override
    public JsonElement serialize(GenericShopTemplate value) {
        JsonBuf json = JsonBuf.empty();

        json.add("price", value.getPrice());
        json.add("amount", (byte) value.getAmount());
        json.addKey("material", value.getMaterial().getKey());

        return json.getSource();
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public enum EditCategory implements SuggestionProvider<CommandSource>, EditConsumer {
        PRICE ((template, reader) -> template.setPrice(reader.readInt())) {
            @Override
            public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
                return FtcCommand.suggestMonies().getSuggestions(context, builder);
            }
        },
        AMOUNT ((template, reader) -> {
            int amount = ITEM_AMOUNT_PARSER.parse(reader);
            byte amountActual = (byte) amount;

            Material material = template.getMaterial();
            if(!template.getAmountRange().contains(amountActual)) {
                throw FtcExceptionProvider.create("Invalid amount (" + amount + ") for item " + material.getKey().asString());
            }

            template.setAmount(amountActual);
        }) {
            @Override
            public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
                return CompletionProvider.suggestMatching(builder, "1", "8", "16", "32", "64");
            }
        },
        MATERIAL ((template, reader) -> {
            int cursor = reader.getCursor();
            NamespacedKey key = KeyArgument.minecraft().parse(reader);

            Material mat = Material.matchMaterial(key.asString());
            if(mat == null) throw FtcExceptionProvider.createWithContext("Unknown material: '" + key.asString() + "'", reader.getString(), cursor);

            template.setMaterial(mat);
        }) {
            @Override
            public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
                return CompletionProvider.suggestBlocks(builder);
            }
        };

        private final EditConsumer consumer;

        EditCategory(EditConsumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void edit(GenericShopTemplate template, StringReader reader) throws CommandSyntaxException {
            consumer.edit(template, reader);
        }
    }

    public interface EditConsumer {
        void edit(GenericShopTemplate template, StringReader reader) throws CommandSyntaxException;
    }
}
