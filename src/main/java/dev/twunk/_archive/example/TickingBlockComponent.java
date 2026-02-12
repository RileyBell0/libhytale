package dev.twunk._archive.example;

import dev.twunk.component.ITickingComponent;
import javax.annotation.Nonnull;

public class TickingBlockComponent implements ITickingComponent {

    @Nonnull
    public TickingBlockComponent clone() {
        return new TickingBlockComponent();
    }
}
