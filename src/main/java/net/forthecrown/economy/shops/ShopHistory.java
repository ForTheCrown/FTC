package net.forthecrown.economy.shops;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.utils.text.format.page.PageEntryIterator;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.TagUtil;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    public PageEntryIterator<HistoryEntry> pageIterator(int page, int pageSize) {
        return PageEntryIterator.of(entries, page, pageSize);
    }

    private boolean isTooOld(HistoryEntry entry) {
        return Time.isPast(GeneralConfig.dataRetentionTime + entry.date());
    }

    private void clearOld() {
        entries.removeIf(this::isTooOld);
    }

    @Nullable
    public Tag save() {
        if (isEmpty()) {
            return null;
        }

        clearOld();
        return TagUtil.writeList(entries, HistoryEntry::save);
    }

    public void load(@Nullable Tag tag) {
        entries.clear();

        if (tag == null) {
            return;
        }

        entries.addAll(TagUtil.readList((ListTag) tag, HistoryEntry::of));
        clearOld();
    }
}