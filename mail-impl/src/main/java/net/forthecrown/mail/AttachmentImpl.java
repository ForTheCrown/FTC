package net.forthecrown.mail;

import static net.kyori.adventure.text.Component.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.command.Exceptions;
import net.forthecrown.mail.event.MailClaimEvent;
import net.forthecrown.scripts.Scripts;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.text.TextWriter;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.currency.Currency;
import net.forthecrown.user.currency.CurrencyMap;
import net.forthecrown.user.currency.CurrencyMaps;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.inventory.ItemList;
import net.forthecrown.utils.inventory.ItemLists;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.FtcCodecs;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.source.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
@RequiredArgsConstructor
class AttachmentImpl implements Attachment {

  static final String KEY_TAGS = "tags";
  static final String KEY_ITEMS = "items";
  static final String KEY_SCRIPT = "claim_script";
  static final String KEY_GAIN_MULTIPLIER = "use_currency_multiplier";

  private final CurrencyMap<Integer> currencyRewards;
  private final List<String> tags;
  private final ItemList items;
  private final Source claimScript;

  private final boolean useGainMultiplier;

  public ItemList getItems() {
    return ItemLists.cloneAllItems(items);
  }

  public List<String> getTags() {
    return Collections.unmodifiableList(tags);
  }

  public CurrencyMap<Integer> getCurrencyRewards() {
    return CurrencyMaps.unmodifiable(currencyRewards);
  }

  public static Result<AttachmentImpl> load(JsonElement element) {
    if (!element.isJsonObject()) {
      return Result.error("Not an object");
    }

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());
    var builder = new BuilderImpl();

    if (json.has(KEY_TAGS)) {
      json.getList(KEY_TAGS, JsonElement::getAsString).forEach(builder::addTag);
      json.remove(KEY_TAGS);
    }

    if (json.has(KEY_GAIN_MULTIPLIER)) {
      boolean state = json.getBool(KEY_GAIN_MULTIPLIER);
      json.remove(KEY_GAIN_MULTIPLIER);
      builder.useGainMultiplier(state);
    }

    if (json.has(KEY_ITEMS)) {
      DataResult<ItemList> itemListRes
          = FtcCodecs.ITEM_LIST_CODEC.decode(JsonOps.INSTANCE, json.get(KEY_ITEMS))
          .map(Pair::getFirst);

      if (itemListRes.error().isPresent()) {
        return Result.fromDataResult(itemListRes).cast();
      }

      itemListRes.result().ifPresent(builder::items);
      json.remove(KEY_ITEMS);
    }

    if (json.has(KEY_SCRIPT)) {
      JsonElement elem = json.remove(KEY_SCRIPT);
      DataResult<Source> result
          = Scripts.loadScriptSource(new Dynamic<>(JsonOps.INSTANCE, elem), false);

      if (result.error().isPresent()) {
        return Result.fromDataResult(result).cast();
      }

      result.result().ifPresent(builder::claimScript);
    }

    if (!json.isEmpty()) {
      CurrencyMap<Integer> map = builder.currencyMap;

      for (Entry<String, JsonElement> entry : json.entrySet()) {
        map.putCurrency(entry.getKey(), entry.getValue().getAsInt());
      }
    }

