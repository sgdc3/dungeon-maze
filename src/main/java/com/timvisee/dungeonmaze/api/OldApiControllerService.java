package com.timvisee.dungeonmaze.api;

import com.timvisee.dungeonmaze.Core;
import com.timvisee.dungeonmaze.DungeonMaze;
import com.timvisee.dungeonmaze.config.ConfigHandler;
import com.timvisee.dungeonmaze.service.Service;
import org.bukkit.configuration.file.FileConfiguration;

public class OldApiControllerService extends Service {

    /** Service name. */
    private static final String SERVICE_NAME = "Old API Controller";

    /** Old API Controller instance. */
    private OldApiController apiController;

    /**
     * Initialize the service.
     *
     * @return True on success, false on failure. True will also be returned if the service was initialized already.
     */
    @Override
    public boolean init() {
        // Initialize the Old API controller
        this.apiController = new OldApiController(false);

        // Show a status message
        Core.getLogger().debug("Old Dungeon Maze API started!");

        // Get the Dungeon Maze config
        ConfigHandler configHandler = DungeonMaze.instance.getCore()._getConfigHandler();
        FileConfiguration config = configHandler.config;

        // Check whether the API should be enabled
        boolean apiEnabled = true;
        if(config != null)
            apiEnabled = config.getBoolean("api.enabled", true);

        // Enable the API if it should be enabled
        if(apiEnabled)
            this.apiController.init();
        else
            Core.getLogger().info("Not enabling Old Dungeon Maze API, disabled in config file!");

        return true;
    }

    /**
     * Check whether the service is initialized.
     *
     * @return True if the service is initialized, false otherwise.
     */
    @Override
    public boolean isInit() {
        // Check whether the API controller instance is set
        if(this.apiController == null)
            return false;

        // Check whether the API controller is instantiated
        return this.apiController.isInit();
    }

    /**
     * Destroy the service. The destruction won't be forced.
     *
     * @param force True to force the destruction. This wil re-destroy the service even if it isn't initialized.
     *              This will also force the initialization state to be set to false even if an error occurred while
     *              destroying.
     *
     * @return True on success, false on failure. True will also be returned if the service wasn't initialized. False
     * might be returned if force is set to true, even though the initialization state is set to false.
     */
    @Override
    public boolean destroy(boolean force) {
        // Make sure the api controller is initialized, or it must be forced
        if(!this.isInit() && !force)
            return true;

        // Unregister all API sessions and disable the controller
        if(this.apiController != null)
            this.apiController.destroy(force);

        // Return the result
        this.apiController = null;
        return true;
    }

    /**
     * Get the name of the service.
     *
     * @return Service name.
     */
    @Override
    public String getName() {
        return SERVICE_NAME;
    }

    /**
     * Get the API controller.
     *
     * @return API controller instance.
     */
    public OldApiController getApiController() {
        return this.apiController;
    }
}
