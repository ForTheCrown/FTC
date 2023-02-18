package net.forthecrown.commands.usables;

import net.forthecrown.useables.UsageTypeHolder;

@FunctionalInterface
public interface UsableSaveCallback<H extends UsageTypeHolder> {
  UsableSaveCallback EMPTY = holder -> {};

  void save(H holder);

  @SuppressWarnings({"unchecked", "rawtypes"})
  default void dumbHack(UsageTypeHolder o) {
    UsableSaveCallback callback = this;
    callback.save(o);
  }

  static <H extends UsageTypeHolder> UsableSaveCallback<H> empty() {
    return EMPTY;
  }
}