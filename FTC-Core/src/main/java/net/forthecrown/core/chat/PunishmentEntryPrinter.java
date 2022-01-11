package net.forthecrown.core.chat;

import net.forthecrown.core.admin.PunishmentEntry;
import net.forthecrown.core.admin.record.PunishmentRecord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.Collection;

public class PunishmentEntryPrinter implements ComponentPrinter {
    private final TextComponent.Builder builder = Component.text();
    private final PunishmentEntry entry;
    private int indent;

    public PunishmentEntryPrinter(PunishmentEntry entry) {
        this.entry = entry;
    }

    private void writeIndent() {
        if(indent <= 0) return;

        builder.append(
                Component.text(" ".repeat(indent))
        );
    }

    private void modIndent(int amount) {
        indent = Math.max(0, amount + indent);
    }

    public PunishmentEntryPrinter addCurrent() {
        return addRecords(entry.getCurrent(), "Current");
    }

    public PunishmentEntryPrinter addPast() {
        return addRecords(entry.getPast(), "Past");
    }

    private PunishmentEntryPrinter addRecords(Collection<PunishmentRecord> records, String category) {

        return this;
    }

    @Override
    public Component print() {
        if(!entry.getCurrent().isEmpty()) {
            addCurrent();
        }

        if(!entry.getPast().isEmpty()) {
            addPast();
        }

        return printCurrent();
    }

    @Override
    public Component printCurrent() {
        return builder.build();
    }
}
