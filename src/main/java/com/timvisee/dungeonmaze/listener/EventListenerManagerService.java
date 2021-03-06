package com.timvisee.dungeonmaze.listener;

import com.timvisee.dungeonmaze.service.Service;

public class EventListenerManagerService extends Service {

    /** Service name. */
    private static final String SERVICE_NAME = "Event Listener Manager";

    /** Event listener manager instance. */
    private EventListenerManager eventListenerManager;

    /**
     * Initialize the service.
     *
     * @return True on success, false on failure. True will also be returned if the service was initialized already.
     */
    @Override
    public boolean init() {
        // Instantiate the event listener manager
        this.eventListenerManager = new EventListenerManager(false);

        // Initialize the manager, return false on failure
        if(!this.eventListenerManager.init(false))
            return false;

        // Register the event listeners, return the result
        return this.eventListenerManager.registerListeners();
    }

    /**
     * Check whether the service is initialized.
     *
     * @return True if the service is initialized, false otherwise.
     */
    @Override
    public boolean isInit() {
        // Check whether it's instantiated
        if(this.eventListenerManager == null)
            return false;

        // Check if the manager is instantiated, return the result
        return eventListenerManager.isInit();
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
        // Make sure the manager is instantiated, or it must be forced
        if(!this.isInit() && !force)
            return true;

        // Destroy the manager
        if(this.eventListenerManager != null) {
            if(!this.eventListenerManager.destroy()) {
                if(force)
                    this.eventListenerManager = null;
                return false;
            }
        }

        // Return the result
        this.eventListenerManager = null;
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
     * Get the event listener manager.
     *
     * @return Event listener manager instance.
     */
    public EventListenerManager getEventListenerManager() {
        return this.eventListenerManager;
    }
}
