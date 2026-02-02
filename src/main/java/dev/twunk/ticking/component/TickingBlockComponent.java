package dev.twunk.ticking.component;

import javax.annotation.Nonnull;

public class TickingBlockComponent implements ITickingComponent {

    @Nonnull
    public TickingBlockComponent clone() {
        return new TickingBlockComponent();
    }

}
