package dev.twunk.ticking.example;

import dev.twunk.ticking.component.ITickingComponent;
import javax.annotation.Nonnull;

public class TickingBlockComponent implements ITickingComponent {

    @Nonnull
    public TickingBlockComponent clone() {
        return new TickingBlockComponent();
    }
}
