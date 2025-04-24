package edu.ntnu.idi.bidata.service;

public class ServiceLocator {
    private static MonopolyService monopolyService;

    public static void setMonopolyService(MonopolyService service) {
        monopolyService = service;
    }

    public static MonopolyService getMonopolyService() {
        return monopolyService;
    }
}