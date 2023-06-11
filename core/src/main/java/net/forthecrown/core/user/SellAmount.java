package net.forthecrown.core.user;

import static net.kyori.adventure.text.Component.text;

import lombok.Getter;
import net.kyori.adventure.text.Component;

@Getter
public enum SellAmount {

  PER_1   ( 1, "Sell per 1"),
  PER_16  (16, "Sell per 16"),
  PER_64  (64, "Sell per stack"),
  ALL     (-1, "Sell all");

  private final byte value;
  private final String sellPerText;

  SellAmount(int i, String text) {
    value = (byte) i;
    this.sellPerText = text;
  }

  public int getItemAmount() {
    return Math.max(1, getValue());
  }

  public Component amountText() {
    return text(value == -1 ? "All" : (String.valueOf(value)));
  }
}