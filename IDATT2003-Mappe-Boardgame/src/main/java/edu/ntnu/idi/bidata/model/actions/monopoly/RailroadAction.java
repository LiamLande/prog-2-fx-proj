package edu.ntnu.idi.bidata.model.actions.monopoly;

public class RailroadAction extends PropertyAction {

    public RailroadAction(String propertyName, int propertyId) {
        super(propertyName, propertyId);
    }

    @Override
    public String toString() {
        return "RailroadAction{" +
                "propertyName='" + getPropertyName() + '\'' +
                ", propertyId=" + getPropertyId() +
                '}';
    }

}
