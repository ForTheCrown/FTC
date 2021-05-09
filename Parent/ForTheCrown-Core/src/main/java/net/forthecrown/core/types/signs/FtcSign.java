package net.forthecrown.core.types.signs;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownSign;
import net.forthecrown.core.serialization.AbstractJsonSerializer;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.JsonUtils;
import net.forthecrown.core.utils.MapUtils;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FtcSign extends AbstractJsonSerializer<FtcCore> implements CrownSign {

    private final Location location;
    private boolean sendFail;

    private final Map<String, SignPrecondition> preconditions = Maps.newHashMap();
    private final List<SignAction> actions = Lists.newArrayList();

    public FtcSign(Location location, boolean create){
        super(CrownUtils.locationToFilename(location), "signs", FtcCore.getInstance());
        this.location = location;

        if(!fileExists && !create) throw new IllegalStateException(super.fileName + " doesn't exist");

        SignManager.SIGNS.put(location, this);
        reload();
    }

    @Override
    protected void save(JsonObject json) {
        json.add("sendFail", new JsonPrimitive(sendFail));

        JsonObject preconditions = new JsonObject();
        for (SignPrecondition p: getPreconditions()){
            preconditions.add(p.getRegistrationName(), p.serialize());
        }
        json.add("preconditions", preconditions);

        JsonArray array = new JsonArray();

        for (SignAction a: getActions()){
            JsonObject object = new JsonObject();
            object.add("type", new JsonPrimitive(a.getRegistrationName()));
            object.add("value", a.serialize());

            array.add(object);
        }

        json.add("actions", array);
    }

    @Override
    protected void reload(JsonObject json) {
        sendFail = json.get("sendFail").getAsBoolean();

        preconditions.clear();
        JsonElement precons = json.get("preconditions");
        if(precons != null && precons.isJsonObject()){
            for (Map.Entry<String, JsonElement> e: precons.getAsJsonObject().entrySet()){
                SignPrecondition pre = SignManager.getPrecondition(e.getKey());

                try {
                    pre.parse(e.getValue());
                } catch (CommandSyntaxException ignored) {}

                preconditions.put(pre.getRegistrationName(), pre);
            }
        }

        actions.clear();
        JsonElement actionsElement = json.get("actions");
        if(actionsElement == null || !actionsElement.isJsonArray()) return;

        for (JsonElement e: actionsElement.getAsJsonArray()){
            JsonObject j = e.getAsJsonObject();
            SignAction action = SignManager.getAction(j.get("type").getAsString());

            try {
                action.parse(j.get("value"));
            } catch (CommandSyntaxException ignored) {}

            actions.add(action);
        }
    }

    @Override
    protected JsonObject createDefaults(JsonObject json) {
        json.add("location", JsonUtils.serializeLocation(location));
        json.add("preconditions", new JsonArray());
        json.add("actions", new JsonArray());

        return json;
    }

    @Override
    public void interact(Player player){
        if(!test(player)) return;
        actions.forEach(a -> a.onInteract(player));
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public List<SignAction> getActions() {
        return actions;
    }

    @Override
    public void addAction(SignAction action){
        actions.add(action);
    }

    @Override
    public void removeAction(int index){
        actions.remove(index);
    }

    @Override
    public void clearActions(){
        actions.clear();
    }

    @Override
    public Sign getSign(){
        return (Sign) location.getBlock().getState();
    }

    @Override
    public boolean sendFailMessage(){
        return sendFail;
    }

    @Override
    public void setSendFail(boolean send){
        this.sendFail = send;
    }

    @Override
    public List<SignPrecondition> getPreconditions() {
        return new ArrayList<>(preconditions.values());
    }

    @Override
    public Set<String> getPreconditionTypes() {
        return preconditions.keySet();
    }

    @Override
    public void addPrecondition(SignPrecondition precondition) {
        preconditions.put(precondition.getRegistrationName(), precondition);
    }

    @Override
    public void removePrecondition(String index) {
        preconditions.remove(index);
    }

    @Override
    public void clearPreconditions() {
        preconditions.clear();
    }


    @Override
    public boolean test(Player player) {
        if(MapUtils.isNullOrEmpty(preconditions)) return true;

        for (SignPrecondition pre: preconditions.values()){
            if(!pre.test(player)){
                if(sendFail && pre.getPersonalizedFailMessage(player) != null) player.sendMessage(pre.getPersonalizedFailMessage(player));
                return false;
            }
        }

        return true;
    }

    @Override
    public void delete() {
        super.delete();

        Sign sign = getSign();
        sign.getPersistentDataContainer().remove(SignManager.SIGN_KEY);
        sign.update();
    }
}
