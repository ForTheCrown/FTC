package net.forthecrown.serverlist;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.scripts.ExecResult;
import net.forthecrown.scripts.ExecResults;
import net.forthecrown.scripts.Script;
import net.forthecrown.text.Text;
import net.forthecrown.utils.MonthDayPeriod;
import net.kyori.adventure.text.Component;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
class DisplayEntry implements Comparable<DisplayEntry> {

  private final MonthDayPeriod period;
  private final List<CachedServerIcon> icons;
  private final Script condition;
  private final Component motdPart;
  private final int priority;

  public CachedServerIcon get(Random random) {
    if (icons.isEmpty()) {
      return null;
    }

    if (icons.size() == 1) {
      return icons.get(0);
    }

    return icons.get(random.nextInt(icons.size()));
  }

  public boolean shouldUse(LocalDate date, Random random) {
    if (condition == null) {
      return true;
    }

    condition.put("random", random);
    condition.put("date", date);

    ExecResult<Object> evalResult = condition.evaluate();
    return ExecResults.toBoolean(evalResult).result().orElse(false);
  }

  @Override
  public String toString() {
    return "%s[condition=%s, motd=%s, period=%s, iconsSize=%s, priority=%s]".formatted(
        getClass().getSimpleName(),
        getCondition(),
        getMotdPart() == null ? null : Text.plain(getMotdPart()),
        getPeriod(),
        getIcons().size(),
        getPriority()
    );
  }

  @Override
  public int compareTo(@NotNull DisplayEntry o) {
    return Integer.compare(o.getPriority(), getPriority());
  }
}
