package net.forthecrown.sellshop.loader;

import static net.forthecrown.menu.Menus.MAX_INV_SIZE;
import static net.forthecrown.menu.Menus.MIN_INV_SIZE;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.Slot;
import net.forthecrown.sellshop.ItemPriceMap;
import net.forthecrown.sellshop.ItemSellData;
import net.forthecrown.sellshop.ItemSellData.Builder;
import net.forthecrown.sellshop.SellShopPlugin;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.FtcCodecs;
import net.forthecrown.utils.io.Results;
import net.kyori.adventure.text.Component;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;

final class SellShopCodecs {
  private SellShopCodecs() {}

  public static final Codec<ItemSellData> SELL_DATA = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            FtcCodecs.registryCodec(Registry.MATERIAL)
                .fieldOf("material")
                .forGetter(ItemSellData::getMaterial),

            Codec.INT
                .fieldOf("price")
                .forGetter(ItemSellData::getPrice),

            FtcCodecs.registryCodec(Registry.MATERIAL)
                .optionalFieldOf("compact-material")
                .forGetter(d -> Optional.ofNullable(d.getCompactMaterial())),

            Codec.INT
                .optionalFieldOf("compact-multiplier", 9)
                .forGetter(ItemSellData::getCompactMultiplier),

            Codec.INT
                .optionalFieldOf("max-earnings")
                .forGetter(d -> Optional.of(d.getMaxEarnings()))
        )

        .apply(instance, (material, price, compactMaterial, compactMod, maxEarnings) -> {
          Builder builder = ItemSellData.builder();
          builder.material(material);
          builder.price(price);

          compactMaterial.ifPresent(compact -> {
            builder.compactMaterial(compact);
            builder.compactMultiplier(compactMod);
          });

          maxEarnings.ifPresentOrElse(builder::maxEarnings, () -> {
            SellShopPlugin plugin = SellShopPlugin.getPlugin();
            int max = plugin.getShopConfig().defaultMaxEarnings();
            builder.maxEarnings(max);
          });

          return builder.build();
        });
  });

  public static final Codec<ItemPriceMap> PRICE_MAP = SELL_DATA.listOf()
      .xmap(
          itemSellData -> {
            ItemPriceMap map = new ItemPriceMap();
            for (ItemSellData data : itemSellData) {
              map.add(data);
            }
            return map;
          },
          priceMap -> {
            List<ItemSellData> dataList = new ArrayList<>();
            Iterator<ItemSellData> it = priceMap.nonRepeatingIterator();

            while (it.hasNext()) {
              dataList.add(it.next());
            }

            return dataList;
          }
      );

  /* ------------------------------------------------------- */

  public static final Codec<Slot> SLOT_RECORD = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            Codec.mapEither(Codec.BYTE.fieldOf("x"), Codec.BYTE.fieldOf("column"))
                .xmap(SellShopCodecs::getEither, Either::left)
                .forGetter(Slot::getX),

            Codec.mapEither(Codec.BYTE.fieldOf("y"), Codec.BYTE.fieldOf("row"))
                .xmap(SellShopCodecs::getEither, Either::left)
                .forGetter(Slot::getY)
        )
        .apply(instance, Slot::of);
  });

  public static final Codec<Slot> SLOT_STRING = new PrimitiveCodec<>() {
    @Override
    public <T> DataResult<Slot> read(DynamicOps<T> ops, T input) {
      return ops.getStringValue(input).flatMap(string -> {
        return FtcCodecs.safeParse(string, reader -> {
          reader.skipWhitespace();
          int x = reader.readInt();
          reader.skipWhitespace();
          reader.expect(',');
          reader.skipWhitespace();
          int y = reader.readInt();
          return Slot.of(x, y);
        });
      });
    }

    @Override
    public <T> T write(DynamicOps<T> ops, Slot value) {
      return ops.createString(value.getX() + "," + value.getY());
    }
  };

  private static final Codec<Slot> SLOT_INT = Codec.INT.xmap(Slot::of, Slot::getIndex);

  public static final Codec<Slot> SLOT = FtcCodecs.combine(SLOT_STRING, SLOT_RECORD, SLOT_INT);

  /* ------------------------------------------------------- */

  public static final Codec<Integer> INV_SIZE_INT = Codec.INT.comapFlatMap(integer -> {
    if (Menus.isValidSize(integer)) {
      return Results.success(integer);
    }
    return Results.error("Invalid inventory size: %s", integer);
  }, Function.identity());

  public static final Codec<Integer> INV_SIZE_STRING;
  public static final Codec<Integer> INVENTORY_SIZE;

  static {
    Map<String, Integer> suffixes = new HashMap<>();
    suffixes.put("row", 9);
    suffixes.put("r", 9);

    ArgumentType<Integer> sizeParser
        = ArgumentTypes.suffixedInt(suffixes, MIN_INV_SIZE, MAX_INV_SIZE);

    INV_SIZE_STRING = Codec.STRING.comapFlatMap(string -> {
      return FtcCodecs.safeParse(string, sizeParser);
    }, String::valueOf);

    INVENTORY_SIZE = FtcCodecs.combine(INV_SIZE_INT, INV_SIZE_STRING);
  }

  /* ------------------------------------------------------- */

  private static final Codec<ItemStack> BORDER_ITEM_CODEC
      = Codec.either(FtcCodecs.ITEM_CODEC, Codec.BOOL)
      .xmap(
          either -> either.map(i -> i, hasBorder -> hasBorder ? Menus.defaultBorderItem() : ItemStack.empty()),
          itemStack -> {
            if (ItemStacks.isEmpty(itemStack)) {
              return Either.right(false);
            }
            return Either.left(itemStack);
          }
      );

  static final Codec<LoadingPage> PAGE_CODEC = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            INVENTORY_SIZE
                .optionalFieldOf("size", MIN_INV_SIZE)
                .forGetter(o -> o.size),

            FtcCodecs.COMPONENT
                .optionalFieldOf("title")
                .forGetter(o -> Optional.ofNullable(o.title)),

            FtcCodecs.COMPONENT.listOf()
                .xmap(components -> components.toArray(Component[]::new), Arrays::asList)
                .optionalFieldOf("description")
                .forGetter(o -> Optional.ofNullable(o.desc)),

            FtcCodecs.registryCodec(Registry.MATERIAL)
                .optionalFieldOf("header-item")
                .forGetter(o -> Optional.ofNullable(o.headerItem)),

            BORDER_ITEM_CODEC.optionalFieldOf("border", ItemStack.empty())
                .forGetter(o -> o.border),

            Codec.BOOL.optionalFieldOf("command-accessible", false)
                .forGetter(o -> o.commandAccessible)

        )
        .apply(instance, (size, title, desc, header, border, inCommand) -> {
          LoadingPage page = new LoadingPage();
          page.size = size;
          page.commandAccessible = inCommand;
          page.border = border;

          title.ifPresent(component -> page.title = component);
          desc.ifPresent(components -> page.desc = components);

          return page;
        });
  });

  private static <T> T getEither(Either<T, T> either) {
    if (either.left().isPresent()) {
      return either.left().get();
    }
    return either.right().get();
  }
}
