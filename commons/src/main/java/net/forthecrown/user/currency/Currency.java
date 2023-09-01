package net.forthecrown.user.currency;

import java.util.UUID;
import net.forthecrown.text.Text;
import net.forthecrown.text.UnitFormat;
import net.forthecrown.utils.ScoreIntMap;
import net.kyori.adventure.text.Component;

public interface Currency {

  static Currency wrap(String singularName, ScoreIntMap<UUID> map) {
    return new Currency() {

      @Override
      public Component format(int amount) {
        return UnitFormat.unit(amount, singularName);
      }

      @Override
      public String name() {
        return UnitFormat.plural(singularName, 2);
      }

      @Override
      public int get(UUID playerId) {
        return map.get(playerId);
      }

      @Override
      public void set(UUID playerId, int value) {
        map.set(playerId, value);
      }

      @Override
      public void add(UUID playerId, int value) {
        map.add(playerId, value);
      }

      @Override
      public void remove(UUID playerId, int value) {
        map.remove(playerId, value);
      }
    };
  }

  default float getGainMultiplier(UUID playerId) {
    return 1.0F;
  }

  String name();

  Component format(int amount);

  default Component format(int amount, float multiplier) {
    if (multiplier > 1) {
      return Text.format("{0} ({1, number}x multiplier)", format(amount), multiplier);
    }

    return format(amount);
  }

  int get(UUID playerId);

  void set(UUID playerId, int value);

  default void add(UUID playerId, int value) {
    set(playerId, get(playerId) + value);
  }

  default void remove(UUID playerId, int value) {
    set(playerId, get(playerId) - value);
  }
}
