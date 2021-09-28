package net.forthecrown.economy;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;
import java.util.function.IntSupplier;

/**
 * A constantly sorted map of balances.
 * <p></p>
 * This implementation isn't actually a map, rather it is a list of entries
 * that is constantly reorganized and sorted whenever a balance is modified,
 * added or removed. This keeps the map sorted at all times.
 * <p></p>
 * The downside of this implementation is that currently it is possible for
 * two entries with the same UUID to exist. So uhh TODO: fix multiple entries for one UUID.
 */
public class SortedBalanceMap implements BalanceMap {
    private final ObjectArrayList<Balance> entries = new ObjectArrayList<>(300);
    private final IntSupplier defaultAmount;

    public SortedBalanceMap(IntSupplier defaultAmount){
        this.defaultAmount = defaultAmount;
    }

    @Override
    public IntSupplier getDefaultSupplier() {
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
        if(index == -1) return getDefaultAmount();

        return entries.get(index).getValue();
    }

    @Override
    public int get(int index){
        return entries.get(index).getValue();
    }

    @Override
    public Balance getEntry(int index){
        return entries.get(index);
    }

    @Override
    public Component getPrettyDisplay(int index){
        if(!isInList(index)) throw new IndexOutOfBoundsException(index);

        Balance entry = getEntry(index);
        OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getUniqueId());
        if(player == null || player.getName() == null){
            remove(entry.getUniqueId());
            return null;
        }

        CrownUser user = UserManager.getUser(player);
        Component displayName = user.nickDisplayName();

        user.unloadIfOffline();

        return Component.text()
                .append(displayName)
                .append(Component.text(" - "))
                .append(FtcFormatter.rhines(entry.getValue()).color(NamedTextColor.YELLOW))
                .build();
    }

    @Override
    public int getIndex(UUID id){
        for (int i = 0; i < entries.size()/2; i++){
            Balance entry = entries.get(i);
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
        Balance entry = new Balance(id, amount);

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

        Balance entry = entries.get(newIndex);
        Balance newE = entries.get(index);

        entries.set(newIndex, newE);
        entries.set(index, entry);

        checkMoving(newIndex);
    }

    private int moveDir(int index){
        Balance entry = entries.get(index);

        int towardsTop = index + 1;
        if(isInList(towardsTop)){
            Balance top = entries.get(towardsTop);
            if(top.compareTo(entry) == 1) return 1;
        }

        int towardsBottom = index - 1;
        if(isInList(towardsBottom)){
            Balance bottom = entries.get(towardsBottom);
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
        int defAmount = getDefaultSupplier().getAsInt();

        return entries.stream()
                .filter(e -> e.getValue() > defAmount)
                .mapToLong(Balance::getValue)
                .sum();
    }

    @Override
    public String toString(){
        return ListUtils.join(entries, "\n", bal -> bal.getUniqueId() + " has " + bal.getValue());
    }

    @Override
    public List<UUID> keys(){
        return ImmutableList.copyOf(ListUtils.convert(entries, Balance::getUniqueId));
    }

    @Override
    public List<Integer> values(){
        return ImmutableList.copyOf(ListUtils.convert(entries, Balance::getValue));
    }

    @Override
    public List<Balance> entries() {
        return ImmutableList.copyOf(entries);
    }
}
