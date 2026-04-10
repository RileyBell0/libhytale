package dev.twunk.lib;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.bson.BsonString;
import org.bson.BsonValue;

public class TransformCodec<T> implements Codec<T> {

    private final Function<T, String> encoder;
    private final Function<String, T> decoder;

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    public TransformCodec(Function<T, String> encoder, Function<String, T> decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Nullable
    public T decode(@Nullable BsonValue bsonValue, @Nullable ExtraInfo extraInfo) {
        if (bsonValue == null) {
            return null;
        }

        return this.decoder.apply(bsonValue.asString().getValue());
    }

    @Override
    public BsonValue encode(@Nullable T t, @Nullable ExtraInfo extraInfo) {
        return new BsonString(this.encoder.apply(t));
    }

    @Nullable
    public T decodeJson(RawJsonReader reader, @Nullable ExtraInfo extraInfo) throws IOException {
        return this.decoder.apply(reader.readString());
    }

    @Override
    public Schema toSchema(SchemaContext arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'toSchema'");
    }
}
