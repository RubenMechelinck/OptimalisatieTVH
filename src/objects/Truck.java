package objects;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Truck {

    private final int TRUCK_CAPACITY;
    private final int TRUCK_WORKING_TIME;
    private Location startlocatie;
    private Location eindlocatie;
    private Location currentLocation;
    private double totaleAfstandTruck;
    private List<Request> route;
    private List<Machine> machineList;


    public Truck(Location startlocatie, Location eindlocatie, int truckCapacity, int truckWorkingTime) {
        this.TRUCK_CAPACITY = truckCapacity;
        this.TRUCK_WORKING_TIME = truckWorkingTime;
        this.startlocatie = startlocatie;
        this.currentLocation = startlocatie;
        this.eindlocatie = eindlocatie;
        totaleAfstandTruck = 0;
        route = new LinkedList<>();
        machineList = new ArrayList<>();
    }

    public Location getStartlocatie() {
        return startlocatie;
    }

    public void setStartlocatie(Location startlocatie) {
        this.startlocatie = startlocatie;
    }

    public Location getEindlocatie() {
        return eindlocatie;
    }

    public void setEindlocatie(Location eindlocatie) {
        this.eindlocatie = eindlocatie;
    }

    public double getTotaleAfstandTruck() {
        return totaleAfstandTruck;
    }

    public void setTotaleAfstandTruck(double totaleAfstandTruck) {
        this.totaleAfstandTruck = totaleAfstandTruck;
    }

    public List<Request> getRoute() {
        return route;
    }

    public void setRoute(LinkedList<Request> route) {
        this.route = route;
    }

    public List<Machine> getMachineList() {
        return machineList;
    }

    public void setMachineList(List<Machine> machineList) {
        this.machineList = machineList;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void print(){
        System.out.println("start: "+ startlocatie + " eind: "+eindlocatie );
    }

    public void addRequestToRoute(Request request){
        route.add(request);
    }
}
