package com.timvisee.dungeonmaze.world.dungeon.chunk;

import com.timvisee.dungeonmaze.Core;
import com.timvisee.dungeonmaze.DungeonMaze;
import com.timvisee.dungeonmaze.world.dungeon.chunk.grid.DungeonRegionGrid;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import java.io.*;

public class DungeonRegion {

    /**
     * Defines the dungeon region data section name.
     */
    private final static String CONFIG_DUNGEON_REGION_SECTION = "dungeonRegion";

    /**
     * Defines the size of the region on a single side of the region.
     * Thus, a size of 32 will result in 32*32=1024 chunks in total inside this region.
     */
    public final static int REGION_SIZE = 32;

    /**
     * Defines the prefix for the region file names.
     */
    private static final String REGION_FILE_PREFIX = "r.";

    /**
     * Defines the separator that is used for the region file names.
     */
    private static final String REGION_FILE_SEPARATOR = ".";

    /**
     * Defines the suffix for the region file names.
     */
    private static final String REGION_FILE_SUFFIX = ".dmr";

    /**
     * Defines the dungeon region grid this region is in.
     */
    private DungeonRegionGrid regionGrid;

    /**
     * Defines the X and Y coordinate of the region (on a 2D region plane) in the world.
     */
    private int x, y;

    /**
     * The grid of chunks inside this region.
     */
    // TODO: Make sure this is initialized with null!
    private DungeonChunk[][] chunks = new DungeonChunk[REGION_SIZE][REGION_SIZE];

    /**
     * The last region that has been accessed.
     */
    private DungeonChunk lastChunkCache = null;

    /**
     * Constructor.
     *
     * @param regionGrid The region grid this region is in.
     * @param regionX Region X coordinate.
     * @param regionY Region Y coordinate.
     */
    public DungeonRegion(DungeonRegionGrid regionGrid, int regionX, int regionY) {
        this.regionGrid = regionGrid;
        this.x = regionX;
        this.y = regionY;
    }

    /**
     * Get the dungeon grid this region is in.
     *
     * @return The grid.
     */
    public DungeonRegionGrid getGrid() {
        return this.regionGrid;
    }

    /**
     * Get the world the region is in.
     *
     * @return The world the region is in.
     */
    public World getWorld() {
        return this.regionGrid.getWorld();
    }

    /**
     * Get the name of the world this region is in.
     *
     * @return Name of the world.
     */
    public String getWorldName() {
        return getWorld().getName();
    }

    /**
     * Check whether this region is in the given world.
     *
     * @param world The world.
     *
     * @return True if the region is in the given world, false if not.
     */
    public boolean isWorld(World world) {
        return this.regionGrid.isWorld(world);
    }

    /**
     * Get the X coordinate of the region.
     *
     * @return The X coordinate of the region.
     */
    public int getX() {
        return this.x;
    }

    /**
     * Get the X coordinate of the region in the world space.
     *
     * @return The X coordinate of the region in the world space.
     */
    public int getWorldX() {
        return this.x * REGION_SIZE * DungeonChunk.CHUNK_SIZE;
    }

    /**
     * Get the Y coordinate of the chunk.
     *
     * @return The Y coordinate of the chunk.
     */
    public int getY() {
        return this.y;
    }

    /**
     * Get the Z coordinate of the region in the world space.
     *
     * @return The Z coordinate of the region in the world space.
     */
    public int getWorldZ() {
        return this.y * REGION_SIZE * DungeonChunk.CHUNK_SIZE;
    }

    /**
     * Check whether this region is at a specific position.
     *
     * @param regionX The X coordinate of the region.
     * @param regionY The Z coordinate of the region.
     *
     * @return True if the region is at this position, false otherwise.
     */
    public boolean isAt(int regionX, int regionY) {
        return this.x == regionX && this.y == regionY;
    }

    /**
     * Get the chunk data file.
     *
     * @param regionGrid The Dungeon Chunk Grid instance.
     *
     * @return The chunk data file.
     */
    public File getRegionDataFile(DungeonRegionGrid regionGrid) {
        return regionGrid.getRegionDataFile(this.x, this.y);
    }

