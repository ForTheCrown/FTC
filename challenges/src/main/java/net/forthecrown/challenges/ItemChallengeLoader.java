package net.forthecrown.challenges;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.forthecrown.menu.Slot;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.text.Component;

public class ItemChallengeLoader {

  static final String KEY_REWARD = "reward";
  static final String KEY_SLOT = "slot";
  static final String KEY_DESCRIPTION = "description";
  static final String KEY_TYPE = "type";
  static final String KEY_WILDCARD = "wildcard_allowed";

  public static Result<ItemChallenge> parse(JsonObject element) {
    JsonWrapper json = JsonWrapper.wrap(element);

    if (!json.has(KEY_SLOT)) {
      return Result.error("Missing %s value", KEY_SLOT);
    }

    Slot slot = Slot.load(json.get(KEY_SLOT));
    Reward reward = Reward.EMPTY;

    ResetInterval interval = json.getEnum(
        KEY_TYPE,
        ResetInterval.class,
        ResetInterval.DAILY
    );

    ImmutableList.Builder<Component> desc = ImmutableList.builder();

    for (var c : json.getList(KEY_DESCRIPTION, JsonUtils::readText)) {
      desc.add(c);
    }

    if (json.has(KEY_REWARD)) {
      reward = Reward.deserialize(json.get(KEY_REWARD));
    }

    boolean wildcardAllowed = json.getBool(KEY_WILDCARD);

    return Result.success(
        new ItemChallenge(slot, reward, desc.build(), interval, wildcardAllowed)
    );
  }
}