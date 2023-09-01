package net.forthecrown.mail;

import static net.forthecrown.mail.Mail.NULL_ID;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import net.forthecrown.Loggers;
import net.forthecrown.utils.Result;
import org.slf4j.Logger;

class MailMap {

  private static final Logger LOGGER = Loggers.getLogger();

  private final Map<UUID, List<MailImpl>> byTarget = new Object2ObjectOpenHashMap<>(250);
  private final Map<UUID, List<MailImpl>> bySender = new Object2ObjectOpenHashMap<>(250);

  private final Set<MailImpl> allMessages = new ObjectOpenHashSet<>();
  private final Set<MailImpl> nonDeleted = new ObjectOpenHashSet<>();

  private final Long2ObjectMap<MailImpl> idLookup = new Long2ObjectOpenHashMap<>();

  private final ServiceImpl service;

  private final Random idGen;

  public MailMap(ServiceImpl service) {
    this.service = service;
    this.idGen = new Random();
  }

  public void save(JsonArray arr) {
    for (MailImpl message : allMessages) {
      JsonElement element = message.save();
      arr.add(element);
    }
  }

  public void load(JsonArray arr) {
    clear();

    for (int i = 0; i < arr.size(); i++) {
      JsonElement element = arr.get(i);

      final int fI = i;
      Result<MailImpl> result = MailImpl.load(element);

      if (result.isError()) {
        var message = result
            .mapError(string -> "Couldn't load mail message index " + fI + ": " + string)
            .getError();

        LOGGER.error(message);
        continue;
      }

      MailImpl mail = result.getValue();
      add(mail);
    }
  }

  long genId() {
    long id = idGen.nextLong();

    while (idLookup.containsKey(id)) {
      id = idGen.nextLong();
    }

    return id;
  }

  public void ensureSorted() {
    byTarget.forEach((uuid, mail) -> {
      mail.sort(SentDateComparator.INSTANCE);
    });

    bySender.forEach((uuid, mail) -> {
      mail.sort(SentDateComparator.INSTANCE);
    });
  }

  public void add(MailImpl mail) {
    Preconditions.checkArgument(mail.service == null, "Mail already added");

    mail.service = service;

    if (mail.mailId == NULL_ID) {
      mail.mailId = genId();
    }

    if (mail.getSender() != null) {
      addToMap(mail.getSender(), bySender, mail);
    }

    addToMap(mail.getTarget(), byTarget, mail);
    allMessages.add(mail);
    idLookup.put(mail.mailId, mail);

    if (!mail.isDeleted()) {
      nonDeleted.add(mail);
    }
  }

  public void remove(MailImpl mail) {
    if (mail.getSender() != null) {
      removeFromMap(bySender, mail.getSender(), mail);
    }

    removeFromMap(byTarget, mail.getTarget(), mail);
    allMessages.remove(mail);

    if (mail.mailId != NULL_ID) {
      idLookup.remove(mail.mailId);
    }

    if (!mail.isDeleted()) {
      nonDeleted.remove(mail);
    }

    mail.service = null;
  }

  public void clear() {
    byTarget.clear();
    bySender.clear();

    allMessages.forEach(mail -> mail.service = null);

    allMessages.clear();
    nonDeleted.clear();
    idLookup.clear();
  }

  public void updateDeletedState(MailImpl mail) {
    if (mail.isDeleted()) {
      nonDeleted.remove(mail);
    } else {
      nonDeleted.add(mail);
    }
  }

  public Stream<MailImpl> getMailStream(boolean nonDeleted) {
    return nonDeleted ? this.nonDeleted.stream() : allMessages.stream();
  }

  private void removeFromMap(Map<UUID, List<MailImpl>> map, UUID playerId, MailImpl mail) {
    var list = map.get(playerId);

    if (list == null) {
      return;
    }

    list.remove(mail);

    if (list.isEmpty()) {
      map.remove(playerId);
    }
  }

  private void addToMap(UUID playerId, Map<UUID, List<MailImpl>> map, MailImpl mail) {
    map.compute(playerId, (uuid, mailList) -> {
      if (mailList == null) {
        mailList = new ArrayList<>();
      }
      mailList.add(mail);
      return mailList;
    });
  }

  public List<MailImpl> getByTarget(UUID playerId) {
    return byTarget.get(playerId);
  }

  public Mail getById(long messageId) {
    return idLookup.get(messageId);
  }

  public boolean hasUnread(UUID playerId) {
    return byTarget.getOrDefault(playerId, ObjectLists.emptyList())
        .stream()
        .anyMatch(mail -> !mail.isRead());
  }
}
