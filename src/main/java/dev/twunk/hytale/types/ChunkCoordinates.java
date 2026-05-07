package dev.twunk.hytale.types;

import javax.annotation.Nullable;
import java.util.Objects;

public record ChunkCoordinates(int x, int z) {

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(@Nullable Object o) {
        // self check
        if (this == o) return true;
        // null check
        if (o == null) return false;
        // type check and cast
        if (getClass() != o.getClass()) return false;
        ChunkCoordinates otherCoords = (ChunkCoordinates) o;
        // field comparison
        return Objects.equals(x, otherCoords.x) && Objects.equals(z, otherCoords.z);
    }
}
