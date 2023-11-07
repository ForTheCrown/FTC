package net.forthecrown.core.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.LongArrays;
import java.util.Arrays;
import net.forthecrown.Loggers;
import net.forthecrown.user.ComponentName;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.UserComponent;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@ComponentName("timeStamps")
public class UserTimestamps implements UserComponent {

  public static final Logger LOGGER = Loggers.getLogger();

  private long[] fields;
  private JsonObject unknownFields;

  public long getTime(TimeField field) {
    if (fields == null) {
      return -1L;
    }

    if (fields.length <= field.getId()) {
      return -1L;
    }

    return fields[field.getId()];
  }

  public void setTime(TimeField field, long value) {
    int id = field.getId();

    if (fields == null) {
      fields = new long[id + 1];
      Arrays.fill(fields, -1);
    } else if (fields.length <= id) {
      int oldLength = fields.length;
      int newLength = id + 1;

      fields = LongArrays.ensureCapacity(fields, id + 1);

      Arrays.fill(fields, oldLength, newLength, -1);
    }

    fields[id] = value;
  }

  @Override
  public @Nullable JsonElement serialize() {
    if (fields == null && (unknownFields == null || unknownFields.size() < 1)) {
      return null;
    }

    JsonWrapper json = JsonWrapper.create();
    if (unknownFields != null && unknownFields.size() > 0) {
      json.addAll(unknownFields);
    }

    for (int i = 0; i < fields.length; i++) {
      long l = fields[i];

      if (l < 0) {
        continue;
      }

      TimeField.REGISTRY.getHolder(i).ifPresent(holder -> {
        json.addTimeStamp(holder.getKey(), l);
      });
    }

    return json.getSource();
  }

  @Override
  public void deserialize(@Nullable JsonElement element) {
    if (element == null || element.isJsonNull()) {
      unknownFields = null;
      fields = null;
      return;
    }

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    for (var e: json.entrySet()) {
      long time = JsonUtils.readTimestamp(e.getValue());

      TimeField.REGISTRY.getHolder(e.getKey()).ifPresentOrElse(holder -> {
        setTime(holder.getValue(), time);
      }, () -> {
        LOGGER.warn("Unknown timestamp field '{}', adding to fallback list", e.getKey());

        if (unknownFields == null) {
          unknownFields = new JsonObject();
        }

        unknownFields.add(e.getKey(), e.getValue());
      });
    }
  }
}
