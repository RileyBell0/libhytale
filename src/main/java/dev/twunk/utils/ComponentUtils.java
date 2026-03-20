package dev.twunk.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.lib.component.TwunkDevTestComponent;
import dev.twunk.lib.test.TestUtil;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// TESTS ADDED AND VERIFIED
public abstract class ComponentUtils {

    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    // ==================================================
    // Component types (trust me bro, i swear they're not null)
    // ==================================================

    @Nonnull
    @SuppressWarnings("null")
    private static final ComponentType<ChunkStore, WorldChunk> WORLD_CHUNK_COMPONENT = WorldChunk.getComponentType();

    @Nonnull
    @SuppressWarnings("null")
    public static final ComponentType<ChunkStore, BlockComponentChunk> BLOCK_COMPONENT_CHUNK =
        BlockComponentChunk.getComponentType();

    @Nonnull
    @SuppressWarnings("null")
    public static final ComponentType<ChunkStore, BlockStateInfo> BLOCK_STATE_INFO_COMPONENT_TYPE =
        BlockStateInfo.getComponentType();

    /**
     * Tests all methods i've defined for getWorldChunk
     */
    public static final void test(
        @Nonnull final Ref<ChunkStore> blockRef,
        @Nonnull final WorldChunk worldChunk,
        @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull final Vector3i providedCoords,
        Boolean verbose
    ) {
        final var blockX = providedCoords.x;
        final var blockY = providedCoords.y;
        final var blockZ = providedCoords.z;
        final var blockCoords = new Vector3i(blockX, blockY, blockZ);

        final var test = new TestUtil(commandBuffer, blockCoords);

        final var chunkRef = test.chunkRef;

        final var blockRefComponent = new TwunkDevTestComponent().setVal(1);
        final var chunkRefComponent = new TwunkDevTestComponent().setVal(2);

        // ensure BlockStateInfo is on the BlockRef
        Component<ChunkStore> component;
        component = ComponentUtils.get(blockRef, BLOCK_STATE_INFO_COMPONENT_TYPE);
        if (component == null) {
            throw new RuntimeException("!! No BlockStateInfo component on BlockRef");
        }

        // Chunk should contain WorldChunk component
        component = ComponentUtils.get(chunkRef, WORLD_CHUNK_COMPONENT);
        if (component == null) {
            throw new RuntimeException("!! No WorldChunk component on ChunkRef");
        }

        // Chunk should contain BlockComponentChunk component
        component = ComponentUtils.get(chunkRef, BLOCK_COMPONENT_CHUNK);
        if (component == null) {
            throw new RuntimeException("!! No BlockComponentChunk component on ChunkRef (base method)");
        }
        component = ComponentUtils.getBlockComponentChunk(chunkRef);
        if (component == null) {
            throw new RuntimeException("!! No BlockComponentChunk component on ChunkRef (higher level method)");
        }

        // validate the components AREN'T on there yet (BlockRef)
        component = ComponentUtils.get(blockRef, TwunkDevTestComponent.COMPONENT_TYPE);
        if (ComponentUtils.has(blockRef, TwunkDevTestComponent.COMPONENT_TYPE) || component != null) {
            throw new RuntimeException("!! ERROR: BlockRef contained TwunkDevTestComponent before we added it");
        }

        // validate the components AREN'T on there yet (ChunkRef)
        component = ComponentUtils.get(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE);
        if (ComponentUtils.has(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE) || component != null) {
            throw new RuntimeException("!! ERROR: ChunkRef contained TwunkDevTestComponent before we added it");
        }

        // Add components
        commandBuffer.run(componentAccessor -> {
            try {
                try {
                    componentAccessor.putComponent(blockRef, TwunkDevTestComponent.COMPONENT_TYPE, blockRefComponent);
                } catch (Exception e) {
                    throw new RuntimeException("!! !! ERROR: Failed to put component onto block");
                }

                // and we'll check that it's there
                var blockRefComponentREFETCHED = ComponentUtils.get(blockRef, TwunkDevTestComponent.COMPONENT_TYPE);
                if (
                    !ComponentUtils.has(blockRef, TwunkDevTestComponent.COMPONENT_TYPE) ||
                    blockRefComponentREFETCHED == null
                ) {
                    throw new RuntimeException("!! !! ERROR: Failed to get component we just added from blockRef");
                }

                if (blockRefComponent != blockRefComponentREFETCHED || blockRefComponentREFETCHED.getVal() != 1) {
                    throw new RuntimeException(
                        "new component on our block wasn't the same one we put on there " +
                            blockRefComponent +
                            " " +
                            blockRefComponent.getVal() +
                            " | new - " +
                            blockRefComponentREFETCHED +
                            " " +
                            blockRefComponentREFETCHED.getVal()
                    );
                }

                try {
                    componentAccessor
                        .getExternalData()
                        .getStore()
                        .putComponent(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE, chunkRefComponent);
                } catch (Exception e) {
                    throw new RuntimeException("!! !! ERROR: Failed to put component onto chunk");
                }

                var chunkRefComponentREFETCHED = ComponentUtils.get(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE);
                if (
                    !ComponentUtils.has(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE) ||
                    chunkRefComponentREFETCHED == null
                ) {
                    throw new RuntimeException("!! !! ERROR: Failed to get component we just added from chunkRef");
                }

                if (chunkRefComponent != chunkRefComponentREFETCHED || chunkRefComponentREFETCHED.getVal() != 2) {
                    throw new RuntimeException(
                        "new component on our chunk wasn't the same one we put on there " +
                            chunkRefComponent +
                            " " +
                            chunkRefComponent.getVal() +
                            " | new - " +
                            chunkRefComponentREFETCHED +
                            " " +
                            chunkRefComponentREFETCHED.getVal()
                    );
                }

                // we'll mutate one and validate it is mutated on the other
                blockRefComponent.setVal(3);
                chunkRefComponent.setVal(4);
                if (
                    blockRefComponent.getVal() != blockRefComponentREFETCHED.getVal() || blockRefComponent.getVal() != 3
                ) {
                    throw new RuntimeException("new component on our block doesn't respond to updates");
                }
                if (
                    chunkRefComponent.getVal() != chunkRefComponentREFETCHED.getVal() || chunkRefComponent.getVal() != 4
                ) {
                    throw new RuntimeException("new component on our chunk doesn't respond to updates");
                }

                var component2 = ComponentUtils.get_blockCoords(
                    test.world,
                    TwunkDevTestComponent.COMPONENT_TYPE,
                    blockX,
                    blockY,
                    blockZ
                );
                if (component2 == null) {
                    throw new RuntimeException("Failed to get component by block coords");
                }

                final var localCoords = BlockUtils.Coords.Local.get(blockCoords);
                var component3 = ComponentUtils.get_localCoords(
                    test.blockComponentChunk,
                    TwunkDevTestComponent.COMPONENT_TYPE,
                    localCoords.x,
                    localCoords.y,
                    localCoords.z
                );
                if (component3 == null) {
                    throw new RuntimeException("Failed to get component by local coords");
                }

                // then we'll remove it from one, check its still on the other and not on the one we removed it from
                // and then remove it from the other
                blockRef.getStore().removeComponent(blockRef, TwunkDevTestComponent.COMPONENT_TYPE);
                chunkRef.getStore().removeComponent(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE);
                blockRefComponentREFETCHED = ComponentUtils.get(blockRef, TwunkDevTestComponent.COMPONENT_TYPE);
                chunkRefComponentREFETCHED = ComponentUtils.get(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE);
                if (
                    ComponentUtils.has(blockRef, TwunkDevTestComponent.COMPONENT_TYPE) ||
                    blockRefComponentREFETCHED != null
                ) {
                    throw new RuntimeException("failed to remove our new component from the block lmao");
                }
                if (
                    ComponentUtils.has(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE) ||
                    chunkRefComponentREFETCHED != null
                ) {
                    throw new RuntimeException("failed to remove our new component from the block lmao");
                }

                component2 = ComponentUtils.get_blockCoords(
                    test.world,
                    TwunkDevTestComponent.COMPONENT_TYPE,
                    blockX,
                    blockY,
                    blockZ
                );
                if (component2 != null) {
                    throw new RuntimeException(
                        "Should NOT have been able to get component after deleting it (from blockCoords)"
                    );
                }

                component3 = ComponentUtils.get_localCoords(
                    test.blockComponentChunk,
                    TwunkDevTestComponent.COMPONENT_TYPE,
                    localCoords.x,
                    localCoords.y,
                    localCoords.z
                );
                if (component3 != null) {
                    throw new RuntimeException(
                        "Should NOT have been able to get component after deleting it (from localCoords)"
                    );
                }
                // yeah these next two logs are useless, but it helps to easily verify all my tests are working by them all having the same structure visually in their responses
                if (verbose != null) {
                    console.log("Ran alot of test(s)");
                    console.log("10/10 tests successful");
                    console.log("+ All tests successful");
                }
                if (verbose == null) {
                    console.log("+ (13) SUCCESS: TEST_Component_");
                }
            } catch (Exception e) {
                if (verbose != null) {
                    console.log("ERROR IN TESTS" + e);
                }
                if (verbose == null) {
                    console.log("- (13) FAILED:  TEST_Component_");
                }
            }
        });
    }

