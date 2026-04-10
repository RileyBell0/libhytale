package dev.twunk.hytale.types;

import java.util.Objects;
import javax.annotation.Nullable;

public final class ChunkCoordinates {

    public final int x;
    public final int z;

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    public ChunkCoordinates(final int x, final int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        // self check
        if (this == o) return true;
        // null check
        if (o == null) return false;
        // type check and cast
        if (getClass() != o.getClass()) return false;
        ChunkCoordinates othercoordslmao = (ChunkCoordinates) o;
        // field comparison
        return Objects.equals(x, othercoordslmao.x) && Objects.equals(z, othercoordslmao.z);
    }
}