    return Result.success(builder.build());
  }

  public JsonElement save() {
    JsonWrapper json = JsonWrapper.create();
    if (!tags.isEmpty()) {
      json.addList(KEY_TAGS, tags, JsonPrimitive::new);
    }

    if (!items.isEmpty()) {
      var list
          = FtcCodecs.ITEM_LIST_CODEC.encodeStart(JsonOps.INSTANCE, items)
          .getOrThrow(false, s -> {});

      json.add(KEY_ITEMS, list);
    }

    if (claimScript != null) {
      JsonElement script = claimScript.saveAsJson();
      json.add(KEY_SCRIPT, script);
    }

    if (!currencyRewards.isEmpty()) {
      for (Entry<String, Integer> entry : currencyRewards.idEntrySet()) {
        json.add(entry.getKey(), entry.getValue());
      }
    }

    if (useGainMultiplier) {
      json.add(KEY_GAIN_MULTIPLIER, true);
    }

    return json.getSource();
  }

  @Override
  public boolean useGainMultiplier() {
    return useGainMultiplier;
  }

  @Override
  public boolean isEmpty() {
    return currencyRewards.isEmpty()
        && tags.isEmpty()
        && items.isEmpty()
        && claimScript == null;
  }

  @Override
  public void claim(Player player) throws CommandSyntaxException {
    User user = Users.get(player);
    UUID id = player.getUniqueId();

    MailClaimEvent event = new MailClaimEvent(Users.get(player), this);
    event.callEvent();

    if (event.isCancelled()) {
      Component reason = event.getDenyReason();

      if (reason == null) {
        throw MailExceptions.CLAIM_NOT_ALLOWED;
      }

      throw Exceptions.create(reason);
    }

    if (!items.isEmpty()) {
      var inventory = player.getInventory();

      if (!ItemStacks.hasRoom(inventory, items)) {
        throw Exceptions.INVENTORY_FULL;
      }

      for (ItemStack item : items) {
        inventory.addItem(item.clone());
      }
    }

    currencyRewards.forEach((currency, integer) -> {
      int amount;

      if (useGainMultiplier) {
        amount = (int) (currency.getGainMultiplier(id) * integer);
      } else {
        amount = integer;
      }

      currency.add(id, amount);
    });

    if (claimScript == null) {
      return;
    }

    Scripts.newScript(claimScript)
        .compile()
        .put("user", user)
        .evaluate()
        .logError()
        .script()
        .close();
  }

  @Override
  public Component claimMessage(Player player) {
    TextJoiner joiner = TextJoiner.onComma()
        .setColor(NamedTextColor.YELLOW)
        .setPrefix(text("You got: ", NamedTextColor.GOLD));

    if (items.size() == 1) {
      var item = items.get(0);
      joiner.add(Text.itemAndAmount(item));
    } else if (items.size() > 1) {
      joiner.add(
          text("[Items]").hoverEvent(
              TextJoiner.onNewLine().add(items.stream().map(Text::itemAndAmount)).asComponent()
          )
      );
    }

    currencyRewards.forEach((currency, integer) -> {
      if (useGainMultiplier) {
        float mod = currency.getGainMultiplier(player.getUniqueId());
        joiner.add(currency.format(integer, mod));
      } else {
        joiner.add(currency.format(integer));
      }
    });

    return joiner.asComponent();
  }

  public void write(TextWriter writer) {
    currencyRewards.forEach((currency, integer) -> {
      writer.field(currency.name(), Text.formatNumber(integer));
    });

    if (items.isEmpty()) {
      return;
    }

    if (items.size() > 1) {
      writer.field("Items");
      var prefixed = writer.withPrefix(text("- "));

      for (ItemStack item : items) {
        prefixed.line(Text.itemAndAmount(item));
      }
    } else {
      writer.field("Item", Text.itemAndAmount(items.get(0)));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AttachmentImpl that)) {
      return false;
    }
    return useGainMultiplier == that.useGainMultiplier
        && Objects.equals(currencyRewards, that.currencyRewards)
        && Objects.equals(tags, that.tags)
        && Objects.equals(items, that.items)
        && Objects.equals(claimScript, that.claimScript);
  }

  @Override
  public int hashCode() {
    return Objects.hash(currencyRewards, tags, items, claimScript, useGainMultiplier);
  }

  static class BuilderImpl implements Builder {

    private final CurrencyMap<Integer> currencyMap = CurrencyMaps.newMap();
    private final ItemList items = ItemLists.newList();
    private final List<String> tags = new ArrayList<>();

    private Source claimScript;

    private boolean useGainMultiplier;

    @Override
    public Builder useGainMultiplier(boolean useMultiplier) {
      this.useGainMultiplier = useMultiplier;
      return this;
    }

    @Override
    public Builder currency(String currencyName, int reward) {
      currencyMap.putCurrency(currencyName, reward);
      return this;
    }

    @Override
    public Builder currency(Currency currency, int reward) {
      currencyMap.put(currency, reward);
      return this;
    }

    @Override
    public Builder addTag(String tag) {
      Objects.requireNonNull(tag);
      tags.add(tag);
      return this;
    }

    @Override
    public Builder addTags(Collection<String> strings) {
      tags.addAll(strings);
      return this;
    }

    @Override
    public Builder items(ItemList list) {
      items.clear();
      items.addAll(ItemLists.cloneAllItems(list));
      return this;
    }

    @Override
    public Builder addItem(ItemStack item) {
      if (ItemStacks.isEmpty(item)) {
        throw new NullPointerException("Empty item");
      }

      items.add(item.clone());
      return this;
    }

    @Override
    public Builder claimScript(Source source) {
      this.claimScript = Objects.requireNonNull(source);
      return this;
    }

    @Override
    public AttachmentImpl build() {
      return new AttachmentImpl(
          CurrencyMaps.unmodifiable(currencyMap),
          Collections.unmodifiableList(tags),
          ItemLists.cloneAllItems(items),
          claimScript,
          useGainMultiplier
      );
    }
  }
}
