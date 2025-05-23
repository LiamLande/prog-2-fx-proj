package edu.ntnu.idi.bidata.service;

/**
 * A simple service locator for providing global access to the {@link MonopolyService}.
 * This class uses static methods to set and get the MonopolyService instance.
 */
public class ServiceLocator {
    private static MonopolyService monopolyService;

    /**
     * Sets the global {@link MonopolyService} instance.
     *
     * @param service The {@link MonopolyService} instance to be used by the application.
     */
    public static void setMonopolyService(MonopolyService service) {
        monopolyService = service;
    }

    /**
     * Retrieves the global {@link MonopolyService} instance.
     *
     * @return The currently set {@link MonopolyService} instance, or null if none has been set.
     */
    public static MonopolyService getMonopolyService() {
        return monopolyService;
    }
}