    // TODO: Saving and loading!

    /**
     * Get the grid of chunks inside this region as a 2 dimensional array with the specified region size.
     *
     * @return Grid of chunks.
     */
    public DungeonChunk[][] getChunks() {
        return this.chunks;
    }

    /**
     * Get the total number of chunks that can be store in the region.
     *
     * @return Number of chunks that can be stored in this region.
     */
    public int getChunkCapacity() {
        return REGION_SIZE * REGION_SIZE;
    }

    /**
     * Get the number of chunks in this region. Chunks that haven't been created yet will not be included in the count.
     *
     * @return Numbers in this chunk.
     */
    public int getChunkCount() {
        // Keep track of the count
        int count = 0;

        // Loop through the grid, increase the count if it's a chunk
        for(int x = 0; x < REGION_SIZE; x++)
            for(int y = 0; y < REGION_SIZE; y++)
                if(getChunk(x, y) != null)
                    count++;

        // Return the number of chunks
        return count;
    }

    /**
     * Get a specific dungeon chunk instance. The dungeon chunk data will be created if it doesn't exist yet.
     *
     * @param chunkX The X coordinate of the chunk.
     * @param chunkY The Y coordinate of the chunk.
     *
     * @return The dungeon chunk instance, or null on failure.
     */
    public DungeonChunk getOrCreateChunk(int chunkX, int chunkY) {
        // Compare the requested chunk with the cache
        if(lastChunkCache != null && lastChunkCache.isAt(chunkX, chunkY))
            return lastChunkCache;

        // Make sure the chunk coordinates are in-bound
        if(chunkX < 0 || chunkX >= REGION_SIZE || chunkY < 0 || chunkY >= REGION_SIZE)
            // TODO: Add some sort of exception here?
            return null;

        // Check whether this chunk exists, return it if it exists
        DungeonChunk chunk = getChunk(chunkX, chunkY);
        if(chunk != null)
            return chunk;

        // Create the chunk data, return the result
        return (lastChunkCache = createChunk(chunkX, chunkY));
    }

    /**
     * Get the dungeon chunk at the specified coordinate.
     * If a chunk at this coordinate doesn't exist yet, null will be returned.
     *
     * @param chunkX The X coordinate of the chunk.
     * @param chunkY The Y coordinate of the chunk.
     *
     * @return The dungeon chunk, or null if it doesn't exist yet.
     */
    public DungeonChunk getChunk(int chunkX, int chunkY) {
        return this.chunks[chunkX][chunkY];
    }

    /**
     * Create a dungeon chunk at the specified coordinates.
     *
     * @param chunkX The X coordinate of the chunk to create.
     * @param chunkY The Y coordinate of the chunk to create.
     *
     * @return The dungeon chunk instance on success, or null on failure.
     */
    public DungeonChunk createChunk(int chunkX, int chunkY) {
        // Make sure no data exists for this chunk
        if(hasChunk(chunkX, chunkY))
            return getOrCreateChunk(chunkX, chunkY);

        // Create the chunk data for this chunk, save it and return the instance
        DungeonChunk dungeonChunk = new DungeonChunk(this, chunkX, chunkY);

        // Set the dungeon chunk in the grid
        this.chunks[chunkX][chunkY] = dungeonChunk;

        // Unload the excess regions
        // TODO: Get this to work again!
        //unloadExcessRegions();

        // Return the new chunk
        return dungeonChunk;
    }

    /**
     * Check whether a dungeon chunk is available at the specified location.
     * If a chunk isn't available, it might not have been created yet.
     *
     * @param chunkX The X coordinate of the chunk.
     * @param chunkY The Y coordinate of the chunk.
     *
     * @return True if the chunk is available, false if not.
     */
    public boolean hasChunk(int chunkX, int chunkY) {
        return getChunk(chunkX, chunkY) != null;
    }

    /**
     * Get the file name for this region.
     *
     * @return Region file name.
     */
    public String getRegionFileName() {
        return getRegionFileName(this.x, this.y);
    }

