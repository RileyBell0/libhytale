package dev.twunk.ticking.system.smart;

/**
 * Information about the block that's being ticked, cached between calls to
 * make calling ticks way easier
 *
 * without this, i'd have to (every tick)
 * - use the ref to our block to get its "BlockStateInfo" component
 * - use the "BlockStateInfo" to get the local coords of the block (within its
 * chunk)
 * - use the "BlockStateInfo" component to get the chunk the block is in
 * - use the chunk to convert the local coords of the block to global coords
 * - use the chunk to get a ref to the world itself
 * and THEN finally i could run a regular tick method for a component.
 *
 * e.g. code i used to use
 * ```java
 *  var ref = archetypeChunk.getReferenceTo(index);
 *
 *      var info = BlockUtils.getInfo(commandBuffer, ref);
 *      if (info == null) {
 *          return;
 *      }
 *      var worldChunk = BlockUtils.getWorldChunk(commandBuffer, info);
 *      if (worldChunk == null) {
 *          return;
 *      }
 *      var world = worldChunk.getWorld();
 *      if (world == null) {
 *          return;
 *      }
 *      var coords = BlockUtils.getGlobalCoords(worldChunk, info);
 * ```
 */
public class TickingEntityCache {}
