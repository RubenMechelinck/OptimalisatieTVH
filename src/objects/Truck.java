package objects;

import java.rmi.Remote;
import java.util.*;

import static utils.Utils.getDistance;
import static utils.Utils.getTime;

public class Truck {

    private int truckCapacity;
    private int truckWorkingTime;
    private int REAL_TRUCK_WORKING_TIME;
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

    public Truck(Location startlocatie, Location eindlocatie, int truckCapacity, int truckWorkingTime, int realTruckWorkingTime, int truckId) {
        this.truckCapacity = truckCapacity;
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
        REAL_TRUCK_WORKING_TIME = realTruckWorkingTime;
    }

    public Truck(Truck truck){
        this.truckId = truck.truckId;
        this.truckCapacity = truck.truckCapacity;
        this.truckWorkingTime = truck.truckWorkingTime;
        this.startlocatie = truck.startlocatie.clone();
        this.eindlocatie = truck.eindlocatie.clone();
        this.currentLocation = truck.currentLocation.clone();
        this.totaleTijdGereden = truck.totaleTijdGereden;
        this.totaleAfstandTruck = truck.totaleAfstandTruck;
        this.tijdVoorRequest = truck.tijdVoorRequest;
        this.routeSet = new HashSet<>();
        this.route = new LinkedList<>();
        this.REAL_TRUCK_WORKING_TIME = truck.REAL_TRUCK_WORKING_TIME;

        for(Request request: truck.route) {
            Request q = request.clone();
            route.add(q);
            routeSet.add(q);
        }

        this.machineList = new ArrayList<>();
        for(Machine machine: truck.machineList)
            machineList.add(machine.clone());
    }

    public boolean worked(){
        for (Request req : route) {
            if (req.getMachine() != null) {
                return true;
            }
        }
        return false;
    }

    public Request getRequestForMachine(Machine machine) {
        for(Request req: route){
            if (req.getMachine() == machine) {
                return req;
            }
        }
        return null;

    }

    public void addTotaleAfstand(int distance) {
        totaleAfstandTruck += distance;
    }

    public void removeTotaleAfstand(int distance){
        totaleAfstandTruck -= distance;
    }

    public void removeTotaleTijdGereden(int time){
        totaleTijdGereden -= time;
    }

    public void addTotaleTijdGereden(int tijd) {
        this.totaleTijdGereden += tijd;
    }

    //request achteraan toevoegen => moet gwn tijd en afstand bijtellen
    //niet voor startlocatie toe te voegen!!
    public void addRequestToRoute(Request request, boolean contructive) {
        if(route.size() != 0) {
            addTotaleTijdGereden(getTime(request.getLocation(), route.getLast().getLocation()));
            addTotaleAfstand(getDistance(request.getLocation(), route.getLast().getLocation()));
        }
        currentLocation = request.getLocation();
        route.add(request);
        routeSet.add(request);

        if(!contructive) {
            if (request.getMachineType() != null) {
                addTotaleTijdGereden(request.getMachineType().getServiceTime());
            } else if (request.getMachine() != null) {
                addTotaleTijdGereden(request.getMachine().getMachineType().getServiceTime());
            }
        }
    }


    //request wordt op index par:i gezet
    //request toevoegen op specifieke plaats => meer werk om tijd en afstand aan te passen
    public void addRequestToRoute(Request request, int index, boolean contructive) {
        int distanceToAdd = 0;
        int timeToAdd = 0;
        int timeToRemove = 0;
        int distanceToRemove = 0;
        Location previous = null;
        Location next = null;

        if(index != 0) {
            previous = route.get(index - 1).getLocation();
            timeToAdd += getTime(request.getLocation(), previous);
            distanceToAdd = getDistance(request.getLocation(), previous);

        }
        if(index < route.size()) {
            next = route.get(index).getLocation();
            timeToAdd += getTime(request.getLocation(), next);
            distanceToAdd += getDistance(request.getLocation(), next);
        }

        if(previous != null && next != null){
            timeToRemove = getTime(previous, next);
            distanceToRemove = getDistance(previous, next);
            removeTotaleAfstand(distanceToRemove);
            removeTotaleTijdGereden(timeToRemove);
        }

        addTotaleAfstand(distanceToAdd);
        addTotaleTijdGereden(timeToAdd);

        route.add(index, request);
        routeSet.add(request);

        if(!contructive) {
            if (request.getMachineType() != null) {
                addTotaleTijdGereden(request.getMachineType().getServiceTime());
            } else if (request.getMachine() != null) {
                addTotaleTijdGereden(request.getMachine().getMachineType().getServiceTime());
            }
        }
    }

