package net.forthecrown.economy;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.IntSupplier;

public class SortedBalanceMap implements BalanceMap {
    private final ObjectList<BalEntry> entries = new ObjectArrayList<>(300);
    private final IntSupplier defaultAmount;

    public SortedBalanceMap(IntSupplier defaultAmount){
        this.defaultAmount = defaultAmount;
    }

    @Override
    public IntSupplier getDefaultAmount() {
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
        if(index == -1) return defaultAmount.getAsInt();

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
        if(player == null || player.getName() == null){
            remove(entry.getUniqueId());
            return null;
        }

        CrownUser user = UserManager.getUser(player);
        Component displayName = user.nickDisplayName();

        user.unloadIfNotOnline();

        return Component.text()
                .append(displayName)
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
    public void clear(){
        entries.clear();
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
        for (BalEntry e: entries()){
            totalAmount += e.getValue();
        }

        return totalAmount;
    }

    @Override
    public String toString(){
        return ListUtils.join(entries, "\n", bal -> bal.getUniqueId() + " has " + bal.getValue());
    }

    @Override
    public List<UUID> keySet(){
        return ListUtils.convert(entries, BalEntry::getUniqueId);
    }

    @Override
    public List<Integer> values(){
        return ListUtils.convert(entries, BalEntry::getValue);
    }

    @Override
    public List<BalEntry> entries() {
        return new ArrayList<>(entries);
    }
}
