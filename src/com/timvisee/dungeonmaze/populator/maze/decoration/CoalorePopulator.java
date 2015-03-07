package com.timvisee.dungeonmaze.populator.maze.decoration;

import java.util.Random;


import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.timvisee.dungeonmaze.populator.maze.MazeRoomBlockPopulator;
import com.timvisee.dungeonmaze.populator.maze.MazeRoomBlockPopulatorArgs;

public class CoalorePopulator extends MazeRoomBlockPopulator {

    /** General populator constants. */
	public static final int LAYER_MIN = 1;
	public static final int LAYER_MAX = 6;
	public static final float ROOM_CHANCE = .02f;
    public static final int ROOM_ITERATIONS = 5;

	@Override
	public void populateRoom(MazeRoomBlockPopulatorArgs args) {
        final Chunk chunk = args.getSourceChunk();
        final Random rand = args.getRandom();
        final int x = args.getChunkX();
        final int y = args.getChunkY();
        final int z = args.getChunkZ();

        Block block = chunk.getBlock(x + rand.nextInt(8), rand.nextInt((y + 6) - y + 1) + y, z + rand.nextInt(8));
        if (block.getType() == Material.COBBLESTONE)
            block.setType(Material.COAL_ORE);
	}

    @Override
    public float getRoomChance() {
        return ROOM_CHANCE;
    }

    @Override
    public int getRoomIterations() {
        return ROOM_ITERATIONS;
    }

    @Override
	public int getMinimumLayer() {
		return LAYER_MIN;
	}

    @Override
    public int getMaximumLayer() {
        return LAYER_MAX;
    }
}