    //verwijder request en bijhorende tijden en afstanden
    public int removeRequest(Request request, boolean contructive){
        int distanceToAdd = 0;
        int timeToAdd = 0;
        int timeToRemove = 0;
        int distanceToRemove = 0;
        Location previous = null;
        Location next = null;
        Location current = request.getLocation();
        int index = route.indexOf(request);

        if(index != 0) {
            previous = route.get(index - 1).getLocation();
            timeToRemove += getTime(current, previous);
            distanceToRemove = getDistance(current, previous);

        }
        if(index < route.size()-1) {
            next = route.get(index+1).getLocation();
            timeToRemove += getTime(current, next);
            distanceToRemove += getDistance(current, next);
        }

        removeTotaleAfstand(distanceToRemove);
        removeTotaleTijdGereden(timeToRemove);

        if(previous != null && next != null) {
            timeToAdd = getTime(previous, next);
            distanceToAdd = getDistance(previous, next);
            addTotaleAfstand(distanceToAdd);
            addTotaleTijdGereden(timeToAdd);
        }

        route.remove(request);
        routeSet.remove(request);

        if(!contructive) {
            if (request.getMachineType() != null) {
                removeTotaleTijdGereden(request.getMachineType().getServiceTime());
            } else if (request.getMachine() != null) {
                removeTotaleTijdGereden(request.getMachine().getMachineType().getServiceTime());
            }
        }

        return index;
    }

    //verzijder request op index par:i en bijhorende tijden en afstanden
    public Request removeRequest(int index, boolean contructive){
        int distanceToAdd = 0;
        int timeToAdd = 0;
        int timeToRemove = 0;
        int distanceToRemove = 0;
        Location previous = null;
        Location next = null;
        Request request = route.get(index);
        Location current = request.getLocation();

        if(index != 0) {
            previous = route.get(index - 1).getLocation();
            timeToRemove += getTime(current, previous);
            distanceToRemove = getDistance(current, previous);

        }
        if(index < route.size()-1) {
            next = route.get(index+1).getLocation();
            timeToRemove += getTime(current, next);
            distanceToRemove += getDistance(current, next);
        }

        removeTotaleAfstand(distanceToRemove);
        removeTotaleTijdGereden(timeToRemove);

        if(previous != null && next != null) {
            timeToAdd = getTime(previous, next);
            distanceToAdd = getDistance(previous, next);
            addTotaleAfstand(distanceToAdd);
            addTotaleTijdGereden(timeToAdd);
        }

        if(!contructive) {
            if (request.getMachineType() != null) {
                removeTotaleTijdGereden(request.getMachineType().getServiceTime());
            } else if (request.getMachine() != null) {
                removeTotaleTijdGereden(request.getMachine().getMachineType().getServiceTime());
            }
        }

        routeSet.remove(index);
        return route.remove(index);
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


    public Set<Request> getRouteSet() {
        return routeSet;
    }

    public void setRouteSet(Set<Request> routeSet) {
        this.routeSet = routeSet;
    }

    public int getTruckCapacity() {
        return truckCapacity;
    }

    public int getTruckWorkingTime() {
        return truckWorkingTime;
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

    public LinkedList<Request> getRoute() {
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

    public int getTruckId() {
        return truckId;
    }

    public void setTruckId(int truckId) {
        this.truckId = truckId;
    }

    public void print() {
        System.out.println("start: " + startlocatie + " eind: " + eindlocatie);
    }


    public void addToTruckWorkingTime(int timeToAdd) {
        truckWorkingTime += timeToAdd;
    }

    public int getREAL_TRUCK_WORKING_TIME() {
        return REAL_TRUCK_WORKING_TIME;
    }


    public List<Request> getDepotRequestList() {
        List<Request> depotRequestList = new ArrayList<Request>();
        for (int i = 1; i < route.size() - 1; i++) { //Negeer eerste en laatste request -> Mag niet veranderen, is start en eindlocatie
            //System.out.println(route.get(i).getLocation().getName()+" is de locatie van dit request ");

            if (route.get(i).isDepot() && route.get(i)!=route.get(0)) {
                depotRequestList.add(route.get(i));
            }
        }
        Collections.shuffle(depotRequestList);


        return depotRequestList;
    }

    public void printDepots(){
        System.out.println("Depotlijst van truck "+this.getTruckId());
        for (int i = 0; i < route.size(); i++) {

            if(route.get(i).isDepot()){
                System.out.print(i+ " is depot");
                System.out.println(i+ ". "+route.get(i).getLocation().getName());
            }
            else{System.out.print(i+ " is geen depot");System.out.println(i+ ". "+route.get(i).getLocation().getName());}
        }
        System.out.println();
    }

}
