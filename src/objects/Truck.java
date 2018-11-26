package objects;

import java.util.*;

import utils.Utils;

import static utils.Utils.getDistance;
import static utils.Utils.getTime;

public class Truck {

    private final int TRUCK_CAPACITY;
    private final int TRUCK_WORKING_TIME;
    private int truckWorkingTime;
    private Location startlocatie;
    private Location eindlocatie;
    private Location currentLocation;
    private int totaleTijdGereden;
    private int totaleAfstandTruck;
    private boolean tijdVoorRequest;
    // de requests die deze truck afhandeld
    private LinkedList<Request> route;
    private Set<Request> routeSet;
    // de machines die currently op de truck staan
    private List<Machine> machineList;
    private int truckId;


    public Truck(Location startlocatie, Location eindlocatie, int truckCapacity, int truckWorkingTime, int truckId) {
        this.TRUCK_CAPACITY = truckCapacity;
        this.TRUCK_WORKING_TIME = truckWorkingTime;
        this.truckWorkingTime = truckWorkingTime;
        this.startlocatie = startlocatie;
        this.currentLocation = startlocatie;
        this.eindlocatie = eindlocatie;
        totaleTijdGereden = 0;
        totaleAfstandTruck = 0;
        tijdVoorRequest = true;
        route = new LinkedList<>();
        routeSet = new HashSet<>();
        machineList = new ArrayList<>();
        this.truckId = truckId;
    }

    public Set<Request> getRouteSet() {
        return routeSet;
    }

    public void setRouteSet(Set<Request> routeSet) {
        this.routeSet = routeSet;
    }

    public int getTRUCK_CAPACITY() {
        return TRUCK_CAPACITY;
    }

    public int getTRUCK_WORKING_TIME() {
        return TRUCK_WORKING_TIME;
    }

    public int getTruckWorkingTime() {
        return truckWorkingTime;
    }

    public void setTruckWorkingTime(int truckWorkingTime) {
        this.truckWorkingTime = truckWorkingTime;
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

    public int getTotaleTijdGereden() {
        return totaleTijdGereden;
    }

    public void setTotaleTijdGereden(int totaleTijdGereden) {
        this.totaleTijdGereden = totaleTijdGereden;
    }

    public void addTotaleTijdGereden(int tijd) {
        this.totaleTijdGereden += tijd;
    }

    public boolean isTijdVoorRequest() {
        return tijdVoorRequest;
    }

    public void setTijdVoorRequest(boolean tijdVoorRequest) {
        this.tijdVoorRequest = tijdVoorRequest;
    }

    public int getTotaleAfstandTruck() {
        return totaleAfstandTruck;
    }

    public void setTotaleAfstandTruck(int totaleAfstandTruck) {
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

    public void print() {
        System.out.println("start: " + startlocatie + " eind: " + eindlocatie);
    }

    public int totaleAfstand() {
        int distance = 0;
        Request previousReq = null;
        for (Request req : route) {
            System.out.println("distance: " + distance);

            if (previousReq != null) {
                System.out.println("berekende afstand: " + getDistance(req.getLocation(), previousReq.getLocation()));
                distance += getDistance(req.getLocation(), previousReq.getLocation());
            }
            previousReq = req;

        }
        totaleAfstandTruck = distance;
        return distance;
    }

    public int totaleTijd() {
        System.out.println("truck: "+truckId);
        int tijd = 0;
        Request previousReq = null;
        for (Request req : route) {
            System.out.println("tijd: " + tijd);
            if (req.getMachine() != null) {
                System.out.println("service tijd: " + req.getMachine().getMachineType().getServiceTime());
                tijd += req.getMachine().getMachineType().getServiceTime();
            }
            if (previousReq != null) {
                System.out.println("berekende tijd: " + getTime(req.getLocation(), previousReq.getLocation()));

                tijd += getTime(req.getLocation(), previousReq.getLocation());
            }
            previousReq = req;

        }
        totaleTijdGereden = tijd;
        return tijd;
    }

    public void addRequestToRoute(Request request) {
        route.add(request);
        routeSet.add(request);
    }

    public boolean worked(){
        for (Request req : route) {
            if (req.getMachine()!=null) {
                return true;
            }
        }
        return false;
    }

    public void addTotaleAfstand(int distance) {
        totaleAfstandTruck += distance;
    }

    public int getTruckId() {
        return truckId;
    }

    public void setTruckId(int truckId) {
        this.truckId = truckId;
    }

    public int getCurrentUsedCapacity() {
        int capacityInUse = 0;
        for (Machine machine : machineList) {
            capacityInUse += machine.getMachineType().getVolume();
        }
        return capacityInUse;
    }

    public boolean isMachineTypeAvailable(MachineType machineType) {
        for (Machine machine : machineList) {
            if (machine.getMachineType() == machineType) {
                return true;
            }
        }
        return false;
    }

    public void printRequestList() {
        int i = 0;
        for (Request req : route) {
            req.print();
        }
    }

    public Machine getMachineOfType(MachineType machineType) {

        for (Machine machine : machineList) {
            if (machine.getMachineType() == machineType) {
                return machine;
            }
        }
        return null;
    }

    public void removeMachine(Machine machine) {
        machineList.remove(machine);
    }
}
