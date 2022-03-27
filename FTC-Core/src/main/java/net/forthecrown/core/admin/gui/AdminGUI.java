package net.forthecrown.core.admin.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.admin.*;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.InventoryBuilder;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.InventoryBorder;
import net.forthecrown.registry.Registries;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.function.IntFunction;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

public class AdminGUI {
    public static final int ENTRIES_PER_PAGE = 14;
    private static final InventoryPos ONE = new InventoryPos(1, 1);

    public static BuiltInventory createOveriew(CrownUser punished) {
        PunishEntry entry = Punishments.entry(punished);

        InventoryBuilder builder = createBase(27, punished)
                .add(new ViewPunishmentsOption(entry, new InventoryPos(2, 1), false))
                .add(new ViewPunishmentsOption(entry, new InventoryPos(6, 1), true))
                .add(new NoteViewOption(entry, new InventoryPos(4, 1)))
                .add(new ViewPunishTypesOption(entry, new InventoryPos(4, 2)));

        return builder.build();
    }

    public static BuiltInventory createNoteView(PunishEntry entry, int page) {
        InventoryBuilder builder = createBase(36, entry.entryUser())
                .add(new ReturnOption(entry.entryUser()));

        List<EntryNote> notes = entry.notes();
        int start = page * ENTRIES_PER_PAGE;
        int end = ENTRIES_PER_PAGE;

        for (int i = start; i < end; i++) {
            if(i >= notes.size()) break;

            InventoryPos pos = ONE.add(i % 7, i / 7);
            builder.add(new NoteOption(notes.get(i), pos));
        }


        return viewPageBased(notes.size(), page, builder, value -> createNoteView(entry, value));
    }

    public static BuiltInventory createPunishmentsView(PunishEntry entry, boolean past, int page) {
        List<Punishment> list = new ObjectArrayList<>(past ? entry.past() : entry.current());
        InventoryBuilder builder = createBase(36, entry.entryUser())
                .add(new ReturnOption(entry.entryUser()));

        int start = page * ENTRIES_PER_PAGE;
        int end = start + ENTRIES_PER_PAGE;

        for (int i = start; i < end; i++) {
            if(i >= list.size()) break;

            InventoryPos pos = ONE.add(i % 7, i / 7);
            builder.add(new PunishmentOption(list.get(i), entry, !past, true, pos));
        }

        return viewPageBased(list.size(), page, builder, value -> createPunishmentsView(entry, past, value));
    }

    private static BuiltInventory viewPageBased(int size, int page, InventoryBuilder builder, IntFunction<BuiltInventory> pageCreator) {
        int maxPage = (int) Math.ceil((double) size / (double) ENTRIES_PER_PAGE);
        boolean lastPage = page >= maxPage;
        boolean firstPage = page <= 0;

        if (!lastPage) {
            builder.add(new PageSwitchOption(page, 1, pageCreator, new InventoryPos(8, 3)));
        }

        if (!firstPage) {
            builder.add(new PageSwitchOption(page, -1, pageCreator, new InventoryPos(0, 3)));
        }

        return builder.build();
    }

    public static BuiltInventory createCurrentPunishmentView(Punishment punishment, PunishEntry entry) {
        InventoryBuilder builder = createBase(27, entry.entryUser())
                .add(new ReturnOption(entry.entryUser()))
                .add(new PardonOption(entry, punishment, new InventoryPos(3, 1), true))
                .add(new PardonOption(entry, punishment, new InventoryPos(5, 1), false))
                .add(new PunishmentOption(punishment, entry, true, false, new InventoryPos(4, 0)));

        return builder.build();
    }

    public static PunishType[] PLACEABLE_TYPES = {
            PunishType.BAN, PunishType.IP_BAN,
            PunishType.JAIL,
            PunishType.MUTE, PunishType.SOFT_MUTE,
    };

    public static BuiltInventory createPunishmentSelection(PunishEntry entry) {
        InventoryBuilder builder = createBase(27, entry.entryUser())
                .add(new ReturnOption(entry.entryUser()));

        for (int i = 0; i < PLACEABLE_TYPES.length; i++) {
            PunishType type = PLACEABLE_TYPES[i];

            PunishOption option = new PunishOption(entry, type, new InventoryPos(2 + i, 1));
            builder.add(option);
        }

        return builder.build();
    }

    static BuiltInventory createTimeSelection(PunishEntry entry, PunishType type, String extra) {
        InventoryBuilder builder = createBase(27, entry.entryUser())
                .add(new ReturnOption(entry.entryUser()));
        InventoryPos start = new InventoryPos(2, 1);

        TimeSelectionOption.TimeAmount[] values = TimeSelectionOption.TimeAmount.values();

        for (int i = 0; i < values.length; i++) {
            InventoryPos pos = start.add(i, 0);
            TimeSelectionOption.TimeAmount amount = values[i];

            TimeSelectionOption option = new TimeSelectionOption(entry, type, extra, amount, pos);
            builder.add(option);
        }

        return builder.build();
    }

    static BuiltInventory createJailSelector(PunishEntry entry) {
        InventoryBuilder builder = createBase(27, entry.entryUser())
                .add(new ReturnOption(entry.entryUser()));

        int i = 0;
        for (JailCell c: Registries.JAILS) {
            InventoryPos pos = new InventoryPos(i + 2, 1);
            i++;

            JailOption option = new JailOption(entry, c, pos);
            builder.add(option);
        }

        return builder.build();
    }

    private static InventoryBuilder createBase(int size, CrownUser punished) {
        InventoryBuilder builder = new InventoryBuilder(size)
                .title(Component.text("Admin GUI: ").append(punished.nickDisplayName()).style(nonItalic()))
                .add(new InventoryBorder())
                .add(new PlayerHeadOption(punished));

        return builder;
    }
}