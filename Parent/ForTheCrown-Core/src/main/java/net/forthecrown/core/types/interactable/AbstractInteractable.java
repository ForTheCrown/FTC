package net.forthecrown.core.types.interactable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Interactable;
import net.forthecrown.core.serialization.AbstractJsonSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class AbstractInteractable extends AbstractJsonSerializer<FtcCore> implements Interactable {

    protected final List<InteractionAction> actions = new ArrayList<>();
    protected final Map<String, InteractionCheck> preconditions = new HashMap<>();
    protected boolean sendFail;

    protected AbstractInteractable(String fileName, String directory, boolean stopIfFileDoesntExist) {
        super(fileName, directory, stopIfFileDoesntExist, FtcCore.getInstance());
    }

    protected void deleteFile(){
        super.delete();
    }

    public abstract void delete();

    protected void saveInto(JsonObject json){
        json.add("sendFail", new JsonPrimitive(sendFail));

        JsonObject preconditions = new JsonObject();
        for (InteractionCheck p: getPreconditions()){
            preconditions.add(p.getRegistrationName(), p.serialize());
        }
        json.add("preconditions", preconditions);

        JsonArray array = new JsonArray();

        for (InteractionAction a: getActions()){
            JsonObject object = new JsonObject();
            object.add("type", new JsonPrimitive(a.getRegistrationName()));
            object.add("value", a.serialize());

            array.add(object);
        }

        json.add("actions", array);
    }

    protected void reloadFrom(JsonObject json){
        sendFail = json.get("sendFail").getAsBoolean();

        preconditions.clear();
        JsonElement precons = json.get("preconditions");
        if(precons != null && precons.isJsonObject()){
            for (Map.Entry<String, JsonElement> e: precons.getAsJsonObject().entrySet()){
                InteractionCheck pre = UseablesManager.getPrecondition(e.getKey());

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
            InteractionAction action = UseablesManager.getAction(j.get("type").getAsString());

            try {
                action.parse(j.get("value"));
            } catch (CommandSyntaxException ignored) {}

            actions.add(action);
        }
    }

    @Override
    public void interact(Player player) {
        if(!test(player)) return;
        actions.forEach(a -> a.onInteract(player));
    }

    @Override
    public void addAction(InteractionAction action) {
        actions.add(action);
    }

    @Override
    public void removeAction(int index) {
        actions.remove(index);
    }

    @Override
    public List<InteractionAction> getActions() {
        return actions;
    }

    @Override
    public void clearActions() {
        actions.clear();
    }

    @Override
    public List<InteractionCheck> getPreconditions() {
        return new ArrayList<>(preconditions.values());
    }

    @Override
    public void addPrecondition(InteractionCheck precondition) {
        preconditions.put(precondition.getRegistrationName(), precondition);
    }

    @Override
    public void removePrecondition(String name) {
        preconditions.remove(name);
    }

    @Override
    public void clearPreconditions() {
        preconditions.clear();
    }

    @Override
    public Set<String> getPreconditionTypes() {
        return preconditions.keySet();
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
    public boolean test(Player player) {
        for (InteractionCheck p: preconditions.values()){
            if(!p.test(player)){
                if(sendFail){
                    Component failMsg = p.getPersonalizedFailMessage(player);
                    if(failMsg != null) player.sendMessage(failMsg);
                }

                return false;
            }
        }
        return true;
    }
}