    public static final <T extends Component<ChunkStore>> boolean has(
        @Nonnull final Ref<ChunkStore> ref,
        @Nonnull final Supplier<ComponentType<ChunkStore, T>> getComponentType
    ) {
        final var componentType = getComponentType.get();
        if (componentType == null) {
            return false;
        }

        return ComponentUtils.has(ref, componentType);
    }

    public static final <T extends Component<ChunkStore>> boolean has(
        @Nonnull final Ref<ChunkStore> ref,
        @Nonnull final ComponentType<ChunkStore, T> componentType
    ) {
        return (T) ref.getStore().getComponent(ref, componentType) != null;
    }

    // ====================================================================
    // Get another component that's ON the same ref you passed in
    // ====================================================================

    public static final <T extends Component<ChunkStore>> T get(
        @Nonnull final Ref<ChunkStore> anyRef,
        @Nonnull final Supplier<ComponentType<ChunkStore, T>> getComponentType
    ) {
        final var componentType = getComponentType.get();
        if (componentType == null) {
            return null;
        }

        return anyRef.getStore().getComponent(anyRef, componentType);
    }

    public static final <T extends Component<ChunkStore>> T get(
        @Nonnull final Ref<ChunkStore> anyRef,
        @Nonnull final ComponentType<ChunkStore, T> componentType
    ) {
        return anyRef.getStore().getComponent(anyRef, componentType);
    }

