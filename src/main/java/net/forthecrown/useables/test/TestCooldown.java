package net.forthecrown.useables.test;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.LongTag;
import net.forthecrown.useables.CheckHolder;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.useables.UsageType;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class TestCooldown extends UsageTest {

  // --- TYPE ---
  public static final UsageType<TestCooldown> TYPE = UsageType.of(TestCooldown.class)
      .setSuggests(ArgumentTypes.time()::listSuggestions);

  /**
   * NBT tag for cooldown duration
   */
  private static final String TAG_DURATION = "millisDuration";

  /**
   * NBT tag for UUIDs that are on cooldown
   */
  private static final String TAG_ENTRIES = "entries";

  private final long millisDuration;
  private final Object2LongMap<UUID> entries = new Object2LongOpenHashMap<>();

  public TestCooldown(long millis) {
    super(TYPE);
    this.millisDuration = millis;
  }

  @Override
  public Component displayInfo() {
    return Text.format("duration='{0, time}', players={1, number}",
        millisDuration, entries.size()
    );
  }

  @Override
  public BinaryTag save() {
    CompoundTag tag = BinaryTags.compoundTag();
    tag.putLong(TAG_DURATION, millisDuration);

    // Serialize cooldown entry map only if
    // there are entries in it
    if (!entries.isEmpty()) {
      CompoundTag entries = BinaryTags.compoundTag();

      for (var e : this.entries.object2LongEntrySet()) {
        // Cooldown has already expired, don't save
        if (Time.isPast(e.getLongValue())) {
          continue;
        }

        entries.putLong(e.getKey().toString(), e.getLongValue());
      }

      if (!entries.isEmpty()) {
        tag.put(TAG_ENTRIES, entries);
      }
    }

    return tag;
  }

  @Override
  public boolean test(Player player, CheckHolder holder) {
    var cooldownEnds = entries.getLong(player.getUniqueId());
    return Time.isPast(cooldownEnds);
  }

  @Override
  public Component getFailMessage(Player player, CheckHolder holder) {
    if (millisDuration < TimeUnit.MINUTES.toMillis(2)
        || !entries.containsKey(player.getUniqueId())
    ) {
      return Component.text("You're on cooldown!", NamedTextColor.GRAY);
    }

    return Text.format("You cannot use this for {0, time, -timestamp}",
        NamedTextColor.GRAY,
        entries.getLong(player.getUniqueId())
    );
  }

  @Override
  public void postTests(Player player, CheckHolder holder) {
    entries.put(player.getUniqueId(), System.currentTimeMillis() + millisDuration);
  }

  // --- TYPE CONSTRUCTORS ---

  @UsableConstructor(ConstructType.PARSE)
  public static TestCooldown parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException {
    return new TestCooldown(ArgumentTypes.time().parse(reader).toMillis());
  }

  @UsableConstructor(ConstructType.TAG)
  public static TestCooldown load(BinaryTag tag) {
    CompoundTag cTag = (CompoundTag) tag;

    long millis = cTag.getLong(TAG_DURATION);
    TestCooldown result = new TestCooldown(millis);

    CompoundTag entryTag = cTag.getCompound(TAG_ENTRIES);

    if (!entryTag.isEmpty()) {
      for (var e : entryTag.entrySet()) {
        UUID uuid = UUID.fromString(e.getKey());
        long ends = ((LongTag) e.getValue()).longValue();

        // Don't load already expired entries
        if (Time.isPast(ends)) {
          continue;
        }

        result.entries.put(uuid, ends);
      }
    }

    return result;
  }
}