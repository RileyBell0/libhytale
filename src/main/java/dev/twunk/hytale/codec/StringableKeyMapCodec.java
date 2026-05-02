package dev.twunk.hytale.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class StringableKeyMapCodec<K, V, M extends Map<K, V>> implements Codec<Map<K, V>> {

    private final FromStringCodec<K> keyCodec;
    private final Codec<V> valueCodec;

    private final Supplier<M> supplier;

    public StringableKeyMapCodec(FromStringCodec<K> keyCodec, Codec<V> valueCodec, Supplier<M> supplier) {
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
        this.supplier = supplier;
    }

    @SuppressWarnings("null")
    @Override
    public Map<K, V> decode(BsonValue bsonValue, ExtraInfo extraInfo) {
        final BsonDocument bsonDocument = bsonValue.asDocument();
        if (bsonDocument.isEmpty()) {
            return this.supplier.get();
        } else {
            final Map<K, V> map = this.supplier.get();

            for (Entry<String, BsonValue> entry : bsonDocument.entrySet()) {
                @SuppressWarnings("null")
                @Nonnull
                final String keyStr = entry.getKey();
                extraInfo.pushKey(keyStr);
                final var key = this.keyCodec.fromString(keyStr);

                final BsonValue value = entry.getValue();
                try {
                    map.put(key, this.valueCodec.decode(value, extraInfo));
                } catch (Exception var13) {
                    throw new CodecException("Failed to decode", value, extraInfo, var13);
                } finally {
                    extraInfo.popKey();
                }
            }

            return map;
        }
    }

    @SuppressWarnings("null")
    @Override
    public BsonValue encode(Map<K, V> map, @Nullable ExtraInfo extraInfo) {
        final BsonDocument bsonDocument = new BsonDocument();

        for (Entry<K, V> entry : map.entrySet()) {
            @SuppressWarnings("null")
            @Nonnull
            var keyStr = entry.getKey();

            final String key = this.keyCodec.stringify(keyStr);
            final BsonValue value = this.valueCodec.encode(entry.getValue(), extraInfo);

            if (
                value != null &&
                !value.isNull() &&
                (!value.isDocument() || !value.asDocument().isEmpty()) &&
                (!value.isArray() || !value.asArray().isEmpty())
            ) {
                bsonDocument.put(key, value);
            }
        }

        return bsonDocument;
    }

    @SuppressWarnings("null")
    @Override
    public Map<K, V> decodeJson(RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
        reader.expect('{');
        reader.consumeWhiteSpace();
        if (reader.tryConsume('}')) {
            return this.supplier.get();
        } else {
            Map<K, V> map = this.supplier.get();

            while (true) {
                String keyStr = reader.readString();
                var key = this.keyCodec.fromString(keyStr);
                reader.consumeWhiteSpace();
                reader.expect(':');
                reader.consumeWhiteSpace();
                extraInfo.pushKey(keyStr, reader);

                try {
                    map.put(key, this.valueCodec.decodeJson(reader, extraInfo));
                } catch (Exception var9) {
                    throw new CodecException("Failed to decode", reader, extraInfo, var9);
                } finally {
                    extraInfo.popKey();
                }

                reader.consumeWhiteSpace();
                if (reader.tryConsumeOrExpect('}', ',')) {
                    return map;
                }

                reader.consumeWhiteSpace();
            }
        }
    }

    @Nonnull
    @Override
    public Schema toSchema(@Nonnull SchemaContext context) {
        ObjectSchema schema = new ObjectSchema();
        schema.setTitle("Map");
        return schema;
    }
}
