package net.forthecrown.economy;

import com.google.common.base.Strings;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.log.DataLogs;
import net.forthecrown.log.LogEntry;
import net.forthecrown.log.LogSchema;
import net.forthecrown.log.SchemaField;
import net.forthecrown.utils.io.FtcCodecs;

import java.util.Objects;

/**
 * Utility class for logging transaction data
 */
public @UtilityClass class Transactions {

    /** Schema for transaction logs */
    public final Holder<LogSchema> TRANSACTION_SCHEMA;

    /**
     * Field that represents the receiver, aka the
     * target, of the transaction, may be unset, in
     * the case of money being given to the server
     */
    public final SchemaField<String>            T_TARGET;

    /**
     * Represents the entity in a transaction sending
     * rhines to the other party, may be unset, in
     * the case of the server giving money to a user
     */
    public final SchemaField<String>            T_SENDER;

    /**
     * Arbitrary random data about the transaction,
     * unique to each {@link TransactionType}
     */
    public final SchemaField<String>            T_EXTRA;

    /**
     * The amount of Rhines that were exchanged
     * in the transaction
     */
    public final SchemaField<Integer>           T_AMOUNT;

    /** The transaction's type */
    public final SchemaField<TransactionType>   T_TYPE;

    static {
        var builder = LogSchema.builder("economy/transactions");

        T_TARGET = builder.add("target", Codec.STRING);
        T_SENDER = builder.add("sender", Codec.STRING);
        T_EXTRA  = builder.add("extra",  Codec.STRING);

        T_AMOUNT = builder.add("amount", Codec.INT);

        T_TYPE = builder.add(
                "type",
                FtcCodecs.enumCodec(TransactionType.class)
        );

        TRANSACTION_SCHEMA = builder.register();
    }

    // Called reflectively by BootStrap
    @OnEnable
    private void init() {
        // Force class load
    }

    public TransactionBuilder builder() {
        return new TransactionBuilder(System.currentTimeMillis());
    }

    @Setter @Getter
    @Accessors(fluent = true, chain = true)
    @RequiredArgsConstructor
    public class TransactionBuilder {
        private String target;
        private String sender;
        private String extra;
        private int amount;
        private final long time;
        private TransactionType type;

        public TransactionBuilder sender(Object o) {
            this.sender = (o == null) ? null : String.valueOf(o);
            return this;
        }

        public TransactionBuilder target(Object o) {
            this.target = (o == null) ? null : String.valueOf(o);
            return this;
        }

        public TransactionBuilder extra(String s, Object... args) {
            this.extra = String.format(s, args);
            return this;
        }

        public void log() {
            Objects.requireNonNull(type, "Type not given");

            LogEntry entry = LogEntry.of(TRANSACTION_SCHEMA)
                    .set(T_TYPE, type)
                    .set(T_AMOUNT, amount);

            if (!Strings.isNullOrEmpty(target)) {
                entry.set(T_TARGET, target);
            }

            if (!Strings.isNullOrEmpty(sender)) {
                entry.set(T_SENDER, sender);
            }

            if (!Strings.isNullOrEmpty(extra)) {
                entry.set(T_EXTRA, extra);
            }

            DataLogs.log(TRANSACTION_SCHEMA, entry);
        }
    }
}