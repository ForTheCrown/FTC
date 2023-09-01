package net.forthecrown.usables.actions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.UUID;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.registry.Registry;
import net.forthecrown.text.Text;
import net.forthecrown.usables.Action;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.UsageType;
import net.forthecrown.user.currency.Currency;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CurrencyActions {

  public static void registerAll(
      Registry<UsageType<? extends Action>> registry,
      String currencyName,
      Currency currency
  ) {
    for (Modification mod : Modification.values()) {
      CurrencyActionType type = new CurrencyActionType(mod, currency);
      String key;

      if (mod == Modification.ADD_WITH_MULTIPLIER) {
        key = "add_" + currencyName + "_with_multiplier";
      } else {
        key = mod.prefix() + "_" + currencyName;
      }

      registry.register(key, type);
    }
  }
}

class CurrencyActionType implements UsageType<CurrencyAction> {

  private final Modification modification;
  private final Currency currency;

  public CurrencyActionType(Modification modification, Currency currency) {
    this.modification = modification;
    this.currency = currency;
  }

  @Override
  public CurrencyAction parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException
  {
    int value = reader.readInt();
    return new CurrencyAction(this, value, modification, currency);
  }

  @Override
  public @NotNull <S> DataResult<CurrencyAction> load(@Nullable Dynamic<S> dynamic) {
    return dynamic.asNumber()
        .map(Number::intValue)
        .map(integer -> new CurrencyAction(this, integer, modification, currency));
  }

  @Override
  public <S> DataResult<S> save(@NotNull CurrencyAction value, @NotNull DynamicOps<S> ops) {
    return DataResult.success(ops.createInt(value.value()));
  }
}

record CurrencyAction(
    UsageType<CurrencyAction> type,
    int value,
    Modification action,
    Currency currency
) implements Action {

  @Override
  public void onUse(Interaction interaction) {
    action.apply(currency, interaction.playerId(), value);
  }

  @Override
  public UsageType<? extends UsableComponent> getType() {
    return type;
  }

  @Override
  public @Nullable Component displayInfo() {
    return Text.formatNumber(value);
  }
}

enum Modification {
  ADD {
    @Override
    void apply(Currency map, UUID uuid, int amount) {
      map.add(uuid, amount);
    }
  },

  ADD_WITH_MULTIPLIER {
    @Override
    void apply(Currency map, UUID uuid, int amount) {
      int finalAmount = (int) (map.getGainMultiplier(uuid) * amount);
      map.add(uuid, finalAmount);
    }
  },

  SET {
    @Override
    void apply(Currency map, UUID uuid, int amount) {
      map.set(uuid, amount);
    }
  },

  REMOVE {
    @Override
    void apply(Currency map, UUID uuid, int amount) {
      map.remove(uuid, amount);
    }
  };

  public String prefix() {
    return name().toLowerCase();
  }

  abstract void apply(Currency map, UUID uuid, int amount);
}
