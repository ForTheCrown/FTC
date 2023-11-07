package net.forthecrown.usables.virtual;

import net.forthecrown.Loggers;
import net.forthecrown.usables.UsablesPlugin;
import net.forthecrown.usables.trigger.TriggerManager;
import org.slf4j.Logger;

public class RegionTriggerSystem implements TriggerSystem<RegionTrigger> {

  private static final Logger LOGGER = Loggers.getLogger();

  TriggerManager manager() {
    return UsablesPlugin.get().getTriggers();
  }

  @Override
  public void onTriggerLoaded(VirtualUsable usable, RegionTrigger trigger) {
    onTriggerAdd(usable, trigger);
  }

  @Override
  public void onTriggerAdd(VirtualUsable usable, RegionTrigger trigger) {
    var region = manager().get(trigger.getRegionName());

    if (region == null) {
      LOGGER.error("Couldn't find AreaTrigger named '{}'", trigger.getRegionName());
      return;
    }

    region.getExternalTriggers().add(trigger.getAction(), usable.getName());
  }

  @Override
  public void onTriggerRemove(VirtualUsable usable, RegionTrigger trigger) {
    var region = manager().get(trigger.getRegionName());

    if (region == null) {
      return;
    }

    region.getExternalTriggers().remove(trigger.getAction(), usable.getName());
  }
}
