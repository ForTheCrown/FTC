package net.forthecrown.economy.signshops;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import lombok.Getter;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.ListTag;
import net.forthecrown.nbt.TypeIds;
import net.forthecrown.text.page.PagedIterator;
import net.forthecrown.utils.io.TagUtil;
import org.jetbrains.annotations.Nullable;

public class ShopHistory {

  private final List<HistoryEntry> entries = new ObjectArrayList<>();

  @Getter
  private final SignShop shop;

  public ShopHistory(SignShop shop) {
    this.shop = shop;
  }

  public void addEntry(SignShopSession session) {
    addEntry(
        new HistoryEntry(
            System.currentTimeMillis(),
            session.getCustomer().getUniqueId(),
            session.getAmount(),
            session.getTotalEarned(),
            session.getShop().getType().isBuyType()
        )
    );
  }

  public void addEntry(HistoryEntry entry) {
    entries.add(0, entry);
    clearOld();
  }

  public HistoryEntry getEntry(int index) {
    return entries.get(index);
  }

  public void clear() {
    entries.clear();
  }

  public boolean isEmpty() {
    clearOld();
    return entries.isEmpty();
  }

  public int size() {
    clearOld();
    return entries.size();
  }

  public PagedIterator<HistoryEntry> pageIterator(int page, int pageSize) {
    return PagedIterator.of(entries, page, pageSize);
  }

  private boolean isTooOld(HistoryEntry entry) {
    //long expiryDate = GeneralConfig.dataRetentionTime + entry.date();
    //return Time.isPast(expiryDate);
    return false;
  }

  private void clearOld() {
    entries.removeIf(this::isTooOld);
  }

  @Nullable
  public BinaryTag save() {
    if (isEmpty()) {
      return null;
    }

    clearOld();
    return TagUtil.writeList(entries, HistoryEntry::save);
  }

  public void load(@Nullable BinaryTag tag) {
    entries.clear();

    if (tag == null || tag.getId() != TypeIds.LIST) {
      return;
    }

    entries.addAll(TagUtil.readList((ListTag) tag, HistoryEntry::of));
    clearOld();
  }
}