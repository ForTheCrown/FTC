package net.forthecrown.economy.shops;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.chat.PagedDisplay;
import net.forthecrown.utils.TagUtil;
import net.forthecrown.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ShopHistory implements ShopComponent {
    private static final Component BORDER = Component.text("                        ")
            .color(NamedTextColor.GRAY)
            .decorate(TextDecoration.STRIKETHROUGH);

    public static final int PAGE_SIZE = 10;

    private final List<HistoryEntry> entries = new ObjectArrayList<>();
    private final FtcSignShop shop;

    public ShopHistory(FtcSignShop shop) {
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

    public void addEntry(UUID customer, int amount, int earned, HistoryEntry.Type type) {
        addEntry(new HistoryEntry(System.currentTimeMillis(), customer, amount, earned, type));
    }

    public void addEntry(HistoryEntry entry) {
        entries.add(0, entry);
    }

    public HistoryEntry getEntry(int index) {
        return entries.get(index);
    }

    public void clear() {
        entries.clear();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    /**
     * Creates a display of the shop's history
     * @param page The page of history to show
     * @return The display of the shop's history for the given page
     */
    public Component display(int page) {
        if(isEmpty()) {
            return Component.translatable("shops.history.empty", NamedTextColor.RED);
        }

        return PagedDisplay.create(page, PAGE_SIZE, entries,
                (e, index) -> Component.text()
                        .append(Component.text(index + ") ").color(NamedTextColor.YELLOW))
                        .append(e.display(shop))
                        .build(),

                // Header
                () -> Component.text()
                        .append(BORDER)
                        .append(Component.space())
                        .append(Component.translatable("shops.history", NamedTextColor.YELLOW))
                        .append(Component.space())
                        .append(BORDER)
                        .build(),

                // Footer
                (currentPage, lastPage, firstPage, maxPage) -> {
                    Component nextPage = lastPage  ? Component.space() : createPageButton(currentPage + 1, '>', "Next");
                    Component prevPage = firstPage ? Component.space() : createPageButton(currentPage - 1, '<', "Previous");

                    return Component.text()
                            .append(BORDER)

                            .append(prevPage)
                            .append(Component.text(currentPage + "/" + maxPage).color(NamedTextColor.YELLOW))
                            .append(nextPage)

                            .append(BORDER)
                            .build();
                }
        );
    }

    Component createPageButton(int newPage, char pointer, String hover) {
        return Component.text(" " + pointer + " ")
                .color(NamedTextColor.YELLOW)
                .hoverEvent(Component.text(hover + " page"))
                .clickEvent(ClickEvent.runCommand("/shophistory " + newPage + " " + shop.getName()))
                .decorate(TextDecoration.BOLD);
    }

    public boolean isValidPage(int page) {
        return !isInvalidIndex(PAGE_SIZE * page);
    }

    private boolean isInvalidIndex(int index) {
        if(index < 0) return true;
        return index >= size();
    }

    private boolean isTooOld(HistoryEntry entry) {
        return TimeUtil.hasCooldownEnded(FtcVars.dataRetentionTime.get(), entry.date());
    }

    private void clearOld() {
        entries.removeIf(this::isTooOld);
    }

    @Override
    public String getSerialKey() {
        return "history";
    }

    @Nullable
    @Override
    public Tag save() {
        if(isEmpty()) return null;

        clearOld();
        return TagUtil.writeList(entries, HistoryEntry::save);
    }

    @Override
    public void load(@Nullable Tag tag) {
        entries.clear();
        if(tag == null) return;

        entries.addAll(TagUtil.readList((ListTag) tag, HistoryEntry::of));
        clearOld();
    }
}