package objects;

import java.util.List;

import static main.Main.depots;
import static utils.Utils.getDistance;

public class Request {

    private Location location;
    private MachineType machineType;
    private Machine machine;
    private int id;

    private boolean drop; //true = drop, false = collect
    private boolean depot;
    private Request pair = null;

    public Request(Location location, Machine machine, boolean drop, boolean depot) {
        this.location = location;
        this.machineType = null;
        this.machine = machine;
        this.machineType = null;
        this.drop = drop;
        this.depot = depot;
    }

    public Request(Location location, MachineType machineType, boolean drop, boolean depot) {
        this.location = location;
        this.machineType = machineType;
        this.machine = null;
        this.drop = drop;
        this.depot = depot;
    }

    public Request(Location location, boolean drop, boolean depot) {
        this.location = location;
        this.machineType = null;
        this.machine = null;
        this.drop = drop;
        this.depot = depot;
    }

    public int getVolume() {
        if (drop) {
            return machineType.getVolume();
        } else {
            return machine.getMachineType().getVolume();
        }
    }



    public void setPair(Request pair){
        this.pair = pair;
    }

    public Request getPair() {
        return pair;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public MachineType getMachineType() {
        return machineType;
    }

    public void setMachineType(MachineType machineType) {
        this.machineType = machineType;
    }

    public boolean isDrop() {
        return drop;
    }

    public void setDrop(boolean drop) {
        this.drop = drop;
    }

    public boolean isDepot() {
        return depot;
    }

    public void setDepot(boolean depot) {
        this.depot = depot;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void print() {
        if (machine != null) {
            System.out.println("location: " + location.getLocatieId() + " machine: " + machine.getMachineId() + " drop: " + drop + " depot: " + depot);
        } else {
            System.out.println("location: "+location.getLocatieId() +" geen machine drop "+ drop+ " depot "+depot);
        }
    }

    public Request clone(){
        Request request = new Request(location.clone(), drop, depot);
        if(machine != null)
            request.setMachine(machine.clone());
        if(machineType != null)
            request.setMachineType(machineType.clone());

        return request;
    }

    public void changeDepot(Request pair) {
        Location forbiddenLoc = this.getLocation();
        List<Depot> depotList = depots;
        int minDistance=10000000;

        for(Depot d:depotList){
            //System.out.println("Test voor "+ d.getLocation()+ " en "+forbiddenLoc);
            if(d.getLocation()!=forbiddenLoc){
                if(getDistance(d.getLocation(),pair.getLocation())<minDistance){
                    minDistance= getDistance(d.getLocation(),pair.getLocation());
                    this.setLocation(d.getLocation());
                }
            }
        }


    }


}