    /**
     * Get a component from a chunk, can either provide the ref to the chunk itself OR
     * a ref to the block that's in the chunk
     */
    public static final <T extends Component<ChunkStore>> T getChunkComponent(
        @Nonnull final Ref<ChunkStore> anyRef,
        @Nonnull final ComponentType<ChunkStore, T> componentType
    ) {
        if (BlockUtils.isChunkRef(anyRef)) {
            return ComponentUtils.get(anyRef, componentType);
        } else if (BlockUtils.isBlockRef(anyRef)) {
            final var chunkIndex = ChunkUtils.Coords.Index.get_blockRef(anyRef);
            if (chunkIndex == null) {
                return null;
            }

            return anyRef.getStore().getExternalData().getChunkComponent(chunkIndex, componentType);
        }

        console.log("WARNING WARNING RILEY WARNING WE HAD A REF THAT WASN'T A BLOCK AND WASN'T A CHUNK");
        console.log("WARNING WARNING RILEY WARNING WE HAD A REF THAT WASN'T A BLOCK AND WASN'T A CHUNK");
        console.log("WARNING WARNING RILEY WARNING WE HAD A REF THAT WASN'T A BLOCK AND WASN'T A CHUNK");
        return null;
    }

    @Nullable
    public static final BlockComponentChunk getBlockComponentChunk(@Nonnull final Ref<ChunkStore> anyRef) {
        if (BlockUtils.isChunkRef(anyRef)) {
            return anyRef.getStore().getComponent(anyRef, BLOCK_COMPONENT_CHUNK);
        } else if (BlockUtils.isBlockRef(anyRef)) {
            // get the chunkRef first
            final var chunkRef = ChunkUtils.Ref_.get(anyRef);
            if (chunkRef == null) {
                return null;
            }

            // now we get the BlockComponentChunk
            return chunkRef.getStore().getComponent(chunkRef, BLOCK_COMPONENT_CHUNK);
        }

        return null;
    }

    // ====================================================================
    // Get a component from a block at the given coords (global)
    // ====================================================================

    @Nullable
    public static final <T extends Component<ChunkStore>> T get_blockCoords(
        @Nonnull final World world,
        @Nonnull final ComponentType<ChunkStore, T> componentType,
        final int blockX,
        final int blockY,
        final int blockZ
    ) {
        final var chunkRef = world.getChunkStore().getChunkReference(ChunkUtil.indexChunkFromBlock(blockX, blockZ));
        if (chunkRef == null) {
            return null;
        }

        final var chunkStore = world.getChunkStore().getStore();
        final var blockComponentChunk = ComponentUtils.getBlockComponentChunk(chunkRef);
        if (blockComponentChunk == null) {
            return null;
        }

        final var blockRef = blockComponentChunk.getEntityReference(
            ChunkUtil.indexBlockInColumn(blockX, blockY, blockZ)
        );
        if (blockRef == null || !blockRef.isValid()) {
            return null;
        }

        return chunkStore.getComponent(blockRef, componentType);
    }

    // ====================================================================
    // Get a component from a block at the given LOCAL coords
    // ====================================================================

    public static final <T extends Component<ChunkStore>> T get_localCoords(
        @Nonnull final BlockComponentChunk chunk,
        @Nonnull final Supplier<ComponentType<ChunkStore, T>> getComponentType,
        final int localX,
        final int localY,
        final int localZ
    ) {
        final var ref = BlockUtils.Ref_.getLocal(chunk, localX, localY, localZ);
        if (ref == null) {
            return null;
        }
        final var componentType = getComponentType.get();
        if (componentType == null) {
            return null;
        }

        return ref.getStore().getComponent(ref, componentType);
    }

    @Nullable
    public static final <T extends Component<ChunkStore>> T get_localCoords(
        @Nonnull final BlockComponentChunk chunk,
        @Nonnull final ComponentType<ChunkStore, T> componentType,
        final int localX,
        final int localY,
        final int localZ
    ) {
        final var ref = BlockUtils.Ref_.getLocal(chunk, localX, localY, localZ);
        if (ref == null) {
            return null;
        }
        return ref.getStore().getComponent(ref, componentType);
    }
}
