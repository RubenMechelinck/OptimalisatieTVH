package objects;

import java.util.ArrayList;
import java.util.List;

public class Depot {

    private Location location;
    private List<Truck> trucksList;
    private List<Machine> machineList;

    public Depot(Location location) {
        this.location = location;
        this.trucksList = new ArrayList<>();
        this.machineList = new ArrayList<>();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<Truck> getTrucksList() {
        return trucksList;
    }

    public void setTrucksList(List<Truck> trucksList) {
        this.trucksList = trucksList;
    }

    public List<Machine> getMachineList() {
        return machineList;
    }

    public void setMachineList(List<Machine> machineList) {
        this.machineList = machineList;
    }
}
