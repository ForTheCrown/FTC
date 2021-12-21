package net.forthecrown.economy;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private Balance[] entries = new Balance[30];
    private int size;

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

        //Remove entry
        entries[index] = null;
        size--;

        //If the index wasn't the last entry in the list,
        //collapse array at index
        if(size != index) {
            //Collapse array at index so there's no empty spaces
            System.arraycopy(entries, index+1, entries, index, entries.length-index - 1);

            //Nullify last entry, as it's a copy of length - 2 now
            entries[entries.length - 1] = null;
        }
    }

    @Override
    public int get(UUID id) {
        int index = getIndex(id);
        if(index == -1) return getDefaultAmount();

        return entries[index].getValue();
    }

    @Override
    public int get(int index){
        validateIndex(index);
        return entries[index].getValue();
    }

    @Override
    public Balance getEntry(int index){
        validateIndex(index);
        return entries[index];
    }

    @Override
    public Component getPrettyDisplay(int index) {
        validateIndex(index);

        Balance entry = getEntry(index);
        if(!UserManager.isPlayerID(entry.getUniqueId())) return null;

        CrownUser user = UserManager.getUser(entry.getUniqueId());
        Component displayName = user.nickDisplayName();

        user.unloadIfOffline();

        return Component.text()
                .append(displayName)
                .append(Component.text(" - "))
                .append(FtcFormatter.rhines(entry.getValue()).color(NamedTextColor.YELLOW))
                .build();
    }

    @Override
    public int getIndex(UUID id) {
        //Slow, but idk how better to do it
        //Go through list, checking each end of the list
        //To find the entry

        int half = size >> 1;
        boolean divisible = half * 2 == size;

        //Use divisible variable to correct for odd sizes
        for (int i = 0; i < half + (divisible ? 0 : 1); i++) {
            //Check front half
            Balance entry = getEntry(i);
            if(entry != null && entry.getUniqueId().equals(id)) return i;

            //Check last half
            int oppositeEnd = size - 1 - i;
            entry = getEntry(oppositeEnd);
            if(entry != null && entry.getUniqueId().equals(id)) return oppositeEnd;
        }

        //Not found
        return -1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        entries = new Balance[0];
        size = 0;
    }

    @Override
    public void put(UUID id, int amount) {
        int index = getIndex(id);
        //Either get the entry, and update it's value, or create it
        Balance entry = index == -1 ? new Balance(id, amount) : getEntry(index).setValue(amount);

        //If new entry
        if(index == -1) {
            index = size;

            //Increment size
            size++;

            //If array size has to be increased
            if(size >= entries.length) {
                Balance[] copy = entries;                                               //Copy old entries
                entries = new Balance[copy.length + 1];                                 //Make new array with bigger size
                System.arraycopy(copy, 0, entries, 0, copy.length);     //Copy all entries from copy to new array
            }

            setEntry(index, entry);
        }

        checkSorted(index);
    }

    private void checkSorted(int index){
        int moveDir = moveDir(index);
        if(moveDir == 0) return;

        moveInDir(index, moveDir);
    }

    private void setEntry(int index, Balance bal) {
        entries[index] = bal;
    }

    private void moveInDir(int index, int dir){
        int newIndex = index + dir;

        Balance entry = getEntry(newIndex);
        Balance newE = getEntry(index);

        setEntry(newIndex, newE);
        setEntry(index, entry);

        checkSorted(newIndex);
    }

    private int moveDir(int index){
        Balance entry = getEntry(index);

        int towardsTop = index + 1;
        if(isInList(towardsTop)){
            Balance top = getEntry(towardsTop);
            if(top != null && top.compareTo(entry) == 1) return 1;
        }

        int towardsBottom = index - 1;
        if(isInList(towardsBottom)){
            Balance bottom = getEntry(towardsBottom);
            if(bottom != null && bottom.compareTo(entry) == -1) return -1;
        }

        return 0;
    }

    private boolean isInList(int index){
        if(index < 0) return false;
        return index <= size - 1;
    }

    @Override
    public long getTotalBalance(){
        int defAmount = getDefaultSupplier().getAsInt();

        return Arrays.stream(entries)
                .filter(b -> b != null && b.getValue() > defAmount)
                .mapToInt(Balance::getValue)
                .sum();
    }

    @Override
    public String toString(){
        return ListUtils.join(entries(), "\n", bal -> bal.getUniqueId() + " = " + bal.getValue());
    }

    @Override
    public List<UUID> keys(){
        return nonNullEntries()
                .map(Balance::getUniqueId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Integer> values(){
        return nonNullEntries()
                .map(Balance::getValue)
                .collect(Collectors.toList());
    }

    private Stream<Balance> nonNullEntries() {
        return Arrays.stream(entries)
                .filter(Objects::nonNull);
    }

    @Override
    public List<Balance> entries() {
        ObjectList<Balance> bals = new ObjectArrayList<>();

        for (Balance b: entries) {
            if(b == null) continue;

            bals.add(b);
        }

        return bals;
    }

    private void validateIndex(int index) {
        if(!isInList(index)) throw new IndexOutOfBoundsException(index);
    }
}
