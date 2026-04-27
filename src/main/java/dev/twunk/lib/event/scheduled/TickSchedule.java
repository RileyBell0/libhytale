package dev.twunk.lib.event.scheduled;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import dev.twunk.hytale.codec.auto.Serializable;
import dev.twunk.hytale.codec.auto.Serialize;
import dev.twunk.lib.codec.AutoSerializeParser;
import dev.twunk.lib.event.scheduled.TickSchedule.Sleeping;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

class TickScheduleCodec implements Codec<TickSchedule> {

    @Nullable
    public TickSchedule decode(@Nullable BsonValue bsonValue, @Nullable ExtraInfo extraInfo) {
        if (bsonValue == null) {
            return null;
        }

        final BsonDocument bsonDocument = bsonValue.asDocument();
        if (bsonDocument.isEmpty()) {
            throw new CodecException("Failed to decode TickSchedule from " + bsonValue);
        } else {
            final Map<String, BsonValue> map = new HashMap<>();

            for (Entry<String, BsonValue> entry : bsonDocument.entrySet()) {
                final String key = entry.getKey();
                extraInfo.pushKey(key);

                final BsonValue value = entry.getValue();
                try {
                    map.put(key, value);
                } catch (Exception var13) {
                    throw new CodecException("Failed to decode", value, extraInfo, var13);
                } finally {
                    extraInfo.popKey();
                }
            }

            // ensure the map has the key we need
            if (!map.containsKey("State")) {
                throw new CodecException("No 'State' key found when decoding codec");
            }

            String state = map.remove("State").asString().getValue();
            if (!state.equals("active") && !state.equals("sleeping") && !state.equals("stopped")) {
                throw new CodecException("'State' must be one of (active|sleeping|stopped) for TickSchedule codec");
            }

            switch (state) {
                case "active":
                    if (map.size() != 0) {
                        throw new CodecException("Error, extra keys found in TickSchedule codec of type 'active'");
                    }
                    return TickSchedule.ACTIVE;
                case "sleeping":
                    if (map.size() != 1 || !map.containsKey("NextTick")) {
                        throw new CodecException(
                            "Error, `NextTick` field not found in TickSchedule of type 'sleeping'"
                        );
                    }
                    var val = map.get("NextTick").asString().getValue();

                    try {
                        var nextTick = Long.valueOf(val.trim());
                        return new Sleeping(nextTick);
                    } catch (NumberFormatException e) {
                        throw new CodecException(
                            "Error, `NextTick` field was not of type 'long' in TickSchedule of type 'sleeping'",
                            e
                        );
                    }
                case "stopped":
                    if (map.size() != 0) {
                        throw new CodecException("Error, extra keys found in TickSchedule codec of type 'stopped'");
                    }
                    return TickSchedule.STOP;
                default:
                    throw new CodecException("'State' must be one of (active|sleeping|stopped) for TickSchedule codec");
            }
        }
    }

    @Override
    @Nullable
    public BsonValue encode(@Nullable TickSchedule t, @Nullable ExtraInfo extraInfo) {
        if (t == null || extraInfo == null) {
            return null;
        }

        return switch (t) {
            case TickSchedule.Active schedule -> TickSchedule.Active.CODEC.encode(schedule, extraInfo);
            case TickSchedule.Sleeping schedule -> TickSchedule.Sleeping.CODEC.encode(schedule, extraInfo);
            case TickSchedule.Stopped schedule -> TickSchedule.Stopped.CODEC.encode(schedule, extraInfo);
        };
    }

    @Nullable
    public TickSchedule decodeJson(RawJsonReader reader, @Nullable ExtraInfo extraInfo) throws IOException {
        reader.expect('{');
        reader.consumeWhiteSpace();
        if (reader.tryConsume('}')) {
            // return TickSchedule.ACTIVE;
            throw new CodecException("TEMP ERROR - failed to consume decode json TickSchedule");
        } else {
            String state = null;
            Long nextTick = null;

            while (true) {
                String key = reader.readString();
                reader.consumeWhiteSpace();
                reader.expect(':');
                reader.consumeWhiteSpace();
                extraInfo.pushKey(key, reader);

                if (key.equals("State")) {
                    try {
                        state = Codec.STRING.decodeJson(reader, extraInfo);
                    } catch (Exception var9) {
                        throw new CodecException("Failed to decode", reader, extraInfo, var9);
                    } finally {
                        extraInfo.popKey();
                    }
                } else if (key.equals("NextTick")) {
                    nextTick = Codec.LONG.decodeJson(reader, extraInfo);
                } else {
                    throw new CodecException(
                        "Unexpected key " + key + " encountered when decoding TickSchedule via json"
                    );
                }

                reader.consumeWhiteSpace();
                if (reader.tryConsumeOrExpect('}', ',')) {
                    break;
                }

                reader.consumeWhiteSpace();
            }

            if (state == null || (!state.equals("active") && !state.equals("sleeping") && !state.equals("stopped"))) {
                throw new CodecException(
                    "Unexpected value " +
                        state +
                        " encountered for 'State' in codec TickSchedule (json) - must be one of (active|sleeping|stopped)"
                );
            }
            if (!state.equals("sleeping") && nextTick != null) {
                throw new CodecException(
                    "NextTick set for state " + state + " except its only expected for 'State' sleeping"
                );
            }
            if (state.equals("sleeping") && nextTick == null) {
                throw new CodecException("NextTick MUST be set if 'State' is sleeping");
            }

            if (state.equals("active")) {
                return TickSchedule.ACTIVE;
            } else if (state.equals("sleeping")) {
                return new Sleeping(nextTick);
            } else {
                return TickSchedule.STOP;
            }
        }
    }

    @Override
    public Schema toSchema(SchemaContext arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'toSchema'");
    }
}

public sealed interface TickSchedule permits TickSchedule.Active, TickSchedule.Sleeping, TickSchedule.Stopped {
    // its defined above in TickScheduleCodec to be basically the same logic as a NoSQL db entry
    // -> a field defines what "type" it is, then the rest gets parsed as that type
    public static final Codec<TickSchedule> CODEC = new TickScheduleCodec();

    public static final TickSchedule ACTIVE = new TickSchedule.Active();
    public static final TickSchedule STOP = new TickSchedule.Stopped();

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Keep ticking at the same frequency as before
     */
    @Serializable
    public final class Active implements TickSchedule {

        public static final BuilderCodec<Active> CODEC = AutoSerializeParser.build(Active.class);
    }

    /**
     * Don't tick until the given amount of time has passed (or until x ticks etc)
     *
     * notably, you can set this to be sleeping forever
     */
    @Serializable
    public final class Sleeping implements TickSchedule {

        public static final BuilderCodec<Sleeping> CODEC = AutoSerializeParser.build(Sleeping.class);

        // note: only has a default value so that the codec can construct this
        @SuppressWarnings("null")
        @Serialize
        public long nextTick = 0;

        /**
         * Default: sleep forever
         */
        public Sleeping(long wakeUpTick) {
            this.nextTick = wakeUpTick;
        }

        public Sleeping() {}

        public static Sleeping forSeconds(final long seconds) {
            return new TickSchedule.Sleeping(30 * seconds);
        }

        public static Sleeping forTicks(final long ticks) {
            return new Sleeping(ticks);
        }
    }

    /**
     * Goodbye ticking forever
     */
    @Serializable
    public final class Stopped implements TickSchedule {

        public static final BuilderCodec<Stopped> CODEC = AutoSerializeParser.build(Stopped.class);
    }
}