    /**
     * Get the file name for a region at the given coordinates.
     *
     * @param x Region X coordinate.
     * @param y Region Y coordinate.
     *
     * @return Region file name.
     */
    public static String getRegionFileName(int x, int y) {
        return REGION_FILE_PREFIX +
                x +
                REGION_FILE_SEPARATOR +
                y +
                REGION_FILE_SUFFIX;
    }

    /**
     * Load the dungeon region at the specified location.
     *
     * @param regionGrid The dungeon region grid of the world.
     * @param regionX The X coordinate of the region.
     * @param regionY The Y coordinate of the region.
     *
     * @return The dungeon region instance.
     */
    public static DungeonRegion load(DungeonRegionGrid regionGrid, int regionX, int regionY) throws IOException {
        // Get the region data file for the given region coordinates
        File regionFile = regionGrid.getRegionDataFile(regionX, regionY);
        File regionFile2 = new File(regionFile.getParentFile(), regionFile.getName() + ".bin");

        // Make sure the region file exists before loading it
        if(!regionFile.exists())
            // TODO: Throw a proper exception!
            return null;

        // Create a reader for the region data
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(new FileInputStream(regionFile2));

        // Unpack the version number and name
        System.out.println("Version code: " + unpacker.unpackInt());
        System.out.println("Version name: " + unpacker.unpackString());

        // Load the configuration for the file
        YamlConfiguration config = YamlConfiguration.loadConfiguration(regionFile);

        // Get the configuration section to load the region from
        ConfigurationSection regionSection = config.getConfigurationSection(CONFIG_DUNGEON_REGION_SECTION);

        // Load the region from the given configuration
        DungeonRegion region = load(regionGrid, regionSection, unpacker);

        // Close the unpacker
        unpacker.close();

        // Return the region
        return region;
    }

    /**
     * Load a dungeon region from the given configuration section.
     *
     * @param regionGrid The dungeon region grid of the world.
     * @param regionSection The configuration section load the region from.
     *
     * @return The dungeon region instance.
     */
    public static DungeonRegion load(DungeonRegionGrid regionGrid, ConfigurationSection regionSection, MessageUnpacker unpacker) throws IOException {
        // Read the coordinates
        System.out.println("Region X:" + unpacker.unpackInt());
        System.out.println("Region Y:" + unpacker.unpackInt());

        // Get the number of chunks
        int chunkCount = unpacker.unpackInt();
        System.out.println("Chunk count: " + chunkCount);

        // Get the coordinates of the region
        int x = regionSection.getInt("loc.x");
        int y = regionSection.getInt("loc.y");

        // Construct the region instance
        DungeonRegion region = new DungeonRegion(regionGrid, x, y);

        // Get the region the chunks are stored in
        ConfigurationSection chunksSection = regionSection.getConfigurationSection("chunks");

        // Loop through all the sections in the chunks section to load the chunks from
        for(String key : chunksSection.getKeys(false)) {
            // Get the section of the chunk
            ConfigurationSection chunkSection = chunksSection.getConfigurationSection(key);

            // Get the size of the chunk data, and read it's payload
            int chunkDataSize = unpacker.unpackInt();
            byte[] bytes = unpacker.readPayload(chunkDataSize);
            MessageUnpacker chunkUnpacker = MessagePack.newDefaultUnpacker(bytes);

            // Load the given chunk in the region from the configuration section of the chunk
            region.loadChunkFromConfig(chunkSection, chunkUnpacker);

            // Close the unpacker
            chunkUnpacker.close();
        }

        // Show a status message, that the region was loaded
        Core.getLogger().debug("Loaded region for '" + region.getWorldName() + "' at (, " + x + ", " + y + ")");

        // Return the dungeon region
        return region;
    }

    /**
     * Load a dungeon chunk from the given configuration section.
     *
     * @param chunkSection Configuration section the chunk is stored in.
     *
     * @return The dungeon chunk.
     */
    public DungeonChunk loadChunkFromConfig(ConfigurationSection chunkSection, MessageUnpacker unpacker) throws IOException {
        // Load the dungeon chunk
        DungeonChunk chunk = DungeonChunk.load(this, chunkSection, unpacker);

        // Add the chunk to the grid
        this.chunks[chunk.getX()][chunk.getY()] = chunk;

        // Return the loaded chunk
        return chunk;
    }

