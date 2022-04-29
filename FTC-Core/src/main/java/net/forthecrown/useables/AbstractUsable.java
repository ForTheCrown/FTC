package net.forthecrown.useables;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.useables.actions.UsageActionInstance;
import net.forthecrown.useables.checks.UsageCheckInstance;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AbstractUsable extends AbstractJsonSerializer implements Usable {

    protected final List<UsageActionInstance> actions = new ObjectArrayList<>();
    private final Object2ObjectMap<Key, UsageCheckInstance> checks = new Object2ObjectOpenHashMap<>();
    protected boolean sendFail, cancelVanilla = true;

    protected AbstractUsable(String fileName, String directory, boolean stopIfFileDoesntExist) {
        super(fileName, directory, stopIfFileDoesntExist);
    }

    protected void deleteFile(){
        super.delete();
    }

    public abstract void delete();

    protected void saveInto(JsonWrapper json){
        json.add("sendFail", sendFail);
        json.add("cancelVanilla", cancelVanilla);

        InteractionUtils.saveChecks(this, json.getSource());
        InteractionUtils.saveActions(this, json.getSource());
    }

    protected void reloadFrom(JsonWrapper json) {
        sendFail = json.get("sendFail").getAsBoolean();
        cancelVanilla = json.getBool("cancelVanilla");

        try {
            InteractionUtils.loadChecks(this, json.getSource());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }

        InteractionUtils.loadActions(this, json.getSource());
    }

    @Override
    public void interact(Player player) {
        if(!test(player)) return;
        actions.forEach(a -> a.onInteract(player));
    }

    @Override
    public void addAction(UsageActionInstance action) {
        actions.add(action);
    }

    @Override
    public void removeAction(int index) {
        actions.remove(index);
    }

    @Override
    public List<UsageActionInstance> getActions() {
        return actions;
    }

    @Override
    public void clearActions() {
        actions.clear();
    }

    @Override
    public List<UsageCheckInstance> getChecks() {
        return new ArrayList<>(checks.values());
    }

    @Override
    public void addCheck(UsageCheckInstance precondition) {
        checks.put(FtcUtils.ensureBukkit(precondition.typeKey()), precondition);
    }

    @Override
    public void removeCheck(Key name) {
        checks.remove(FtcUtils.ensureBukkit(name));
    }

    @Override
    public void clearChecks() {
        checks.clear();
    }

    @Override
    public Set<Key> getCheckTypes() {
        return checks.keySet();
    }

    @Override
    public boolean sendFailMessage() {
        return sendFail;
    }

    @Override
    public void setSendFail(boolean send) {
        this.sendFail = send;
    }

    @Override
    public void setCancelVanilla(boolean cancelVanilla) {
        this.cancelVanilla = cancelVanilla;
    }

    @Override
    public boolean cancelVanillaInteraction() {
        return cancelVanilla;
    }

    @Override
    public boolean test(Player player) {
        List<Consumer<Player>> onSuccesses = new ArrayList<>();
        for (UsageCheckInstance p: checks.values()){
            if(!p.test(player)){
                if(sendFail){
                    Component failMsg = p.failMessage(player);
                    if(failMsg != null) player.sendMessage(failMsg);
                }
                return false;
            }

            Consumer<Player> s = p.onSuccess();
            if(s != null) onSuccesses.add(s);
        }

        onSuccesses.forEach(c -> c.accept(player));
        return true;
    }

    @Override
    public <T extends UsageActionInstance> T getAction(Key key, Class<T> clazz) {
        key = FtcUtils.ensureBukkit(key);
        for (UsageActionInstance a: actions){
            if(!a.typeKey().equals(key)) continue;
            if(!clazz.isAssignableFrom(a.getClass())) continue;
            return (T) a;
        }

        return null;
    }

    @Override
    public <T extends UsageCheckInstance> T getCheck(Key key, Class<T> clazz) {
        key = FtcUtils.ensureBukkit(key);
        if(!checks.containsKey(key)) return null;

        UsageCheckInstance c = checks.get(key);
        if(!clazz.isAssignableFrom(c.getClass())) return null;
        return (T) c;
    }
}