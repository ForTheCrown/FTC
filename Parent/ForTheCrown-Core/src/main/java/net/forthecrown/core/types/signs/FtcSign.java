package net.forthecrown.core.types.signs;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownSign;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.serialization.AbstractJsonSerializer;
import net.forthecrown.core.utils.Cooldown;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.List;

public class FtcSign extends AbstractJsonSerializer<FtcCore> implements CrownSign {

    private final Location location;
    private boolean sendFail;
    private boolean sendCooldown;

    private int requiredGems;
    private int requiredBal;
    private int cooldown;

    private String requiredPermission;
    private Branch requiredBranch;
    private Rank requiredRank;

    private final List<SignAction> actions = Lists.newArrayList();

    public FtcSign(Location location){
        super(CrownUtils.locationToFilename(location), "signs", FtcCore.getInstance());
        this.location = location;

        if(!fileExists) throw new IllegalStateException(super.fileName + " doesn't exist");

        SignManager.SIGNS.put(location, this);
        reload();
    }

    public FtcSign(Location location, SignAction action){
        super(CrownUtils.locationToFilename(location), "signs", FtcCore.getInstance());
        this.location = location;
        actions.add(action);

        SignManager.SIGNS.put(location, this);
        reload();
    }

    @Override
    protected void save(JsonObject json) {
        json.add("bal", new JsonPrimitive(requiredBal));
        json.add("gems", new JsonPrimitive(requiredGems));
        json.add("cooldown", new JsonPrimitive(cooldown));

        json.add("branch", JsonUtils.serializeEnum(requiredBranch));
        json.add("rank", JsonUtils.serializeEnum(requiredRank));

        json.add("sendFail", new JsonPrimitive(sendFail));
        json.add("sendCooldown", new JsonPrimitive(sendCooldown));

        JsonArray array = new JsonArray();

        for (SignAction a: actions){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", a.getAction().toString().toLowerCase());
            jsonObject.add("value", a.serialize());

            array.add(jsonObject);
        }

        json.add("actions", array);
    }

    @Override
    protected void reload(JsonObject json) {
        cooldown = json.getAsJsonPrimitive("cooldown").getAsInt();
        requiredGems = json.getAsJsonPrimitive("gems").getAsInt();
        requiredBal = json.getAsJsonPrimitive("bal").getAsInt();

        sendFail = json.get("sendFail").getAsBoolean();
        sendCooldown = json.get("sendCooldown").getAsBoolean();

        requiredRank = JsonUtils.parseEnum(Rank.class, json.get("rank"));
        requiredBranch = JsonUtils.parseEnum(Branch.class, json.get("branch"));

        JsonElement arrayElement = json.get("actions");
        if(arrayElement == null || !arrayElement.isJsonArray()) return;
        JsonArray array = arrayElement.getAsJsonArray();

        actions.clear();
        for (JsonElement e: array){
            JsonObject object = e.getAsJsonObject();
            JsonElement value = object.get("value");
            SignAction.Action action = SignAction.Action.valueOf(object.get("action").getAsString().toUpperCase());

            SignAction type = action.get();
            try {
                type.parse(value);
            } catch (CommandSyntaxException ignored) { continue; }
            actions.add(type);
        }
    }

    @Override
    protected JsonObject createDefaults(JsonObject json) {
        json.add("location", JsonUtils.serializeLocation(location));
        json.add("bal", new JsonPrimitive(0));
        json.add("gems", new JsonPrimitive(0));
        json.add("cooldown", new JsonPrimitive(0));

        json.add("sendFail", new JsonPrimitive(false));
        json.add("sendCooldown", new JsonPrimitive(false));

        json.add("rank", JsonNull.INSTANCE);
        json.add("branch", JsonNull.INSTANCE);

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
    public int getRequiredGems() {
        return requiredGems;
    }

    @Override
    public void setRequiredGems(int requiredGems) {
        this.requiredGems = requiredGems;
    }

    @Override
    public int getRequiredBal() {
        return requiredBal;
    }

    @Override
    public void setRequiredBal(int requiredBal) {
        this.requiredBal = requiredBal;
    }

    @Override
    public int getCooldown() {
        return cooldown;
    }

    @Override
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    @Override
    public String getRequiredPermission() {
        return requiredPermission;
    }

    @Override
    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    @Override
    public Branch getRequiredBranch() {
        return requiredBranch;
    }

    @Override
    public void setRequiredBranch(Branch requiredBranch) {
        this.requiredBranch = requiredBranch;
    }

    @Override
    public Rank getRequiredRank() {
        return requiredRank;
    }

    @Override
    public void setRequiredRank(Rank requiredRank) {
        this.requiredRank = requiredRank;
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
    public void setFailSendMessage(boolean send){
        this.sendFail = send;
    }

    @Override
    public boolean sendCooldownMessage(){
        return sendCooldown;
    }

    @Override
    public void setSendCooldownMessage(boolean send){
        this.sendCooldown = send;
    }

    @Override
    public boolean test(Player player) {
        if(Cooldown.contains(player, "signs_" + fileName)){
            if(sendCooldown) player.sendMessage(Component.text("Can't use this currently").color(NamedTextColor.GRAY));
            return false;
        }
        Cooldown.add(player, "signs_" + fileName, cooldown == 0 ? 20 : cooldown);

        CrownUser user = UserManager.getUser(player);

        if(requiredRank != null && !user.hasRank(requiredRank)){
            if(sendFail) player.sendMessage(failMessage("rank", requiredRank.noEndSpacePrefix()));
            return false;
        }
        if(requiredBranch != null && user.getBranch() != requiredBranch){
            if(sendFail) player.sendMessage(failMessage("branch", Component.text(requiredBranch.getName())));
            return false;
        }

        if(requiredGems > 0 && user.getGems() < requiredGems){
            if(sendFail) player.sendMessage(failMessage("gems", Component.text(requiredGems + " Gems")));
            return false;
        }
        if(requiredBal > 0 && FtcCore.getBalances().get(user.getUniqueId()) < requiredBal){
            if(sendFail) player.sendMessage(failMessage("balance", Balances.formatted(requiredBal)));
            return false;
        }

        return true;
    }

    private Component failMessage(String requirement, Component needed){
        return Component.text()
                .color(NamedTextColor.GRAY)
                .append(Component.text("You don't have the required " + requirement + ": "))
                .append(needed.colorIfAbsent(NamedTextColor.YELLOW))
                .append(Component.text("."))
                .build();
    }

    @Override
    public void delete() {
        super.delete();

        Sign sign = getSign();
        sign.getPersistentDataContainer().remove(SignManager.SIGN_KEY);
        sign.update();
    }
}
