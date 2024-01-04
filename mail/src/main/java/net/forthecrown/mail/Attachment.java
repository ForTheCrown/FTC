package net.forthecrown.mail;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;
import net.forthecrown.user.currency.Currency;
import net.forthecrown.user.currency.CurrencyMap;
import net.forthecrown.utils.inventory.ItemList;
import net.forthecrown.utils.io.source.Source;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Attachment {

  static Builder builder() {
    return MailService.service().attachmentBuilder();
  }

  CurrencyMap<Integer> getCurrencyRewards();

  boolean useGainMultiplier();

  List<String> getTags();

  ItemList getItems();

  Source getClaimScript();

  boolean isEmpty();

  void claim(Player player) throws CommandSyntaxException;

  Component claimMessage(Player player);

  interface Builder {

    Builder currency(String currencyName, int reward);

    Builder currency(Currency currency, int reward);

    Builder useGainMultiplier(boolean useMultiplier);

    Builder addTag(String tag);

    Builder addTags(Collection<String> strings);

    Builder items(ItemList list);

    Builder addItem(ItemStack item);

    Builder claimScript(Source source);

    Attachment build();
  }
}
