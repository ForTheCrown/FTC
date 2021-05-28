package net.forthecrown.emperor.economy;

import net.forthecrown.emperor.utils.ListUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SortedBalanceMap implements BalanceMap {
    private final List<BalEntry> entries = new ArrayList<>();
    private final int defaultAmount;

    public SortedBalanceMap(int defaultAmount){
        this.defaultAmount = defaultAmount;
    }

    @Override
    public int getDefaultAmount() {
        return defaultAmount;
    }

    @Override
    public boolean contains(UUID id){
        return getIndex(id) != -1;
    }

    @Override
    public void remove(UUID id){
        int index = getIndex(id);
        if(index == -1) return;

        entries.remove(index);
    }

    @Override
    public int get(UUID id){
        int index = getIndex(id);
        if(index == -1) return defaultAmount;

        return entries.get(index).getValue();
    }

    @Override
    public int get(int index){
        return entries.get(index).getValue();
    }

    @Override
    public BalEntry getEntry(int index){
        return entries.get(index);
    }

    @Override
    public Component getPrettyDisplay(int index){
        if(!isInList(index)) throw new IndexOutOfBoundsException(index);

        BalEntry entry = getEntry(index);
        OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getUniqueId());
        if(player == null || player.getName() == null) return null;

        return Component.text()
                .append(Component.text(player.getName()))
                .append(Component.text(" - "))
                .append(Balances.formatted(entry.getValue()).color(NamedTextColor.YELLOW))
                .build();
    }

    @Override
    public int getIndex(UUID id){
        for (int i = 0; i < entries.size()/2; i++){
            BalEntry entry = entries.get(i);
            if(entry.getUniqueId().equals(id)) return i;

            int oppositeEnd = entries.size() - 1 - i;
            entry = entries.get(oppositeEnd);
            if(entry.getUniqueId().equals(id)) return oppositeEnd;
        }
        return -1;
    }

    @Override
    public int size(){
        return entries.size();
    }

    @Override
    public void put(UUID id, int amount){
        int index = getIndex(id);
        BalEntry entry = new BalEntry(id, amount);

        if(index == -1){
            entries.add(entry);
            index = entries.size()-1;
        } else entries.set(index, entry);

        checkMoving(index);
    }

    private void checkMoving(int index){
        int moveDir = moveDir(index);
        if(moveDir == 0) return;

        moveInDir(index, moveDir);
    }

    private void moveInDir(int index, int dir){
        int newIndex = index + dir;

        BalEntry entry = entries.get(newIndex);
        BalEntry newE = entries.get(index);

        entries.set(newIndex, newE);
        entries.set(index, entry);

        checkMoving(newIndex);
    }

    private int moveDir(int index){
        BalEntry entry = entries.get(index);

        int towardsTop = index + 1;
        if(isInList(towardsTop)){
            BalEntry top = entries.get(towardsTop);
            if(top.compareTo(entry) == 1) return 1;
        }

        int towardsBottom = index - 1;
        if(isInList(towardsBottom)){
            BalEntry bottom = entries.get(towardsBottom);
            if(bottom.compareTo(entry) == -1) return -1;
        }

        return 0;
    }

    private boolean isInList(int index){
        if(index < 0) return false;
        return index <= entries.size() - 1;
    }

    @Override
    public long getTotalBalance(){
        long totalAmount = 0;
        for (BalEntry e: getEntries()){
            totalAmount += e.getValue();
        }

        return totalAmount;
    }

    @Override
    public String toString(){
        return ListUtils.join(entries, "\n", bal -> bal.getUniqueId() + " has " + bal.getValue());
    }

    @Override
    public List<UUID> getKeys(){
        return ListUtils.convertToList(entries, BalEntry::getUniqueId);
    }

    @Override
    public List<Integer> getValues(){
        return ListUtils.convertToList(entries, BalEntry::getValue);
    }

    List<BalEntry> getEntries(){
        return new ArrayList<>(entries);
    }

}