    /**
     * Save the region to the default file.
     *
     * @param regionGrid The dungeon region grid instance, used to determine the save location based on the world.
     *
     * @return The number of chunks that were saved in this region.
     * This doesn't count the chunks that weren't available yet.
     *
     * @throws IOException Throws if an error occurred while saving the region file.
     */
    public int save(DungeonRegionGrid regionGrid) throws IOException {
        // Get the file to save the region to
        File regionFile = getRegionDataFile(regionGrid);
        File regionFile2 = new File(regionFile.getParentFile(), regionFile.getName() + ".bin");

        // Save the file, and return the result
        return save(regionFile, regionFile2);
    }

    /**
     * Save the region to the specified file.
     *
     * @param regionFile The file to save the region in.
     *
     * @return The number of chunks that were saved in this region.
     * This doesn't count the chunks that weren't available yet.
     *
     * @throws IOException Throws if an error occurred while saving the region file.
     */
    public int save(File regionFile, File regionFile2) throws IOException {
        // Open an output stream for the data to write to the target file
        FileOutputStream out = new FileOutputStream(regionFile2);

        // Create the data packer
        MessagePacker packer = MessagePack.newDefaultPacker(out);

        // Pack the data
        packer.packInt(DungeonMaze.getVersionCode());
        packer.packString(DungeonMaze.getVersionName());

        // Create the Yaml configuration to store the data in
        YamlConfiguration config = new YamlConfiguration();

        // Create a section to save the region in
        ConfigurationSection regionSection = config.createSection(CONFIG_DUNGEON_REGION_SECTION);

        // Save the actual region to the section
        int saved = save(regionSection, packer);

        // Append the current Dungeon Maze version to the file
        config.set("version.name", DungeonMaze.getVersionName());
        config.set("version.code", DungeonMaze.getVersionCode());

        // Save the file
        config.save(regionFile);

        // Close the file
        packer.close();

        // Return the number of saved chunks
        return saved;
    }

    /**
     * Save all the loaded chunks in the current chunks grid.
     *
     * @return The number of chunks that were saved in this region.
     * This doesn't count the chunks that weren't available yet.
     */
    public int save(ConfigurationSection config, MessagePacker packer) throws IOException {
        // Count how many chunks are saved
        int saved = 0;

        // Pack the X and Y coordinate of the region
        packer.packInt(this.x);
        packer.packInt(this.y);

        // Pack the number of chunks that will be stored
        packer.packInt(getChunkCount());

        // Store the location of the region
        config.set("loc.x", getX());
        config.set("loc.y", getY());

        // Create a section to store the chunks in
        ConfigurationSection chunksSection = config.createSection("chunks");

        // Loop through the chunk grid to save all of them
        for(int x = 0; x < REGION_SIZE; x++) {
            for(int y = 0; y < REGION_SIZE; y++) {
                // Get the dungeon chunk at this coordinate, but skip it if there's no chunk at this location
                DungeonChunk chunk = getChunk(x, y);
                if(chunk == null)
                    continue;

                // Calculate the index of the chunk
                int chunkIndex = x * REGION_SIZE + y;

                // Create a buffered packer for the chunk
                MessageBufferPacker chunkPacker = MessagePack.newDefaultBufferPacker();

                // Create a configuration section for the current chunk
                ConfigurationSection chunkSection = chunksSection.createSection(String.valueOf(chunkIndex));

                // Save the chunk to the given section
                chunk.save(chunkSection, chunkPacker);

                // Pack the chunk data
                byte[] chunkPackerData = chunkPacker.toByteArray();
                packer.packInt(chunkPackerData.length);
                packer.writePayload(chunkPackerData);

                // Increase the saved counter
                saved++;
            }
        }

        // Show a status message, that the region was saved
        Core.getLogger().debug("Saved region for '" + getWorldName() + "' at (" + getX() + ", " + getY() + ")");

        // Return the number of saved chunks
        return saved;
    }
}
