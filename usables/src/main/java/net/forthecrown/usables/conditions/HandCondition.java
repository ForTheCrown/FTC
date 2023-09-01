package net.forthecrown.usables.conditions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.usables.BuiltType;
import net.forthecrown.usables.BuiltType.Parser;
import net.forthecrown.usables.Condition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.UsageType;
import net.forthecrown.usables.objects.InWorldUsable;
import net.forthecrown.utils.io.FtcCodecs;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

public class HandCondition implements Condition {

  private static final Codec<EquipmentSlot> SLOT_CODEC = FtcCodecs.enumCodec(EquipmentSlot.class);

  public static final UsageType<HandCondition> TYPE = BuiltType.<HandCondition>builder()
      .parser(new Parser<>() {
        private final ArgumentType<EquipmentSlot> slotParser;

        {
          Map<String, EquipmentSlot> slotMap = new HashMap<>();
          slotMap.put("weapon.mainhand", EquipmentSlot.HAND);
          slotMap.put("weapon.offhand", EquipmentSlot.OFF_HAND);
          slotParser = ArgumentTypes.map(slotMap);
        }

        @Override
        public HandCondition parse(StringReader reader, CommandSource source)
            throws CommandSyntaxException
        {
          EquipmentSlot slot = slotParser.parse(reader);
          return new HandCondition(slot);
        }
      })
      .loader(dynamic -> SLOT_CODEC.decode(dynamic).map(Pair::getFirst).map(HandCondition::new))
      .saver((value, ops) -> SLOT_CODEC.encodeStart(ops, value.slot))

      .applicableTo(object -> object instanceof InWorldUsable)
      .build();

  private final EquipmentSlot slot;

  public HandCondition(EquipmentSlot slot) {
    this.slot = slot;
  }

  @Override
  public boolean test(Interaction interaction) {
    return interaction.getValue("hand", EquipmentSlot.class).map(s -> s == slot).orElse(true);
  }

  @Override
  public UsageType<? extends UsableComponent> getType() {
    return TYPE;
  }

  @Override
  public @Nullable Component displayInfo() {
    return Component.text(slot.name().toLowerCase());
  }
}
