package objects;

import java.util.ArrayList;
import java.util.List;

public class Depot {

    private Location location;
    private static List<Truck> trucksList;

    public Depot(Location location) {
        this.location = location;
        this.trucksList = new ArrayList<>();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public static List<Truck> getTrucksList() {
        return trucksList;
    }

    public static void setTrucksList(List<Truck> trucksList) {
        Depot.trucksList = trucksList;
    }
}
